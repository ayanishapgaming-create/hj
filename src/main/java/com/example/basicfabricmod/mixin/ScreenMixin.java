package com.example.basicfabricmod.mixin;

import com.example.basicfabricmod.duck.MultiplayerScreenUiAccessor;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * render() is declared on Screen, not on MultiplayerScreen (MultiplayerScreen inherits it
 * unchanged), so injecting "render" via @Mixin(MultiplayerScreen.class) has no bytecode
 * target and throws InvalidInjectionException. Mixing into Screen instead works because
 * that's the class that actually has the method; the instanceof check scopes the effect
 * back down to MultiplayerScreen only, so every other Screen is untouched.
 */
@Mixin(Screen.class)
public abstract class ScreenMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void basicfabricmod$delegateRenderHints(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if ((Object) this instanceof MultiplayerScreenUiAccessor accessor) {
            accessor.basicfabricmod$renderHints(context, mouseX, mouseY, delta);
        }
    }
}
