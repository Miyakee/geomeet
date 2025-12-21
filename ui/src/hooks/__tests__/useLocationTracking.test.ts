import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { renderHook, act, waitFor } from '@testing-library/react';
import { useLocationTracking } from '../useLocationTracking';
import { sessionApi } from '../../services/api';

vi.mock('../../services/api', async () => {
  const actual = await vi.importActual<typeof import('../../services/api')>('../../services/api');
  return {
    ...actual,
    sessionApi: {
      updateLocation: vi.fn(),
    },
  };
});

// Define GeolocationPositionError constants for testing
const PERMISSION_DENIED = 1;
const POSITION_UNAVAILABLE = 2;
const TIMEOUT = 3;

// Mock geolocation API
const mockGeolocation = {
  getCurrentPosition: vi.fn(),
  watchPosition: vi.fn(),
  clearWatch: vi.fn(),
};

Object.defineProperty(global.navigator, 'geolocation', {
  value: mockGeolocation,
  writable: true,
});

describe('useLocationTracking', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.useFakeTimers();
    // Mock isSecureContext for tests
    Object.defineProperty(window, 'isSecureContext', {
      value: true,
      writable: true,
    });
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('should initialize with disabled location tracking', () => {
    const { result } = renderHook(() => useLocationTracking('test-session-id'));

    expect(result.current.locationEnabled).toBe(false);
    expect(result.current.locationError).toBeNull();
    expect(result.current.currentLocation).toBeNull();
    expect(result.current.updatingLocation).toBe(false);
  });

  it('should set error when geolocation is not supported', () => {
    Object.defineProperty(global.navigator, 'geolocation', {
      value: undefined,
      writable: true,
    });

    const { result } = renderHook(() => useLocationTracking('test-session-id'));

    act(() => {
      result.current.handleLocationToggle({
        target: { checked: true },
      } as React.ChangeEvent<HTMLInputElement>);
    });

    expect(result.current.locationError).toBe('Geolocation is not supported by your browser. Please manually enter your location.');
  });

  it('should start location tracking successfully', async () => {
    vi.useRealTimers();
    
    // Restore geolocation for this test
    Object.defineProperty(global.navigator, 'geolocation', {
      value: mockGeolocation,
      writable: true,
    });

    const mockPosition: GeolocationPosition = {
      coords: {
        latitude: 37.7749,
        longitude: -122.4194,
        accuracy: 10,
        altitude: null,
        altitudeAccuracy: null,
        heading: null,
        speed: null,
      },
      timestamp: Date.now(),
    } as GeolocationPosition;

    vi.mocked(sessionApi.updateLocation).mockResolvedValue({
      participantId: 1,
      sessionId: 1,
      sessionIdString: 'test-session-id',
      userId: 1,
      latitude: 37.7749,
      longitude: -122.4194,
      accuracy: 10,
      updatedAt: new Date().toISOString(),
      message: 'Location updated successfully',
    });

    // Call success callback synchronously for testing
    mockGeolocation.getCurrentPosition.mockImplementation((success) => {
      if (success) {
        success(mockPosition);
      }
    });

    mockGeolocation.watchPosition.mockReturnValue(1);

    const { result } = renderHook(() => useLocationTracking('test-session-id'));

    await act(async () => {
      result.current.handleLocationToggle({
        target: { checked: true },
      } as React.ChangeEvent<HTMLInputElement>);
    });

    // Wait for async operations to complete
    await waitFor(() => {
      expect(result.current.locationEnabled).toBe(true);
    }, { timeout: 2000 });

    expect(sessionApi.updateLocation).toHaveBeenCalled();
  });

  it('should handle permission denied error', async () => {
    // Restore geolocation for this test
    Object.defineProperty(global.navigator, 'geolocation', {
      value: mockGeolocation,
      writable: true,
    });

    const mockError = {
      code: PERMISSION_DENIED,
      message: 'User denied geolocation',
      PERMISSION_DENIED,
      POSITION_UNAVAILABLE,
      TIMEOUT,
    } as GeolocationPositionError;

    // Call error callback synchronously for testing
    mockGeolocation.getCurrentPosition.mockImplementation((_success, error) => {
      if (error) {
        error(mockError);
      }
    });

    const { result } = renderHook(() => useLocationTracking('test-session-id'));

    await act(async () => {
      result.current.handleLocationToggle({
        target: { checked: true },
      } as React.ChangeEvent<HTMLInputElement>);
    });

    // Advance timers to process any pending async operations
    await act(async () => {
      vi.advanceTimersByTime(100);
    });

    expect(result.current.locationEnabled).toBe(false);
    expect(result.current.locationError).toContain('permission denied');
  });

  it('should stop location tracking', async () => {
    vi.useRealTimers();
    
    // Restore geolocation for this test
    Object.defineProperty(global.navigator, 'geolocation', {
      value: mockGeolocation,
      writable: true,
    });

    const mockPosition: GeolocationPosition = {
      coords: {
        latitude: 37.7749,
        longitude: -122.4194,
        accuracy: 10,
        altitude: null,
        altitudeAccuracy: null,
        heading: null,
        speed: null,
      },
      timestamp: Date.now(),
    } as GeolocationPosition;

    vi.mocked(sessionApi.updateLocation).mockResolvedValue({
      participantId: 1,
      sessionId: 1,
      sessionIdString: 'test-session-id',
      userId: 1,
      latitude: 37.7749,
      longitude: -122.4194,
      accuracy: 10,
      updatedAt: new Date().toISOString(),
      message: 'Location updated successfully',
    });

    const watchId = 123;
    // Call success callback synchronously for testing
    mockGeolocation.getCurrentPosition.mockImplementation((success) => {
      if (success) {
        success(mockPosition);
      }
    });
    mockGeolocation.watchPosition.mockReturnValue(watchId);

    const { result } = renderHook(() => useLocationTracking('test-session-id'));

    // First start tracking
    await act(async () => {
      result.current.handleLocationToggle({
        target: { checked: true },
      } as React.ChangeEvent<HTMLInputElement>);
    });

    // Wait for tracking to start
    await waitFor(() => {
      expect(result.current.locationEnabled).toBe(true);
    }, { timeout: 2000 });

    // Then stop tracking
    await act(async () => {
      result.current.handleLocationToggle({
        target: { checked: false },
      } as React.ChangeEvent<HTMLInputElement>);
    });

    expect(result.current.locationEnabled).toBe(false);
    expect(mockGeolocation.clearWatch).toHaveBeenCalledWith(watchId);
  });
});

