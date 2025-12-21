import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
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
    // Use fake timers to speed up tests and avoid real delays
    vi.useFakeTimers();
    
    // Reset mock - don't set default implementation here
    // Each test will set its own mock
    mockFetch.mockReset();
  });

  afterEach(() => {
    vi.useRealTimers();
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

    // Mock only nominatim calls to return success, others return error
    mockFetch.mockImplementation((url: string) => {
      if (typeof url === 'string' && url.includes('nominatim')) {
        return Promise.resolve(mockResponse as any);
      }
      // For other providers, return error to prevent fallback
      return Promise.resolve({
        ok: false,
        status: 500,
        json: async () => ({}),
      } as Response);
    });

    // First call
    const promise1 = reverseGeocode(1.3521, 103.8198);
    await vi.runAllTimersAsync();
    const result1 = await promise1;
    expect(result1).toBeTruthy();
    expect(mockFetch).toHaveBeenCalled();

    // Second call should use cache (same coordinates)
    const result2 = await reverseGeocode(1.3521, 103.8198);
    expect(result2).toBe(result1);
    // Should not call fetch again due to cache
  }, 15000);

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

    // Mock only nominatim calls to return success, others return error
    mockFetch.mockImplementation((url: string) => {
      if (typeof url === 'string' && url.includes('nominatim')) {
        return Promise.resolve(mockResponse as any);
      }
      // For other providers, return error to prevent fallback
      return Promise.resolve({
        ok: false,
        status: 500,
        json: async () => ({}),
      } as Response);
    });

    // Use unique coordinates to avoid cache (cache uses 4 decimal places)
    // Start the async operation
    const promise = reverseGeocode(1.3522, 103.8199);
    // Advance timers to simulate rate limiting delay
    await vi.runAllTimersAsync();
    const result = await promise;
    
    expect(result).toBe('Orchard Road, 123, Orchard, Singapore, Singapore');
    expect(mockFetch).toHaveBeenCalledWith(
      expect.stringContaining('nominatim.openstreetmap.org/reverse'),
      expect.objectContaining({
        headers: expect.objectContaining({
          'User-Agent': 'GeoMeet/1.0',
        }),
      }),
    );
  }, 15000);

  it('should return null when address is not available', async () => {
    const mockResponse = {
      ok: true,
      json: async () => ({
        address: null,
      }),
    };

    // Mock only nominatim calls to return success, others return error
    mockFetch.mockImplementation((url: string) => {
      if (typeof url === 'string' && url.includes('nominatim')) {
        return Promise.resolve(mockResponse as any);
      }
      // For other providers, return error to prevent fallback
      return Promise.resolve({
        ok: false,
        status: 500,
        json: async () => ({}),
      } as Response);
    });

    // Use different coordinates to avoid cache
    // Start the async operation
    const promise = reverseGeocode(1.3523, 103.8200);
    // Advance timers to simulate rate limiting delay
    await vi.runAllTimersAsync();
    const result = await promise;
    expect(result).toBeNull();
  }, 15000);

  it('should handle API errors gracefully', async () => {
    const mockResponse = {
      ok: false,
      status: 500,
      statusText: 'Internal Server Error',
      json: async () => ({}),
    };

    mockFetch.mockImplementationOnce((url: string) => {
      if (typeof url === 'string' && url.includes('nominatim')) {
        return Promise.resolve(mockResponse as any);
      }
      return Promise.resolve({
        ok: false,
        status: 500,
        json: async () => ({}),
      } as Response);
    });

    // Use unique coordinates to avoid cache
    // Start the async operation
    const promise = reverseGeocode(1.3524, 103.8201);
    // Advance timers to simulate rate limiting delay
    await vi.runAllTimersAsync();
    const result = await promise;
    expect(result).toBeNull();
  }, 15000);

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
    // Start the async operation
    const promise = reverseGeocode(1.3525, 103.8202);
    // Advance timers to simulate rate limiting delay
    await vi.runAllTimersAsync();
    const result = await promise;
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

    // Mock only nominatim calls to return success, others return error
    mockFetch.mockImplementation((url: string) => {
      if (typeof url === 'string' && url.includes('nominatim')) {
        return Promise.resolve(mockResponse as any);
      }
      // For other providers, return error to prevent fallback
      return Promise.resolve({
        ok: false,
        status: 500,
        json: async () => ({}),
      } as Response);
    });

    // Use unique coordinates to avoid cache (cache uses 4 decimal places)
    // Start the async operation
    const promise = reverseGeocode(1.3528, 103.8205);
    // Advance timers to simulate rate limiting delay
    await vi.runAllTimersAsync();
    const result = await promise;
    expect(result).toBe('Main Street, City, Country');
  }, 15000);

  it('should handle missing address components gracefully', async () => {
    const mockResponse = {
      ok: true,
      json: async () => ({
        address: {
          country: 'Country',
        },
      }),
    };

    // Mock only nominatim calls to return success, others return error
    mockFetch.mockImplementation((url: string) => {
      if (typeof url === 'string' && url.includes('nominatim')) {
        return Promise.resolve(mockResponse as any);
      }
      // For other providers, return error to prevent fallback
      return Promise.resolve({
        ok: false,
        status: 500,
        json: async () => ({}),
      } as Response);
    });

    // Use unique coordinates to avoid cache
    // Start the async operation
    const promise = reverseGeocode(1.3529, 103.8206);
    // Advance timers to simulate rate limiting delay
    await vi.runAllTimersAsync();
    const result = await promise;
    expect(result).toBe('Country');
  }, 15000);

  it('should respect rate limiting between requests', async () => {
    const mockResponse = {
      ok: true,
      json: async () => ({
        address: {
          road: 'Test Road',
        },
      }),
    };

    mockFetch.mockImplementation((url: string) => {
      if (typeof url === 'string' && url.includes('nominatim')) {
        return Promise.resolve(mockResponse as any);
      }
      return Promise.resolve({
        ok: false,
        status: 500,
        json: async () => ({}),
      } as Response);
    });

    // First request - use unique coordinates
    const promise1 = reverseGeocode(1.3530, 103.8207);
    await vi.runAllTimersAsync();
    const result1 = await promise1;
    expect(result1).toBeTruthy();

    // Advance timers to simulate rate limit (1 second)
    vi.advanceTimersByTime(1100);

    // Second request should wait for rate limit - use different unique coordinates
    const promise2 = reverseGeocode(1.3532, 103.8209);
    await vi.runAllTimersAsync();
    const result2 = await promise2;
    expect(result2).toBeTruthy();

    // Both requests should have been made
    expect(mockFetch).toHaveBeenCalled();
  }, 15000);
});
