import {useCallback, useEffect, useState} from 'react';
import {
  Alert,
  Box,
  Button,
  CircularProgress,
  Container,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  Divider,
  Paper,
  Typography,
} from '@mui/material';
import {LocationOn, Stop} from '@mui/icons-material';
import {useAuth} from '../contexts/AuthContext';
import {useParams} from 'react-router-dom';
import {useSessionData} from '../hooks/useSessionData';
import {useInviteLink} from '../hooks/useInviteLink';
import {useLocationTracking} from '../hooks/useLocationTracking';
import {useOptimalLocation} from '../hooks/useOptimalLocation';
import {useMeetingLocation} from '../hooks/useMeetingLocation';
import {useEndSession} from '../hooks/useEndSession';
import {SessionEndNotification, useWebSocket} from '../hooks/useWebSocket';
import {SessionHeader} from '../components/session/SessionHeader';
import {ParticipantList} from '../components/session/ParticipantList';
import {LocationTrackingSection} from '../components/session/LocationTrackingSection';
import {InviteSection} from '../components/session/InviteSection';
import {MeetingLocationSection} from '../components/session/MeetingLocationSection';
import {OptimalLocationMap} from '../components/session/OptimalLocationMap';
import {
  ParticipantLocation,
  SessionDetailResponse,
} from '../types/session';
import {CalculateOptimalLocationResponse, UpdateMeetingLocationResponse} from '../services/api';

