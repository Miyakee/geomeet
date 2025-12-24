import { Typography, List, Alert } from '@mui/material';
import { ParticipantItem } from './ParticipantItem';
import { SessionDetailResponse } from '../../types/session';

interface ParticipantListProps {
  session: SessionDetailResponse;
  currentUserId?: number;
  participantAddresses: Map<number, string>;
}

export const ParticipantList = ({
  session,
  currentUserId,
  participantAddresses,
}: ParticipantListProps) => {
  return (
    <>
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
            <ParticipantItem
              key={participant.participantId || `user-${participant.userId}`}
              participant={participant}
              isInitiator={participant.userId === session.initiatorId}
              isCurrentUser={participant.userId === currentUserId}
              address={participantAddresses.get(participant.userId)}
            />
          ))}
        </List>
      )}
    </>
  );
};

