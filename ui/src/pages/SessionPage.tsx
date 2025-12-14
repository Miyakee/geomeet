import { useState, useEffect, useRef } from 'react';
import {
  Container,
  Paper,
  Typography,
  Box,
  List,
  ListItem,
  ListItemAvatar,
  ListItemText,
  Avatar,
  Chip,
  Alert,
  CircularProgress,
  Divider,
  TextField,
  IconButton,
  InputAdornment,
  Button,
} from '@mui/material';
import { Person, Group, ContentCopy } from '@mui/icons-material';
import { useAuth } from '../contexts/AuthContext';
import { useNavigate, useParams } from 'react-router-dom';
import { sessionApi } from '../services/api';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

interface ParticipantInfo {
  participantId: number;
  userId: number;
  username: string;
  email: string;
  joinedAt: string;
}

interface SessionDetailResponse {
  id: number;
  sessionId: string;
  initiatorId: number;
  initiatorUsername: string;
  status: string;
  createdAt: string;
  participants: ParticipantInfo[];
  participantCount: number;
}

const SessionPage = () => {
  const { sessionId } = useParams<{ sessionId: string }>();
  const { user } = useAuth();
  const navigate = useNavigate();
  const [session, setSession] = useState<SessionDetailResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [inviteLink, setInviteLink] = useState<string | null>(null);
  const [inviteCode, setInviteCode] = useState<string | null>(null);
  const [copied, setCopied] = useState(false);
  const [loadingInvite, setLoadingInvite] = useState(false);
  const stompClientRef = useRef<Client | null>(null);

  useEffect(() => {
    if (!sessionId) {
      setError('Session ID is required');
      setLoading(false);
      return;
    }

    // Load initial session data
    loadSessionData();

    // Setup WebSocket connection after a short delay to ensure session data is loaded
    const wsTimer = setTimeout(() => {
      setupWebSocket();
    }, 500);

    return () => {
      // Cleanup WebSocket connection
      clearTimeout(wsTimer);
      if (stompClientRef.current) {
        console.log('Cleaning up WebSocket connection');
        stompClientRef.current.deactivate();
        stompClientRef.current = null;
      }
    };
  }, [sessionId]);

  const loadSessionData = async () => {
    if (!sessionId) return;

    try {
      setLoading(true);
      const data = await sessionApi.getSessionDetails(sessionId);
      setSession(data);
      setError(null);
      
      // If user is the initiator, load invite link
      if (data.initiatorId === user?.id) {
        loadInviteLink();
      }
    } catch (err: any) {
      console.error('Failed to load session:', err);
      if (err.response?.status === 403) {
        setError('You do not have permission to view this session.');
      } else if (err.response?.status === 404) {
        setError('Session not found.');
      } else {
        setError('Failed to load session. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  };

  const loadInviteLink = async () => {
    if (!sessionId) return;
    
    try {
      setLoadingInvite(true);
      const invite = await sessionApi.generateInviteLink(sessionId);
      setInviteLink(`${window.location.origin}${invite.inviteLink}`);
      setInviteCode(invite.inviteCode);
    } catch (err: any) {
      console.error('Failed to load invite link:', err);
    } finally {
      setLoadingInvite(false);
    }
  };

  const handleCopyInviteLink = () => {
    if (inviteLink) {
      navigator.clipboard.writeText(inviteLink);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  const handleCopyInviteCode = () => {
    if (inviteCode) {
      navigator.clipboard.writeText(inviteCode);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  const setupWebSocket = () => {
    if (!sessionId) return;

    const token = localStorage.getItem('token');
    if (!token) {
      console.error('No token found for WebSocket connection');
      return;
    }

    // Clean up existing connection if any
    if (stompClientRef.current) {
      stompClientRef.current.deactivate();
      stompClientRef.current = null;
    }

    // Determine WebSocket URL based on environment
    // In development, use relative path to leverage Vite proxy
    // In production, use the full API URL
    const wsUrl = import.meta.env.PROD
      ? (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080').replace(/^http/, 'ws')
      : '';

    // Use SockJS for WebSocket connection
    const socket = new SockJS(wsUrl ? `${wsUrl}/ws` : '/ws');
    const client = new Client({
      webSocketFactory: () => socket as any,
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: (frame) => {
        console.log('WebSocket connected, frame:', frame);
        // Subscribe to session updates
        const subscription = client.subscribe(`/topic/session/${sessionId}`, (message) => {
          try {
            console.log('Raw WebSocket message:', message.body);
            const updatedSession: SessionDetailResponse = JSON.parse(message.body);
            console.log('Parsed session update:', updatedSession);
            console.log('Participants count:', updatedSession.participants?.length);
            setSession(updatedSession);
          } catch (err) {
            console.error('Failed to parse WebSocket message:', err);
            console.error('Message body:', message.body);
          }
        });
        console.log('Subscribed to /topic/session/' + sessionId, subscription);
      },
      onStompError: (frame) => {
        console.error('WebSocket STOMP error:', frame);
      },
      onWebSocketError: (event) => {
        console.error('WebSocket error:', event);
      },
      onDisconnect: () => {
        console.log('WebSocket disconnected');
      },
    });

    stompClientRef.current = client;
    client.activate();
  };

  if (loading) {
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
          <CircularProgress />
          <Typography variant="body1" sx={{ mt: 2 }}>
            Loading session...
          </Typography>
        </Box>
      </Container>
    );
  }

  if (error) {
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
          <Alert severity="error" sx={{ width: '100%', mb: 2 }}>
            {error}
          </Alert>
        </Box>
      </Container>
    );
  }

  if (!session) {
    return null;
  }

  const isInitiator = user?.id === session.initiatorId;

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
            width: '100%',
          }}
        >
          <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
            <Group sx={{ fontSize: 40, color: 'primary.main', mr: 2 }} />
            <Box>
              <Typography component="h1" variant="h4" gutterBottom>
                Session: {session.sessionId.substring(0, 8)}...
              </Typography>
              <Box sx={{ display: 'flex', gap: 1, alignItems: 'center', mt: 1 }}>
                <Chip
                  label={`Status: ${session.status}`}
                  color={session.status === 'Active' ? 'success' : 'default'}
                  size="small"
                />
                {isInitiator && (
                  <Chip label="Initiator" color="primary" size="small" />
                )}
              </Box>
            </Box>
          </Box>

          <Divider sx={{ my: 2 }} />

          <Typography variant="h6" gutterBottom>
            Participants ({session.participantCount})
          </Typography>

          {session.participants.length === 0 ? (
            <Alert severity="info" sx={{ mt: 2 }}>
              No participants yet. Waiting for others to join...
            </Alert>
          ) : (
            <List sx={{ width: '100%', mt: 2 }}>
              {session.participants.map((participant) => (
                <ListItem
                  key={participant.participantId}
                  sx={{
                    bgcolor: participant.userId === user?.id ? 'action.selected' : 'transparent',
                    borderRadius: 1,
                    mb: 1,
                  }}
                >
                  <ListItemAvatar>
                    <Avatar>
                      <Person />
                    </Avatar>
                  </ListItemAvatar>
                  <ListItemText
                    primary={
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <Typography variant="body1" fontWeight="medium">
                          {participant.username}
                        </Typography>
                        {participant.userId === session.initiatorId && (
                          <Chip label="Initiator" size="small" color="primary" />
                        )}
                        {participant.userId === user?.id && (
                          <Chip label="You" size="small" />
                        )}
                      </Box>
                    }
                    secondary={
                      <Typography variant="body2" color="text.secondary">
                        {participant.email} • Joined: {new Date(participant.joinedAt).toLocaleString()}
                      </Typography>
                    }
                  />
                </ListItem>
              ))}
            </List>
          )}

          <Divider sx={{ my: 3 }} />

          {isInitiator && (
            <>
              <Typography variant="h6" sx={{ mb: 2, width: '100%' }}>
                Invite Friends
              </Typography>
              {loadingInvite ? (
                <CircularProgress size={24} />
              ) : inviteLink && inviteCode ? (
                <Box sx={{ width: '100%', mb: 3 }}>
                  <Box sx={{ mb: 2 }}>
                    <Typography variant="body2" color="text.secondary" gutterBottom>
                      Invite Link:
                    </Typography>
                    <TextField
                      fullWidth
                      value={inviteLink}
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
                      value={inviteCode}
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
              ) : (
                <Button
                  variant="outlined"
                  onClick={loadInviteLink}
                  disabled={loadingInvite}
                >
                  Generate Invite Link
                </Button>
              )}
              <Divider sx={{ my: 3 }} />
            </>
          )}

          <Typography variant="body2" color="text.secondary">
            Session created: {new Date(session.createdAt).toLocaleString()}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Created by: {session.initiatorUsername}
          </Typography>
        </Paper>
      </Box>
    </Container>
  );
};

export default SessionPage;

