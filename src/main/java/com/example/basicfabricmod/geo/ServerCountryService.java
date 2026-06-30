package com.example.basicfabricmod.geo;

import com.example.basicfabricmod.BasicFabricMod;
import net.minecraft.client.network.ServerInfo;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Coordinates asynchronous hostname resolution, provider lookup, and caching.
 */
public final class ServerCountryService {
    private static final ServerCountryService INSTANCE = new ServerCountryService();

    private final CountryLookupCache cache = new CountryLookupCache();
    private final GeoLocationProvider provider = new IpApiGeoLocationProvider();
    private final ExecutorService executor = Executors.newFixedThreadPool(2, runnable -> {
        Thread thread = new Thread(runnable, "basicfabricmod-country-lookup");
        thread.setDaemon(true);
        return thread;
    });
    private final Set<String> inFlightHosts = ConcurrentHashMap.newKeySet();

    private ServerCountryService() {
    }

    public static ServerCountryService getInstance() {
        return INSTANCE;
    }

    public CountryLookupResult getOrRequest(ServerInfo serverInfo) {
        if (serverInfo == null || !BasicFabricMod.getConfig().isShowServerCountryFlags()) {
            return null;
        }

        if (serverInfo.isLocal()) {
            CountryLookupResult skipped = CountryLookupResult.skipped(serverInfo.address);
            cache.put(skipped);
            return skipped;
        }

        String host = ServerAddressHelper.extractHost(serverInfo.address);
        if (host.isBlank() || ServerAddressHelper.shouldSkipHost(host)) {
            CountryLookupResult skipped = CountryLookupResult.skipped(host);
            cache.put(skipped);
            return skipped;
        }

        CountryLookupResult cached = cache.getFreshByHostname(host);
        if (cached != null) {
            return cached;
        }

        if (inFlightHosts.add(host)) {
            CountryLookupResult pending = CountryLookupResult.pending(host);
            cache.put(pending);
            CompletableFuture.runAsync(() -> performLookup(host), executor)
                    .whenComplete((unused, throwable) -> {
                        if (throwable != null) {
                            cache.put(CountryLookupResult.failed(host, ""));
                            BasicFabricMod.LOGGER.debug("Country lookup failed for {}", host);
                        }
                        inFlightHosts.remove(host);
                    });
            return pending;
        }

        return CountryLookupResult.pending(host);
    }

    private void performLookup(String host) {
        try {
            String resolvedIp = ServerAddressHelper.resolvePublicIp(host);
            CountryLookupResult cachedByIp = cache.getFreshByResolvedIp(resolvedIp);
            if (cachedByIp != null && cachedByIp.status() != LookupStatus.PENDING) {
                cache.put(new CountryLookupResult(host, resolvedIp, cachedByIp.countryInfo(), cachedByIp.status(), cachedByIp.timestamp()));
                return;
            }

            CountryInfo countryInfo = provider.lookup(resolvedIp);
            cache.put(countryInfo == CountryInfo.UNKNOWN
                    ? CountryLookupResult.failed(host, resolvedIp)
                    : CountryLookupResult.resolved(host, resolvedIp, countryInfo));
        } catch (Exception ignored) {
            cache.put(CountryLookupResult.failed(host, ""));
        }
    }
}
