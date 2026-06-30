package com.example.basicfabricmod.servermanager.ui.controller;

import com.example.basicfabricmod.servermanager.ui.drag.DragPayload;
import com.example.basicfabricmod.servermanager.ui.drag.DragType;
import com.example.basicfabricmod.servermanager.ui.drag.DropTarget;
import com.example.basicfabricmod.servermanager.ui.drag.DropTargetType;

import java.util.List;

/**
 * Stores drag state and computes drop preview information.
 */
public final class DragAndDropController {
    private DragPayload payload;
    private DropTarget target = new DropTarget(DropTargetType.INVALID, "", "", -1, false);
    private double ghostX;
    private double ghostY;
    private boolean dragging;

    public void startServers(List<String> addresses) {
        this.payload = new DragPayload(DragType.SERVERS, List.copyOf(addresses), "");
        this.dragging = true;
    }

    public void startFolder(String folderId) {
        this.payload = new DragPayload(DragType.FOLDER, List.of(), folderId);
        this.dragging = true;
    }

    public void update(double mouseX, double mouseY, DropTarget target) {
        this.ghostX = mouseX;
        this.ghostY = mouseY;
        this.target = target == null ? new DropTarget(DropTargetType.INVALID, "", "", -1, false) : target;
    }

    public void cancel() {
        this.payload = null;
        this.target = new DropTarget(DropTargetType.INVALID, "", "", -1, false);
        this.dragging = false;
    }

    public boolean isDragging() {
        return dragging && payload != null;
    }

    public DragPayload getPayload() {
        return payload;
    }

    public DropTarget getTarget() {
        return target;
    }

    public double getGhostX() {
        return ghostX;
    }

    public double getGhostY() {
        return ghostY;
    }
}
