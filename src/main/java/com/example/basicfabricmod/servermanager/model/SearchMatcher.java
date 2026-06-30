package com.example.basicfabricmod.servermanager.model;

import java.util.Locale;

public final class SearchMatcher {
    private SearchMatcher() {
    }

    public static boolean matchesServer(ServerEntryViewModel server, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }

        String normalized = query.trim().toLowerCase(Locale.ROOT);
        return contains(server.serverInfo().name, normalized)
                || contains(server.serverInfo().address, normalized)
                || contains(server.searchableCountryName(), normalized)
                || (normalized.contains("favorite") && server.favorite())
                || contains(server.pingQuality(), normalized);
    }

    public static boolean matchesFolder(ServerFolder folder, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }

        String normalized = query.trim().toLowerCase(Locale.ROOT);
        return contains(folder.getName(), normalized)
                || (normalized.contains("pinned") && folder.isPinned());
    }

    public static int matchIndex(String value, String query) {
        if (value == null || query == null || query.isBlank()) {
            return -1;
        }
        return value.toLowerCase(Locale.ROOT).indexOf(query.trim().toLowerCase(Locale.ROOT));
    }

    private static boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }
}
