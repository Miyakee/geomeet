import { useState, useEffect, useCallback } from 'react';
import { sessionApi, ApiError } from '../services/api';
import { SessionDetailResponse } from '../types/session';

export const useSessionData = (sessionId: string | undefined) => {
  const [session, setSession] = useState<SessionDetailResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadSessionData = useCallback(async () => {
    if (!sessionId) {
      return;
    }

    try {
      setLoading(true);
      const data = await sessionApi.getSessionDetails(sessionId);
      setSession(data);
      setError(null);
    } catch (err: any) {
      console.error('Failed to load session:', err);
      if (err instanceof ApiError || err.response) {
        if(err.message){
          setError(err.message);
        }
      } else {
        setError('Failed to load session. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  }, [sessionId]);

  useEffect(() => {
    if (!sessionId) {
      setError('Session ID is required');
      setLoading(false);
      return;
    }
    loadSessionData();
  }, [sessionId, loadSessionData]);

  return {
    session,
    loading,
    error,
    reload: loadSessionData,
    updateSession: setSession,
  };
};

