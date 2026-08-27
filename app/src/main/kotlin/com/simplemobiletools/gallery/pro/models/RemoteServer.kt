package com.simplemobiletools.gallery.pro.models

/**
 * Saved FTP server definition.
 *
 * NOTE: the password is NEVER stored here. The [passwordEncrypted] field holds an opaque token
 * that [com.simplemobiletools.gallery.pro.helpers.RemoteManager] resolves to the real credential
 * stored in Android Keystore / EncryptedSharedPreferences. Never put a real password in this model.
 */
data class RemoteServer(
    val id: Long,
    val name: String,
    val host: String,
    val port: Int,
    val username: String,
    // opaque token; see RemoteManager — never a plaintext password
    val passwordEncrypted: String,
    val remotePath: String,
    val protocol: Int = PROTOCOL_FTP
) {
    fun fullPath(subPath: String = ""): String {
        val base = if (remotePath.isEmpty() || remotePath == "/") "" else remotePath.removeSuffix("/")
        val sub = subPath.removePrefix("/")
        return if (sub.isEmpty()) "remote://$id$base" else "remote://$id$base/$sub"
    }

    companion object {
        const val PROTOCOL_FTP = 0
    }
}
