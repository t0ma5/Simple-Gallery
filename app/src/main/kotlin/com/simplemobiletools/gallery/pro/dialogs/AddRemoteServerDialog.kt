package com.simplemobiletools.gallery.pro.dialogs

import androidx.appcompat.app.AlertDialog
import com.google.gson.Gson
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.getAlertDialogBuilder
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.commons.extensions.showErrorToast
import com.simplemobiletools.commons.extensions.value
import com.simplemobiletools.gallery.pro.R
import com.simplemobiletools.gallery.pro.databinding.DialogAddRemoteServerBinding
import com.simplemobiletools.gallery.pro.extensions.config
import com.simplemobiletools.gallery.pro.models.RemoteServer

class AddRemoteServerDialog(val activity: BaseSimpleActivity, val callback: () -> Unit) {
    private var view = DialogAddRemoteServerBinding.inflate(activity.layoutInflater)

    init {
        view.serverType.setOnCheckedChangeListener { _, checkedId ->
            view.serverPort.setText(if (checkedId == R.id.type_ftp) "21" else "22")
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(view.root, this, R.string.add_remote_server) { alertDialog ->
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val name = view.serverName.value
                        val host = view.serverHost.value
                        val port = view.serverPort.value.toIntOrNull() ?: 0
                        val username = view.serverUsername.value
                        val password = view.serverPassword.value
                        val remotePath = view.serverRemotePath.value
                        val type = if (view.typeFtp.isChecked) RemoteServer.TYPE_FTP else RemoteServer.TYPE_SFTP

                        if (name.isEmpty() || host.isEmpty() || username.isEmpty() || port == 0) {
                            activity.toast(R.string.invalid_values)
                            return@setOnClickListener
                        }

                        val server = RemoteServer(System.currentTimeMillis(), type, name, host, port, username, password, remotePath)
                        val servers = activity.config.parseRemoteServers()
                        servers.add(server)
                        activity.config.remoteServers = Gson().toJson(servers)
                        callback()
                        alertDialog.dismiss()
                    }
                }
            }
    }
}
