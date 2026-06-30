package com.example.basicfabricmod.servermanager.ui.controller;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Lightweight recent-operation undo stack.
 */
public final class UndoController {
    private final Deque<Runnable> undoStack = new ArrayDeque<>();
    private final int maxEntries;

    public UndoController(int maxEntries) {
        this.maxEntries = Math.max(1, maxEntries);
    }

    public void push(Runnable undoAction) {
        if (undoAction == null) {
            return;
        }
        while (undoStack.size() >= maxEntries) {
            undoStack.removeLast();
        }
        undoStack.push(undoAction);
    }

    public boolean undo() {
        Runnable action = undoStack.pollFirst();
        if (action == null) {
            return false;
        }
        action.run();
        return true;
    }
}
