import { useState, useEffect, useRef } from 'react';
import {
  Container,
  Paper,
  Typography,
  Button,
  Box,
  TextField,
  Alert,
  CircularProgress,
} from '@mui/material';
import { Login, GroupAdd } from '@mui/icons-material';
import { useAuth } from '../contexts/AuthContext';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { sessionApi } from '../services/api';
import { SessionDetailResponse } from '../types/session';

const JoinSessionPage = () => {
  const { isAuthenticated, isInitialized } = useAuth();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [sessionId, setSessionId] = useState(searchParams.get('sessionId') || '');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);
  const [checkingSession, setCheckingSession] = useState(false);
  const [sessionDetails, setSessionDetails] = useState<SessionDetailResponse | null>(null);
  const hasAttemptedJoin = useRef(false);

  // Redirect to login if not authenticated, preserving sessionId
  // Only redirect after auth state is initialized to avoid false redirects
  useEffect(() => {
    if (isInitialized && !isAuthenticated) {
      const redirectPath = sessionId ? `/login?redirect=/join&sessionId=${sessionId}` : '/login?redirect=/join';
      navigate(redirectPath);
    }
  }, [isAuthenticated, isInitialized, navigate, sessionId]);

  // Check session status when sessionId changes (with debounce)
  useEffect(() => {
    if (!sessionId.trim() || !isAuthenticated) {
      setSessionDetails(null);
      setError(null);
      return;
    }

    const checkSessionStatus = async () => {
      setCheckingSession(true);
      try {
        const details = await sessionApi.getSessionDetails(sessionId.trim());
        setSessionDetails(details);
        
        // Check if session is ended
        if (details.status === 'Ended') {
          setError('This session has ended. You cannot join an ended session.');
        } else {
          setError(null); // Clear error if session is active
        }
      } catch (err: any) {
        // If session not found or other error, clear session details
        setSessionDetails(null);
        // Don't set error here - let the join attempt handle it
        if (err.response?.status === 404) {
          // Session not found - clear error, will be handled on join attempt
          setError(null);
        }
      } finally {
        setCheckingSession(false);
      }
    };

    // Debounce: wait 500ms after user stops typing
    const timer = setTimeout(() => {
      checkSessionStatus();
    }, 500);

    return () => clearTimeout(timer);
  }, [sessionId, isAuthenticated]);

  const handleJoinSession = async () => {
    if (!sessionId.trim()) {
      setError('Please enter a session ID');
      return;
    }

    if (!isAuthenticated) {
      setError('Please login first');
      return;
    }

    // Check if session is ended before attempting to join
    if (sessionDetails && sessionDetails.status === 'Ended') {
      setError('This session has ended. You cannot join an ended session.');
      return;
    }

    if (hasAttemptedJoin.current) {
      return; // Prevent multiple attempts
    }

    hasAttemptedJoin.current = true;
    setLoading(true);
    setError(null);
    try {
      const result = await sessionApi.joinSession(sessionId.trim());
      setSuccess(true);
      // Navigate to session page to see all participants
      setTimeout(() => {
        navigate(`/session/${result.sessionIdString}`, { replace: true });
      }, 1000);
    } catch (err: any) {
      hasAttemptedJoin.current = false; // Allow retry on error
      console.error('Join session error:', err);
      if (err.response?.data?.message) {
        setError(err.response.data.message);
      } else if (err.response?.data?.error) {
        setError(err.response.data.error);
      } else if (err.message) {
        setError(err.message);
      } else if (err.response?.status === 401) {
        setError('Authentication failed. Please login again.');
      } else if (err.response?.status === 403) {
        setError('You do not have permission to join this session.');
      } else {
        setError('Failed to join session. Please check the session ID and try again.');
      }
    } finally {
      setLoading(false);
    }
  };

  // Auto-join if sessionId is provided and user is authenticated (only once)
  // Only auto-join if sessionId is in URL params (not manually entered)
  useEffect(() => {
    if (
      isInitialized &&
      isAuthenticated &&
      sessionId &&
      !success &&
      !loading &&
      !hasAttemptedJoin.current &&
      !error &&
      searchParams.get('sessionId') // Only auto-join if sessionId is in URL
    ) {
      // Small delay to ensure component is fully mounted
      const timer = setTimeout(() => {
        handleJoinSession();
      }, 100);
      return () => clearTimeout(timer);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isInitialized, isAuthenticated, sessionId]);

  return (
    <Container component="main" maxWidth="sm">
      <Box
        sx={{
          marginTop: 8,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
        }}
      >
        <Paper
          elevation={3}
          sx={{
            padding: 4,
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            width: '100%',
          }}
        >
          <GroupAdd sx={{ fontSize: 48, color: 'primary.main', mb: 2 }} />
          <Typography component="h1" variant="h4" gutterBottom>
            Join Session
          </Typography>
          <Typography variant="body1" color="text.secondary" sx={{ mb: 3, textAlign: 'center' }}>
            Enter the session ID or invitation code to join a meeting session.
          </Typography>

          {error && (
            <Alert severity="error" sx={{ width: '100%', mb: 2 }}>
              {error}
            </Alert>
          )}

          {success && (
            <>
              <Alert severity="success" sx={{ width: '100%', mb: 2 }}>
                Successfully joined the session! Redirecting to dashboard...
              </Alert>
              <Button
                variant="contained"
                color="primary"
                onClick={() => navigate('/dashboard', { replace: true })}
                sx={{ mt: 2 }}
              >
                Go to Dashboard
              </Button>
            </>
          )}

          {!success && (
            <>
              <TextField
                fullWidth
                label="Session ID / Invitation Code"
                variant="outlined"
                value={sessionId}
                onChange={(e) => setSessionId(e.target.value)}
                placeholder="Enter session ID or invitation code"
                disabled={loading || checkingSession}
                sx={{ mb: 3 }}
                helperText={
                  checkingSession
                    ? 'Checking session status...'
                    : sessionDetails && sessionDetails.status === 'Ended'
                      ? 'This session has ended'
                      : sessionDetails && sessionDetails.status === 'Active'
                        ? 'Session is active'
                        : ''
                }
                onKeyPress={(e) => {
                  if (e.key === 'Enter') {
                    handleJoinSession();
                  }
                }}
              />
              <Button
                variant="contained"
                color="primary"
                fullWidth
                startIcon={loading ? <CircularProgress size={20} /> : <GroupAdd />}
                onClick={handleJoinSession}
                disabled={
                  loading ||
                  checkingSession ||
                  !sessionId.trim() ||
                  (sessionDetails?.status === 'Ended')
                }
                sx={{ mb: 2 }}
              >
                {loading ? 'Joining...' : 'Join Session'}
              </Button>
            </>
          )}

          <Button
            variant="outlined"
            startIcon={<Login />}
            onClick={() => navigate('/dashboard')}
            sx={{ mt: 2 }}
          >
            Back to Dashboard
          </Button>
        </Paper>
      </Box>
    </Container>
  );
};

export default JoinSessionPage;

