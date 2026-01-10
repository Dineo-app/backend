package com.dineo_backend.dineo.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Shared geocoding utility for all controllers
 * Implements rate limiting and caching to avoid Nominatim API blocks
 */
public class GeocodingUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(GeocodingUtil.class);
    
    // Shared cache across entire application
    private static final Map<String, Double[]> GEOCODE_CACHE = new ConcurrentHashMap<>();
    
    // Rate limiting: Track last API call time
    private static volatile long lastApiCallTime = 0;
    private static final long MIN_REQUEST_INTERVAL_MS = 1100; // 1.1 seconds between requests
    
    /**
     * Geocode an address to coordinates with rate limiting and caching
     * @param address The address to geocode
     * @return Array of [latitude, longitude] or null if geocoding fails
     */
    public static Double[] geocodeAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            logger.warn("⚠️ Cannot geocode null or empty address");
            return null;
        }
        
        // Trim address to remove whitespace/newlines
        address = address.trim();
        
        // Check cache first
        if (GEOCODE_CACHE.containsKey(address)) {
            logger.info("💾 Cache hit for address: {}", address);
            return GEOCODE_CACHE.get(address);
        }
        
        // Rate limiting: Ensure we don't exceed 1 request per second
        synchronized (GeocodingUtil.class) {
            long currentTime = System.currentTimeMillis();
            long timeSinceLastCall = currentTime - lastApiCallTime;
            
            if (timeSinceLastCall < MIN_REQUEST_INTERVAL_MS) {
                long sleepTime = MIN_REQUEST_INTERVAL_MS - timeSinceLastCall;
                logger.info("⏱️ Rate limiting: sleeping for {}ms", sleepTime);
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.warn("Rate limiting sleep interrupted");
                }
            }
            
            try {
                logger.info("🌍 Attempting to geocode: {}", address);
                
                String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
                String url = "https://nominatim.openstreetmap.org/search?q=" + encodedAddress 
                           + "&format=json&limit=1";
                
                logger.info("📡 Calling Nominatim API: {}", url);
                
                // Create HTTP client with timeout
                java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                    .connectTimeout(java.time.Duration.ofSeconds(10))
                    .build();
                
                java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .header("User-Agent", "DineoApp/1.0")
                    .timeout(java.time.Duration.ofSeconds(10))
                    .GET()
                    .build();
                
                // Update last API call time BEFORE making the call
                lastApiCallTime = System.currentTimeMillis();
                
                // Send request
                java.net.http.HttpResponse<String> response = client.send(request, 
                    java.net.http.HttpResponse.BodyHandlers.ofString());
                
                logger.info("📥 Nominatim response status: {}", response.statusCode());
                
                if (response.statusCode() == 200) {
                    String responseBody = response.body();
                    
                    // Parse JSON response
                    if (responseBody.startsWith("[") && responseBody.contains("\"lat\"") && responseBody.contains("\"lon\"")) {
                        // Extract lat and lon using regex
                        java.util.regex.Pattern latPattern = java.util.regex.Pattern.compile("\"lat\":\"([^\"]+)\"");
                        java.util.regex.Pattern lonPattern = java.util.regex.Pattern.compile("\"lon\":\"([^\"]+)\"");
                        
                        java.util.regex.Matcher latMatcher = latPattern.matcher(responseBody);
                        java.util.regex.Matcher lonMatcher = lonPattern.matcher(responseBody);
                        
                        if (latMatcher.find() && lonMatcher.find()) {
                            double lat = Double.parseDouble(latMatcher.group(1));
                            double lon = Double.parseDouble(lonMatcher.group(1));
                            
                            Double[] coords = new Double[]{lat, lon};
                            // Cache the result
                            GEOCODE_CACHE.put(address, coords);
                            
                            logger.info("✅ Geocoded '{}' to: [{}, {}]", address, lat, lon);
                            return coords;
                        }
                    }
                    
                    logger.warn("⚠️ No geocoding results for address: {}", address);
                }
                
            } catch (Exception e) {
                logger.error("❌ Error geocoding address '{}': {}", address, e.getMessage());
            }
        }
        
        return null;
    }
    
    /**
     * Calculate distance between two coordinates using Haversine formula
     * @return Distance in kilometers
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS_KM = 6371;
        
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS_KM * c;
    }
    
    /**
     * Pre-populate cache with known addresses
     * Call this on application startup to avoid hitting Nominatim for known addresses
     */
    public static void prePopulateCache(Map<String, Double[]> knownAddresses) {
        GEOCODE_CACHE.putAll(knownAddresses);
        logger.info("📦 Pre-populated geocoding cache with {} addresses", knownAddresses.size());
    }
    
    /**
     * Get cache size for monitoring
     */
    public static int getCacheSize() {
        return GEOCODE_CACHE.size();
    }
}
