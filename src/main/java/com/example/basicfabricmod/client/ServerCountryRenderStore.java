package com.example.basicfabricmod.client;

import net.minecraft.client.network.ServerInfo;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Weakly tracks tooltip bounds for server rows without leaking screen state.
 */
public final class ServerCountryRenderStore {
    private static final Map<ServerInfo, FlagRenderState> STATES = Collections.synchronizedMap(new WeakHashMap<>());

    private ServerCountryRenderStore() {
    }

    public static void put(ServerInfo serverInfo, FlagRenderState state) {
        if (serverInfo != null && state != null) {
            STATES.put(serverInfo, state);
        }
    }

    public static FlagRenderState get(ServerInfo serverInfo) {
        return serverInfo == null ? null : STATES.get(serverInfo);
    }
}
