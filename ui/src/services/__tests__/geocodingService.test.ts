import { describe, it, expect, beforeEach, vi } from 'vitest';
import { reverseGeocode } from '../geocodingService';

// Mock fetch globally
const mockFetch = vi.fn();
global.fetch = mockFetch;

// Mock AbortSignal.timeout to avoid actual timeout
global.AbortSignal = {
  ...global.AbortSignal,
  timeout: vi.fn(() => ({ aborted: false } as AbortSignal)),
} as any;

describe('geocodingService', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    // Use real timers for these tests
    vi.useRealTimers();
    // Reset lastRequestTime by using different coordinates
  });

  it('should return cached result for same coordinates', async () => {
    const mockResponse = {
      ok: true,
      json: async () => ({
        address: {
          road: 'Test Street',
          city: 'Test City',
          country: 'Test Country',
        },
      }),
    };

    mockFetch.mockResolvedValueOnce(mockResponse as any);

    // First call
    const result1 = await reverseGeocode(1.3521, 103.8198);
    expect(result1).toBeTruthy();
    expect(mockFetch).toHaveBeenCalledTimes(1);

    // Second call should use cache (same coordinates)
    const result2 = await reverseGeocode(1.3521, 103.8198);
    expect(result2).toBe(result1);
    expect(mockFetch).toHaveBeenCalledTimes(1); // Still only called once
  });

  it('should return address string from Nominatim API', async () => {
    const mockResponse = {
      ok: true,
      json: async () => ({
        address: {
          road: 'Orchard Road',
          house_number: '123',
          suburb: 'Orchard',
          city: 'Singapore',
          country: 'Singapore',
        },
      }),
    };

    mockFetch.mockResolvedValueOnce(mockResponse as any);

    // Use unique coordinates to avoid cache (cache uses 4 decimal places)
    // Wait a bit to avoid rate limiting
    await new Promise(resolve => setTimeout(resolve, 1100));
    const result = await reverseGeocode(1.3522, 103.8199);
    
    expect(result).toBe('Orchard Road, 123, Orchard, Singapore, Singapore');
    expect(mockFetch).toHaveBeenCalledWith(
      expect.stringContaining('nominatim.openstreetmap.org/reverse'),
      expect.objectContaining({
        headers: expect.objectContaining({
          'User-Agent': 'GeoMeet/1.0',
        }),
      }),
    );
  }, 10000);

  it('should return null when address is not available', async () => {
    const mockResponse = {
      ok: true,
      json: async () => ({
        address: null,
      }),
    };

    mockFetch.mockResolvedValueOnce(mockResponse as any);

    // Use different coordinates to avoid cache
    // Wait a bit to avoid rate limiting
    await new Promise(resolve => setTimeout(resolve, 1100));
    const result = await reverseGeocode(1.3523, 103.8200);
    expect(result).toBeNull();
  }, 10000);

  it('should handle API errors gracefully', async () => {
    const mockResponse = {
      ok: false,
      status: 500,
      statusText: 'Internal Server Error',
    };

    mockFetch.mockResolvedValueOnce(mockResponse as any);

    // Use unique coordinates to avoid cache
    // Wait a bit to avoid rate limiting
    await new Promise(resolve => setTimeout(resolve, 1100));
    const result = await reverseGeocode(1.3524, 103.8201);
    expect(result).toBeNull();
  }, 10000);

  it('should handle rate limiting (429 status)', async () => {
    // First call returns 429, retries also return 429, eventually returns null
    const rateLimitResponse = {
      ok: false,
      status: 429,
      statusText: 'Too Many Requests',
    };

    // Mock all retry attempts to return 429
    mockFetch.mockResolvedValue(rateLimitResponse as any);

    // Use unique coordinates to avoid cache
    // Wait a bit to avoid rate limiting from previous tests
    await new Promise(resolve => setTimeout(resolve, 1100));
    const result = await reverseGeocode(1.3525, 103.8202);
    // Should eventually return null after retries fail
    expect(result).toBeNull();
    expect(mockFetch).toHaveBeenCalled();
  }, 20000);


  it('should build address string from available components', async () => {
    const mockResponse = {
      ok: true,
      json: async () => ({
        address: {
          road: 'Main Street',
          city: 'City',
          country: 'Country',
        },
      }),
    };

    mockFetch.mockResolvedValueOnce(mockResponse as any);

    // Use unique coordinates to avoid cache (cache uses 4 decimal places)
    // Wait a bit to avoid rate limiting
    await new Promise(resolve => setTimeout(resolve, 1100));
    const result = await reverseGeocode(1.3528, 103.8205);
    expect(result).toBe('Main Street, City, Country');
  }, 10000);

  it('should handle missing address components gracefully', async () => {
    const mockResponse = {
      ok: true,
      json: async () => ({
        address: {
          country: 'Country',
        },
      }),
    };

    mockFetch.mockResolvedValueOnce(mockResponse as any);

    // Use unique coordinates to avoid cache
    // Wait a bit to avoid rate limiting
    await new Promise(resolve => setTimeout(resolve, 1100));
    const result = await reverseGeocode(1.3529, 103.8206);
    expect(result).toBe('Country');
  }, 10000);

  it('should respect rate limiting between requests', async () => {
    const mockResponse = {
      ok: true,
      json: async () => ({
        address: {
          road: 'Test Road',
        },
      }),
    };

    mockFetch.mockResolvedValue(mockResponse as any);

    // First request - use unique coordinates
    const result1 = await reverseGeocode(1.3530, 103.8207);
    expect(result1).toBeTruthy();

    // Wait for rate limit (1 second)
    await new Promise(resolve => setTimeout(resolve, 1100));

    // Second request should wait for rate limit - use different unique coordinates
    const result2 = await reverseGeocode(1.3532, 103.8209);
    expect(result2).toBeTruthy();

    // Both requests should have been made
    expect(mockFetch).toHaveBeenCalled();
  }, 15000);
});
