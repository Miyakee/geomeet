import { useState } from 'react';
import {
  TextField,
  Button,
  Typography,
  Box,
  InputAdornment,
  IconButton,
} from '@mui/material';
import { Visibility, VisibilityOff, Lock, Email } from '@mui/icons-material';
import { useAuth } from '../contexts/AuthContext';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { LoginRequest } from '../services/api';
import { RegisterDialog } from '../components/auth/RegisterDialog';
import { getErrorMessage } from '../utils/errorHandler';
import { PageContainer } from '../components/layout/PageContainer';
import { ErrorAlert } from '../components/common/ErrorAlert';
import { LoadingButton } from '../components/common/LoadingButton';
import { ROUTES, QUERY_PARAMS } from '../constants/routes';

const LoginPage = () => {
  const [usernameOrEmail, setUsernameOrEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [registerDialogOpen, setRegisterDialogOpen] = useState(false);

  const { login } = useAuth();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const sessionId = searchParams.get(QUERY_PARAMS.SESSION_ID);
    
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      const credentials: LoginRequest = {
        usernameOrEmail,
        password,
      };
      await login(credentials);

      // Check if this is an invite link (has sessionId parameter)
      const sessionIdParam = searchParams.get(QUERY_PARAMS.SESSION_ID);
      const inviteCode = searchParams.get(QUERY_PARAMS.INVITE_CODE);
      const redirect = searchParams.get(QUERY_PARAMS.REDIRECT);
            
      // If sessionId exists, this is an invite link
      if (sessionIdParam) {
        // Navigate to join page, which will auto-join and redirect to session
        const joinUrl = inviteCode
          ? `${ROUTES.JOIN}?${QUERY_PARAMS.SESSION_ID}=${sessionIdParam}&${QUERY_PARAMS.INVITE_CODE}=${inviteCode}`
          : `${ROUTES.JOIN}?${QUERY_PARAMS.SESSION_ID}=${sessionIdParam}`;
        navigate(joinUrl, { replace: true });
      } else if (redirect) {
        // Handle other redirect cases
        navigate(redirect, { replace: true });
      } else {
        // Default: go to dashboard
        navigate(ROUTES.DASHBOARD, { replace: true });
      }
    } catch (err: unknown) {
      setError(getErrorMessage(err, 'Login failed. Please try again.'));
    } finally {
      setLoading(false);
    }
  };

  return (
    <PageContainer maxWidth="xs">
      <Lock sx={{ fontSize: 40, color: 'primary.main', mb: 2 }} />
      <Typography component="h1" variant="h4" gutterBottom>
                Sign In
      </Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
                Welcome to GeoMeet
      </Typography>

      <ErrorAlert message={error} />

      <Box component="form" onSubmit={handleSubmit} sx={{ width: '100%', mt: 1 }}>
        <TextField
          margin="normal"
          required
          fullWidth
          id="usernameOrEmail"
          label="Username or Email"
          name="usernameOrEmail"
          autoComplete="username"
          autoFocus
          value={usernameOrEmail}
          onChange={(e) => setUsernameOrEmail(e.target.value)}
          InputProps={{
            startAdornment: (
              <InputAdornment position="start">
                <Email />
              </InputAdornment>
            ),
          }}
        />
        <TextField
          margin="normal"
          required
          fullWidth
          name="password"
          label="Password"
          type={showPassword ? 'text' : 'password'}
          id="password"
          autoComplete="current-password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          InputProps={{
            startAdornment: (
              <InputAdornment position="start">
                <Lock />
              </InputAdornment>
            ),
            endAdornment: (
              <InputAdornment position="end">
                <IconButton
                  aria-label="toggle password visibility"
                  onClick={() => setShowPassword(!showPassword)}
                  edge="end"
                >
                  {showPassword ? <VisibilityOff /> : <Visibility />}
                </IconButton>
              </InputAdornment>
            ),
          }}
        />
        <LoadingButton
          type="submit"
          fullWidth
          variant="contained"
          loading={loading}
          loadingText="Signing in..."
          sx={{ mt: 3, mb: 2, py: 1.5 }}
        >
                    Sign In
        </LoadingButton>
        <Button
          fullWidth
          variant="outlined"
          disabled={loading}
          onClick={() => setRegisterDialogOpen(true)}
          sx={{ mb: 2 }}
        >
                    Create Account Now !
        </Button>
      </Box>

      <RegisterDialog
        open={registerDialogOpen}
        onClose={() => setRegisterDialogOpen(false)}
        sessionId={sessionId || undefined}
      />

      <Box sx={{ mt: 2, textAlign: 'center' }}>
        <Typography variant="body2" color="text.secondary">
                    Demo credentials:
        </Typography>
        <Typography variant="caption" color="text.secondary" display="block">
                    admin / admin123
        </Typography>
        <Typography variant="caption" color="text.secondary" display="block">
                    testuser / test123
        </Typography>
      </Box>
    </PageContainer>
  );
};

export default LoginPage;

