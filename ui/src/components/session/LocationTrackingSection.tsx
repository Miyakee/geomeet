import { Box, Typography, Switch, FormControlLabel, Alert, Button, CircularProgress } from '@mui/material';
import { LocationOn } from '@mui/icons-material';

interface LocationTrackingSectionProps {
  locationEnabled: boolean;
  locationError: string | null;
  currentLocation: GeolocationPosition | null;
  updatingLocation: boolean;
  onToggle: (event: React.ChangeEvent<HTMLInputElement>) => void;
  onRetry: () => void;
}

export const LocationTrackingSection = ({
  locationEnabled,
  locationError,
  currentLocation,
  updatingLocation,
  onToggle,
  onRetry,
}: LocationTrackingSectionProps) => {
  return (
    <Box sx={{ mb: 3 }}>
      <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
        <LocationOn sx={{ mr: 1, color: 'primary.main' }} />
        <Typography variant="h6" sx={{ flexGrow: 1 }}>
          Location Tracking
        </Typography>
        <FormControlLabel
          control={
            <Switch
              checked={locationEnabled}
              onChange={onToggle}
              disabled={updatingLocation}
            />
          }
          label={locationEnabled ? 'Enabled' : 'Disabled'}
        />
      </Box>
      {locationError && (
        <Alert 
          severity={locationError.includes('permission denied') ? 'error' : 'warning'} 
          sx={{ mb: 2 }}
          action={
            locationError.includes('permission denied') ? (
              <Button 
                color="inherit" 
                size="small" 
                onClick={onRetry}
              >
                Retry
              </Button>
            ) : null
          }
        >
          {locationError}
        </Alert>
      )}
      {locationEnabled && currentLocation && (
        <Alert severity="success" sx={{ mb: 2 }}>
          Location: {currentLocation.coords.latitude.toFixed(6)},{' '}
          {currentLocation.coords.longitude.toFixed(6)} (Accuracy: ±
          {Math.round(currentLocation.coords.accuracy)}m)
          {updatingLocation && <CircularProgress size={16} sx={{ ml: 1 }} />}
        </Alert>
      )}
      {locationEnabled && !currentLocation && (
        <Alert severity="info" sx={{ mb: 2 }}>
          Getting your location...
        </Alert>
      )}
    </Box>
  );
};

