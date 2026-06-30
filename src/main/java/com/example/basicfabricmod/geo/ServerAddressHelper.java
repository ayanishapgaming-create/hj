package com.example.basicfabricmod.geo;

import net.minecraft.client.network.ServerAddress;

import java.net.IDN;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Locale;

/**
 * Normalizes server hostnames and filters out addresses that should not be geolocated.
 */
public final class ServerAddressHelper {
    private ServerAddressHelper() {
    }

    public static String extractHost(String address) {
        if (address == null || address.isBlank()) {
            return "";
        }

        try {
            String host = ServerAddress.parse(address).getAddress();
            return host == null ? "" : IDN.toASCII(host.trim()).toLowerCase(Locale.ROOT);
        } catch (Exception ignored) {
            return address.trim().toLowerCase(Locale.ROOT);
        }
    }

    public static boolean shouldSkipHost(String host) {
        if (host == null || host.isBlank()) {
            return true;
        }

        String normalized = host.trim().toLowerCase(Locale.ROOT);
        if (normalized.equals("localhost") || normalized.endsWith(".local") || normalized.endsWith(".lan")) {
            return true;
        }

        try {
            return isNonPublic(InetAddress.getByName(normalized));
        } catch (UnknownHostException ignored) {
            return false;
        }
    }

    public static String resolvePublicIp(String host) throws UnknownHostException {
        InetAddress address = InetAddress.getByName(host);
        if (isNonPublic(address)) {
            throw new UnknownHostException("Non-public address: " + host);
        }
        return address.getHostAddress();
    }

    private static boolean isNonPublic(InetAddress address) {
        return address.isAnyLocalAddress()
                || address.isLoopbackAddress()
                || address.isLinkLocalAddress()
                || address.isSiteLocalAddress()
                || address.isMulticastAddress();
    }
}
