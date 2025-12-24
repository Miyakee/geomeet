import { useState } from 'react';
import Button from '@mui/material/Button';
import Dialog from '@mui/material/Dialog';
import DialogTitle from '@mui/material/DialogTitle';
import DialogActions from '@mui/material/DialogActions';
import {
  Alert,
  DialogContent,
  IconButton,
  InputAdornment,
  TextField,
} from '@mui/material';
import { authApi, RegisterRequest, sessionApi } from '../../services/api';
import { useAuth } from '../../contexts/AuthContext';
import { useNavigate } from 'react-router-dom';
import { Visibility, VisibilityOff } from '@mui/icons-material';
import { getErrorMessage } from '../../utils/errorHandler';
import { LoadingButton } from '../common/LoadingButton';
import { ROUTES } from '../../constants/routes';
import { buildJoinUrl, getInviteCodeFromUrl } from '../../utils/navigation';

interface RegisterDialogProps {
    open: boolean;
    onClose: () => void;
    sessionId?: string | null;
}

export const RegisterDialog = ({ open, onClose, sessionId }: RegisterDialogProps) => {
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [verificationCode, setVerificationCode] = useState('2025'); // 固定验证码
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  const { setAuthFromResponse } = useAuth();
  const navigate = useNavigate();

  const handleSignUp = async () => {
    if (!username || !email || !verificationCode || !password ) {
      setError('Please fill in all required fields');
      return;
    }

    if (verificationCode !== '2025') {
      setError('Verification code error, please input: 2025');
      return;
    }

    setError(null);
    setLoading(true);

    try {
      const registerInfo: RegisterRequest = {
        username,
        password,
        email,
        verificationCode,
      };

      const response = await authApi.register(registerInfo);

      setAuthFromResponse(response.token, response.username, response.email);

      setUsername('');
      setPassword('');
      setEmail('');
      setVerificationCode('2025');
            
      onClose();
            
      if (sessionId) {
        const inviteCode = getInviteCodeFromUrl();
        try {
          if (inviteCode) {
            const joinResult = await sessionApi.joinSession(sessionId, inviteCode);
            navigate(ROUTES.SESSION(joinResult.sessionIdString), { replace: true });
          } else {
            navigate(buildJoinUrl(sessionId), { replace: true });
          }
        } catch {
          // Redirect to join page on error
          navigate(buildJoinUrl(sessionId, inviteCode), { replace: true });
        }
      } else {
        navigate(ROUTES.DASHBOARD, { replace: true });
      }
    } catch (err: unknown) {
      setError(getErrorMessage(err, 'Registration failed. Please try again.'));
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    setUsername('');
    setEmail('');
    setVerificationCode('2025');
    onClose();
  };

  return (
    <Dialog
      onClose={handleClose}
      open={open}
      maxWidth="xs"
      fullWidth
    >
      <DialogTitle sx={{ m: 0, p: 2 }} id="customized-dialog-title">
                   Sign Up
      </DialogTitle>
      <DialogContent>
        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}
        <TextField 
          fullWidth 
          id="username"
          label="Username"
          variant="outlined"  
          margin="normal"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
        />
        <TextField
          fullWidth
          id="password"
          label="Password"
          variant="outlined"
          type={showPassword ? 'text' : 'password'}
          margin="normal"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          InputProps={{
            endAdornment: (
              <InputAdornment position="end">
                <IconButton
                  aria-label="toggle password visibility"
                  onClick={() => setShowPassword(!showPassword)}
                  edge="end"
                >
                  {showPassword ? <VisibilityOff/> : <Visibility/>}
                </IconButton>
              </InputAdornment>
            ),
          }}
        />
        <TextField 
          fullWidth 
          id="email"
          label="Email"
          variant="outlined"  
          margin="normal"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />
        <TextField 
          fullWidth 
          id="code"
          label="Code" 
          variant="outlined" 
          margin="normal"
          value={verificationCode}
          onChange={(e) => setVerificationCode(e.target.value)}
          helperText="verification code ：2025"
        />
      </DialogContent>
      <DialogActions>
        <Button onClick={handleClose} disabled={loading}>Cancel</Button>
        <LoadingButton
          onClick={handleSignUp}
          variant="contained"
          loading={loading}
          loadingText="Registering..."
        >
          Register
        </LoadingButton>
      </DialogActions>
    </Dialog>
  );
};
