package com.example.basicfabricmod.servermanager.ui.layout;

import net.minecraft.client.font.TextRenderer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Calculates row sub-component positions once per shape and reuses them.
 */
public final class RowLayoutEngine {
    private final Map<RowLayoutKey, RowLayout> cache = new ConcurrentHashMap<>();

    public RowLayout getLayout(int x, int y, int width, int height, boolean folderRow, boolean favorite, boolean pinned, TextRenderer textRenderer) {
        RowLayoutKey key = new RowLayoutKey(width, height, folderRow, favorite, pinned);
        RowLayout base = cache.computeIfAbsent(key, ignored -> createBaseLayout(width, height));
        return new RowLayout(
                x,
                y,
                width,
                height,
                x + base.arrowX(),
                y + base.arrowY(),
                x + base.iconX(),
                y + base.iconY(),
                x + base.flagX(),
                y + base.flagY(),
                x + base.titleX(),
                y + base.titleY(),
                x + base.subtitleX(),
                y + base.subtitleY(),
                x + base.favoriteX(),
                y + base.favoriteY(),
                x + base.playerCountX(),
                y + base.playerCountY(),
                x + base.versionX(),
                y + base.versionY(),
                x + base.pingBarsX(),
                y + base.pingBarsY(),
                x + base.pingTextX(),
                y + base.pingTextY(),
                x + base.rightEdge()
        );
    }

    private RowLayout createBaseLayout(int width, int height) {
        int centerY = Math.max(0, (height - 8) / 2);
        int rightEdge = width - 6;
        return new RowLayout(
                0, 0, width, height,
                4, centerY,
                16, centerY,
                36, centerY,
                50, 3,
                50, 14,
                155, 3,
                rightEdge - 110, 3,
                rightEdge - 66, 3,
                rightEdge - 42, 3,
                rightEdge - 38, 3,
                rightEdge
        );
    }
}
