import { useState, useCallback, useEffect } from 'react';
import {
  Container,
  Paper,
  Typography,
  Box,
  Alert,
  CircularProgress,
  Divider,
  Button,
} from '@mui/material';
import { LocationOn } from '@mui/icons-material';
import { useAuth } from '../contexts/AuthContext';
import { useParams } from 'react-router-dom';
import { useSessionData } from '../hooks/useSessionData';
import { useInviteLink } from '../hooks/useInviteLink';
import { useLocationTracking } from '../hooks/useLocationTracking';
import { useOptimalLocation } from '../hooks/useOptimalLocation';
import { useMeetingLocation } from '../hooks/useMeetingLocation';
import { useWebSocket } from '../hooks/useWebSocket';
import { SessionHeader } from '../components/session/SessionHeader';
import { ParticipantList } from '../components/session/ParticipantList';
import { LocationTrackingSection } from '../components/session/LocationTrackingSection';
import { InviteSection } from '../components/session/InviteSection';
import { MeetingLocationSection } from '../components/session/MeetingLocationSection';
import { OptimalLocationMap } from '../components/session/OptimalLocationMap';
import { ParticipantLocation, SessionDetailResponse } from '../types/session';
import { CalculateOptimalLocationResponse, UpdateMeetingLocationResponse } from '../services/api';

