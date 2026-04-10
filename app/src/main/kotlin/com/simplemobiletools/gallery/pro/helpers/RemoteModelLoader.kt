package com.simplemobiletools.gallery.pro.helpers

import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.signature.ObjectKey
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import org.apache.commons.net.ftp.FTPClient
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
    private var jschSession: com.jcraft.jsch.Session? = null
    private var sftpChannel: ChannelSftp? = null
    private var inputStream: InputStream? = null

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

            val password = config.getRemoteServerPassword(serverId)

            if (protocol == "ftp") {
                ftpClient = FTPClient()
                ftpClient?.connect(server.host, server.port)
                ftpClient?.login(server.username, password)
                ftpClient?.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE)
                inputStream = ftpClient?.retrieveFileStream(remotePath)
                if (inputStream != null) {
                    callback.onDataReady(inputStream)
                } else {
                    callback.onLoadFailed(Exception("Failed to retrieve FTP file stream"))
                }
            } else if (protocol == "sftp") {
                val jsch = JSch()
                jschSession = jsch.getSession(server.username, server.host, server.port)
                jschSession?.setPassword(password)
                jschSession?.setConfig("StrictHostKeyChecking", "no")
                jschSession?.connect()
                sftpChannel = jschSession?.openChannel("sftp") as ChannelSftp
                sftpChannel?.connect()
                inputStream = sftpChannel?.get(remotePath)
                if (inputStream != null) {
                    callback.onDataReady(inputStream)
                } else {
                    callback.onLoadFailed(Exception("Failed to retrieve SFTP file stream"))
                }
            }
        } catch (e: Exception) {
            callback.onLoadFailed(e)
        }
    }

    override fun cleanup() {
        try {
            inputStream?.close()
        } catch (e: Exception) {}
        try {
            ftpClient?.completePendingCommand()
            ftpClient?.logout()
            ftpClient?.disconnect()
        } catch (e: Exception) {}
        try {
            sftpChannel?.disconnect()
            jschSession?.disconnect()
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
