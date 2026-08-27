package com.simplemobiletools.gallery.pro.dialogs

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.getAlertDialogBuilder
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.extensions.value
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import com.simplemobiletools.gallery.pro.R
import com.simplemobiletools.gallery.pro.databinding.DialogAddRemoteServerBinding
import com.simplemobiletools.gallery.pro.helpers.RemoteManager
import com.simplemobiletools.gallery.pro.models.RemoteServer
import org.apache.commons.net.ftp.FTPClient

class AddRemoteServerDialog(val activity: BaseSimpleActivity, val callback: () -> Unit) :
    androidx.appcompat.app.AppCompatDialog(activity) {

    private var binding: DialogAddRemoteServerBinding = DialogAddRemoteServerBinding.inflate(activity.layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.add_remote_server)
        binding.serverType.visibility = View.GONE

        activity.getAlertDialogBuilder()
            .setPositiveButton(com.simplemobiletools.commons.R.string.ok, null)
            .setNegativeButton(com.simplemobiletools.commons.R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this, titleId = R.string.add_remote_server) { alertDialog ->
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        testAndSave(alertDialog)
                    }
                }
            }
    }

    private fun testAndSave(dialog: AlertDialog) {
        val name = binding.serverName.value.trim()
        val host = binding.serverHost.value.trim()
        val port = binding.serverPort.value.toIntOrNull() ?: 21
        val user = binding.serverUsername.value.trim()
        val pass = binding.serverPassword.value
        val remotePath = binding.serverRemotePath.value.trim().ifEmpty { "/" }

        if (host.isEmpty()) {
            activity.toast(com.simplemobiletools.commons.R.string.unknown_error_occurred)
            return
        }

        ensureBackgroundThread {
            if (!testConnection(host, port, user, pass, remotePath)) {
                activity.runOnUiThread {
                    activity.toast(com.simplemobiletools.commons.R.string.unknown_error_occurred)
                }
                return@ensureBackgroundThread
            }
            val id = RemoteManager.nextId()
            val server = RemoteServer(
                id = id,
                name = name.ifEmpty { host },
                host = host,
                port = port,
                username = user,
                passwordEncrypted = pass,
                remotePath = remotePath
            )
            RemoteManager.addServer(server)
            activity.runOnUiThread {
                callback()
                dialog.dismiss()
            }
        }
    }

    private fun testConnection(host: String, port: Int, user: String, pass: String, path: String): Boolean {
        return try {
            val cli = FTPClient()
            cli.connectTimeout = 8000
            cli.connect(host, port)
            cli.login(user, pass)
            cli.enterLocalPassiveMode()
            if (path.isNotEmpty() && path != "/") {
                cli.changeWorkingDirectory(path)
            }
            cli.list()
            cli.disconnect()
            true
        } catch (e: Exception) {
            false
        }
    }
}
