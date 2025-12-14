import { Box, Typography, Chip } from '@mui/material';
import { Group } from '@mui/icons-material';
import { SessionDetailResponse } from '../../types/session';

interface SessionHeaderProps {
  session: SessionDetailResponse;
  isInitiator: boolean;
}

export const SessionHeader = ({ session, isInitiator }: SessionHeaderProps) => {
  return (
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
  );
};

