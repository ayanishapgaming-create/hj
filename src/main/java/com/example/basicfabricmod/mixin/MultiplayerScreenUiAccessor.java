package com.example.basicfabricmod.mixin;

import net.minecraft.client.gui.DrawContext;

/**
 * Implemented by {@link MultiplayerScreenMixin}. Screen.render() is not overridden by
 * MultiplayerScreen (it's inherited as-is), so ScreenMixin injects into the real
 * declaring class (Screen) and uses this interface to reach MultiplayerScreen's
 * unique fields instead of mixing into a method MultiplayerScreen doesn't have.
 */
public interface MultiplayerScreenUiAccessor {
    void basicfabricmod$renderHints(DrawContext context, int mouseX, int mouseY, float delta);
}
