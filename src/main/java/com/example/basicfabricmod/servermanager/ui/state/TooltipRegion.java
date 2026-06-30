package com.example.basicfabricmod.servermanager.ui.state;

import net.minecraft.text.Text;

public record TooltipRegion(int x, int y, int width, int height, Text tooltip) {
    public boolean contains(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}
