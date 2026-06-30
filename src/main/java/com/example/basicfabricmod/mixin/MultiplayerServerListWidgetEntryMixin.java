package com.example.basicfabricmod.mixin;

import com.example.basicfabricmod.BasicFabricMod;
import com.example.basicfabricmod.client.FlagRenderState;
import com.example.basicfabricmod.client.ServerCountryRenderStore;
import com.example.basicfabricmod.duck.ServerEntryAccessor;
import com.example.basicfabricmod.geo.CountryLookupResult;
import com.example.basicfabricmod.geo.LookupStatus;
import com.example.basicfabricmod.geo.ServerCountryService;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.network.ServerInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiplayerServerListWidget.ServerEntry.class)
public abstract class MultiplayerServerListWidgetEntryMixin implements ServerEntryAccessor {
    @Shadow @Final private MinecraftClient client;
    @Shadow @Final private ServerInfo server;

    @Override
    public ServerInfo basicfabricmod$getServer() {
        return this.server;
    }


    @Unique
    private static final int TEXT_COLOR = 0xFFFFFF;
    @Unique
    private static final int NAME_X_OFFSET = 35;
    @Unique
    private static final int NAME_Y_OFFSET = 1;
    @Unique
    private static final int FLAG_BOX_HEIGHT = 9;

    @Inject(method = "render", at = @At("TAIL"), remap = false)
    private void basicfabricmod$renderFlag(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks, CallbackInfo ci) {
        if (!BasicFabricMod.getConfig().isShowServerCountryFlags()) {
            return;
        }

        CountryLookupResult result = ServerCountryService.getInstance().getOrRequest(server);
        if (result == null) {
            return;
        }

        String flag = result.status() == LookupStatus.RESOLVED ? result.countryInfo().flagEmoji() : "🌍";
        String tooltip = result.status() == LookupStatus.RESOLVED ? result.countryInfo().countryName() : result.countryInfo().countryName();

        TextRenderer textRenderer = this.client.textRenderer;
        int x = ((MultiplayerServerListWidget.ServerEntry) (Object) this).getX() + NAME_X_OFFSET;
        int y = ((MultiplayerServerListWidget.ServerEntry) (Object) this).getY() + NAME_Y_OFFSET;
        int textWidth = textRenderer.getWidth(flag);

        context.drawTextWithShadow(textRenderer, flag, x, y, TEXT_COLOR);
        ServerCountryRenderStore.put(this.server, new FlagRenderState(x, y, Math.max(textWidth, 8), FLAG_BOX_HEIGHT, tooltip));
    }
}
