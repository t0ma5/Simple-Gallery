package com.simplemobiletools.gallery.pro.helpers

import com.simplemobiletools.gallery.pro.models.RemoteServer
import org.apache.commons.net.ftp.FTPClient
import java.io.InputStream

/**
 * FTP write primitives for remote:// paths. Mirrors the read path used by getRemoteFiles /
 * downloadRemoteFileToTemp: connect, changeWorkingDirectory into the parent, then act on the
 * file name. Absolute paths are avoided because chrooted servers only resolve relative to the
 * current working directory.
 *
 * Every public function returns true on success. Failures are swallowed (return false) so the
 * callers can show a toast instead of crashing the UI thread. A Config instance is passed in by
 * the caller (activities/adapters always have one) so we never construct a Config ourselves.
 */
object RemoteOps {
    private const val PREFIX = "remote://ftp/"

    private fun connect(server: RemoteServer, config: Config): FTPClient? {
        return try {
            val password = config.getRemoteServerPassword(server.id!!, server.passwordHash)
            val client = FTPClient()
            client.connectTimeout = 3000
            client.connect(server.host, server.port)
            client.login(server.username, password)
            client.enterLocalPassiveMode()
            client.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE)
            client
        } catch (e: Exception) {
            null
        }
    }

    private fun serverFromPath(path: String, config: Config): RemoteServer? {
        val serverId = path.removePrefix(PREFIX).split("/").first().toLongOrNull() ?: return null
        return config.parseRemoteServers().firstOrNull { it.id == serverId }
    }

    /** (parentDir, fileName) for a remote:// path. parentDir is "" for the server root. */
    private fun parentAndName(path: String): Pair<String, String> {
        val cleaned = path.removePrefix(PREFIX).split("/", limit = 2).let { if (it.size == 2) "/${it[1]}" else "/" }
        val fileIndex = cleaned.lastIndexOf('/')
        val parent = if (fileIndex > 0) cleaned.substring(0, fileIndex) else ""
        val name = cleaned.substring(fileIndex + 1)
        return parent to name
    }

    /** Store a stream at the given remote:// path. Overwrites if it exists. */
    fun storeFile(config: Config, remotePath: String, input: InputStream): Boolean {
        val server = serverFromPath(remotePath, config) ?: return false
        val (parentDir, fileName) = parentAndName(remotePath)
        val client = connect(server, config) ?: return false
        return try {
            if (parentDir.isNotEmpty() && !client.changeWorkingDirectory(parentDir)) return false
            val ok = client.storeFile(fileName, input)
            client.completePendingCommand()
            ok
        } catch (e: Exception) {
            false
        } finally {
            try {
                client.disconnect()
            } catch (ignored: Exception) {
            }
        }
    }

    /** Delete a file or (recursively) a folder at the given remote:// path. */
    fun delete(config: Config, remotePath: String): Boolean {
        val server = serverFromPath(remotePath, config) ?: return false
        val (parentDir, fileName) = parentAndName(remotePath)
        val client = connect(server, config) ?: return false
        return try {
            if (parentDir.isNotEmpty() && !client.changeWorkingDirectory(parentDir)) return false
            val isDir = client.listFiles(fileName)?.firstOrNull()?.isDirectory == true
            val ok = if (isDir) deleteRecursive(client, fileName) else client.deleteFile(fileName)
            client.completePendingCommand()
            ok
        } catch (e: Exception) {
            false
        } finally {
            try {
                client.disconnect()
            } catch (ignored: Exception) {
            }
        }
    }

    private fun deleteRecursive(client: FTPClient, dir: String): Boolean {
        val files = client.listFiles(dir) ?: return false
        for (f in files) {
            val child = "$dir/${f.name}"
            if (f.isDirectory) {
                if (!deleteRecursive(client, child)) return false
            } else {
                if (!client.deleteFile(child)) return false
            }
        }
        return client.removeDirectory(dir)
    }

    /** Rename/move within the same server. from and to are both remote:// paths. */
    fun rename(config: Config, from: String, to: String): Boolean {
        val server = serverFromPath(from, config) ?: return false
        val (fromParent, fromName) = parentAndName(from)
        val (toParent, toName) = parentAndName(to)
        if (fromParent != toParent) {
            return copyRemote(config, from, to) && delete(config, from)
        }
        val client = connect(server, config) ?: return false
        return try {
            if (fromParent.isNotEmpty() && !client.changeWorkingDirectory(fromParent)) return false
            val ok = client.rename(fromName, toName)
            client.completePendingCommand()
            ok
        } catch (e: Exception) {
            false
        } finally {
            try {
                client.disconnect()
            } catch (ignored: Exception) {
            }
        }
    }

    /** Copy a remote file to another remote:// path (same server). */
    fun copyRemote(config: Config, from: String, to: String): Boolean {
        val server = serverFromPath(from, config) ?: return false
        val (fromParent, fromName) = parentAndName(from)
        val (toParent, toName) = parentAndName(to)
        val client = connect(server, config) ?: return false
        return try {
            if (fromParent.isNotEmpty()) client.changeWorkingDirectory(fromParent)
            val input = client.retrieveFileStream(fromName) ?: return false
            if (toParent.isNotEmpty()) client.changeWorkingDirectory(toParent)
            val ok = client.storeFile(toName, input)
            input.close()
            client.completePendingCommand()
            ok
        } catch (e: Exception) {
            false
        } finally {
            try {
                client.disconnect()
            } catch (ignored: Exception) {
            }
        }
    }

    /** Create a directory at the given remote:// path. */
    fun mkdir(config: Config, remotePath: String): Boolean {
        val server = serverFromPath(remotePath, config) ?: return false
        val (parentDir, dirName) = parentAndName(remotePath)
        val client = connect(server, config) ?: return false
        return try {
            if (parentDir.isNotEmpty()) client.changeWorkingDirectory(parentDir)
            val ok = client.makeDirectory(dirName)
            client.completePendingCommand()
            ok
        } catch (e: Exception) {
            false
        } finally {
            try {
                client.disconnect()
            } catch (ignored: Exception) {
            }
        }
    }
}
