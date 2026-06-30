package com.example.basicfabricmod.config;

/**
 * Runtime configuration holder for client UI features.
 * Structured for future disk persistence without changing callers.
 */
public final class ModConfig {
    private volatile boolean showServerCountryFlags;
    private volatile boolean showNumericalPing;
    private volatile boolean showFavorites;
    private volatile boolean showFolderIcons;
    private volatile boolean showSearchBar;
    private volatile boolean showFolderCounts;
    private volatile boolean hoverAnimations;
    private volatile boolean expandAnimations;
    private volatile boolean contextMenus;
    private volatile boolean dragAndDrop;

    public ModConfig(boolean showServerCountryFlags) {
        this.showServerCountryFlags = showServerCountryFlags;
        this.showNumericalPing = true;
        this.showFavorites = true;
        this.showFolderIcons = true;
        this.showSearchBar = true;
        this.showFolderCounts = true;
        this.hoverAnimations = true;
        this.expandAnimations = true;
        this.contextMenus = true;
        this.dragAndDrop = true;
    }

    public boolean isShowServerCountryFlags() {
        return showServerCountryFlags;
    }

    public void setShowServerCountryFlags(boolean showServerCountryFlags) {
        this.showServerCountryFlags = showServerCountryFlags;
    }

    public boolean isShowNumericalPing() {
        return showNumericalPing;
    }

    public void setShowNumericalPing(boolean showNumericalPing) {
        this.showNumericalPing = showNumericalPing;
    }

    public boolean isShowFavorites() {
        return showFavorites;
    }

    public void setShowFavorites(boolean showFavorites) {
        this.showFavorites = showFavorites;
    }

    public boolean isShowFolderIcons() {
        return showFolderIcons;
    }

    public void setShowFolderIcons(boolean showFolderIcons) {
        this.showFolderIcons = showFolderIcons;
    }

    public boolean isShowSearchBar() {
        return showSearchBar;
    }

    public void setShowSearchBar(boolean showSearchBar) {
        this.showSearchBar = showSearchBar;
    }

    public boolean isShowFolderCounts() {
        return showFolderCounts;
    }

    public void setShowFolderCounts(boolean showFolderCounts) {
        this.showFolderCounts = showFolderCounts;
    }

    public boolean isHoverAnimations() {
        return hoverAnimations;
    }

    public void setHoverAnimations(boolean hoverAnimations) {
        this.hoverAnimations = hoverAnimations;
    }

    public boolean isExpandAnimations() {
        return expandAnimations;
    }

    public void setExpandAnimations(boolean expandAnimations) {
        this.expandAnimations = expandAnimations;
    }

    public boolean isContextMenus() {
        return contextMenus;
    }

    public void setContextMenus(boolean contextMenus) {
        this.contextMenus = contextMenus;
    }

    public boolean isDragAndDrop() {
        return dragAndDrop;
    }

    public void setDragAndDrop(boolean dragAndDrop) {
        this.dragAndDrop = dragAndDrop;
    }
}
