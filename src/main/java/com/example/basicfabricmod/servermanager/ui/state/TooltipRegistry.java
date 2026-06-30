package com.example.basicfabricmod.servermanager.ui.state;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TooltipRegistry {
    private final List<TooltipRegion> regions = new ArrayList<>();

    public void clear() {
        regions.clear();
    }

    public void add(TooltipRegion region) {
        if (region != null) {
            regions.add(region);
        }
    }

    public TooltipRegion find(double mouseX, double mouseY) {
        for (int i = regions.size() - 1; i >= 0; i--) {
            TooltipRegion region = regions.get(i);
            if (region.contains(mouseX, mouseY)) {
                return region;
            }
        }
        return null;
    }

    public List<TooltipRegion> snapshot() {
        return Collections.unmodifiableList(regions);
    }
}
