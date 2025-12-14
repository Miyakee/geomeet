import { Typography, Box, TextField, IconButton, InputAdornment, Button, Alert, CircularProgress } from '@mui/material';
import { ContentCopy } from '@mui/icons-material';

interface InviteSectionProps {
  inviteLink: string | null;
  inviteCode: string | null;
  copied: boolean;
  loadingInvite: boolean;
  onLoadInviteLink: () => void;
  onCopyInviteLink: () => void;
  onCopyInviteCode: () => void;
}

export const InviteSection = ({
  inviteLink,
  inviteCode,
  copied,
  loadingInvite,
  onLoadInviteLink,
  onCopyInviteLink,
  onCopyInviteCode,
}: InviteSectionProps) => {
  return (
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
                    <IconButton onClick={onCopyInviteLink} edge="end">
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
                    <IconButton onClick={onCopyInviteCode} edge="end">
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
          onClick={onLoadInviteLink}
          disabled={loadingInvite}
        >
          Generate Invite Link
        </Button>
      )}
    </>
  );
};

