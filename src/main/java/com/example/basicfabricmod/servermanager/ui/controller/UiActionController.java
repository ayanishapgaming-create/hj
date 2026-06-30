package com.example.basicfabricmod.servermanager.ui.controller;

import com.example.basicfabricmod.servermanager.service.ServerFolderManager;

import java.util.List;

public final class UiActionController {
    public void favoriteServers(List<String> addresses, boolean favorite) {
        ServerFolderManager.getInstance().setFavorites(addresses, favorite);
    }

    public void moveServersToRoot(List<String> addresses) {
        ServerFolderManager.getInstance().reorderServers(addresses, "");
    }
}
