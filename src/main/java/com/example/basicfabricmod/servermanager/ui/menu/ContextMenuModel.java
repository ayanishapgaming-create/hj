package com.example.basicfabricmod.servermanager.ui.menu;

import java.util.List;

public record ContextMenuModel(int x, int y, int width, List<ContextMenuAction> actions) {
}
