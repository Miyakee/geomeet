import { describe, it, expect } from 'vitest';
import { calculateHaversineDistance, formatDistance } from '../distanceCalculator';

describe('distanceCalculator', () => {
  describe('calculateHaversineDistance', () => {
    it('should calculate distance between two points correctly', () => {
      // Singapore to Kuala Lumpur (approximately 300km)
      const distance = calculateHaversineDistance(1.3521, 103.8198, 3.1390, 101.6869);
      // Actual distance is around 309km, so we check it's in a reasonable range
      expect(distance).toBeGreaterThan(300);
      expect(distance).toBeLessThan(320);
    });

    it('should calculate distance for same point as zero', () => {
      const distance = calculateHaversineDistance(1.3521, 103.8198, 1.3521, 103.8198);
      expect(distance).toBeCloseTo(0, 2);
    });

    it('should calculate distance for nearby points correctly', () => {
      // Two points in Singapore (approximately 5km apart)
      const distance = calculateHaversineDistance(1.2903, 103.8520, 1.2966, 103.7764);
      expect(distance).toBeGreaterThan(4);
      expect(distance).toBeLessThan(10); // More lenient tolerance
    });

    it('should handle negative coordinates (southern/western hemisphere)', () => {
      // Sydney to Melbourne (approximately 700km)
      const distance = calculateHaversineDistance(-33.8688, 151.2093, -37.8136, 144.9631);
      // Actual distance is around 713km, so we check it's in a reasonable range
      expect(distance).toBeGreaterThan(700);
      expect(distance).toBeLessThan(730);
    });

    it('should handle coordinates across the equator', () => {
      // Point in northern hemisphere to point in southern hemisphere
      const distance = calculateHaversineDistance(1.0, 103.0, -1.0, 103.0);
      expect(distance).toBeCloseTo(222, 0); // Approximately 222km for 2 degrees latitude
    });

    it('should handle coordinates across the prime meridian', () => {
      // Point in eastern hemisphere to point in western hemisphere
      const distance = calculateHaversineDistance(0.0, 1.0, 0.0, -1.0);
      expect(distance).toBeCloseTo(222, 0); // Approximately 222km for 2 degrees longitude at equator
    });
  });

  describe('formatDistance', () => {
    it('should format distance in kilometers when >= 1km', () => {
      expect(formatDistance(1.0)).toBe('1.00 km');
      expect(formatDistance(10.5)).toBe('10.50 km');
      expect(formatDistance(100.123)).toBe('100.12 km');
    });

    it('should format distance in meters when < 1km', () => {
      expect(formatDistance(0.5)).toBe('500 m');
      expect(formatDistance(0.1)).toBe('100 m');
      expect(formatDistance(0.001)).toBe('1 m');
    });

    it('should handle zero distance', () => {
      expect(formatDistance(0)).toBe('0 m');
    });

    it('should handle very small distances', () => {
      expect(formatDistance(0.0001)).toBe('0 m'); // Less than 1 meter rounds to 0
      expect(formatDistance(0.0005)).toBe('1 m'); // Rounds to 1 meter
    });

    it('should handle very large distances', () => {
      expect(formatDistance(10000)).toBe('10000.00 km');
    });
  });
});

