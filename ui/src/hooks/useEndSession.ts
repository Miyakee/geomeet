import { useState, useCallback } from 'react';
import { sessionApi, EndSessionResponse, ApiError } from '../services/api';

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
      if (err instanceof ApiError || err.response) {
        const status = err.status || err.response?.status;
        const data = err.response?.data || err.response;
        if (status === 403) {
          setError('You do not have permission to end this session.');
        } else if (status === 404) {
          setError('Session not found.');
        } else if (status === 400) {
          setError(data?.message || 'Cannot end session. Session may already be ended.');
        } else {
          setError('Failed to end session. Please try again.');
        }
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

