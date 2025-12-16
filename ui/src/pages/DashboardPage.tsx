import {useEffect, useState} from 'react';
import {
  Alert,
  Avatar,
  Box,
  Button,
  Chip,
  CircularProgress,
  Container,
  Divider,
  IconButton,
  InputAdornment,
  Paper,
  TextField,
  Typography,
} from '@mui/material';
import {Add, ContentCopy, Logout, Person} from '@mui/icons-material';
import {useAuth} from '../contexts/AuthContext';
import {useNavigate} from 'react-router-dom';
import {CreateSessionResponse, InviteLinkResponse, sessionApi} from '../services/api';

const DashboardPage = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [session, setSession] = useState<CreateSessionResponse | null>(null);
  const [inviteLink, setInviteLink] = useState<InviteLinkResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [_loadingInvite, setLoadingInvite] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [copied, setCopied] = useState(false);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const handleCreateSession = async () => {
    setLoading(true);
    setError(null);
    try {
      const createdSession = await sessionApi.createSession();
      setSession(createdSession);
      // Navigate to session page
      navigate(`/session/${createdSession.sessionId}`);
    } catch (err: any) {
      if (err.response?.data?.message) {
        setError(err.response.data.message);
      } else if (err.message) {
        setError(err.message);
      } else {
        setError('Failed to create session. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleCopySessionId = () => {
    if (session?.sessionId) {
      navigator.clipboard.writeText(session.sessionId);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  const handleCopyInviteLink = () => {
    if (inviteLink?.inviteLink) {
      const fullLink = `${window.location.origin}${inviteLink.inviteLink}`;
      navigator.clipboard.writeText(fullLink);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  const handleCopyInviteCode = () => {
    if (inviteLink?.inviteCode) {
      navigator.clipboard.writeText(inviteLink.inviteCode);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  const handleGenerateInviteLink = async () => {
    if (!session?.sessionId) {
      return;
    }
    setLoadingInvite(true);
    setError(null);
    try {
      const invite = await sessionApi.generateInviteLink(session.sessionId);
      setInviteLink(invite);
    } catch (err: any) {
      if (err.response?.data?.message) {
        setError(err.response.data.message);
      } else if (err.message) {
        setError(err.message);
      } else {
        setError('Failed to generate invite link. Please try again.');
      }
    } finally {
      setLoadingInvite(false);
    }
  };

  useEffect(() => {
    if (session?.sessionId && !inviteLink) {
      handleGenerateInviteLink();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [session?.sessionId]);

  return (
    <Container component="main" maxWidth="md">
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
          <Avatar sx={{ m: 1, bgcolor: 'primary.main', width: 64, height: 64 }}>
            <Person sx={{ fontSize: 40 }} />
          </Avatar>
          <Typography component="h1" variant="h4" gutterBottom>
            Welcome to GeoMeet
          </Typography>
          <Typography variant="h6" color="text.secondary" gutterBottom>
            {user?.username}
          </Typography>
          <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
            {user?.email}
          </Typography>

          <Divider sx={{ width: '100%', my: 2 }} />

          {error && (
            <Alert severity="error" sx={{ width: '100%', mb: 2 }}>
              {error}
            </Alert>
          )}

          {!session ? (
            <>
              <Typography variant="body1" sx={{ mb: 3, textAlign: 'center' }}>
                Create a new session to invite friends to join.
              </Typography>
              <Button
                variant="contained"
                color="primary"
                startIcon={loading ? <CircularProgress size={20} /> : <Add />}
                onClick={handleCreateSession}
                disabled={loading}
                sx={{ mb: 2 }}
              >
                Create Session
              </Button>
            </>
          ) : (
            <>
              <Typography variant="h5" sx={{ mb: 2, textAlign: 'center' }}>
                Session Created Successfully!
              </Typography>
              <Box sx={{ width: '100%', mb: 2 }}>
                <Typography variant="body2" color="text.secondary" gutterBottom>
                  Session ID:
                </Typography>
                <Box
                  sx={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: 1,
                    mb: 2,
                    p: 1,
                    bgcolor: 'grey.100',
                    borderRadius: 1,
                  }}
                >
                  <Typography
                    variant="body1"
                    sx={{
                      fontFamily: 'monospace',
                      flex: 1,
                      wordBreak: 'break-all',
                    }}
                  >
                    {session.sessionId}
                  </Typography>
                  <Button
                    size="small"
                    startIcon={<ContentCopy />}
                    onClick={handleCopySessionId}
                  >
                    Copy
                  </Button>
                </Box>
                <Box sx={{ display: 'flex', gap: 2, mb: 2 }}>
                  <Chip
                    label={`Status: ${session.status}`}
                    color={session.status === 'Active' ? 'success' : 'default'}
                  />
                  <Typography variant="body2" color="text.secondary">
                    Created: {new Date(session.createdAt).toLocaleString()}
                  </Typography>
                </Box>
                <Alert severity="success" sx={{ mb: 2 }}>
                  {session.message}
                </Alert>

                {inviteLink && (
                  <Box sx={{ width: '100%', mt: 3 }}>
                    <Typography variant="h6" sx={{ mb: 2 }}>
                      Invite Friends
                    </Typography>
                    <Box sx={{ mb: 2 }}>
                      <Typography variant="body2" color="text.secondary" gutterBottom>
                        Invite Link:
                      </Typography>
                      <TextField
                        fullWidth
                        value={`${window.location.origin}${inviteLink.inviteLink}`}
                        InputProps={{
                          readOnly: true,
                          endAdornment: (
                            <InputAdornment position="end">
                              <IconButton onClick={handleCopyInviteLink} edge="end">
                                <ContentCopy />
                              </IconButton>
                            </InputAdornment>
                          ),
                        }}
                        sx={{ mb: 2 }}
                      />
                    </Box>
                    <Box>
                      <Typography variant="body2" color="text.secondary" gutterBottom>
                        Invite Code:
                      </Typography>
                      <TextField
                        fullWidth
                        value={inviteLink.inviteCode}
                        InputProps={{
                          readOnly: true,
                          endAdornment: (
                            <InputAdornment position="end">
                              <IconButton onClick={handleCopyInviteCode} edge="end">
                                <ContentCopy />
                              </IconButton>
                            </InputAdornment>
                          ),
                        }}
                      />
                    </Box>
                    {copied && (
                      <Alert severity="success" sx={{ mt: 1 }}>
                        Copied to clipboard!
                      </Alert>
                    )}
                  </Box>
                )}
              </Box>
            </>
          )}

          <Divider sx={{ width: '100%', my: 2 }} />

          <Button
            variant="contained"
            color="error"
            startIcon={<Logout />}
            onClick={handleLogout}
            sx={{ mt: 2 }}
          >
            Logout
          </Button>
        </Paper>
      </Box>
    </Container>
  );
};

export default DashboardPage;

