import { useEffect, useRef, useState } from 'react';
import {
  Alert,
  Button,
  TextField,
  Typography,
} from '@mui/material';
import { GroupAdd, Login } from '@mui/icons-material';
import { useAuth } from '../contexts/AuthContext';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { sessionApi } from '../services/api';
import { SessionDetailResponse } from '../types/session';
import { getErrorMessage } from '../utils/errorHandler';
import { PageContainer } from '../components/layout/PageContainer';
import { ErrorAlert } from '../components/common/ErrorAlert';
import { LoadingButton } from '../components/common/LoadingButton';
import { ROUTES, QUERY_PARAMS } from '../constants/routes';

const JoinSessionPage = () => {
  const { isAuthenticated, isInitialized } = useAuth();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [sessionId, setSessionId] = useState(searchParams.get(QUERY_PARAMS.SESSION_ID) || '');
  const [inviteCode, setInviteCode] = useState(searchParams.get(QUERY_PARAMS.INVITE_CODE) || '');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);
  const [checkingSession] = useState(false);
  const [sessionDetails, setSessionDetails] = useState<SessionDetailResponse | null>(null);
  const hasAttemptedJoin = useRef(false);

  // Redirect to login if not authenticated, preserving sessionId
  // Only redirect after auth state is initialized to avoid false redirects
  useEffect(() => {
    if (isInitialized && !isAuthenticated) {
      const redirectPath = sessionId
        ? `${ROUTES.LOGIN}?${QUERY_PARAMS.REDIRECT}=${ROUTES.JOIN}&${QUERY_PARAMS.SESSION_ID}=${sessionId}&${QUERY_PARAMS.INVITE_CODE}=${inviteCode}`
        : `${ROUTES.LOGIN}?${QUERY_PARAMS.REDIRECT}=${ROUTES.JOIN}`;
      navigate(redirectPath);
    }
  }, [isAuthenticated, isInitialized, navigate, sessionId, inviteCode]);

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
      navigate(ROUTES.SESSION(result.sessionIdString), { replace: true });
    } catch (err: unknown) {
      hasAttemptedJoin.current = false; // Allow retry on error
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
      searchParams.get(QUERY_PARAMS.SESSION_ID) && searchParams.get(QUERY_PARAMS.INVITE_CODE) // Only auto-join if both are in URL
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
    <PageContainer maxWidth="sm">
      <GroupAdd sx={{ fontSize: 48, color: 'primary.main', mb: 2 }} />
      <Typography component="h1" variant="h4" gutterBottom>
        Join Session
      </Typography>
      <Typography variant="body1" color="text.secondary" sx={{ mb: 3, textAlign: 'center' }}>
        Enter the session ID and invite code to join a meeting session.
      </Typography>

      <ErrorAlert message={error} />

      {success && (
        <>
          <Alert severity="success" sx={{ width: '100%', mb: 2 }}>
            Successfully joined the session! Redirecting to dashboard...
          </Alert>
          <Button
            variant="contained"
            color="primary"
            onClick={() => navigate(ROUTES.DASHBOARD, { replace: true })}
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
          <LoadingButton
            variant="contained"
            color="primary"
            fullWidth
            startIcon={<GroupAdd />}
            onClick={handleJoinSession}
            loading={loading}
            loadingText="Joining..."
            disabled={
              checkingSession ||
              !sessionId.trim() ||
              !inviteCode.trim() ||
              (sessionDetails?.status === 'Ended')
            }
            sx={{ mb: 2 }}
          >
            Join Session
          </LoadingButton>
        </>
      )}

      <Button
        variant="outlined"
        startIcon={<Login />}
        onClick={() => navigate(ROUTES.DASHBOARD)}
        sx={{ mt: 2 }}
      >
        Back to Dashboard
      </Button>
    </PageContainer>
  );
};

export default JoinSessionPage;

