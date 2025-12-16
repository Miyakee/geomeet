import { useState, useCallback } from 'react';
import { sessionApi, EndSessionResponse } from '../services/api';

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
      if (err.response?.status === 403) {
        setError('You do not have permission to end this session.');
      } else if (err.response?.status === 404) {
        setError('Session not found.');
      } else if (err.response?.status === 400) {
        setError(err.response?.data?.message || 'Cannot end session. Session may already be ended.');
      } else {
        setError('Failed to end session. Please try again.');
      }
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

