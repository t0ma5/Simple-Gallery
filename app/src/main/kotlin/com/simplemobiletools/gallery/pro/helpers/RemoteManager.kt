package com.simplemobiletools.gallery.pro.helpers

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.simplemobiletools.gallery.pro.models.RemoteServer
import java.util.UUID

private const val KEY_SERVERS = "remote_servers_json"

/**
 * Single source of truth for saved remote (FTP) servers.
 * Server metadata is stored in a normal SharedPreferences blob (Gson), but each password is
 * stored separately in EncryptedSharedPreferences — never in plaintext, never in the Gson blob.
 */
object RemoteManager {
    private const val PREFS_NAME = "remote_servers"
    private const val ENC_PREFS_NAME = "remote_passwords"
    private const val KEY_ALIAS = "remote_pwd_key"

    private lateinit var prefs: SharedPreferences
    private lateinit var encPrefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val masterKey = MasterKey.Builder(context)
            .setKeyGenParameterSpec(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build()
            ).build()
        encPrefs = EncryptedSharedPreferences.create(
            context,
            ENC_PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun getServers(): ArrayList<RemoteServer> {
        val json = prefs.getString(KEY_SERVERS, "") ?: ""
        if (json.isEmpty()) return ArrayList()
        val type = object : TypeToken<ArrayList<RemoteServer>>() {}.type
        val list: ArrayList<RemoteServer> = Gson().fromJson(json, type) ?: ArrayList()
        // attach decrypted passwords
        return list.map { it.copy(passwordEncrypted = encPrefs.getString("pwd_${it.id}", "") ?: "") } as ArrayList<RemoteServer>
    }

    fun getServer(id: Long): RemoteServer? = getServers().find { it.id == id }

    fun addServer(server: RemoteServer) {
        val servers = getServers().toMutableList()
        servers.removeAll { it.id == server.id }
        servers.add(server)
        persist(servers)
    }

    fun removeServer(id: Long) {
        val servers = getServers().toMutableList()
        servers.removeAll { it.id == id }
        encPrefs.edit().remove("pwd_${id}").apply()
        persist(servers)
    }

    private fun persist(servers: List<RemoteServer>) {
        // strip passwords out of the Gson blob
        val safe = servers.map { it.copy(passwordEncrypted = "") }
        prefs.edit().putString(KEY_SERVERS, Gson().toJson(safe)).apply()
        servers.forEach { encPrefs.edit().putString("pwd_${it.id}", it.passwordEncrypted).apply() }
    }

    fun nextId(): Long = UUID.randomUUID().mostSignificantBits
}
