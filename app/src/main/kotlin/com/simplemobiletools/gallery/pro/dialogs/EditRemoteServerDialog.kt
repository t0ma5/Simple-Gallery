package com.simplemobiletools.gallery.pro.dialogs

import androidx.appcompat.app.AlertDialog
import com.google.gson.Gson
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.getAlertDialogBuilder
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.commons.extensions.showErrorToast
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.extensions.value
import com.simplemobiletools.gallery.pro.R
import com.simplemobiletools.gallery.pro.databinding.DialogAddRemoteServerBinding
import com.simplemobiletools.gallery.pro.extensions.config
import com.simplemobiletools.gallery.pro.models.RemoteServer

class EditRemoteServerDialog(
    val activity: BaseSimpleActivity,
    val server: RemoteServer,
    val callback: () -> Unit
) {
    private var view = DialogAddRemoteServerBinding.inflate(activity.layoutInflater)

    init {
        view.serverName.setText(server.name)
        view.serverHost.setText(server.host)
        view.serverPort.setText(server.port.toString())
        view.serverUsername.setText(server.username)
        view.serverRemotePath.setText(server.remotePath)
        view.typeFtp.isChecked = true
        view.serverPort.setText("21")

        activity.getAlertDialogBuilder()
            .setPositiveButton(com.simplemobiletools.commons.R.string.ok, null)
            .setNegativeButton(com.simplemobiletools.commons.R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(view.root, this, R.string.edit_remote_server) { alertDialog ->
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val name = view.serverName.value
                        val host = view.serverHost.value
                        val port = view.serverPort.value.toIntOrNull() ?: 0
                        val username = view.serverUsername.value
                        val password = view.serverPassword.value
                        val remotePath = view.serverRemotePath.value

                        if (name.isEmpty() || host.isEmpty() || username.isEmpty() || port == 0) {
                            activity.toast(R.string.invalid_values)
                            return@setOnClickListener
                        }

                        // Update server (FTP only)
                        val updatedServer = RemoteServer(server.id, name, host, port, username, server.passwordHash, remotePath)
                        if (password.isNotEmpty()) {
                            activity.config.saveRemoteServerPassword(server.id!!, password)
                        }
                        val servers = activity.config.parseRemoteServers()
                        val index = servers.indexOfFirst { it.id == server.id }
                        if (index >= 0) {
                            servers[index] = updatedServer
                            activity.config.remoteServers = Gson().toJson(servers)
                        }
                        callback()
                        alertDialog.dismiss()
                    }
                }
            }
    }
}