package com.example.basicfabricmod.geo;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple provider backed by ip-api.com.
 * It only extracts the fields required today and can be swapped out later.
 */
public final class IpApiGeoLocationProvider implements GeoLocationProvider {
    private static final Pattern STATUS_PATTERN = Pattern.compile("\"status\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern COUNTRY_PATTERN = Pattern.compile("\"country\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern COUNTRY_CODE_PATTERN = Pattern.compile("\"countryCode\"\\s*:\\s*\"([^\"]+)\"");

    @Override
    public CountryInfo lookup(String ipAddress) throws IOException {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) URI.create("http://ip-api.com/json/" + ipAddress + "?fields=status,country,countryCode")
                    .toURL()
                    .openConnection();
            connection.setConnectTimeout(2500);
            connection.setReadTimeout(2500);
            connection.setRequestProperty("User-Agent", "basicfabricmod-country-lookup/1.0");

            if (connection.getResponseCode() != 200) {
                throw new IOException("Unexpected geo lookup response: " + connection.getResponseCode());
            }

            try (InputStream inputStream = connection.getInputStream()) {
                String response = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                String status = extract(response, STATUS_PATTERN);
                if (!"success".equalsIgnoreCase(status)) {
                    return CountryInfo.UNKNOWN;
                }

                String country = extract(response, COUNTRY_PATTERN);
                String countryCode = extract(response, COUNTRY_CODE_PATTERN);
                return country == null || countryCode == null ? CountryInfo.UNKNOWN : CountryInfo.of(countryCode, country);
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static String extract(String response, Pattern pattern) {
        Matcher matcher = pattern.matcher(response);
        return matcher.find() ? matcher.group(1) : null;
    }
}
