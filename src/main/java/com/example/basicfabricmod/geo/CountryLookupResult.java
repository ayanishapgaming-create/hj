package com.example.basicfabricmod.geo;

import java.util.Objects;

/**
 * Full lookup result stored in cache and consumed by rendering logic.
 */
public record CountryLookupResult(
        String hostname,
        String resolvedIp,
        CountryInfo countryInfo,
        LookupStatus status,
        long timestamp
) {
    public CountryLookupResult {
        hostname = Objects.requireNonNullElse(hostname, "");
        resolvedIp = Objects.requireNonNullElse(resolvedIp, "");
        countryInfo = Objects.requireNonNullElse(countryInfo, CountryInfo.UNKNOWN);
        status = Objects.requireNonNullElse(status, LookupStatus.FAILED);
    }

    public static CountryLookupResult pending(String hostname) {
        return new CountryLookupResult(hostname, "", CountryInfo.UNKNOWN, LookupStatus.PENDING, System.currentTimeMillis());
    }

    public static CountryLookupResult skipped(String hostname) {
        return new CountryLookupResult(hostname, "", CountryInfo.UNKNOWN, LookupStatus.SKIPPED, System.currentTimeMillis());
    }

    public static CountryLookupResult failed(String hostname, String resolvedIp) {
        return new CountryLookupResult(hostname, resolvedIp, CountryInfo.UNKNOWN, LookupStatus.FAILED, System.currentTimeMillis());
    }

    public static CountryLookupResult resolved(String hostname, String resolvedIp, CountryInfo countryInfo) {
        return new CountryLookupResult(hostname, resolvedIp, countryInfo, LookupStatus.RESOLVED, System.currentTimeMillis());
    }

    public boolean isTerminal() {
        return status == LookupStatus.RESOLVED || status == LookupStatus.FAILED || status == LookupStatus.SKIPPED;
    }
}
