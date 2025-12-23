import {useState} from 'react';
import {
  Alert,
  Avatar,
  Box,
  Button,
  CircularProgress,
  Container,
  Divider,
  Paper,
  Typography,
} from '@mui/material';
import {Add, Logout, Person} from '@mui/icons-material';
import {useAuth} from '../contexts/AuthContext';
import {useNavigate} from 'react-router-dom';
import {sessionApi} from '../services/api';
import {getErrorMessage} from '../utils/errorHandler';

const DashboardPage = () => {
  const {user, logout} = useAuth();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const handleCreateSession = async () => {
    setLoading(true);
    setError(null);
    try {
      const createdSession = await sessionApi.createSession();
      navigate(`/session/${createdSession.sessionId}`);
    } catch (err: any) {
      console.error('Failed to create session:', err);
      setError(getErrorMessage(err, 'Failed to create session. Please try again.'));
    } finally {
      setLoading(false);
    }
  };

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
            alignItems: 'center',
            width: '100%',
          }}
        >
          <Avatar sx={{m: 1, bgcolor: 'primary.main', width: 64, height: 64}}>
            <Person sx={{fontSize: 40}}/>
          </Avatar>
          <Typography component="h1" variant="h4" gutterBottom>
                        Welcome to GeoMeet
          </Typography>
          <Typography variant="h6" color="text.secondary" gutterBottom>
            {user?.username}
          </Typography>
          <Typography variant="body1" color="text.secondary" sx={{mb: 3}}>
            {user?.email}
          </Typography>

          <Divider sx={{width: '100%', my: 2}}/>

          {error && (
            <Alert severity="error" sx={{width: '100%', mb: 2}}>
              {error}
            </Alert>
          )}
          <Typography variant="body1" sx={{mb: 3, textAlign: 'center'}}>
                        Create a new session to invite friends to join.
          </Typography>
          <Button
            variant="contained"
            color="primary"
            startIcon={loading ? <CircularProgress size={20}/> : <Add/>}
            onClick={handleCreateSession}
            disabled={loading}
            sx={{mb: 2}}
          >
                        Create Session
          </Button>

          <Divider sx={{width: '100%', my: 2}}/>

          <Button
            variant="contained"
            color="error"
            startIcon={<Logout/>}
            onClick={handleLogout}
            sx={{mt: 2}}
          >
                        Logout
          </Button>
        </Paper>
      </Box>
    </Container>
  );
};

export default DashboardPage;

