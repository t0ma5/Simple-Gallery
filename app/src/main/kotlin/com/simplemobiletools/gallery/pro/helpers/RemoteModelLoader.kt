package com.simplemobiletools.gallery.pro.helpers

import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import java.io.File

/**
 * Downloads a remote:// media file to a temp file in [cacheDir] and returns its path.
 * Used by thumbnails (Glide), video playback (media3) and duration probes, none of which can
 * open remote:// paths directly.
 *
 * Mirrors getRemoteFiles / RemoteOps: change into the parent directory first, then fetch just
 * the file name — absolute-path retrieveFileStream() fails on chrooted servers.
 *
 * Returns null if the download fails. Reuses an existing temp file when present so repeat
 * loads are instant.
 */
fun downloadRemoteFileToTemp(curPath: String, config: Config, cacheDir: File): String? {
    return try {
        val parts = curPath.removePrefix("remote://").split("/", limit = 3)
        if (parts.size < 3) return null

        val protocol = parts[0]
        val serverId = parts[1].toLongOrNull() ?: return null
        val remotePath = "/${parts[2]}"

        val server = config.parseRemoteServers().find { it.id == serverId } ?: return null
        val password = config.getRemoteServerPassword(server.id!!, server.passwordHash)

        // FTP only - SFTP removed
        if (protocol != "ftp") return null

        val tempFile = File(cacheDir, "remote_${serverId}_${remotePath.hashCode()}.tmp")

        // reuse a previously downloaded copy so we don't re-fetch for metadata + playback
        if (tempFile.exists() && tempFile.length() > 0) {
            return tempFile.absolutePath
        }

        val fileIndex = remotePath.lastIndexOf('/')
        val parentDir = if (fileIndex > 0) remotePath.substring(0, fileIndex) else "/"
        val fileName = remotePath.substring(fileIndex + 1)

        val ftpClient = FTPClient()
        ftpClient.connectTimeout = 3000
        ftpClient.connect(server.host, server.port)
        ftpClient.login(server.username, password)
        ftpClient.enterLocalPassiveMode()
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE)
        ftpClient.changeWorkingDirectory(parentDir)
        val stream = ftpClient.retrieveFileStream(fileName)
        if (stream == null) {
            ftpClient.disconnect()
            return null
        }

        tempFile.outputStream().use { out ->
            stream.copyTo(out)
        }
        stream.close()
        ftpClient.completePendingCommand()
        ftpClient.disconnect()

        if (tempFile.exists() && tempFile.length() > 0) tempFile.absolutePath else null
    } catch (e: Exception) {
        null
    }
}
