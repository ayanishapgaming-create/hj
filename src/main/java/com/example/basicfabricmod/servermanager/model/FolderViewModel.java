package com.example.basicfabricmod.servermanager.model;

import java.util.List;

public record FolderViewModel(
        ServerFolder folder,
        List<ServerEntryViewModel> servers,
        int visibleServerCount
) {
}
