package com.example.basicfabricmod.servermanager.model;

import net.minecraft.client.network.ServerInfo;

public record ServerEntryViewModel(
        ServerInfo serverInfo,
        String folderId,
        boolean favorite,
        int order,
        String searchableCountryName,
        String pingQuality,
        long lastPingUpdateTime
) {
}
