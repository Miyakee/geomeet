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
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
} from '@mui/material';
import { LocationOn, Stop } from '@mui/icons-material';
import { useAuth } from '../contexts/AuthContext';
import { useParams } from 'react-router-dom';
import { useSessionData } from '../hooks/useSessionData';
import { useInviteLink } from '../hooks/useInviteLink';
import { useLocationTracking } from '../hooks/useLocationTracking';
import { useOptimalLocation } from '../hooks/useOptimalLocation';
import { useMeetingLocation } from '../hooks/useMeetingLocation';
import { useEndSession } from '../hooks/useEndSession';
import { useWebSocket } from '../hooks/useWebSocket';
import { SessionHeader } from '../components/session/SessionHeader';
import { ParticipantList } from '../components/session/ParticipantList';
import { LocationTrackingSection } from '../components/session/LocationTrackingSection';
import { InviteSection } from '../components/session/InviteSection';
import { MeetingLocationSection } from '../components/session/MeetingLocationSection';
import { OptimalLocationMap } from '../components/session/OptimalLocationMap';
import { ParticipantLocation, SessionDetailResponse } from '../types/session';
import { CalculateOptimalLocationResponse, UpdateMeetingLocationResponse } from '../services/api';
import { SessionEndNotification } from '../hooks/useWebSocket';

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
  const [sessionEndNotification, setSessionEndNotification] = useState<SessionEndNotification | null>(null);

  const {
    optimalLocation,
    loading: calculatingOptimalLocation,
    error: optimalLocationError,
    calculateOptimalLocation,
    updateOptimalLocation,
  } = useOptimalLocation(sessionId);

  const {
    meetingLocation,
    meetingLocationAddress,
    loadingAddress: loadingMeetingLocationAddress,
    loading: updatingMeetingLocation,
    error: meetingLocationError,
    updateMeetingLocation,
    updateMeetingLocationFromResponse,
  } = useMeetingLocation(sessionId);

  const {
    loading: endingSession,
    error: endSessionError,
    endSession,
  } = useEndSession(sessionId);

  const [endSessionDialogOpen, setEndSessionDialogOpen] = useState(false);

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

  const handleSessionEnd = useCallback((notification: SessionEndNotification) => {
    // Update session status to Ended
    if (session) {
      updateSession({
        ...session,
        status: 'Ended',
      });
    }
    // Store notification for display
    setSessionEndNotification(notification);
  }, [session, updateSession]);

  useWebSocket({
    sessionId,
    onSessionUpdate: handleSessionUpdate,
    onLocationUpdate: handleLocationUpdate,
    onAddressUpdate: handleAddressUpdate,
    onOptimalLocationUpdate: handleOptimalLocationUpdate,
    onMeetingLocationUpdate: handleMeetingLocationUpdate,
    onSessionEnd: handleSessionEnd,
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
  // This ensures new users who join after meeting location is set can see it
  useEffect(() => {
    if (session) {
      if (session.meetingLocationLatitude != null && session.meetingLocationLongitude != null) {
        // Always update to ensure new users see the meeting location
        // Check if the location has changed to avoid unnecessary updates
        if (!meetingLocation || 
            meetingLocation.latitude !== session.meetingLocationLatitude || 
            meetingLocation.longitude !== session.meetingLocationLongitude) {
          updateMeetingLocationFromResponse({
            sessionId: session.id,
            sessionIdString: session.sessionId,
            latitude: session.meetingLocationLatitude,
            longitude: session.meetingLocationLongitude,
            message: '',
          });
        }
      }
    }
  }, [session?.meetingLocationLatitude, session?.meetingLocationLongitude, session?.id, session?.sessionId, updateMeetingLocationFromResponse, meetingLocation]);

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

          {/* Session End Notification */}
          {sessionEndNotification && (
            <Alert 
              severity={sessionEndNotification.hasMeetingLocation ? 'success' : 'warning'} 
              sx={{ mb: 2 }}
              onClose={() => setSessionEndNotification(null)}
            >
              {sessionEndNotification.hasMeetingLocation ? (
                <Box>
                  <Typography variant="body1" fontWeight="bold" gutterBottom>
                    Session Ended - Final Meeting Location
                  </Typography>
                  <Typography variant="body2">
                    The session has ended. The final meeting location has been set:
                  </Typography>
                  <Typography variant="body2" sx={{ mt: 1, fontWeight: 'medium' }}>
                    {sessionEndNotification.meetingLocationLatitude?.toFixed(6)}, {sessionEndNotification.meetingLocationLongitude?.toFixed(6)}
                  </Typography>
                </Box>
              ) : (
                <Box>
                  <Typography variant="body1" fontWeight="bold" gutterBottom>
                    Session Cancelled
                  </Typography>
                  <Typography variant="body2">
                    The session has been ended without a final meeting location.
                  </Typography>
                </Box>
              )}
            </Alert>
          )}

          {/* End Session Button - Only visible to initiator for active sessions */}
          {isInitiator && session.status === 'Active' && (
            <Box sx={{ mb: 2, display: 'flex', justifyContent: 'flex-end' }}>
              <Button
                variant="outlined"
                color="error"
                startIcon={<Stop />}
                onClick={() => setEndSessionDialogOpen(true)}
                disabled={endingSession}
              >
                End Session
              </Button>
            </Box>
          )}

          <Divider sx={{ my: 2 }} />

          <ParticipantList
            session={session}
            currentUserId={user?.id}
            participantLocations={participantLocations}
            participantAddresses={participantAddresses}
          />

          <Divider sx={{ my: 3 }} />

          {/* Meeting Location Section */}
          <MeetingLocationSection
              meetingLocation={meetingLocation}
              meetingLocationAddress={meetingLocationAddress}
              loadingAddress={loadingMeetingLocationAddress}
              currentUserLocation={currentLocation ? {
                latitude: currentLocation.coords.latitude,
                longitude: currentLocation.coords.longitude,
              } : null}
              isInitiator={isInitiator}
              onUpdateLocation={updateMeetingLocation}
              loading={updatingMeetingLocation}
              error={meetingLocationError}
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
            meetingLocation={meetingLocation ? {
              latitude: meetingLocation.latitude,
              longitude: meetingLocation.longitude,
            } : null}
            meetingLocationAddress={meetingLocationAddress}
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

      {/* End Session Confirmation Dialog */}
      <Dialog
        open={endSessionDialogOpen}
        onClose={() => setEndSessionDialogOpen(false)}
        aria-labelledby="end-session-dialog-title"
        aria-describedby="end-session-dialog-description"
      >
        <DialogTitle id="end-session-dialog-title">
          End Session
        </DialogTitle>
        <DialogContent>
          <DialogContentText id="end-session-dialog-description">
            Are you sure you want to end this session? This action cannot be undone.
            {meetingLocation && (
              <>
                <br />
                <br />
                The final meeting location will be shared with all participants.
              </>
            )}
            {!meetingLocation && (
              <>
                <br />
                <br />
                No meeting location has been set. The session will be marked as cancelled.
              </>
            )}
          </DialogContentText>
          {endSessionError && (
            <Alert severity="error" sx={{ mt: 2 }}>
              {endSessionError}
            </Alert>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setEndSessionDialogOpen(false)} disabled={endingSession}>
            Cancel
          </Button>
          <Button
            onClick={async () => {
              try {
                await endSession();
                setEndSessionDialogOpen(false);
              } catch (err) {
                // Error is handled by useEndSession hook
              }
            }}
            variant="contained"
            color="error"
            disabled={endingSession}
            startIcon={<Stop />}
          >
            {endingSession ? 'Ending...' : 'End Session'}
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default SessionPage;
