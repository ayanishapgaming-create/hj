package com.example.basicfabricmod.servermanager.ui.controller;

import net.minecraft.client.MinecraftClient;

public final class ClipboardController {
    public void copy(String text) {
        if (text != null) {
            MinecraftClient.getInstance().keyboard.setClipboard(text);
        }
    }
}
