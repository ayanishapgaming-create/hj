package com.example.basicfabricmod.servermanager.ui.controller;

import com.example.basicfabricmod.servermanager.ui.menu.ContextMenuAction;
import com.example.basicfabricmod.servermanager.ui.menu.ContextMenuModel;

import java.util.ArrayList;
import java.util.List;

public final class ContextMenuController {
    private ContextMenuModel currentMenu;

    public void open(int mouseX, int mouseY, int screenWidth, int screenHeight, List<ContextMenuAction> actions) {
        int width = 120;
        int height = actions.size() * 12 + 6;
        int x = Math.min(mouseX, screenWidth - width - 4);
        int y = Math.min(mouseY, screenHeight - height - 4);
        currentMenu = new ContextMenuModel(Math.max(4, x), Math.max(4, y), width, new ArrayList<>(actions));
    }

    public ContextMenuModel getCurrentMenu() {
        return currentMenu;
    }

    public void close() {
        currentMenu = null;
    }

    public boolean isOpen() {
        return currentMenu != null;
    }
}
