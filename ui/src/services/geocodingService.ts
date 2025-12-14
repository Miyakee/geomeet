/**
 * Geocoding service for converting coordinates to addresses.
 * This service handles third-party API calls (OpenStreetMap Nominatim).
 * Separated from business logic for better maintainability and testability.
 */

/**
 * Reverse geocoding: convert coordinates to address string.
 * Uses OpenStreetMap Nominatim API (free, no API key required).
 * 
 * @param latitude - Latitude coordinate
 * @param longitude - Longitude coordinate
 * @returns Promise resolving to address string or null if geocoding fails
 */
export const reverseGeocode = async (
  latitude: number,
  longitude: number
): Promise<string | null> => {
  try {
    // Use OpenStreetMap Nominatim API (free, no API key required)
    const response = await fetch(
      `https://nominatim.openstreetmap.org/reverse?format=json&lat=${latitude}&lon=${longitude}&zoom=18&addressdetails=1`,
      {
        headers: {
          'User-Agent': 'GeoMeet/1.0', // Required by Nominatim
        },
      }
    );

    if (!response.ok) {
      console.warn('Reverse geocoding failed:', response.statusText);
      return null;
    }

    const data = await response.json();

    // Extract address components
    const address = data.address;
    if (!address) {
      return null;
    }

    // Build address string: prefer road, then suburb, then city
    const parts: string[] = [];
    if (address.road) parts.push(address.road);
    if (address.house_number) parts.push(address.house_number);
    if (address.suburb) parts.push(address.suburb);
    if (address.city) parts.push(address.city);
    if (address.country) parts.push(address.country);

    return parts.length > 0 ? parts.join(', ') : null;
  } catch (error) {
    console.error('Reverse geocoding error:', error);
    return null;
  }
};

