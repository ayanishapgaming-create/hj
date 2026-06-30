package com.example.basicfabricmod.servermanager.ui.drag;

public record DropTarget(DropTargetType type, String folderId, String serverAddress, int rowIndex, boolean valid) {
}
