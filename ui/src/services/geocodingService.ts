/**
 * Geocoding service for converting coordinates to addresses.
 * This service handles third-party API calls (OpenStreetMap Nominatim).
 * Separated from business logic for better maintainability and testability.
 */

// Cache to reduce API calls
const geocodeCache = new Map<string, { address: string | null; timestamp: number }>();
const CACHE_DURATION = 5 * 60 * 1000; // 5 minutes cache

// Rate limiting: Nominatim allows 1 request per second
let lastRequestTime = 0;
const MIN_REQUEST_INTERVAL = 1000; // 1 second

/**
 * Delay function to ensure rate limiting
 */
const delay = (ms: number): Promise<void> => {
  return new Promise((resolve) => setTimeout(resolve, ms));
};

/**
 * Get cache key from coordinates
 */
const getCacheKey = (latitude: number, longitude: number): string => {
  // Round to 4 decimal places (~11 meters precision) for cache key
  return `${latitude.toFixed(4)},${longitude.toFixed(4)}`;
};

/**
 * Reverse geocoding: convert coordinates to address string.
 * Uses OpenStreetMap Nominatim API (free, no API key required).
 * Includes caching and rate limiting to handle API restrictions.
 * 
 * @param latitude - Latitude coordinate
 * @param longitude - Longitude coordinate
 * @param retries - Number of retry attempts (default: 2)
 * @returns Promise resolving to address string or null if geocoding fails
 */
export const reverseGeocode = async (
  latitude: number,
  longitude: number,
  retries: number = 2
): Promise<string | null> => {
  // Check cache first
  const cacheKey = getCacheKey(latitude, longitude);
  const cached = geocodeCache.get(cacheKey);
  if (cached && Date.now() - cached.timestamp < CACHE_DURATION) {
    return cached.address;
  }

  // Rate limiting: ensure at least 1 second between requests
  const now = Date.now();
  const timeSinceLastRequest = now - lastRequestTime;
  if (timeSinceLastRequest < MIN_REQUEST_INTERVAL) {
    await delay(MIN_REQUEST_INTERVAL - timeSinceLastRequest);
  }

  for (let attempt = 0; attempt <= retries; attempt++) {
    try {
      if (attempt > 0) {
        // Exponential backoff: wait 2^attempt seconds before retry
        const backoffDelay = Math.min(1000 * Math.pow(2, attempt), 5000);
        console.log(`Retrying geocoding (attempt ${attempt + 1}/${retries + 1}) after ${backoffDelay}ms...`);
        await delay(backoffDelay);
      }

      lastRequestTime = Date.now();

      // Use OpenStreetMap Nominatim API (free, no API key required)
      const response = await fetch(
        `https://nominatim.openstreetmap.org/reverse?format=json&lat=${latitude}&lon=${longitude}&zoom=18&addressdetails=1`,
        {
          headers: {
            'User-Agent': 'GeoMeet/1.0', // Required by Nominatim
            'Accept': 'application/json',
          },
          // Add timeout
          signal: AbortSignal.timeout(10000), // 10 second timeout
        }
      );

      if (!response.ok) {
        if (response.status === 429) {
          // Rate limited - wait longer before retry
          console.warn('Rate limited by Nominatim API, waiting before retry...');
          if (attempt < retries) {
            await delay(5000); // Wait 5 seconds for rate limit
            continue;
          }
        }
        console.warn('Reverse geocoding failed:', response.status, response.statusText);
        if (attempt < retries) continue;
        return null;
      }

      const data = await response.json();

      // Extract address components
      const address = data.address;
      if (!address) {
        // Cache null result to avoid repeated failed requests
        geocodeCache.set(cacheKey, { address: null, timestamp: Date.now() });
        return null;
      }

      // Build address string: prefer road, then suburb, then city
      const parts: string[] = [];
      if (address.road) parts.push(address.road);
      if (address.house_number) parts.push(address.house_number);
      if (address.suburb) parts.push(address.suburb);
      if (address.city) parts.push(address.city);
      if (address.country) parts.push(address.country);

      const addressString = parts.length > 0 ? parts.join(', ') : null;
      
      // Cache the result
      geocodeCache.set(cacheKey, { address: addressString, timestamp: Date.now() });
      
      return addressString;
    } catch (error: any) {
      // Handle different error types
      if (error.name === 'AbortError' || error.name === 'TimeoutError') {
        console.warn('Geocoding request timeout:', error);
        if (attempt < retries) continue;
      } else if (error.message?.includes('ERR_CONNECTION_RESET') || 
                 error.message?.includes('network') ||
                 error.message?.includes('Failed to fetch')) {
        console.warn('Network error during geocoding:', error.message);
        if (attempt < retries) continue;
      } else {
        console.error('Reverse geocoding error:', error);
        if (attempt < retries) continue;
      }
      
      // On final attempt, cache null to avoid repeated requests
      if (attempt === retries) {
        geocodeCache.set(cacheKey, { address: null, timestamp: Date.now() });
      }
    }
  }

  return null;
};

