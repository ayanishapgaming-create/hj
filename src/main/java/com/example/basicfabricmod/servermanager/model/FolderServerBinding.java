package com.example.basicfabricmod.servermanager.model;

import java.util.Objects;

public record FolderServerBinding(String serverAddress, String folderId, boolean favorite, int order) {
    public FolderServerBinding {
        serverAddress = normalize(serverAddress);
        folderId = normalize(folderId);
    }

    private static String normalize(String value) {
        return Objects.requireNonNullElse(value, "").trim();
    }
}
