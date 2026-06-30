package com.example.basicfabricmod.servermanager.ui.controller;

import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Independent multi-selection state for server rows.
 */
public final class SelectionController {
    private final Set<String> selectedServers = new LinkedHashSet<>();
    private String anchorServer;

    public void click(String address, List<String> visibleServerOrder) {
        boolean ctrl = MinecraftClient.getInstance().isCtrlPressed();
        boolean shift = MinecraftClient.getInstance().isShiftPressed();
        if (shift && anchorServer != null) {
            selectRange(anchorServer, address, visibleServerOrder);
            return;
        }
        if (ctrl) {
            toggle(address);
            anchorServer = address;
            return;
        }
        selectedServers.clear();
        selectedServers.add(address);
        anchorServer = address;
    }

    public void selectAll(List<String> visibleServerOrder) {
        selectedServers.clear();
        selectedServers.addAll(visibleServerOrder);
        if (!visibleServerOrder.isEmpty()) {
            anchorServer = visibleServerOrder.get(0);
        }
    }

    public boolean isSelected(String address) {
        return selectedServers.contains(address);
    }

    public void clear() {
        selectedServers.clear();
        anchorServer = null;
    }

    public List<String> getSelectedServers() {
        return new ArrayList<>(selectedServers);
    }

    private void toggle(String address) {
        if (!selectedServers.remove(address)) {
            selectedServers.add(address);
        }
    }

    private void selectRange(String start, String end, List<String> visibleServerOrder) {
        int startIndex = visibleServerOrder.indexOf(start);
        int endIndex = visibleServerOrder.indexOf(end);
        if (startIndex < 0 || endIndex < 0) {
            selectedServers.clear();
            selectedServers.add(end);
            anchorServer = end;
            return;
        }
        if (startIndex > endIndex) {
            int swap = startIndex;
            startIndex = endIndex;
            endIndex = swap;
        }
        selectedServers.clear();
        for (int i = startIndex; i <= endIndex; i++) {
            selectedServers.add(visibleServerOrder.get(i));
        }
    }
}
