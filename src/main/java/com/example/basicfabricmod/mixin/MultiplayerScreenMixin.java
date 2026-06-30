package com.example.basicfabricmod.mixin;

import com.example.basicfabricmod.BasicFabricMod;
import com.example.basicfabricmod.duck.MultiplayerScreenUiAccessor;
import com.example.basicfabricmod.servermanager.ui.ServerManagerListWidget;
import com.example.basicfabricmod.servermanager.ui.controller.FolderDialogController;
import com.example.basicfabricmod.servermanager.ui.controller.KeyboardController;
import com.example.basicfabricmod.servermanager.ui.state.MultiplayerUiState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.option.ServerList;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(MultiplayerScreen.class)
public abstract class MultiplayerScreenMixin extends Screen implements MultiplayerScreenUiAccessor {
    @Shadow @Final protected MultiplayerServerListWidget serverListWidget;
    @Shadow @Final private ServerList serverList;

    @Unique private final MultiplayerUiState basicfabricmod$uiState = new MultiplayerUiState();
    @Unique private final KeyboardController basicfabricmod$keyboard = new KeyboardController();
    @Unique private final FolderDialogController basicfabricmod$folderDialogs = new FolderDialogController();
    @Unique private ServerManagerListWidget basicfabricmod$managedList;
    @Unique private TextFieldWidget basicfabricmod$searchField;

    protected MultiplayerScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void basicfabricmod$initEnhancedUi(CallbackInfo ci) {
        try {
            if (BasicFabricMod.getConfig().isShowSearchBar()) {
                basicfabricmod$searchField = this.addDrawableChild(new TextFieldWidget(this.textRenderer, this.width / 2 - 110, 32, 220, 20, Text.literal("Search")));
                basicfabricmod$searchField.setMaxLength(100);
                basicfabricmod$searchField.setChangedListener(value -> {
                    basicfabricmod$uiState.setSearchQuery(value);
                    basicfabricmod$refreshManagedList();
                });
            }

            basicfabricmod$managedList = this.addDrawableChild(new ServerManagerListWidget(this.client, (MultiplayerScreen) (Object) this, this.width, this.height - 92, 58, 36, basicfabricmod$uiState));
            basicfabricmod$managedList.setOnServerSelected(this::basicfabricmod$mirrorSelection);
            basicfabricmod$refreshManagedList();
            BasicFabricMod.LOGGER.info("[ServerManager+] MultiplayerScreen init complete, screen size {}x{}", this.width, this.height);
        } catch (Throwable t) {
            BasicFabricMod.LOGGER.error("[ServerManager+] MultiplayerScreen init failed", t);
        }
    }

    @Override
    public void basicfabricmod$renderHints(DrawContext context, int mouseX, int mouseY, float delta) {
        if (basicfabricmod$searchField != null && basicfabricmod$searchField.isFocused() && !basicfabricmod$searchField.getText().isBlank()) {
            context.drawTextWithShadow(this.textRenderer, "Esc clears search", basicfabricmod$searchField.getX(), basicfabricmod$searchField.getY() - 10, 0xAAAAAA);
        }
        if (basicfabricmod$managedList != null) {
            basicfabricmod$managedList.renderOverlays(context, mouseX, mouseY);
        }
    }

    @Inject(method = "refreshWidgetPositions", at = @At("TAIL"))
    private void basicfabricmod$refreshPositions(CallbackInfo ci) {
        if (basicfabricmod$searchField != null) {
            basicfabricmod$searchField.setX(this.width / 2 - 110);
            basicfabricmod$searchField.setY(32);
        }
        if (basicfabricmod$managedList != null) {
            basicfabricmod$managedList.position(this.width, this.height - 92, 58);
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true, remap = false)
    private void basicfabricmod$keyPressed(net.minecraft.client.input.KeyInput input, CallbackInfoReturnable<Boolean> cir) {
        int keyCode = input.getKeycode();
        boolean ctrl = MinecraftClient.getInstance().isCtrlPressed();
        if (basicfabricmod$searchField != null && basicfabricmod$searchField.isFocused() && basicfabricmod$keyboard.isEscape(keyCode)) {
            basicfabricmod$searchField.setText("");
            basicfabricmod$uiState.setSearchQuery("");
            basicfabricmod$refreshManagedList();
            cir.setReturnValue(true);
            return;
        }
        if (basicfabricmod$keyboard.isFocusSearch(keyCode, ctrl) && basicfabricmod$searchField != null) {
            basicfabricmod$searchField.setFocused(true);
            cir.setReturnValue(true);
            return;
        }
        if (basicfabricmod$keyboard.isNewFolder(keyCode, ctrl)) {
            basicfabricmod$folderDialogs.createFolder("New Folder");
            basicfabricmod$refreshManagedList();
            cir.setReturnValue(true);
            return;
        }
        if (basicfabricmod$keyboard.isSelectAll(keyCode, ctrl) && basicfabricmod$managedList != null) {
            basicfabricmod$managedList.getSelectionController().selectAll(basicfabricmod$getVisibleAddresses());
            cir.setReturnValue(true);
            return;
        }
        if (basicfabricmod$keyboard.isUndo(keyCode, ctrl) && basicfabricmod$managedList != null) {
            basicfabricmod$managedList.performUndo();
            cir.setReturnValue(true);
        }
    }

    @Unique
    private void basicfabricmod$refreshManagedList() {
        if (basicfabricmod$managedList == null || serverList == null) {
            BasicFabricMod.LOGGER.warn("[ServerManager+] refreshManagedList skipped: managedList={}, serverList={}",
                    basicfabricmod$managedList != null, serverList != null);
            return;
        }
        try {
            List<net.minecraft.client.network.ServerInfo> servers = new ArrayList<>();
            for (int i = 0; i < serverList.size(); i++) {
                servers.add(serverList.get(i));
            }
            basicfabricmod$managedList.rebuild(servers);
            serverListWidget.visible = false;
        } catch (Throwable t) {
            BasicFabricMod.LOGGER.error("[ServerManager+] refreshManagedList failed", t);
        }
    }

    @Unique
    private void basicfabricmod$mirrorSelection(String address) {
        if (serverListWidget == null || address == null) {
            return;
        }
        for (MultiplayerServerListWidget.Entry entry : serverListWidget.children()) {
            if (entry instanceof MultiplayerServerListWidget.ServerEntry serverEntry
                    && entry instanceof com.example.basicfabricmod.duck.ServerEntryAccessor accessor
                    && address.equals(accessor.basicfabricmod$getServer().address)) {
                serverListWidget.setSelected(serverEntry);
                break;
            }
        }
    }

    @Unique
    private List<String> basicfabricmod$getVisibleAddresses() {
        List<String> addresses = new ArrayList<>();
        for (int i = 0; i < serverList.size(); i++) {
            addresses.add(serverList.get(i).address);
        }
        return addresses;
    }
}
