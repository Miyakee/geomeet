import { useState, useCallback } from 'react';
import { sessionApi, CalculateOptimalLocationResponse } from '../services/api';
import { getStatusErrorMessage } from '../utils/errorHandler';

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
      setError(getStatusErrorMessage(err, {
        403: 'You do not have permission to calculate optimal location.',
        404: 'Session not found.',
        400: 'Cannot calculate optimal location. At least one participant must share their location.',
      }, 'Failed to calculate optimal location. Please try again.'));
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

