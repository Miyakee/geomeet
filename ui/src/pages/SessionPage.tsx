import { useState, useCallback, useEffect } from 'react';
import {
  Container,
  Paper,
  Typography,
  Box,
  Alert,
  CircularProgress,
  Divider,
} from '@mui/material';
import { useAuth } from '../contexts/AuthContext';
import { useParams } from 'react-router-dom';
import { useSessionData } from '../hooks/useSessionData';
import { useInviteLink } from '../hooks/useInviteLink';
import { useLocationTracking } from '../hooks/useLocationTracking';
import { useWebSocket } from '../hooks/useWebSocket';
import { SessionHeader } from '../components/session/SessionHeader';
import { ParticipantList } from '../components/session/ParticipantList';
import { LocationTrackingSection } from '../components/session/LocationTrackingSection';
import { InviteSection } from '../components/session/InviteSection';
import { ParticipantLocation, SessionDetailResponse } from '../types/session';

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

  useWebSocket({
    sessionId,
    onSessionUpdate: handleSessionUpdate,
    onLocationUpdate: handleLocationUpdate,
    onAddressUpdate: handleAddressUpdate,
  });

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
