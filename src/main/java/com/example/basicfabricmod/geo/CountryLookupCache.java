package com.example.basicfabricmod.geo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory cache keyed by hostname and resolved IP.
 * The structure is intentionally simple so it can be persisted later.
 */
public final class CountryLookupCache {
    private static final long SUCCESS_TTL_MS = 24L * 60L * 60L * 1000L;
    private static final long FAILURE_TTL_MS = 30L * 60L * 1000L;

    private final Map<String, CountryLookupResult> byHostname = new ConcurrentHashMap<>();
    private final Map<String, CountryLookupResult> byResolvedIp = new ConcurrentHashMap<>();

    public CountryLookupResult getFreshByHostname(String hostname) {
        return getFresh(byHostname.get(normalize(hostname)));
    }

    public CountryLookupResult getFreshByResolvedIp(String ip) {
        return getFresh(byResolvedIp.get(normalize(ip)));
    }

    public void put(CountryLookupResult result) {
        if (result == null) {
            return;
        }

        if (!result.hostname().isBlank()) {
            byHostname.put(normalize(result.hostname()), result);
        }
        if (!result.resolvedIp().isBlank()) {
            byResolvedIp.put(normalize(result.resolvedIp()), result);
        }
    }

    private CountryLookupResult getFresh(CountryLookupResult result) {
        if (result == null) {
            return null;
        }
        long ttl = result.status() == LookupStatus.RESOLVED ? SUCCESS_TTL_MS : FAILURE_TTL_MS;
        return System.currentTimeMillis() - result.timestamp() <= ttl ? result : null;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
