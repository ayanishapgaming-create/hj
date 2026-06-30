package com.example.basicfabricmod.servermanager.service;

import net.minecraft.client.network.ServerInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lightweight cache for ping presentation data derived from vanilla server ping state.
 */
public final class PingDisplayCache {
    private static final PingDisplayCache INSTANCE = new PingDisplayCache();

    private final Map<String, PingDisplayData> byAddress = new ConcurrentHashMap<>();

    private PingDisplayCache() {
    }

    public static PingDisplayCache getInstance() {
        return INSTANCE;
    }

    public PingDisplayData get(ServerInfo info) {
        if (info == null || info.address == null) {
            return PingDisplayData.unknown();
        }
        return byAddress.compute(info.address, (address, previous) -> PingDisplayData.from(info, previous));
    }

    public record PingDisplayData(long pingMs, String quality, int stars, long lastUpdatedTime) {
        public static PingDisplayData from(ServerInfo info, PingDisplayData previous) {
            long ping = info.ping;
            if (ping < 0) {
                return previous != null ? previous : unknown();
            }
            return new PingDisplayData(ping, qualityFor(ping), starsFor(ping), System.currentTimeMillis());
        }

        public static PingDisplayData unknown() {
            return new PingDisplayData(-1L, "Unknown", 0, 0L);
        }

        private static String qualityFor(long ping) {
            if (ping >= 0 && ping < 30) return "Excellent";
            if (ping < 70) return "Good";
            if (ping < 130) return "Fair";
            if (ping < 220) return "Poor";
            return "Bad";
        }

        private static int starsFor(long ping) {
            if (ping >= 0 && ping < 30) return 5;
            if (ping < 70) return 4;
            if (ping < 130) return 3;
            if (ping < 220) return 2;
            return ping >= 0 ? 1 : 0;
        }
    }
}
