import { ListItem, ListItemAvatar, ListItemText, Avatar, Chip, Box, Typography } from '@mui/material';
import { Person, LocationOn } from '@mui/icons-material';
import { ParticipantInfo } from '../../types/session';

interface ParticipantItemProps {
  participant: ParticipantInfo;
  isInitiator: boolean;
  isCurrentUser: boolean;
  address?: string;
}

export const ParticipantItem = ({
  participant,
  isInitiator,
  isCurrentUser,
  address,
}: ParticipantItemProps) => {
  // Extract location from participant object
  const hasLocation = participant.latitude != null && participant.longitude != null;
  const locationUpdatedAt = participant.locationUpdatedAt || participant.joinedAt;
  const locationAge = hasLocation && locationUpdatedAt ? Date.now() - new Date(locationUpdatedAt).getTime() : null;
  const isRecent = locationAge !== null && locationAge < 60000;
  const locationAgeSeconds = locationAge !== null ? Math.round(locationAge / 1000) : null;

  return (
    <ListItem
      key={participant.participantId || `user-${participant.userId}`}
      sx={{
        bgcolor: isCurrentUser ? 'action.selected' : 'transparent',
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
            {isInitiator && (
              <Chip label="Initiator" size="small" color="primary" />
            )}
            {isCurrentUser && (
              <Chip label="You" size="small" />
            )}
          </Box>
        }
        primaryTypographyProps={{ component: 'div' }}
        secondary={
          <Box>
            <Typography variant="body2" color="text.secondary">
              {participant.email} • Joined: {new Date(participant.joinedAt).toLocaleString()}
            </Typography>
            {hasLocation && participant.latitude != null && participant.longitude != null && (
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5, mt: 0.5 }}>
                <LocationOn sx={{ fontSize: 14, color: isRecent ? 'success.main' : 'text.secondary' }} />
                <Typography 
                  variant="body2" 
                  color={isRecent ? 'success.main' : 'text.secondary'} 
                  sx={{ fontSize: '0.75rem' }}
                >
                  {participant.latitude.toFixed(6)}, {participant.longitude.toFixed(6)}
                  {address && <> • {address}</>}
                  {participant.accuracy && <> • ±{Math.round(participant.accuracy)}m</>}
                  {!isRecent && locationAgeSeconds !== null && <> • {locationAgeSeconds}s ago</>}
                </Typography>
              </Box>
            )}
          </Box>
        }
        secondaryTypographyProps={{ component: 'div' }}
      />
    </ListItem>
  );
};

