package com.example.basicfabricmod;

import com.example.basicfabricmod.config.ModConfig;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BasicFabricMod implements ClientModInitializer {
    public static final String MOD_ID = "basicfabricmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static final ModConfig CONFIG = new ModConfig(true);

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing {}", MOD_ID);
    }

    public static ModConfig getConfig() {
        return CONFIG;
    }
}
