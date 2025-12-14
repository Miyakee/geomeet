/**
 * Distance calculation utilities.
 * Uses Haversine formula to calculate great-circle distance between two points on Earth.
 */

/**
 * Calculates the Haversine distance between two coordinates in kilometers.
 * This is more accurate for larger distances.
 *
 * @param lat1 - Latitude of first point
 * @param lon1 - Longitude of first point
 * @param lat2 - Latitude of second point
 * @param lon2 - Longitude of second point
 * @returns distance in kilometers
 */
export const calculateHaversineDistance = (
  lat1: number,
  lon1: number,
  lat2: number,
  lon2: number
): number => {
  const EARTH_RADIUS_KM = 6371.0;

  const toRadians = (degrees: number): number => {
    return degrees * (Math.PI / 180);
  };

  const dLat = toRadians(lat2 - lat1);
  const dLon = toRadians(lon2 - lon1);

  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(toRadians(lat1)) *
      Math.cos(toRadians(lat2)) *
      Math.sin(dLon / 2) *
      Math.sin(dLon / 2);

  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

  return EARTH_RADIUS_KM * c;
};

/**
 * Formats distance for display.
 * Shows in kilometers if >= 1km, otherwise in meters.
 *
 * @param distanceKm - Distance in kilometers
 * @returns formatted distance string
 */
export const formatDistance = (distanceKm: number): string => {
  if (distanceKm >= 1) {
    return `${distanceKm.toFixed(2)} km`;
  } else {
    return `${Math.round(distanceKm * 1000)} m`;
  }
};

