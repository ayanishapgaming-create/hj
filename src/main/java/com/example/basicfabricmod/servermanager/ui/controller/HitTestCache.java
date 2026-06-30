package com.example.basicfabricmod.servermanager.ui.controller;

import com.example.basicfabricmod.servermanager.ui.drag.DropTarget;

import java.util.HashMap;
import java.util.Map;

public final class HitTestCache {
    private final Map<Integer, DropTarget> rowTargets = new HashMap<>();

    public void clear() {
        rowTargets.clear();
    }

    public DropTarget get(int rowIndex) {
        return rowTargets.get(rowIndex);
    }

    public void put(int rowIndex, DropTarget target) {
        rowTargets.put(rowIndex, target);
    }
}
