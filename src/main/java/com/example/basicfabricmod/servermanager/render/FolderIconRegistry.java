package com.example.basicfabricmod.servermanager.render;

import com.example.basicfabricmod.BasicFabricMod;
import com.example.basicfabricmod.servermanager.model.FolderIcon;
import net.minecraft.util.Identifier;

import java.util.EnumMap;
import java.util.Map;

public final class FolderIconRegistry {
    private static final Map<FolderIcon, Identifier> ICONS = new EnumMap<>(FolderIcon.class);

    static {
        for (FolderIcon icon : FolderIcon.values()) {
            ICONS.put(icon, Identifier.of(BasicFabricMod.MOD_ID, "textures/gui/folder_icons/" + icon.name().toLowerCase() + ".png"));
        }
    }

    private FolderIconRegistry() {
    }

    public static Identifier get(FolderIcon icon) {
        return ICONS.getOrDefault(icon, ICONS.get(FolderIcon.FOLDER));
    }
}
