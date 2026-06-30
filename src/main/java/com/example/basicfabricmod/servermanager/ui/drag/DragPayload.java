package com.example.basicfabricmod.servermanager.ui.drag;

import java.util.List;

public record DragPayload(DragType type, List<String> serverAddresses, String folderId) {
}
