package com.example.basicfabricmod.geo;

import java.util.Locale;
import java.util.Objects;

/**
 * Immutable country display data used by the UI and cache.
 */
public record CountryInfo(String countryCode, String countryName, String flagEmoji) {
    public static final CountryInfo UNKNOWN = new CountryInfo("XX", "Unknown Location", "🌍");

    public CountryInfo {
        countryCode = normalizeCode(countryCode);
        countryName = Objects.requireNonNullElse(countryName, UNKNOWN.countryName);
        flagEmoji = Objects.requireNonNullElse(flagEmoji, UNKNOWN.flagEmoji);
    }

    public static CountryInfo of(String countryCode, String countryName) {
        String normalizedCode = normalizeCode(countryCode);
        if (normalizedCode.equals(UNKNOWN.countryCode)) {
            return UNKNOWN;
        }
        return new CountryInfo(normalizedCode, countryName, toFlagEmoji(normalizedCode));
    }

    private static String normalizeCode(String countryCode) {
        if (countryCode == null || countryCode.isBlank()) {
            return UNKNOWN.countryCode;
        }
        String normalized = countryCode.trim().toUpperCase(Locale.ROOT);
        return normalized.length() == 2 ? normalized : UNKNOWN.countryCode;
    }

    private static String toFlagEmoji(String countryCode) {
        if (countryCode.length() != 2 || countryCode.equals(UNKNOWN.countryCode)) {
            return UNKNOWN.flagEmoji;
        }

        int first = Character.codePointAt(countryCode, 0) - 'A' + 0x1F1E6;
        int second = Character.codePointAt(countryCode, 1) - 'A' + 0x1F1E6;
        return new String(Character.toChars(first)) + new String(Character.toChars(second));
    }
}
