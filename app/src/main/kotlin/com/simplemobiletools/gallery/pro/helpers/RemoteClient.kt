package com.simplemobiletools.gallery.pro.helpers

import android.content.Context
import com.simplemobiletools.gallery.pro.models.RemoteServer
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPFile
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream

/**
 * Centralized FTP access. EVERY public function here performs blocking network I/O and MUST be
 * called from a background thread (use ensureBackgroundThread at the call site). There is no
 * main-thread execution path into this class — that was the root cause of the earlier broken
 * design (NetworkOnMainThreadException swallowed silently -> no thumbnails / no playback).
 */
object RemoteClient {

    private const val TIMEOUT = 8000

    private data class Conn(val client: FTPClient, val server: RemoteServer)

    private fun connect(server: RemoteServer): FTPClient {
        val cli = FTPClient()
        cli.connectTimeout = TIMEOUT
        cli.setDataTimeout(TIMEOUT)
        cli.connect(server.host, server.port)
        cli.login(server.username, server.passwordEncrypted)
        cli.enterLocalPassiveMode()
        cli.setFileType(FTP.BINARY_FILE_TYPE)
        cli.changeWorkingDirectory(server.remotePath.ifEmpty { "/" })
        return cli
    }

    private fun withClient(server: RemoteServer, block: (FTPClient) -> Unit) {
        val cli = connect(server)
        try {
            block(cli)
        } finally {
            try {
                cli.logout()
            } catch (_: Exception) {
            }
            try {
                cli.disconnect()
            } catch (_: Exception) {
            }
        }
    }

    /** Download a remote:// file to a local temp file. Returns the local path or null on failure. */
    fun download(remotePath: String, cacheDir: File): String? {
        val (server, rel) = parse(remotePath) ?: return null
        val out = File(cacheDir, "remote_${server.id}_${rel.hashCode()}_${System.currentTimeMillis()}")
        return try {
            var ok = false
            withClient(server) { cli ->
                cli.retrieveFile(rel, FileOutputStream(out)).also { ok = it }
            }
            if (ok && out.exists() && out.length() > 0) out.absolutePath else null
        } catch (_: Exception) {
            null
        }
    }

    /** Upload a local file to a remote:// destination. Returns true on success. */
    fun upload(remoteDest: String, localFile: File): Boolean {
        val (server, rel) = parse(remoteDest) ?: return false
        return try {
            var ok = false
            withClient(server) { cli ->
                ensureParentDirs(cli, rel)
                FileInputStream(localFile).use { fis ->
                    ok = cli.storeFile(rel, fis)
                }
            }
            ok
        } catch (_: Exception) {
            false
        }
    }

    fun delete(remotePath: String): Boolean {
        val (server, rel) = parse(remotePath) ?: return false
        return try {
            var ok = false
            withClient(server) { cli ->
                ok = cli.deleteFile(rel)
                if (!ok) {
                    // maybe a directory -> try recursive remove
                    removeDirectory(cli, rel)
                    ok = true
                }
            }
            ok
        } catch (_: Exception) {
            false
        }
    }

    fun rename(from: String, to: String): Boolean {
        val (server, relFrom) = parse(from) ?: return false
        val (_, relTo) = parse(to) ?: return false
        return try {
            var ok = false
            withClient(server) { cli ->
                if (relTo.startsWith(relFrom + "/")) {
                    // rename across dirs not supported by RNFR/RNTO; fall back to copy+delete
                    copy(cli, relFrom, relTo) && delete(relFrom)
                } else {
                    ensureParentDirs(cli, relTo)
                    cli.rename(relFrom, relTo)
                }.also { ok = it }
            }
            ok
        } catch (_: Exception) {
            false
        }
    }

    /** List entries (files + subdirs) at a remote:// path. Returns null on failure. */
    fun list(remotePath: String): List<RemoteEntry>? {
        val (server, rel) = parse(remotePath) ?: return null
        return try {
            val result = ArrayList<RemoteEntry>()
            withClient(server) { cli ->
                val dir = if (rel.isEmpty() || rel == "/") server.remotePath.ifEmpty { "/" } else rel
                cli.changeWorkingDirectory(dir)
                cli.listFiles().forEach { f ->
                    if (f.name == "." || f.name == "..") return@forEach
                    result.add(
                        RemoteEntry(
                            name = f.name,
                            isDirectory = f.isDirectory,
                            size = f.size,
                            modified = f.timestamp.timeInMillis,
                            path = server.fullPath((dir.removePrefix(server.remotePath.ifEmpty { "/" }).removePrefix("/")).let { base ->
                                (if (base.isEmpty()) "" else "$base/") + f.name
                            })
                        )
                    )
                }
            }
            result
        } catch (_: Exception) {
            null
        }
    }

    fun mkdir(remotePath: String): Boolean {
        val (server, rel) = parse(remotePath) ?: return false
        return try {
            var ok = false
            withClient(server) { cli ->
                ensureParentDirs(cli, rel)
                cli.makeDirectory(rel).also { ok = it }
            }
            ok
        } catch (_: Exception) {
            false
        }
    }

    // ---- internal helpers ----

    private fun copy(cli: FTPClient, from: String, to: String): Boolean {
        return try {
            val tmp = File.createTempFile("rcopy", ".tmp")
            cli.retrieveFile(from, FileOutputStream(tmp))
            ensureParentDirs(cli, to)
            FileInputStream(tmp).use { cli.storeFile(to, it) }.also { tmp.delete() }
        } catch (_: Exception) {
            false
        }
    }

    private fun removeDirectory(cli: FTPClient, path: String) {
        try {
            cli.changeWorkingDirectory(path)
            cli.listFiles().forEach { f ->
                if (f.name != "." && f.name != "..") {
                    if (f.isDirectory) removeDirectory(cli, "$path/${f.name}")
                    else cli.deleteFile("$path/${f.name}")
                }
            }
            cli.changeWorkingDirectory("..")
            cli.removeDirectory(path)
        } catch (_: Exception) {
        }
    }

    private fun ensureParentDirs(cli: FTPClient, rel: String) {
        val parts = rel.split("/").filter { it.isNotEmpty() }
        if (parts.size <= 1) return
        var cur = ""
        for (i in 0 until parts.size - 1) {
            cur += "/${parts[i]}"
            cli.makeDirectory(cur)
        }
    }

    /** Parse remote://<id>/<subpath> into (server, relativePath). */
    fun parse(remotePath: String): Pair<RemoteServer, String>? {
        if (!remotePath.startsWith("remote://")) return null
        val body = remotePath.removePrefix("remote://")
        val slash = body.indexOf('/')
        val idStr = if (slash < 0) body else body.substring(0, slash)
        val id = idStr.toLongOrNull() ?: return null
        val server = RemoteManager.getServer(id) ?: return null
        val rel = if (slash < 0) "" else body.substring(slash + 1)
        return server to rel
    }
}

data class RemoteEntry(
    val name: String,
    val isDirectory: Boolean,
    val size: Long,
    val modified: Long,
    val path: String
)
