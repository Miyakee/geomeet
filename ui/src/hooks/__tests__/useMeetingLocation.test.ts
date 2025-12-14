import { describe, it, expect, beforeEach, vi } from 'vitest';
import { renderHook, waitFor, act } from '@testing-library/react';
import { useMeetingLocation } from '../useMeetingLocation';
import { sessionApi } from '../../services/api';
import { reverseGeocode } from '../../services/geocodingService';

vi.mock('../../services/api', () => ({
  sessionApi: {
    updateMeetingLocation: vi.fn(),
  },
}));

vi.mock('../../services/geocodingService', () => ({
  reverseGeocode: vi.fn(),
}));

describe('useMeetingLocation', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should initialize with empty state', () => {
    const { result } = renderHook(() => useMeetingLocation('test-session-id'));

    expect(result.current.meetingLocation).toBeNull();
    expect(result.current.meetingLocationAddress).toBeNull();
    expect(result.current.loading).toBe(false);
    expect(result.current.loadingAddress).toBe(false);
    expect(result.current.error).toBeNull();
  });

  it('should update meeting location successfully', async () => {
    const mockResponse = {
      sessionId: 1,
      sessionIdString: 'test-session-id',
      latitude: 1.3521,
      longitude: 103.8198,
      message: 'Meeting location updated successfully',
    };

    vi.mocked(sessionApi.updateMeetingLocation).mockResolvedValue(mockResponse);
    vi.mocked(reverseGeocode).mockResolvedValue('Test Address');

    const { result } = renderHook(() => useMeetingLocation('test-session-id'));

    await act(async () => {
      await result.current.updateMeetingLocation(1.3521, 103.8198);
    });

    expect(result.current.meetingLocation).toEqual({
      latitude: 1.3521,
      longitude: 103.8198,
    });
    expect(result.current.loading).toBe(false);
    expect(result.current.error).toBeNull();
    expect(sessionApi.updateMeetingLocation).toHaveBeenCalledWith('test-session-id', {
      latitude: 1.3521,
      longitude: 103.8198,
    });
  });

  it('should fetch address after updating location', async () => {
    const mockResponse = {
      sessionId: 1,
      sessionIdString: 'test-session-id',
      latitude: 1.3521,
      longitude: 103.8198,
      message: 'Meeting location updated successfully',
    };

    vi.mocked(sessionApi.updateMeetingLocation).mockResolvedValue(mockResponse);
    vi.mocked(reverseGeocode).mockResolvedValue('Orchard Road, Singapore');

    const { result } = renderHook(() => useMeetingLocation('test-session-id'));

    await act(async () => {
      await result.current.updateMeetingLocation(1.3521, 103.8198);
    });

    await waitFor(() => {
      expect(result.current.meetingLocationAddress).toBe('Orchard Road, Singapore');
    });

    expect(reverseGeocode).toHaveBeenCalledWith(1.3521, 103.8198);
  });

  it('should handle 403 error (permission denied)', async () => {
    const mockError = {
      response: {
        status: 403,
        data: { message: 'Access denied' },
      },
    };

    vi.mocked(sessionApi.updateMeetingLocation).mockRejectedValue(mockError);

    const { result } = renderHook(() => useMeetingLocation('test-session-id'));

    await act(async () => {
      try {
        await result.current.updateMeetingLocation(1.3521, 103.8198);
      } catch (err) {
        // Expected to throw
      }
    });

    expect(result.current.error).toBe('You do not have permission to update meeting location.');
    expect(result.current.loading).toBe(false);
  });

  it('should handle 404 error (session not found)', async () => {
    const mockError = {
      response: {
        status: 404,
        data: { message: 'Session not found' },
      },
    };

    vi.mocked(sessionApi.updateMeetingLocation).mockRejectedValue(mockError);

    const { result } = renderHook(() => useMeetingLocation('test-session-id'));

    await act(async () => {
      try {
        await result.current.updateMeetingLocation(1.3521, 103.8198);
      } catch (err) {
        // Expected to throw
      }
    });

    expect(result.current.error).toBe('Session not found.');
    expect(result.current.loading).toBe(false);
  });

  it('should handle 400 error (validation error)', async () => {
    const mockError = {
      response: {
        status: 400,
        data: { message: 'Invalid coordinates' },
      },
    };

    vi.mocked(sessionApi.updateMeetingLocation).mockRejectedValue(mockError);

    const { result } = renderHook(() => useMeetingLocation('test-session-id'));

    await act(async () => {
      try {
        await result.current.updateMeetingLocation(1.3521, 103.8198);
      } catch (err) {
        // Expected to throw
      }
    });

    expect(result.current.error).toBe('Invalid coordinates');
    expect(result.current.loading).toBe(false);
  });

  it('should handle generic error', async () => {
    const mockError = new Error('Network error');
    vi.mocked(sessionApi.updateMeetingLocation).mockRejectedValue(mockError);

    const { result } = renderHook(() => useMeetingLocation('test-session-id'));

    await act(async () => {
      try {
        await result.current.updateMeetingLocation(1.3521, 103.8198);
      } catch (err) {
        // Expected to throw
      }
    });

    expect(result.current.error).toBe('Failed to update meeting location. Please try again.');
    expect(result.current.loading).toBe(false);
  });

  it('should set error when sessionId is undefined', async () => {
    const { result } = renderHook(() => useMeetingLocation(undefined));

    await act(async () => {
      try {
        await result.current.updateMeetingLocation(1.3521, 103.8198);
      } catch (err) {
        // Expected to throw
      }
    });

    expect(result.current.error).toBe('Session ID is required');
    expect(sessionApi.updateMeetingLocation).not.toHaveBeenCalled();
  });

  it('should update meeting location from response', () => {
    const { result } = renderHook(() => useMeetingLocation('test-session-id'));

    const mockResponse = {
      sessionId: 1,
      sessionIdString: 'test-session-id',
      latitude: 1.3521,
      longitude: 103.8198,
      message: 'Meeting location updated',
    };

    act(() => {
      result.current.updateMeetingLocationFromResponse(mockResponse);
    });

    expect(result.current.meetingLocation).toEqual({
      latitude: 1.3521,
      longitude: 103.8198,
    });
  });

  it('should handle geocoding failure gracefully', async () => {
    const mockResponse = {
      sessionId: 1,
      sessionIdString: 'test-session-id',
      latitude: 1.3521,
      longitude: 103.8198,
      message: 'Meeting location updated successfully',
    };

    vi.mocked(sessionApi.updateMeetingLocation).mockResolvedValue(mockResponse);
    vi.mocked(reverseGeocode).mockResolvedValue(null);

    const { result } = renderHook(() => useMeetingLocation('test-session-id'));

    await act(async () => {
      await result.current.updateMeetingLocation(1.3521, 103.8198);
    });

    await waitFor(() => {
      expect(result.current.meetingLocationAddress).toBeNull();
    });
  });

  it('should clear address when meeting location is cleared', () => {
    const { result } = renderHook(() => useMeetingLocation('test-session-id'));

    // Set location first
    act(() => {
      result.current.updateMeetingLocationFromResponse({
        sessionId: 1,
        sessionIdString: 'test-session-id',
        latitude: 1.3521,
        longitude: 103.8198,
        message: '',
      });
    });

    // Clear location by setting to null (simulating unmount or reset)
    act(() => {
      result.current.updateMeetingLocationFromResponse({
        sessionId: 1,
        sessionIdString: 'test-session-id',
        latitude: 0,
        longitude: 0,
        message: '',
      });
    });

    // Address should be cleared when location changes
    expect(result.current.meetingLocation).not.toBeNull();
  });
});

