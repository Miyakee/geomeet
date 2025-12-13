import { Container, Paper, Typography, Button, Box, Avatar } from '@mui/material';
import { Logout, Person } from '@mui/icons-material';
import { useAuth } from '../contexts/AuthContext';
import { useNavigate } from 'react-router-dom';

const DashboardPage = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
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
          <Typography variant="body1" sx={{ mb: 3, textAlign: 'center' }}>
            You have successfully logged in! This is your session dashboard.
          </Typography>
          <Button
            variant="contained"
            color="error"
            startIcon={<Logout />}
            onClick={handleLogout}
            sx={{ mt: 2 }}
          >
            Logout
          </Button>
        </Paper>
      </Box>
    </Container>
  );
};

export default DashboardPage;

