import { useState, useCallback, useEffect } from 'react';
import { sessionApi, UpdateMeetingLocationResponse } from '../services/api';
import { reverseGeocode } from '../services/geocodingService';
import { getStatusErrorMessage } from '../utils/errorHandler';

export const useMeetingLocation = (sessionId: string | undefined) => {
  const [meetingLocation, setMeetingLocation] = useState<{
    latitude: number;
    longitude: number;
  } | null>(null);
  const [meetingLocationAddress, setMeetingLocationAddress] = useState<string | null>(null);
  const [loadingAddress, setLoadingAddress] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const updateMeetingLocation = useCallback(async (latitude: number, longitude: number): Promise<void> => {
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
    } catch (err: any) {
      console.error('Failed to update meeting location:', err);
      setError(getStatusErrorMessage(err, {
        403: 'You do not have permission to update meeting location.',
        404: 'Session not found.',
        400: 'Invalid location data.',
      }, 'Failed to update meeting location. Please try again.'));
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

  // Fetch address when meeting location changes
  useEffect(() => {
    if (meetingLocation) {
      setLoadingAddress(true);
      reverseGeocode(meetingLocation.latitude, meetingLocation.longitude)
        .then((address) => {
          setMeetingLocationAddress(address);
        })
        .catch((err) => {
          console.error('Failed to geocode meeting location:', err);
          setMeetingLocationAddress(null);
        })
        .finally(() => {
          setLoadingAddress(false);
        });
    } else {
      setMeetingLocationAddress(null);
    }
  }, [meetingLocation]);

  return {
    meetingLocation,
    meetingLocationAddress,
    loadingAddress,
    loading,
    error,
    updateMeetingLocation,
    updateMeetingLocationFromResponse,
  };
};

