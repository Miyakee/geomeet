/**
 * Geocoding service for converting coordinates to addresses and vice versa.
 * Supports multiple geocoding providers with automatic fallback.
 * Separated from business logic for better maintainability and testability.
 */

// Configuration - Provider priority: opencage > positionstack > nominatim
const GEOCODING_PROVIDER = (import.meta.env.VITE_GEOCODING_PROVIDER || 'auto').toLowerCase();
const OPENCAGE_API_KEY = import.meta.env.VITE_OPENCAGE_API_KEY || '';
const POSITIONSTACK_API_KEY = import.meta.env.VITE_POSITIONSTACK_API_KEY || '';

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
 * Determine which provider to use based on configuration and available API keys
 */
const getProvider = (): 'opencage' | 'positionstack' | 'nominatim' | 'auto' => {
  if (GEOCODING_PROVIDER === 'opencage' && OPENCAGE_API_KEY) {
    return 'opencage';
  }
  if (GEOCODING_PROVIDER === 'positionstack' && POSITIONSTACK_API_KEY) {
    return 'positionstack';
  }
  if (GEOCODING_PROVIDER === 'nominatim') {
    return 'nominatim';
  }
  // Auto mode: will try multiple providers
  return 'auto';
};

/**
 * Reverse geocoding using OpenCage API
 */
const reverseGeocodeOpenCage = async (
  latitude: number,
  longitude: number,
): Promise<string | null> => {
  const response = await fetch(
    `https://api.opencagedata.com/geocode/v1/json?q=${latitude}+${longitude}&key=${OPENCAGE_API_KEY}&no_annotations=1&limit=1`,
    {
      headers: {
        'Accept': 'application/json',
      },
      signal: AbortSignal.timeout(10000),
      referrerPolicy: 'no-referrer',
    },
  );

  if (!response.ok) {
    return null;
  }

  const data = await response.json();
  if (data.results && data.results.length > 0) {
    return data.results[0].formatted;
  }
  return null;
};

/**
 * Reverse geocoding using PositionStack API
 */
const reverseGeocodePositionStack = async (
  latitude: number,
  longitude: number,
): Promise<string | null> => {
  const response = await fetch(
    `https://api.positionstack.com/v1/reverse?access_key=${POSITIONSTACK_API_KEY}&query=${latitude},${longitude}&limit=1`,
    {
      headers: {
        'Accept': 'application/json',
      },
      signal: AbortSignal.timeout(10000),
      referrerPolicy: 'no-referrer',
    },
  );

  if (!response.ok) {
    return null;
  }

  const data = await response.json();
  if (data.data && data.data.length > 0) {
    const result = data.data[0];
    // Build address string from PositionStack response
    const parts: string[] = [];
    if (result.label) {
      return result.label; // PositionStack provides formatted label
    }
    if (result.name) parts.push(result.name);
    if (result.locality) parts.push(result.locality);
    if (result.region) parts.push(result.region);
    if (result.country) parts.push(result.country);
    return parts.length > 0 ? parts.join(', ') : null;
  }
  return null;
};

/**
 * Reverse geocoding using Nominatim API
 */
const reverseGeocodeNominatim = async (
  latitude: number,
  longitude: number,
): Promise<string | null> => {
  const response = await fetch(
    `https://nominatim.openstreetmap.org/reverse?format=json&lat=${latitude}&lon=${longitude}&zoom=18&addressdetails=1`,
    {
      headers: {
        'User-Agent': 'GeoMeet/1.0',
        'Accept': 'application/json',
      },
      signal: AbortSignal.timeout(10000),
      referrerPolicy: 'no-referrer',
    },
  );

  if (!response.ok) {
    return null;
  }

  const data = await response.json();
  const address = data.address;
  if (!address) {
    return null;
  }

  // Build address string
  const parts: string[] = [];
  if (address.road) parts.push(address.road);
  if (address.house_number) parts.push(address.house_number);
  if (address.suburb) parts.push(address.suburb);
  if (address.city) parts.push(address.city);
  if (address.country) parts.push(address.country);

  return parts.length > 0 ? parts.join(', ') : null;
};

/**
 * Forward geocoding using OpenCage API
 */
