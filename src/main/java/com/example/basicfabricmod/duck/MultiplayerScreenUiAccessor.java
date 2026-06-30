package com.example.basicfabricmod.duck;

import net.minecraft.client.gui.DrawContext;

/**
 * Implemented by MultiplayerScreenMixin. Lives outside com.example.basicfabricmod.mixin
 * on purpose: Mixin reserves that entire package tree for @Mixin classes only and refuses
 * to load any other class found inside it (IllegalClassLoadError). Plain interfaces that
 * mixins implement/reference directly (rather than just being merged away at transform
 * time) have to live elsewhere.
 */
public interface MultiplayerScreenUiAccessor {
    void basicfabricmod$renderHints(DrawContext context, int mouseX, int mouseY, float delta);
}
