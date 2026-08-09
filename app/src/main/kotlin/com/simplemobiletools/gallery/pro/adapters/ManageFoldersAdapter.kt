package com.simplemobiletools.gallery.pro.adapters

import android.view.*
import android.widget.PopupMenu
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.adapters.MyRecyclerViewAdapter
import com.simplemobiletools.commons.extensions.getPopupMenuTheme
import com.simplemobiletools.commons.extensions.getProperTextColor
import com.simplemobiletools.commons.extensions.setupViewBackground
import com.simplemobiletools.commons.interfaces.RefreshRecyclerViewListener
import com.simplemobiletools.commons.views.MyRecyclerView
import com.simplemobiletools.gallery.pro.databinding.ItemManageFolderBinding
import com.simplemobiletools.gallery.pro.extensions.config
import com.simplemobiletools.gallery.pro.models.RemoteServer
import com.google.gson.Gson

private const val MENU_EDIT = 1001

class ManageFoldersAdapter(
    activity: BaseSimpleActivity, var folders: ArrayList<Any>, val isShowingExcludedFolders: Boolean, val listener: RefreshRecyclerViewListener?,
    recyclerView: MyRecyclerView, val editCallback: ((Any) -> Unit)? = null, itemClick: (Any) -> Unit
) : MyRecyclerViewAdapter(activity, recyclerView, itemClick) {

    private val config = activity.config

    init {
        setupDragListener(true)
    }

    override fun getActionMenuId() = com.simplemobiletools.commons.R.menu.cab_remove_only

    override fun prepareActionMode(menu: Menu) {}

    override fun actionItemPressed(id: Int) {
        when (id) {
            com.simplemobiletools.commons.R.id.cab_remove -> removeSelection()
        }
    }

    override fun getSelectableItemCount() = folders.size

    override fun getIsItemSelectable(position: Int) = true

    override fun getItemSelectionKey(position: Int) = folders.getOrNull(position)?.hashCode()

    override fun getItemKeyPosition(key: Int) = folders.indexOfFirst { it.hashCode() == key }

    override fun onActionModeCreated() {}

    override fun onActionModeDestroyed() {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return createViewHolder(ItemManageFolderBinding.inflate(layoutInflater, parent, false).root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val folder = folders[position]
        holder.bindView(folder, true, true) { itemView, adapterPosition ->
            setupView(itemView, folder)
        }
        bindViewHolder(holder)
    }

    override fun getItemCount() = folders.size

    private fun getSelectedItems() = folders.filter { selectedKeys.contains(it.hashCode()) } as ArrayList<Any>

    private fun setupView(view: View, folder: Any) {
        val title = when (folder) {
            is String -> {
                // Show folder name + indicate if it's a remote path
                if (folder.startsWith("remote://")) {
                    val parts = folder.removePrefix("remote://").split("/", limit = 3)
                    if (parts.size >= 3) {
                        val protocol = parts[0].uppercase()
                        val remotePath = "/${parts[2]}"
                        "$protocol: $remotePath"
                    } else {
                        folder
                    }
                } else {
                    folder
                }
            }
            is RemoteServer -> "FTP: ${folder.name} (${folder.host})"
            else -> ""
        }

        ItemManageFolderBinding.bind(view).apply {
            root.setupViewBackground(activity)
            manageFolderHolder.isSelected = selectedKeys.contains(folder.hashCode())
            manageFolderTitle.apply {
                text = title
                setTextColor(context.getProperTextColor())
            }

            overflowMenuIcon.drawable.apply {
                mutate()
                setTint(activity.getProperTextColor())
            }

            overflowMenuIcon.setOnClickListener {
                showPopupMenu(overflowMenuAnchor, folder)
            }
        }
    }

    private fun showPopupMenu(view: View, folder: Any) {
        finishActMode()
        val theme = activity.getPopupMenuTheme()
        val contextTheme = ContextThemeWrapper(activity, theme)

        PopupMenu(contextTheme, view, Gravity.END).apply {
            // Edit for both local paths and remote servers
            if (editCallback != null) {
                menu.add(0, MENU_EDIT, 0, com.simplemobiletools.commons.R.string.edit)
            }
            menu.add(0, com.simplemobiletools.commons.R.id.cab_remove, 1, com.simplemobiletools.commons.R.string.remove)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    MENU_EDIT -> {
                        editCallback?.invoke(folder)
                    }

                    com.simplemobiletools.commons.R.id.cab_remove -> {
                        executeItemMenuOperation(folder.hashCode()) {
                            removeSelection()
                        }
                    }
                }
                true
            }
            show()
        }
    }

    private fun executeItemMenuOperation(eventTypeId: Int, callback: () -> Unit) {
        selectedKeys.clear()
        selectedKeys.add(eventTypeId)
        callback()
    }

    private fun removeSelection() {
        val removeFolders = ArrayList<Any>(selectedKeys.size)
        val positions = getSelectedItemPositions()

        getSelectedItems().forEach { item ->
            removeFolders.add(item)
            when (item) {
                is String -> {
                    if (isShowingExcludedFolders) {
                        config.removeExcludedFolder(item)
                    } else {
                        config.removeIncludedFolder(item)
                    }
                }

                is RemoteServer -> {
                    val servers = config.parseRemoteServers()
                    servers.removeAll { s -> s.id == item.id }
                    config.remoteServers = Gson().toJson(servers)
                }
            }
        }

        folders.removeAll(removeFolders)
        removeSelectedItems(positions)
        if (folders.isEmpty()) {
            listener?.refreshItems()
        }
    }
}
