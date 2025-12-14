import { ListItem, ListItemAvatar, ListItemText, Avatar, Chip, Box, Typography } from '@mui/material';
import { Person, LocationOn } from '@mui/icons-material';
import { ParticipantInfo, ParticipantLocation } from '../../types/session';

interface ParticipantItemProps {
  participant: ParticipantInfo;
  isInitiator: boolean;
  isCurrentUser: boolean;
  location?: ParticipantLocation;
  address?: string;
}

export const ParticipantItem = ({
  participant,
  isInitiator,
  isCurrentUser,
  location,
  address,
}: ParticipantItemProps) => {
  const locationAge = location ? Date.now() - new Date(location.updatedAt).getTime() : null;
  const isRecent = locationAge !== null && locationAge < 60000;

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
        secondary={
          <Box>
            <Typography variant="body2" color="text.secondary">
              {participant.email} • Joined: {new Date(participant.joinedAt).toLocaleString()}
            </Typography>
            {location && (
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5, mt: 0.5 }}>
                <LocationOn sx={{ fontSize: 14, color: isRecent ? 'success.main' : 'text.secondary' }} />
                <Typography 
                  variant="body2" 
                  color={isRecent ? 'success.main' : 'text.secondary'} 
                  sx={{ fontSize: '0.75rem' }}
                >
                  {location.latitude.toFixed(6)}, {location.longitude.toFixed(6)}
                  {address && <> • {address}</>}
                  {location.accuracy && <> • ±{Math.round(location.accuracy)}m</>}
                  {!isRecent && <> • {Math.round(locationAge! / 1000)}s ago</>}
                </Typography>
              </Box>
            )}
          </Box>
        }
      />
    </ListItem>
  );
};