const forwardGeocodeOpenCage = async (
  address: string,
): Promise<{ latitude: number; longitude: number } | null> => {
  const encodedAddress = encodeURIComponent(address.trim());
  const response = await fetch(
    `https://api.opencagedata.com/geocode/v1/json?q=${encodedAddress}&key=${OPENCAGE_API_KEY}&limit=1`,
    {
      headers: {
        'Accept': 'application/json',
      },
      signal: AbortSignal.timeout(10000),
      referrerPolicy: 'no-referrer',
    },
  );

  if (!response.ok) {
    return null;
  }

  const data = await response.json();
  if (data.results && data.results.length > 0) {
    const result = data.results[0];
    return {
      latitude: result.geometry.lat,
      longitude: result.geometry.lng,
    };
  }
  return null;
};

/**
 * Forward geocoding using PositionStack API
 */
const forwardGeocodePositionStack = async (
  address: string,
): Promise<{ latitude: number; longitude: number } | null> => {
  const encodedAddress = encodeURIComponent(address.trim());
  const response = await fetch(
    `https://api.positionstack.com/v1/forward?access_key=${POSITIONSTACK_API_KEY}&query=${encodedAddress}&limit=1`,
    {
      headers: {
        'Accept': 'application/json',
      },
      signal: AbortSignal.timeout(10000),
      referrerPolicy: 'no-referrer',
    },
  );

  if (!response.ok) {
    return null;
  }

  const data = await response.json();
  if (data.data && data.data.length > 0) {
    const result = data.data[0];
    return {
      latitude: result.latitude,
      longitude: result.longitude,
    };
  }
  return null;
};

/**
 * Forward geocoding using Nominatim API
 */
const forwardGeocodeNominatim = async (
  address: string,
): Promise<{ latitude: number; longitude: number } | null> => {
  const encodedAddress = encodeURIComponent(address.trim());
  const response = await fetch(
    `https://nominatim.openstreetmap.org/search?format=json&q=${encodedAddress}&limit=1&addressdetails=1`,
    {
      headers: {
        'User-Agent': 'GeoMeet/1.0',
        'Accept': 'application/json',
      },
      signal: AbortSignal.timeout(10000),
      referrerPolicy: 'no-referrer',
    },
  );

  if (!response.ok) {
    return null;
  }

  const data = await response.json();
  if (!Array.isArray(data) || data.length === 0) {
    return null;
  }

  const result = data[0];
  const latitude = parseFloat(result.lat);
  const longitude = parseFloat(result.lon);

  if (isNaN(latitude) || isNaN(longitude)) {
    return null;
  }

  return { latitude, longitude };
};

/**
 * Reverse geocoding: convert coordinates to address string.
 * Supports multiple providers with automatic fallback.
 * 
 * @param latitude - Latitude coordinate
 * @param longitude - Longitude coordinate
 * @param retries - Number of retry attempts (default: 2)
 * @returns Promise resolving to address string or null if geocoding fails
 */
export const reverseGeocode = async (
  latitude: number,
  longitude: number,
  retries: number = 2,
): Promise<string | null> => {
  // Check cache first
  const cacheKey = getCacheKey(latitude, longitude);
  const cached = geocodeCache.get(cacheKey);
  if (cached && Date.now() - cached.timestamp < CACHE_DURATION) {
    return cached.address;
  }

  // Rate limiting for Nominatim
  const provider = getProvider();
  if (provider === 'nominatim') {
    const now = Date.now();
    const timeSinceLastRequest = now - lastRequestTime;
    if (timeSinceLastRequest < MIN_REQUEST_INTERVAL) {
      await delay(MIN_REQUEST_INTERVAL - timeSinceLastRequest);
    }
    lastRequestTime = Date.now();
  }

  // Try providers in order with fallback
  const providers: Array<'opencage' | 'positionstack' | 'nominatim'> = [];
  if (provider === 'auto') {
    providers.push('opencage', 'positionstack', 'nominatim');
  } else {
    providers.push(provider);
    // Add fallbacks
    if (provider !== 'opencage' && OPENCAGE_API_KEY) providers.push('opencage');
    if (provider !== 'positionstack' && POSITIONSTACK_API_KEY) providers.push('positionstack');
    if (provider !== 'nominatim') providers.push('nominatim');
  }

  for (const currentProvider of providers) {
    // Skip if API key is missing
    if (currentProvider === 'opencage' && !OPENCAGE_API_KEY) continue;
    if (currentProvider === 'positionstack' && !POSITIONSTACK_API_KEY) continue;

    for (let attempt = 0; attempt <= retries; attempt++) {
      try {
        if (attempt > 0) {
          const backoffDelay = Math.min(1000 * Math.pow(2, attempt), 5000);
          await delay(backoffDelay);
        }

        let result: string | null = null;
        if (currentProvider === 'opencage') {
          result = await reverseGeocodeOpenCage(latitude, longitude);
        } else if (currentProvider === 'positionstack') {
          result = await reverseGeocodePositionStack(latitude, longitude);
        } else {
          result = await reverseGeocodeNominatim(latitude, longitude);
        }

        if (result) {
          geocodeCache.set(cacheKey, { address: result, timestamp: Date.now() });
          return result;
        }
      } catch (error: any) {
        if (error.name === 'AbortError' || error.name === 'TimeoutError') {
          if (attempt < retries) continue;
        } else {
          console.warn(`Reverse geocoding error (${currentProvider}):`, error);
          if (attempt < retries) continue;
        }
      }
    }
  }

  // Cache null result to avoid repeated failed requests
  geocodeCache.set(cacheKey, { address: null, timestamp: Date.now() });
  return null;
};

