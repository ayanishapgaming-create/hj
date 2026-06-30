package com.example.basicfabricmod.servermanager.ui.controller;

import com.example.basicfabricmod.servermanager.model.FolderIcon;
import com.example.basicfabricmod.servermanager.model.ServerFolder;
import com.example.basicfabricmod.servermanager.service.ServerFolderManager;

public final class FolderDialogController {
    public ServerFolder createFolder(String name) {
        return ServerFolderManager.getInstance().createFolder(name == null || name.isBlank() ? "New Folder" : name);
    }

    public void renameFolder(ServerFolder folder, String newName) {
        if (folder != null && newName != null && !newName.isBlank()) {
            folder.setName(newName);
            ServerFolderManager.getInstance().save();
        }
    }

    public void setFolderIcon(ServerFolder folder, FolderIcon icon) {
        if (folder != null) {
            folder.setIcon(icon);
            ServerFolderManager.getInstance().save();
        }
    }
}
