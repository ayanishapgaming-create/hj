package com.example.basicfabricmod.servermanager.ui.controller;

public final class KeyboardController {
    public boolean isSelectAll(int keyCode, boolean ctrl) {
        return ctrl && keyCode == 65;
    }

    public boolean isFocusSearch(int keyCode, boolean ctrl) {
        return ctrl && keyCode == 70;
    }

    public boolean isNewFolder(int keyCode, boolean ctrl) {
        return ctrl && keyCode == 78;
    }

    public boolean isDuplicate(int keyCode, boolean ctrl) {
        return ctrl && keyCode == 68;
    }

    public boolean isUndo(int keyCode, boolean ctrl) {
        return ctrl && keyCode == 90;
    }

    public boolean isRename(int keyCode) {
        return keyCode == 292;
    }

    public boolean isDelete(int keyCode) {
        return keyCode == 261;
    }

    public boolean isEscape(int keyCode) {
        return keyCode == 256;
    }

    public boolean isEnter(int keyCode) {
        return keyCode == 257 || keyCode == 335;
    }

    public boolean isSpace(int keyCode) {
        return keyCode == 32;
    }
}
