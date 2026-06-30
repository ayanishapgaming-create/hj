package com.example.basicfabricmod.servermanager.model;

import java.util.Objects;

public final class ServerFolder {
    private final String id;
    private String name;
    private FolderIcon icon;
    private boolean pinned;
    private boolean collapsed;
    private int order;

    public ServerFolder(String id, String name, FolderIcon icon, boolean pinned, boolean collapsed, int order) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNullElse(name, "Folder");
        this.icon = icon == null ? FolderIcon.FOLDER : icon;
        this.pinned = pinned;
        this.collapsed = collapsed;
        this.order = order;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNullElse(name, this.name);
    }

    public FolderIcon getIcon() {
        return icon;
    }

    public void setIcon(FolderIcon icon) {
        this.icon = icon == null ? FolderIcon.FOLDER : icon;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public boolean isCollapsed() {
        return collapsed;
    }

    public void setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