const SessionPage = () => {
  const { sessionId } = useParams<{ sessionId: string }>();
  const { user } = useAuth();
  const { session, loading, error, updateSession } = useSessionData(sessionId);
  const {
    inviteLink,
    inviteCode,
    copied,
    loadingInvite,
    loadInviteLink,
    handleCopyInviteLink,
    handleCopyInviteCode,
  } = useInviteLink(sessionId);

  const {
    locationEnabled,
    locationError,
    currentLocation,
    updatingLocation,
    handleLocationToggle,
    startLocationTracking,
  } = useLocationTracking(sessionId);

  const [participantLocations, setParticipantLocations] = useState<Map<number, ParticipantLocation>>(new Map());
  const [participantAddresses, setParticipantAddresses] = useState<Map<number, string>>(new Map());
  const [participantNames, setParticipantNames] = useState<Map<number, string>>(new Map());

  const {
    optimalLocation,
    loading: calculatingOptimalLocation,
    error: optimalLocationError,
    calculateOptimalLocation,
    updateOptimalLocation,
  } = useOptimalLocation(sessionId);

  const {
    meetingLocation,
    loading: updatingMeetingLocation,
    error: meetingLocationError,
    updateMeetingLocation,
    updateMeetingLocationFromResponse,
  } = useMeetingLocation(sessionId);

  const handleSessionUpdate = useCallback((updatedSession: SessionDetailResponse) => {
    updateSession(updatedSession);
  }, [updateSession]);

  const handleLocationUpdate = useCallback((location: ParticipantLocation, userId: number) => {
    setParticipantLocations((prev) => {
      const newMap = new Map(prev);
      newMap.set(userId, location);
      return newMap;
    });
  }, []);

  const handleAddressUpdate = useCallback((address: string, userId: number) => {
    setParticipantAddresses((prev) => {
      const newMap = new Map(prev);
      newMap.set(userId, address);
      return newMap;
    });
  }, []);

  const handleOptimalLocationUpdate = useCallback((optimalLocation: CalculateOptimalLocationResponse) => {
    updateOptimalLocation(optimalLocation);
  }, [updateOptimalLocation]);

  const handleMeetingLocationUpdate = useCallback((meetingLocation: UpdateMeetingLocationResponse) => {
    updateMeetingLocationFromResponse(meetingLocation);
  }, [updateMeetingLocationFromResponse]);

  useWebSocket({
    sessionId,
    onSessionUpdate: handleSessionUpdate,
    onLocationUpdate: handleLocationUpdate,
    onAddressUpdate: handleAddressUpdate,
    onOptimalLocationUpdate: handleOptimalLocationUpdate,
    onMeetingLocationUpdate: handleMeetingLocationUpdate,
  });

  // Update participant names from session data
  useEffect(() => {
    if (session) {
      const names = new Map<number, string>();
      session.participants.forEach((participant) => {
        names.set(participant.userId, participant.username);
      });
      setParticipantNames(names);
    }
  }, [session]);

  // Initialize meeting location from session data
  useEffect(() => {
    if (session && session.meetingLocationLatitude != null && session.meetingLocationLongitude != null) {
      updateMeetingLocationFromResponse({
        sessionId: session.id,
        sessionIdString: session.sessionId,
        latitude: session.meetingLocationLatitude,
        longitude: session.meetingLocationLongitude,
        message: '',
      });
    }
  }, [session, updateMeetingLocationFromResponse]);

  // Load invite link when session is loaded and user is initiator
  useEffect(() => {
    if (session && session.initiatorId === user?.id && !inviteLink && !loadingInvite) {
      loadInviteLink();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [session, user?.id, inviteLink, loadingInvite]);

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
          <SessionHeader session={session} isInitiator={isInitiator} />

          <Divider sx={{ my: 2 }} />

          <ParticipantList
            session={session}
            currentUserId={user?.id}
            participantLocations={participantLocations}
            participantAddresses={participantAddresses}
          />

          <Divider sx={{ my: 3 }} />

          {/* Optimal Location Map */}
          <OptimalLocationMap
            optimalLocation={optimalLocation ? {
              latitude: optimalLocation.optimalLatitude,
              longitude: optimalLocation.optimalLongitude,
              totalTravelDistance: optimalLocation.totalTravelDistance,
              participantCount: optimalLocation.participantCount,
            } : null}
            participantLocations={new Map(
              Array.from(participantLocations.entries()).map(([userId, loc]) => [
                userId,
                {
                  latitude: loc.latitude,
                  longitude: loc.longitude,
                  userId,
                },
              ])
            )}
            participantNames={participantNames}
            currentUserLocation={currentLocation ? {
              latitude: currentLocation.coords.latitude,
              longitude: currentLocation.coords.longitude,
            } : null}
          />

          {/* Calculate Optimal Location Button */}
          <Box sx={{ mb: 3, display: 'flex', justifyContent: 'center' }}>
            <Button
              variant="contained"
              startIcon={<LocationOn />}
              onClick={calculateOptimalLocation}
              disabled={calculatingOptimalLocation || participantLocations.size === 0}
            >
              {calculatingOptimalLocation ? 'Calculating...' : 'Calculate Optimal Location'}
            </Button>
          </Box>

          {optimalLocationError && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {optimalLocationError}
            </Alert>
          )}

          {optimalLocation && (
            <Alert 
              severity="success" 
              sx={{ mb: 2 }}
              action={
                isInitiator && (
                  <Button
                    color="inherit"
                    size="small"
                    onClick={async () => {
                      try {
                        await updateMeetingLocation(
                          optimalLocation.optimalLatitude,
                          optimalLocation.optimalLongitude
                        );
                      } catch (err) {
                        // Error is handled by useMeetingLocation hook
                      }
                    }}
                    disabled={updatingMeetingLocation}
                  >
                    {updatingMeetingLocation ? 'Setting...' : 'Set as Meeting Location'}
                  </Button>
                )
              }
            >
              Optimal location calculated! Total travel distance: {optimalLocation.totalTravelDistance.toFixed(2)} km
            </Alert>
          )}

          <Divider sx={{ my: 3 }} />

          <LocationTrackingSection
            locationEnabled={locationEnabled}
            locationError={locationError}
            currentLocation={currentLocation}
            updatingLocation={updatingLocation}
            onToggle={handleLocationToggle}
            onRetry={() => {
              startLocationTracking();
            }}
          />

          <Divider sx={{ my: 3 }} />

          {/* Meeting Location Section */}
          <MeetingLocationSection
            meetingLocation={meetingLocation}
            isInitiator={isInitiator}
            onUpdateLocation={updateMeetingLocation}
            loading={updatingMeetingLocation}
            error={meetingLocationError}
          />

          <Divider sx={{ my: 3 }} />

          {isInitiator && (
            <>
              <InviteSection
                inviteLink={inviteLink}
                inviteCode={inviteCode}
                copied={copied}
                loadingInvite={loadingInvite}
                onLoadInviteLink={loadInviteLink}
                onCopyInviteLink={handleCopyInviteLink}
                onCopyInviteCode={handleCopyInviteCode}
              />
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
