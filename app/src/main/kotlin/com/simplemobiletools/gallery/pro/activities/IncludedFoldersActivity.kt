package com.simplemobiletools.gallery.pro.activities

import android.os.Bundle
import com.simplemobiletools.commons.extensions.beVisibleIf
import com.simplemobiletools.commons.extensions.getProperTextColor
import com.simplemobiletools.commons.extensions.viewBinding
import com.simplemobiletools.commons.helpers.NavigationIcon
import com.simplemobiletools.commons.dialogs.RadioGroupDialog
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import com.simplemobiletools.commons.interfaces.RefreshRecyclerViewListener
import com.simplemobiletools.commons.models.RadioItem
import com.simplemobiletools.gallery.pro.dialogs.AddRemoteServerDialog
import com.simplemobiletools.gallery.pro.dialogs.EditRemoteServerDialog
import com.simplemobiletools.gallery.pro.R
import com.simplemobiletools.gallery.pro.adapters.ManageFoldersAdapter
import com.simplemobiletools.gallery.pro.databinding.ActivityManageFoldersBinding
import com.simplemobiletools.gallery.pro.extensions.config
import com.simplemobiletools.gallery.pro.extensions.rescanFolderMedia
import com.simplemobiletools.gallery.pro.models.RemoteServer

class IncludedFoldersActivity : SimpleActivity(), RefreshRecyclerViewListener {

    private val binding by viewBinding(ActivityManageFoldersBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        isMaterialActivity = true
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        updateFolders()
        setupOptionsMenu()
        binding.manageFoldersToolbar.title = getString(R.string.include_folders)

        updateMaterialActivityViews(binding.manageFoldersCoordinator, binding.manageFoldersList, useTransparentNavigation = true, useTopSearchMenu = false)
        setupMaterialScrollListener(binding.manageFoldersList, binding.manageFoldersToolbar)
    }

    override fun onResume() {
        super.onResume()
        setupToolbar(binding.manageFoldersToolbar, NavigationIcon.Arrow)
    }

    private fun updateFolders() {
        val folders = ArrayList<Any>()
        config.includedFolders.mapTo(folders) { it }
        folders.addAll(config.parseRemoteServers())

        binding.manageFoldersPlaceholder.apply {
            text = getString(R.string.included_activity_placeholder)
            beVisibleIf(folders.isEmpty())
            setTextColor(getProperTextColor())
        }

        val adapter = ManageFoldersAdapter(
            this, folders, false, this, binding.manageFoldersList,
            editCallback = { folder ->
                when (folder) {
                    is String -> {
                        config.removeIncludedFolder(folder)
                        showAddIncludedFolderDialog {
                            updateFolders()
                        }
                    }
                    is RemoteServer -> {
                        EditRemoteServerDialog(this, folder) {
                            updateFolders()
                        }
                    }
                }
            }
        ) {}
        binding.manageFoldersList.adapter = adapter
    }

    private fun setupOptionsMenu() {
        binding.manageFoldersToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.add_folder -> addFolder()
                else -> return@setOnMenuItemClickListener false
            }
            return@setOnMenuItemClickListener true
        }
    }

    override fun refreshItems() {
        updateFolders()
    }

    private fun addFolder() {
        val items = arrayListOf(
            RadioItem(0, getString(R.string.local_folder)),
            RadioItem(1, getString(R.string.remote_server))
        )

        RadioGroupDialog(this, items) {
            if (it == 0) {
                showAddIncludedFolderDialog {
                    updateFolders()
                }
            } else {
                addRemoteServer()
            }
        }
    }

    private fun addRemoteServer() {
        AddRemoteServerDialog(this) {
            updateFolders()
            ensureBackgroundThread {
                rescanFolderMedia("/")
            }
        }
    }
}