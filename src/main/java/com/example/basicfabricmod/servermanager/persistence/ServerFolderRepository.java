package com.example.basicfabricmod.servermanager.persistence;

import com.example.basicfabricmod.BasicFabricMod;
import com.example.basicfabricmod.servermanager.model.ServerFolderConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.MinecraftClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ServerFolderRepository {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "server_folders.json";

    public ServerFolderConfig load() {
        Path path = resolvePath();
        if (!Files.exists(path)) {
            return new ServerFolderConfig();
        }

        try {
            String json = Files.readString(path);
            ServerFolderConfig config = GSON.fromJson(json, ServerFolderConfig.class);
            return config == null ? new ServerFolderConfig() : config;
        } catch (Exception exception) {
            BasicFabricMod.LOGGER.warn("Failed to load {}", FILE_NAME);
            return new ServerFolderConfig();
        }
    }

    public void save(ServerFolderConfig config) {
        Path path = resolvePath();
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(config));
        } catch (IOException exception) {
            BasicFabricMod.LOGGER.warn("Failed to save {}", FILE_NAME);
        }
    }

    private Path resolvePath() {
        return MinecraftClient.getInstance().runDirectory.toPath().resolve(FILE_NAME);
    }
}
