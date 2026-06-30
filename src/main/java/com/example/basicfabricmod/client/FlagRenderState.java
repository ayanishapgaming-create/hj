package com.example.basicfabricmod.client;

/**
 * Stores per-entry UI coordinates for the rendered flag so tooltip hit testing is reliable.
 */
public record FlagRenderState(int x, int y, int width, int height, String tooltipText) {
    public boolean contains(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}
