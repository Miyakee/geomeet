import { describe, it, expect, beforeEach, vi } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { useOptimalLocation } from '../useOptimalLocation';
import { sessionApi } from '../../services/api';

vi.mock('../../services/api', () => ({
  sessionApi: {
    calculateOptimalLocation: vi.fn(),
  },
}));

describe('useOptimalLocation', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should initialize with empty state', () => {
    const { result } = renderHook(() => useOptimalLocation('test-session-id'));

    expect(result.current.optimalLocation).toBeNull();
    expect(result.current.loading).toBe(false);
    expect(result.current.error).toBeNull();
  });

  it('should calculate optimal location successfully', async () => {
    const mockOptimalLocation = {
      sessionId: 1,
      sessionIdString: 'test-session-id',
      optimalLatitude: 1.3521,
      optimalLongitude: 103.8198,
      totalTravelDistance: 10.5,
      participantCount: 3,
      message: 'Optimal location calculated successfully',
    };

    vi.mocked(sessionApi.calculateOptimalLocation).mockResolvedValue(mockOptimalLocation);

    const { result } = renderHook(() => useOptimalLocation('test-session-id'));

    await act(async () => {
      await result.current.calculateOptimalLocation();
    });

    expect(result.current.optimalLocation).toEqual(mockOptimalLocation);
    expect(result.current.loading).toBe(false);
    expect(result.current.error).toBeNull();
    expect(sessionApi.calculateOptimalLocation).toHaveBeenCalledWith('test-session-id');
  });

  it('should handle 403 error (permission denied)', async () => {
    const mockError = {
      response: {
        status: 403,
        data: { message: 'Access denied' },
      },
    };

    vi.mocked(sessionApi.calculateOptimalLocation).mockRejectedValue(mockError);

    const { result } = renderHook(() => useOptimalLocation('test-session-id'));

    await act(async () => {
      await result.current.calculateOptimalLocation();
    });

    expect(result.current.optimalLocation).toBeNull();
    expect(result.current.loading).toBe(false);
    expect(result.current.error).toBe('You do not have permission to calculate optimal location.');
  });

  it('should handle 404 error (session not found)', async () => {
    const mockError = {
      response: {
        status: 404,
        data: { message: 'Session not found' },
      },
    };

    vi.mocked(sessionApi.calculateOptimalLocation).mockRejectedValue(mockError);

    const { result } = renderHook(() => useOptimalLocation('test-session-id'));

    await act(async () => {
      await result.current.calculateOptimalLocation();
    });

    expect(result.current.optimalLocation).toBeNull();
    expect(result.current.loading).toBe(false);
    expect(result.current.error).toBe('Session not found.');
  });

  it('should handle 400 error (validation error)', async () => {
    const mockError = {
      response: {
        status: 400,
        data: { message: 'No participant locations available' },
      },
    };

    vi.mocked(sessionApi.calculateOptimalLocation).mockRejectedValue(mockError);

    const { result } = renderHook(() => useOptimalLocation('test-session-id'));

    await act(async () => {
      await result.current.calculateOptimalLocation();
    });

    expect(result.current.optimalLocation).toBeNull();
    expect(result.current.loading).toBe(false);
    expect(result.current.error).toBe('No participant locations available');
  });

  it('should handle generic error', async () => {
    const mockError = new Error('Network error');
    vi.mocked(sessionApi.calculateOptimalLocation).mockRejectedValue(mockError);

    const { result } = renderHook(() => useOptimalLocation('test-session-id'));

    await act(async () => {
      await result.current.calculateOptimalLocation();
    });

    expect(result.current.optimalLocation).toBeNull();
    expect(result.current.loading).toBe(false);
    expect(result.current.error).toBe('Failed to calculate optimal location. Please try again.');
  });

  it('should set error when sessionId is undefined', async () => {
    const { result } = renderHook(() => useOptimalLocation(undefined));

    await act(async () => {
      await result.current.calculateOptimalLocation();
    });

    expect(result.current.error).toBe('Session ID is required');
    expect(sessionApi.calculateOptimalLocation).not.toHaveBeenCalled();
  });

  it('should update optimal location', () => {
    const { result } = renderHook(() => useOptimalLocation('test-session-id'));

    const mockOptimalLocation = {
      sessionId: 1,
      sessionIdString: 'test-session-id',
      optimalLatitude: 1.3521,
      optimalLongitude: 103.8198,
      totalTravelDistance: 10.5,
      participantCount: 3,
      message: 'Optimal location calculated successfully',
    };

    act(() => {
      result.current.updateOptimalLocation(mockOptimalLocation);
    });

    expect(result.current.optimalLocation).toEqual(mockOptimalLocation);
  });
});

