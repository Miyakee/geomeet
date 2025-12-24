import { useState } from 'react';
import {
  Avatar,
  Button,
  Divider,
  Typography,
} from '@mui/material';
import { Add, Logout, Person } from '@mui/icons-material';
import { useAuth } from '../contexts/AuthContext';
import { useNavigate } from 'react-router-dom';
import { sessionApi } from '../services/api';
import { getErrorMessage } from '../utils/errorHandler';
import { PageContainer } from '../components/layout/PageContainer';
import { ErrorAlert } from '../components/common/ErrorAlert';
import { LoadingButton } from '../components/common/LoadingButton';
import { ROUTES } from '../constants/routes';

const DashboardPage = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleLogout = () => {
    logout();
    navigate(ROUTES.LOGIN);
  };

  const handleCreateSession = async () => {
    setLoading(true);
    setError(null);
    try {
      const createdSession = await sessionApi.createSession();
      navigate(ROUTES.SESSION(createdSession.sessionId));
    } catch (err: unknown) {
      setError(getErrorMessage(err, 'Failed to create session. Please try again.'));
    } finally {
      setLoading(false);
    }
  };

  return (
    <PageContainer maxWidth="md">
      <Avatar sx={{ m: 1, bgcolor: 'primary.main', width: 64, height: 64 }}>
        <Person sx={{ fontSize: 40 }} />
      </Avatar>
      <Typography component="h1" variant="h4" gutterBottom>
        Welcome to GeoMeet
      </Typography>
      <Typography variant="h6" color="text.secondary" gutterBottom>
        {user?.username}
      </Typography>
      <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
        {user?.email}
      </Typography>

      <Divider sx={{ width: '100%', my: 2 }} />

      <ErrorAlert message={error} />
      <Typography variant="body1" sx={{ mb: 3, textAlign: 'center' }}>
        Create a new session to invite friends to join.
      </Typography>
      <LoadingButton
        variant="contained"
        color="primary"
        startIcon={<Add />}
        onClick={handleCreateSession}
        loading={loading}
        sx={{ mb: 2 }}
      >
        Create Session
      </LoadingButton>

      <Divider sx={{ width: '100%', my: 2 }} />

      <Button
        variant="contained"
        color="error"
        startIcon={<Logout />}
        onClick={handleLogout}
        sx={{ mt: 2 }}
      >
        Logout
      </Button>
    </PageContainer>
  );
};

export default DashboardPage;

