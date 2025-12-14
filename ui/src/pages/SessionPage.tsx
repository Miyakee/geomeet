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
  Switch,
  FormControlLabel,
} from '@mui/material';
import { Person, Group, ContentCopy, LocationOn } from '@mui/icons-material';
import { useAuth } from '../contexts/AuthContext';
import { useParams } from 'react-router-dom';
import { sessionApi, UpdateLocationResponse } from '../services/api';
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
  const [session, setSession] = useState<SessionDetailResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [inviteLink, setInviteLink] = useState<string | null>(null);
  const [inviteCode, setInviteCode] = useState<string | null>(null);
  const [copied, setCopied] = useState(false);
  const [loadingInvite, setLoadingInvite] = useState(false);
  const [locationEnabled, setLocationEnabled] = useState(false);
  const [locationError, setLocationError] = useState<string | null>(null);
  const [currentLocation, setCurrentLocation] = useState<GeolocationPosition | null>(null);
  const [updatingLocation, setUpdatingLocation] = useState(false);
  const stompClientRef = useRef<Client | null>(null);
  const watchIdRef = useRef<number | null>(null);
  const locationUpdateIntervalRef = useRef<ReturnType<typeof setInterval> | null>(null);
  const latestPositionRef = useRef<GeolocationPosition | null>(null);

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
      // Cleanup location watching
      stopLocationTracking();
    };
  }, [sessionId]);

  // Cleanup location tracking when component unmounts
  useEffect(() => {
    return () => {
      stopLocationTracking();
    };
  }, []);

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

  const updateLocationToServer = async (position: GeolocationPosition) => {
    if (!sessionId) return;

    try {
      setUpdatingLocation(true);
      setLocationError(null);
      await sessionApi.updateLocation(sessionId, {
        latitude: position.coords.latitude,
        longitude: position.coords.longitude,
        accuracy: position.coords.accuracy,
      });
      setCurrentLocation(position);
      console.log('Location updated successfully');
    } catch (err: any) {
      console.error('Failed to update location:', err);
      setLocationError('Failed to update location. Please try again.');
    } finally {
      setUpdatingLocation(false);
    }
  };

  const startLocationTracking = () => {
    if (!navigator.geolocation) {
      setLocationError('Geolocation is not supported by your browser.');
      return;
    }

    setLocationError(null);

    // Request permission and get initial position
    navigator.geolocation.getCurrentPosition(
      (position) => {
        setCurrentLocation(position);
        updateLocationToServer(position);
        setLocationEnabled(true);

        // Start watching position changes
        watchIdRef.current = navigator.geolocation.watchPosition(
          (position) => {
            setCurrentLocation(position);
            latestPositionRef.current = position;
            // Clear any previous errors when we get a successful position
            setLocationError(null);
            // Update to server every 5 seconds (throttle)
            if (!locationUpdateIntervalRef.current) {
              updateLocationToServer(position);
              locationUpdateIntervalRef.current = setInterval(() => {
                if (latestPositionRef.current) {
                  updateLocationToServer(latestPositionRef.current);
                }
              }, 5000); // Update every 5 seconds
            }
          },
          (error) => {
            // For watchPosition errors, don't stop tracking, just show warning
            handleGeolocationError(error, false);
          },
          {
            enableHighAccuracy: true,
            timeout: 15000, // Increased timeout to 15 seconds
            maximumAge: 30000, // Accept cached position up to 30 seconds old
          }
        );
      },
      (error) => {
        console.error('Geolocation error (initial request):', error);
        handleGeolocationError(error, true);
        // Only disable if permission is denied
        if (error.code === error.PERMISSION_DENIED) {
          setLocationEnabled(false);
        }
      },
      {
        enableHighAccuracy: true,
        timeout: 15000, // Increased timeout to 15 seconds
        maximumAge: 30000, // Accept cached position up to 30 seconds old
      }
    );
  };

  const stopLocationTracking = () => {
    if (watchIdRef.current !== null) {
      navigator.geolocation.clearWatch(watchIdRef.current);
      watchIdRef.current = null;
    }
    if (locationUpdateIntervalRef.current) {
      clearInterval(locationUpdateIntervalRef.current);
      locationUpdateIntervalRef.current = null;
    }
    setLocationEnabled(false);
    setCurrentLocation(null);
    latestPositionRef.current = null;
  };

  const handleGeolocationError = (error: GeolocationPositionError, isInitialRequest: boolean = false) => {
    let errorMessage = '';
    
    switch (error.code) {
      case error.PERMISSION_DENIED:
        errorMessage = 'Location permission denied. Please enable location access in your browser settings.';
        if (isInitialRequest) {
          setLocationEnabled(false);
        }
        break;
      case error.POSITION_UNAVAILABLE:
        errorMessage = 'Location information is unavailable. This may happen if you are indoors or GPS signal is weak. The app will keep trying to get your location.';
        // Don't stop tracking for POSITION_UNAVAILABLE, just show warning
        break;
      case error.TIMEOUT:
        errorMessage = 'Location request timed out. The app will keep trying to get your location.';
        // Don't stop tracking for TIMEOUT, just show warning
        break;
      default:
        errorMessage = `Unable to get location (error code: ${error.code}). The app will keep trying.`;
        break;
    }
    
    setLocationError(errorMessage);
    console.warn('Geolocation error:', {
      code: error.code,
      message: error.message,
      isInitialRequest,
    });
  };

  const handleLocationToggle = (event: React.ChangeEvent<HTMLInputElement>) => {
    if (event.target.checked) {
      startLocationTracking();
    } else {
      stopLocationTracking();
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
        const sessionSubscription = client.subscribe(`/topic/session/${sessionId}`, (message) => {
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
        console.log('Subscribed to /topic/session/' + sessionId, sessionSubscription);

        // Subscribe to location updates
        const locationSubscription = client.subscribe(`/topic/session/${sessionId}/locations`, (message) => {
          try {
            console.log('Location update received:', message.body);
            const locationUpdate: UpdateLocationResponse = JSON.parse(message.body);
            console.log('Parsed location update:', locationUpdate);
            // Update location state for the participant
            setParticipantLocations((prev) => {
              const newMap = new Map(prev);
              newMap.set(locationUpdate.userId, {
                latitude: locationUpdate.latitude,
                longitude: locationUpdate.longitude,
                accuracy: locationUpdate.accuracy,
                updatedAt: locationUpdate.updatedAt,
              });
              return newMap;
            });
            console.log('Location updated for participant:', locationUpdate.userId);
          } catch (err) {
            console.error('Failed to parse location update message:', err);
            console.error('Message body:', message.body);
          }
        });
        console.log('Subscribed to /topic/session/' + sessionId + '/locations', locationSubscription);
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
                  key={participant.participantId || `user-${participant.userId}`}
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

          {/* Location Tracking Section */}
          <Box sx={{ mb: 3 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
              <LocationOn sx={{ mr: 1, color: 'primary.main' }} />
              <Typography variant="h6" sx={{ flexGrow: 1 }}>
                Location Tracking
              </Typography>
              <FormControlLabel
                control={
                  <Switch
                    checked={locationEnabled}
                    onChange={handleLocationToggle}
                    disabled={updatingLocation}
                  />
                }
                label={locationEnabled ? 'Enabled' : 'Disabled'}
              />
            </Box>
            {locationError && (
              <Alert 
                severity={locationError.includes('permission denied') ? 'error' : 'warning'} 
                sx={{ mb: 2 }}
                action={
                  locationError.includes('permission denied') ? (
                    <Button 
                      color="inherit" 
                      size="small" 
                      onClick={() => {
                        setLocationError(null);
                        startLocationTracking();
                      }}
                    >
                      Retry
                    </Button>
                  ) : null
                }
              >
                {locationError}
              </Alert>
            )}
            {locationEnabled && currentLocation && (
              <Alert severity="success" sx={{ mb: 2 }}>
                Location: {currentLocation.coords.latitude.toFixed(6)},{' '}
                {currentLocation.coords.longitude.toFixed(6)} (Accuracy: ±
                {Math.round(currentLocation.coords.accuracy)}m)
                {updatingLocation && <CircularProgress size={16} sx={{ ml: 1 }} />}
              </Alert>
            )}
            {locationEnabled && !currentLocation && (
              <Alert severity="info" sx={{ mb: 2 }}>
                Getting your location...
              </Alert>
            )}
          </Box>

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

