package com.simplemobiletools.gallery.pro.helpers

import android.content.Context
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.signature.ObjectKey
import com.simplemobiletools.commons.extensions.toast
import org.apache.commons.net.ftp.FTPClient
import java.io.File
import java.io.InputStream

class RemoteModelLoader(private val config: Config) : ModelLoader<String, InputStream> {
    override fun buildLoadData(model: String, width: Int, height: Int, options: Options): ModelLoader.LoadData<InputStream> {
        return ModelLoader.LoadData(ObjectKey(model), RemoteDataFetcher(model, config))
    }

    override fun handles(model: String): Boolean {
        return model.startsWith("remote://")
    }
}

class RemoteModelLoaderFactory(private val config: Config) : ModelLoaderFactory<String, InputStream> {
    override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<String, InputStream> {
        return RemoteModelLoader(config)
    }

    override fun teardown() {}
}

class RemoteDataFetcher(private val curPath: String, private val config: Config) : DataFetcher<InputStream> {
    private var ftpClient: FTPClient? = null
    private var inputStream: InputStream? = null

    companion object {
        private val failedConnections = mutableSetOf<String>()
    }

    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in InputStream>) {
        try {
            val parts = curPath.removePrefix("remote://").split("/", limit = 3)
            if (parts.size < 3) {
                callback.onLoadFailed(Exception("Invalid remote path"))
                return
            }

            val protocol = parts[0]
            val serverId = parts[1].toLongOrNull() ?: return callback.onLoadFailed(Exception("Invalid server ID"))
            val remotePath = "/${parts[2]}"

            val server = config.parseRemoteServers().find { it.id == serverId }
                ?: return callback.onLoadFailed(Exception("Server not found"))

            val password = config.getRemoteServerPassword(serverId, server.passwordHash)

            // FTP only - SFTP removed
            if (protocol != "ftp") {
                callback.onLoadFailed(Exception("Only FTP protocol is supported"))
                return
            }

            ftpClient = FTPClient()
            ftpClient?.connect(server.host, server.port)
            ftpClient?.login(server.username, password)
            ftpClient?.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE)
            inputStream = ftpClient?.retrieveFileStream(remotePath)
            if (inputStream != null) {
                // Clear any previous failure for this server
                failedConnections.remove("${server.id}:${server.host}")
                callback.onDataReady(inputStream)
            } else {
                callback.onLoadFailed(Exception("Failed to retrieve FTP file stream"))
            }
        } catch (e: Exception) {
            // Show toast only once per server+host combination
            val key = curPath.removePrefix("remote://").split("/").let { parts ->
                if (parts.size >= 2) "${parts[1]}:${parts[0]}" else curPath
            }
            if (key !in failedConnections) {
                failedConnections.add(key)
                // Use a callback to show toast on UI thread
                (config.context as? android.app.Activity)?.runOnUiThread {
                    config.context.toast("Failed to connect to FTP server: ${e.message}")
                }
            }
            callback.onLoadFailed(e)
        }
    }

    override fun cleanup() {
        try {
            inputStream?.close()
        } catch (e: Exception) {}
        try {
            ftpClient?.disconnect()
        } catch (e: Exception) {}
    }

    override fun cancel() {}

    override fun getDataClass(): Class<InputStream> {
        return InputStream::class.java
    }

    override fun getDataSource(): DataSource {
        return DataSource.REMOTE
    }
}

/**
 * Downloads a remote:// media file to a temp file in [cacheDir] and returns its path.
 * Used by the video player (media3) which cannot open remote:// URLs directly.
 * Returns null if the download fails.
 */
fun downloadRemoteFileToTemp(curPath: String, config: Config, cacheDir: File): String? {
    return try {
        val parts = curPath.removePrefix("remote://").split("/", limit = 3)
        if (parts.size < 3) return null

        val protocol = parts[0]
        val serverId = parts[1].toLongOrNull() ?: return null
        val remotePath = "/${parts[2]}"

        val server = config.parseRemoteServers().find { it.id == serverId } ?: return null
        val password = config.getRemoteServerPassword(serverId, server.passwordHash)

        val tempFile = File(cacheDir, "remote_${serverId}_${remotePath.hashCode()}.tmp")
        
        // FTP only
        if (protocol != "ftp") return null

        val ftpClient = FTPClient()
        ftpClient.connect(server.host, server.port)
        ftpClient.login(server.username, password)
        ftpClient.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE)
        val stream = ftpClient.retrieveFileStream(remotePath)
        if (stream == null) return null
        
        tempFile.outputStream().use { out ->
            stream.copyTo(out)
        }
        stream.close()
        ftpClient.completePendingCommand()
        ftpClient.disconnect()
        
        tempFile.absolutePath
    } catch (e: Exception) {
        null
    }
}