/**
 * Forward geocoding: convert address string to coordinates.
 * Supports multiple providers with automatic fallback.
 * 
 * @param address - Address string to search for
 * @param retries - Number of retry attempts (default: 2)
 * @returns Promise resolving to coordinates { latitude, longitude } or null if geocoding fails
 */
export const forwardGeocode = async (
  address: string,
  retries: number = 2,
): Promise<{ latitude: number; longitude: number } | null> => {
  if (!address || address.trim().length === 0) {
    return null;
  }

  // Check cache first
  const cacheKey = `forward:${address.trim().toLowerCase()}`;
  const cached = geocodeCache.get(cacheKey);
  if (cached && Date.now() - cached.timestamp < CACHE_DURATION) {
    if (cached.address && cached.address.includes(',')) {
      const [lat, lon] = cached.address.split(',').map(Number);
      if (!isNaN(lat) && !isNaN(lon)) {
        return { latitude: lat, longitude: lon };
      }
    }
  }

  // Rate limiting for Nominatim
  const provider = getProvider();
  if (provider === 'nominatim') {
    const now = Date.now();
    const timeSinceLastRequest = now - lastRequestTime;
    if (timeSinceLastRequest < MIN_REQUEST_INTERVAL) {
      await delay(MIN_REQUEST_INTERVAL - timeSinceLastRequest);
    }
    lastRequestTime = Date.now();
  }

  // Try providers in order with fallback
  const providers: Array<'opencage' | 'positionstack' | 'nominatim'> = [];
  if (provider === 'auto') {
    providers.push('opencage', 'positionstack', 'nominatim');
  } else {
    providers.push(provider);
    // Add fallbacks
    if (provider !== 'opencage' && OPENCAGE_API_KEY) providers.push('opencage');
    if (provider !== 'positionstack' && POSITIONSTACK_API_KEY) providers.push('positionstack');
    if (provider !== 'nominatim') providers.push('nominatim');
  }

  for (const currentProvider of providers) {
    // Skip if API key is missing
    if (currentProvider === 'opencage' && !OPENCAGE_API_KEY) continue;
    if (currentProvider === 'positionstack' && !POSITIONSTACK_API_KEY) continue;

    for (let attempt = 0; attempt <= retries; attempt++) {
      try {
        if (attempt > 0) {
          const backoffDelay = Math.min(1000 * Math.pow(2, attempt), 5000);
          await delay(backoffDelay);
        }

        let result: { latitude: number; longitude: number } | null = null;
        if (currentProvider === 'opencage') {
          result = await forwardGeocodeOpenCage(address);
        } else if (currentProvider === 'positionstack') {
          result = await forwardGeocodePositionStack(address);
        } else {
          result = await forwardGeocodeNominatim(address);
        }

        if (result) {
          const cacheValue = `${result.latitude},${result.longitude}`;
          geocodeCache.set(cacheKey, { address: cacheValue, timestamp: Date.now() });
          return result;
        }
      } catch (error: any) {
        if (error.name === 'AbortError' || error.name === 'TimeoutError') {
          if (attempt < retries) continue;
        } else {
          console.warn(`Forward geocoding error (${currentProvider}):`, error);
          if (attempt < retries) continue;
        }
      }
    }
  }

  // Cache null result to avoid repeated failed requests
  geocodeCache.set(cacheKey, { address: null, timestamp: Date.now() });
  return null;
};
