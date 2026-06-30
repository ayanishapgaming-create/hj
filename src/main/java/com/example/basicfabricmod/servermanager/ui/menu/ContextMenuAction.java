package com.example.basicfabricmod.servermanager.ui.menu;

public record ContextMenuAction(String label, Runnable action, boolean enabled) {
}
