import { useState, useCallback } from 'react';
import { sessionApi, UpdateMeetingLocationResponse } from '../services/api';

export const useMeetingLocation = (sessionId: string | undefined) => {
  const [meetingLocation, setMeetingLocation] = useState<{
    latitude: number;
    longitude: number;
  } | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const updateMeetingLocation = useCallback(async (latitude: number, longitude: number) => {
    if (!sessionId) {
      setError('Session ID is required');
      return;
    }

    try {
      setLoading(true);
      setError(null);
      const result = await sessionApi.updateMeetingLocation(sessionId, {
        latitude,
        longitude,
      });
      setMeetingLocation({
        latitude: result.latitude,
        longitude: result.longitude,
      });
      return result;
    } catch (err: any) {
      console.error('Failed to update meeting location:', err);
      if (err.response?.status === 403) {
        setError('You do not have permission to update meeting location.');
      } else if (err.response?.status === 404) {
        setError('Session not found.');
      } else if (err.response?.status === 400) {
        setError(err.response?.data?.message || 'Invalid location data.');
      } else {
        setError('Failed to update meeting location. Please try again.');
      }
      throw err;
    } finally {
      setLoading(false);
    }
  }, [sessionId]);

  const updateMeetingLocationFromResponse = useCallback((response: UpdateMeetingLocationResponse) => {
    setMeetingLocation({
      latitude: response.latitude,
      longitude: response.longitude,
    });
  }, []);

  return {
    meetingLocation,
    loading,
    error,
    updateMeetingLocation,
    updateMeetingLocationFromResponse,
  };
};

