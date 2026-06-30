package com.example.basicfabricmod.servermanager.model;

import java.util.List;

public record ServerManagerViewModel(
        List<ServerEntryViewModel> rootServers,
        List<FolderViewModel> folders,
        String searchQuery,
        boolean searching
) {
}