const SessionPage = () => {
  const {sessionId} = useParams<{ sessionId: string }>();
  const {user} = useAuth();
  const {session, loading, error, updateSession} = useSessionData(sessionId);
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
    showManualInput,
    handleLocationToggle,
    startLocationTracking,
    setManualLocation,
    setShowManualInput,
    restoreLocation,
  } = useLocationTracking(sessionId, session?.status);

  useEffect(() => {
    if (!locationEnabled && session?.status !== 'Ended' && user?.id) {
      setParticipantLocations((prev) => {
        const newMap = new Map(prev);
        newMap.delete(user.id);
        return newMap;
      });
    }
  }, [locationEnabled, session?.status, user?.id]);

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

  // Unified session update handler - merge location info to preserve existing locations
  // This ensures that even when participants disconnect, their location information is preserved
  const handleSessionUpdate = useCallback((updatedSession: SessionDetailResponse) => {
    if (!session) {
      // If no existing session, just update directly
      updateSession(updatedSession);
      return;
    }

    // Create a map of existing participants with their location info for quick lookup
    const existingParticipantsMap = new Map(
      session.participants.map(p => [p.userId, p]),
    );

    // Merge participants: prioritize updated info, but preserve existing location info
    const mergedParticipants = updatedSession.participants.map(updatedParticipant => {
      const existingParticipant = existingParticipantsMap.get(updatedParticipant.userId);

      // If updated participant has location info, use it (newer data)
      if (updatedParticipant.latitude != null && updatedParticipant.longitude != null) {
        return updatedParticipant;
      }
      
      // If updated participant doesn't have location but existing one does, preserve it
      // This ensures location info persists even after disconnection
      if (existingParticipant?.latitude != null && existingParticipant.longitude != null) {
        return {
          ...updatedParticipant,
          latitude: existingParticipant.latitude,
          longitude: existingParticipant.longitude,
          accuracy: existingParticipant.accuracy,
          locationUpdatedAt: existingParticipant.locationUpdatedAt,
        };
      }

      // Return updated participant as-is (no location info in either)
      return updatedParticipant;
    });

    // Preserve ALL participants from existing session that are not in updated session
    // This is critical for maintaining participant info after disconnection
    const updatedParticipantIds = new Set(updatedSession.participants.map(p => p.userId));
    
    session.participants.forEach(existingParticipant => {
      if (!updatedParticipantIds.has(existingParticipant.userId)) {
        // Keep this participant with their last known location info
        mergedParticipants.push(existingParticipant);
      }
    });

    // Update session with merged participants, preserving all location information
    updateSession({
      ...updatedSession,
      participants: mergedParticipants,
    });
  }, [session, updateSession]);

  // Update participant location in session object (defined early for use in other callbacks)
  const updateParticipantLocation = useCallback((userId: number, location: ParticipantLocation) => {
    if (!session) {
      return;
    }
    
    // Check if participant exists in the list
    const existingParticipant = session.participants.find(p => p.userId === userId);
    
    if (existingParticipant) {
      // Update existing participant's location
      updateSession({
        ...session,
        participants: session.participants.map(p =>
          p.userId === userId
            ? {
              ...p,
              latitude: location.latitude,
              longitude: location.longitude,
              accuracy: location.accuracy ?? null,
              locationUpdatedAt: location.updatedAt,
            }
            : p,
        ),
      });
    } else {
      // Participant not in list - this shouldn't happen normally, but handle it gracefully
      // We'll add them with minimal info (location will be preserved by handleSessionUpdate)
      console.warn(`Participant ${userId} not found in session participants, location update may be lost`);
      // Still update the participantLocations state so it shows immediately
      // The location will be preserved when handleSessionUpdate merges data
    }
  }, [session, updateSession]);

  const handleLocationUpdate = useCallback((location: ParticipantLocation, userId: number) => {
    // Exclude current user's location from participantLocations
    // Current user's location should be managed via currentLocation state
    if (userId === user?.id) {
      return;
    }
    // Update session.participants to persist location info
    updateParticipantLocation(userId, location);
    // Also update participantLocations state for immediate UI update
    setParticipantLocations((prev) => {
      const newMap = new Map(prev);
      newMap.set(userId, location);
      return newMap;
    });
  }, [user?.id, updateParticipantLocation]);

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
    // Also update session object to keep it in sync
    if (session) {
      updateSession({
        ...session,
        meetingLocationLatitude: meetingLocation.latitude,
        meetingLocationLongitude: meetingLocation.longitude,
      });
    }
  }, [updateMeetingLocationFromResponse, session, updateSession]);

  const handleUpdateMeetingLocation = useCallback(async (latitude?: number, longitude?: number) => {
    if (!latitude || !longitude) {
      return;
    }
    await updateMeetingLocation(latitude, longitude);
    // Update session object immediately after successful update
    if (session) {
      updateSession({
        ...session,
        meetingLocationLatitude: latitude,
        meetingLocationLongitude: longitude,
      });
    }
  }, [updateMeetingLocation, session, updateSession]);

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
    // 不清除当前用户的位置，保留最后的位置显示
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

  const generateParticipantsNameMap = useCallback(() => {
    const names = new Map<number, string>();
    session?.participants.forEach((participant) => {
      names.set(participant.userId, participant.username);
    });
    setParticipantNames(names);
  }, [session?.participants, setParticipantNames]);

  const generateParticipantsLocation = useCallback(() => {
    if (!session?.participants) {
      return;
    }
    const locationsMap = new Map<number, ParticipantLocation>();

    for (const participant of session.participants) {
      // Only add location if participant has shared location and it's not the current user
      if (participant.latitude != null && participant.longitude != null && participant.userId !== user?.id) {
        locationsMap.set(participant.userId, {
          latitude: participant.latitude,
          longitude: participant.longitude,
          accuracy: participant.accuracy ?? undefined,
          updatedAt: participant.locationUpdatedAt || participant.joinedAt,
        });
      }
    }
    setParticipantLocations(locationsMap);
  }, [session?.participants, user?.id, setParticipantLocations]);

  const updateMeetingLocationBySessionRes = useCallback(() => {
    if (session?.meetingLocationLatitude && session.meetingLocationLongitude) {
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
  }, [session, meetingLocation, updateMeetingLocationFromResponse]);

  useEffect(() => {
    if (!session) {
      return;
    }
    generateParticipantsNameMap();
    updateMeetingLocationBySessionRes();
    generateParticipantsLocation();
  }, [session, generateParticipantsNameMap, generateParticipantsLocation, updateMeetingLocationBySessionRes]);

  useEffect(() => {
    if (session && session.participants && !currentLocation && user?.id) {
      const currentUserParticipant = session.participants.find(p => p.userId === user.id);
      if (currentUserParticipant && currentUserParticipant.latitude != null && currentUserParticipant.longitude != null) {
        restoreLocation(
          currentUserParticipant.latitude,
          currentUserParticipant.longitude,
          currentUserParticipant.accuracy ?? undefined,
        );
      }
    }
  }, [session?.participants, user?.id, currentLocation, restoreLocation]);


  useEffect(() => {
    if (session && session.status === 'Ended' && !sessionEndNotification) {
      const hasMeetingLocation = session.meetingLocationLatitude != null && session.meetingLocationLongitude != null;
      const notification: SessionEndNotification = {
        sessionId: session.id,
        sessionIdString: session.sessionId,
        status: 'Ended',
        endedAt: session.createdAt, // Use createdAt as fallback since we don't have endedAt in session data
        message: 'Session ended',
        hasMeetingLocation,
        meetingLocationLatitude: session.meetingLocationLatitude ?? null,
        meetingLocationLongitude: session.meetingLocationLongitude ?? null,
      };
      setSessionEndNotification(notification);
    }
  }, [session, sessionEndNotification]);

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
          <CircularProgress/>
          <Typography variant="body1" sx={{mt: 2}}>
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
          <Alert severity="error" sx={{width: '100%', mb: 2}}>
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
          <SessionHeader session={session} isInitiator={isInitiator}/>

          {/* Session End Notification */}
          {(sessionEndNotification) && (
            <Alert
              severity={sessionEndNotification.hasMeetingLocation ? 'success' : 'warning'}
              sx={{mb: 2}}
              onClose={() => setSessionEndNotification(null)}
            >
              {sessionEndNotification.hasMeetingLocation ? (
                <Box>
                  <Typography variant="body1" fontWeight="bold" gutterBottom>
                                        Session Ended - Final Meeting Location
                  </Typography>
                  <Typography variant="body2">
                                        The session has ended. The final meeting location has been
                                        set:
                  </Typography>
                  <Typography variant="body2" sx={{mt: 1, fontWeight: 'medium'}}>
                    {sessionEndNotification.meetingLocationLatitude?.toFixed(6)},{' '}
                    {sessionEndNotification.meetingLocationLongitude?.toFixed(6)}
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
            <Box sx={{mb: 2, display: 'flex', justifyContent: 'flex-end'}}>
              <Button
                variant="outlined"
                color="error"
                startIcon={<Stop/>}
                onClick={() => setEndSessionDialogOpen(true)}
                disabled={endingSession}
              >
                                End Session
              </Button>
            </Box>
          )}

          <Divider sx={{my: 2}}/>

          <ParticipantList
            session={session}
            currentUserId={user?.id}
            participantAddresses={participantAddresses}
          />

          <Divider sx={{my: 3}}/>

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
            sessionStatus={session.status}
            onUpdateLocation={handleUpdateMeetingLocation}
            loading={updatingMeetingLocation}
            error={meetingLocationError}
          />

          <Divider sx={{my: 3}}/>


          <LocationTrackingSection
            locationEnabled={locationEnabled}
            locationError={locationError}
            currentLocation={currentLocation}
            updatingLocation={updatingLocation}
            showManualInput={showManualInput}
            sessionStatus={session?.status}
            onToggle={handleLocationToggle}
            onRetry={() => {
              startLocationTracking();
            }}
            onSetManualLocation={setManualLocation}
            onCloseManualInput={() => setShowManualInput(false)}
          />

          <Divider sx={{my: 3}}/>

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
              ]),
            )}
            participantNames={participantNames}
            currentUserLocation={currentLocation ? {
              latitude: currentLocation.coords.latitude,
              longitude: currentLocation.coords.longitude,
              userId: user?.id,
            } : null}
            meetingLocation={meetingLocation ? {
              latitude: meetingLocation.latitude,
              longitude: meetingLocation.longitude,
            } : null}
            meetingLocationAddress={meetingLocationAddress}
          />

          {/* Calculate Optimal Location Button */}
          <Box sx={{mb: 3, display: 'flex', justifyContent: 'center'}}>
            <Button
              variant="contained"
              startIcon={<LocationOn/>}
              onClick={calculateOptimalLocation}
              disabled={session.status === 'Ended' || calculatingOptimalLocation || participantLocations.size === 0}
            >
              {calculatingOptimalLocation ? 'Calculating...' : 'Calculate Optimal Location'}
            </Button>
          </Box>

          {optimalLocationError && (
            <Alert severity="error" sx={{mb: 2}}>
              {optimalLocationError}
            </Alert>
          )}

          {optimalLocation && (
            <Alert
              severity="success"
              sx={{mb: 2}}
              action={
                isInitiator && (
                  <Button
                    color="inherit"
                    size="small"
                    onClick={async () => {
                      await handleUpdateMeetingLocation(
                        optimalLocation?.optimalLatitude,
                        optimalLocation?.optimalLongitude,
                      );
                    }}
                    disabled={updatingMeetingLocation}
                  >
                    {updatingMeetingLocation ? 'Setting...' : 'Set as Meeting Location'}
                  </Button>
                )
              }
            >
                            Optimal location calculated! Total travel
                            distance: {optimalLocation.totalTravelDistance.toFixed(2)} km
            </Alert>
          )}

          <Divider sx={{my: 3}}/>

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
              <Divider sx={{my: 3}}/>
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
                <br/>
                <br/>
                                The final meeting location will be shared with all participants.
              </>
            )}
            {!meetingLocation && (
              <>
                <br/>
                <br/>
                                No meeting location has been set. The session will be marked as
                                cancelled.
              </>
            )}
          </DialogContentText>
          {endSessionError && (
            <Alert severity="error" sx={{mt: 2}}>
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
            startIcon={<Stop/>}
          >
            {endingSession ? 'Ending...' : 'End Session'}
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default SessionPage;
