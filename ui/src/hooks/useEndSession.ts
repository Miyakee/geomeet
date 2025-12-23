import { useState, useCallback } from 'react';
import { sessionApi, EndSessionResponse } from '../services/api';
import { getStatusErrorMessage } from '../utils/errorHandler';

export const useEndSession = (sessionId: string | undefined) => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const endSession = useCallback(async (): Promise<EndSessionResponse | null> => {
    if (!sessionId) {
      setError('Session ID is required');
      return null;
    }

    try {
      setLoading(true);
      setError(null);
      const result = await sessionApi.endSession(sessionId);
      return result;
    } catch (err: any) {
      console.error('Failed to end session:', err);
      setError(getStatusErrorMessage(err, {
        403: 'You do not have permission to end this session.',
        404: 'Session not found.',
        400: 'Cannot end session. Session may already be ended.',
      }, 'Failed to end session. Please try again.'));
      return null;
    } finally {
      setLoading(false);
    }
  }, [sessionId]);

  return {
    loading,
    error,
    endSession,
  };
};

