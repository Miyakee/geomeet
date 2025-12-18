import { useState, useCallback } from 'react';
import { sessionApi, CalculateOptimalLocationResponse, ApiError } from '../services/api';

export const useOptimalLocation = (sessionId: string | undefined) => {
  const [optimalLocation, setOptimalLocation] = useState<CalculateOptimalLocationResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const calculateOptimalLocation = useCallback(async () => {
    if (!sessionId) {
      setError('Session ID is required');
      return;
    }

    try {
      setLoading(true);
      setError(null);
      const result = await sessionApi.calculateOptimalLocation(sessionId);
      setOptimalLocation(result);
    } catch (err: any) {
      console.error('Failed to calculate optimal location:', err);
      if (err instanceof ApiError || err.response) {
        const status = err.status || err.response?.status;
        const data = err.response?.data || err.response;
        if (status === 403) {
          setError('You do not have permission to calculate optimal location.');
        } else if (status === 404) {
          setError('Session not found.');
        } else if (status === 400) {
          setError(data?.message || 'Cannot calculate optimal location. At least one participant must share their location.');
        } else {
          setError('Failed to calculate optimal location. Please try again.');
        }
      } else {
        setError('Failed to calculate optimal location. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  }, [sessionId]);

  const updateOptimalLocation = useCallback((location: CalculateOptimalLocationResponse) => {
    setOptimalLocation(location);
  }, []);

  return {
    optimalLocation,
    loading,
    error,
    calculateOptimalLocation,
    updateOptimalLocation,
  };
};

