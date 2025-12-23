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
import {getErrorMessage} from "../utils/errorHandler.ts";

const JoinSessionPage = () => {
  const { isAuthenticated, isInitialized } = useAuth();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [sessionId, setSessionId] = useState(searchParams.get('sessionId') || '');
  const [inviteCode, setInviteCode] = useState(searchParams.get('inviteCode') || '');
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
      const redirectPath = sessionId ? `/login?redirect=/join&sessionId=${sessionId}&inviteCode=${inviteCode}` : '/login?redirect=/join';
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
  }, [sessionId, isAuthenticated]);

  const handleJoinSession = async () => {
    if (!sessionId.trim()) {
      setError('Please enter a session ID');
      return;
    }

    if (!inviteCode.trim()) {
      setError('Please enter an invite code');
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
      const result = await sessionApi.joinSession(sessionId.trim(), inviteCode.trim());
      setSuccess(true);
      // Navigate to session page immediately (no delay for better UX)
      navigate(`/session/${result.sessionIdString}`, { replace: true });
    } catch (err: any) {
      hasAttemptedJoin.current = false; // Allow retry on error
      console.error('Join session error:', err);
      setError(getErrorMessage(err, 'Failed to join session. Please check the session ID and invite code.'));
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
      searchParams.get('sessionId') && searchParams.get('inviteCode') // Only auto-join if both are in URL
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
            Enter the session ID and invite code to join a meeting session.
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
                label="Session ID"
                variant="outlined"
                value={sessionId}
                onChange={(e) => setSessionId(e.target.value)}
                placeholder="Enter session ID"
                disabled={loading || checkingSession}
                sx={{ mb: 2 }}
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
                  if (e.key === 'Enter' && inviteCode.trim()) {
                    handleJoinSession();
                  }
                }}
              />
              <TextField
                fullWidth
                label="Invite Code"
                variant="outlined"
                value={inviteCode}
                onChange={(e) => setInviteCode(e.target.value.toUpperCase())}
                placeholder="Enter invite code"
                disabled={loading || checkingSession}
                sx={{ mb: 3 }}
                inputProps={{ maxLength: 8, style: { textTransform: 'uppercase' } }}
                helperText="8-character code provided by the session creator"
                onKeyPress={(e) => {
                  if (e.key === 'Enter' && sessionId.trim() && inviteCode.trim()) {
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
                  !inviteCode.trim() ||
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

