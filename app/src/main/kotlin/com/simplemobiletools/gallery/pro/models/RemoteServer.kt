package com.simplemobiletools.gallery.pro.models

data class RemoteServer(
    val id: Long?,
    val type: Int, // 0 for FTP, 1 for SFTP
    val name: String,
    val host: String,
    val port: Int,
    val username: String,
    val passwordHash: String, // encrypted or hashed
    val remotePath: String
) {
    companion object {
        const val TYPE_FTP = 0
        const val TYPE_SFTP = 1
    }
}
