package com.example.basicfabricmod.geo;

import java.io.IOException;

/**
 * Abstraction for pluggable geolocation providers.
 */
public interface GeoLocationProvider {
    CountryInfo lookup(String ipAddress) throws IOException;
}
