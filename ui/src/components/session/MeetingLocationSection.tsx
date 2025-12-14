import { useState } from 'react';
import {
  Box,
  Paper,
  Typography,
  Button,
  TextField,
  Alert,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  IconButton,
  Tooltip,
} from '@mui/material';
import { Edit, LocationOn, Check, Close } from '@mui/icons-material';

interface MeetingLocationSectionProps {
  meetingLocation: { latitude: number; longitude: number } | null;
  isInitiator: boolean;
  onUpdateLocation: (latitude: number, longitude: number) => Promise<void>;
  loading?: boolean;
  error?: string | null;
}

export const MeetingLocationSection = ({
  meetingLocation,
  isInitiator,
  onUpdateLocation,
  loading = false,
  error = null,
}: MeetingLocationSectionProps) => {
  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const [latitude, setLatitude] = useState<string>('');
  const [longitude, setLongitude] = useState<string>('');
  const [validationError, setValidationError] = useState<string | null>(null);

  const handleOpenEditDialog = () => {
    if (meetingLocation) {
      setLatitude(meetingLocation.latitude.toString());
      setLongitude(meetingLocation.longitude.toString());
    } else {
      setLatitude('');
      setLongitude('');
    }
    setValidationError(null);
    setEditDialogOpen(true);
  };

  const handleCloseEditDialog = () => {
    setEditDialogOpen(false);
    setValidationError(null);
  };

  const validateCoordinates = (lat: string, lon: string): string | null => {
    const latNum = parseFloat(lat);
    const lonNum = parseFloat(lon);

    if (isNaN(latNum) || isNaN(lonNum)) {
      return 'Please enter valid numbers for latitude and longitude';
    }

    if (latNum < -90 || latNum > 90) {
      return 'Latitude must be between -90 and 90';
    }

    if (lonNum < -180 || lonNum > 180) {
      return 'Longitude must be between -180 and 180';
    }

    return null;
  };

  const handleSaveLocation = async () => {
    const validation = validateCoordinates(latitude, longitude);
    if (validation) {
      setValidationError(validation);
      return;
    }

    try {
      await onUpdateLocation(parseFloat(latitude), parseFloat(longitude));
      setEditDialogOpen(false);
      setValidationError(null);
    } catch (err) {
      // Error is handled by parent component
    }
  };

  const handleUseCurrentLocation = () => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          setLatitude(position.coords.latitude.toFixed(6));
          setLongitude(position.coords.longitude.toFixed(6));
          setValidationError(null);
        },
        (err) => {
          setValidationError('Failed to get current location: ' + err.message);
        }
      );
    } else {
      setValidationError('Geolocation is not supported by your browser');
    }
  };

  return (
    <>
      <Paper elevation={2} sx={{ p: 2, mb: 3 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <LocationOn color="primary" />
            <Typography variant="h6">Meeting Location</Typography>
          </Box>
          {isInitiator && (
            <Tooltip title="Edit meeting location">
              <IconButton
                color="primary"
                onClick={handleOpenEditDialog}
                disabled={loading}
                size="small"
              >
                <Edit />
              </IconButton>
            </Tooltip>
          )}
        </Box>

        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        {meetingLocation ? (
          <Box>
            <Typography variant="body1" sx={{ mb: 1 }}>
              <strong>Latitude:</strong> {meetingLocation.latitude.toFixed(6)}
            </Typography>
            <Typography variant="body1">
              <strong>Longitude:</strong> {meetingLocation.longitude.toFixed(6)}
            </Typography>
          </Box>
        ) : (
          <Typography variant="body2" color="text.secondary">
            {isInitiator
              ? 'No meeting location set. Click the edit button to set a location.'
              : 'Meeting location has not been set by the initiator.'}
          </Typography>
        )}
      </Paper>

      <Dialog open={editDialogOpen} onClose={handleCloseEditDialog} maxWidth="sm" fullWidth>
        <DialogTitle>Edit Meeting Location</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
            {validationError && (
              <Alert severity="error">{validationError}</Alert>
            )}

            <TextField
              label="Latitude"
              type="number"
              value={latitude}
              onChange={(e) => setLatitude(e.target.value)}
              fullWidth
              inputProps={{ step: 'any', min: -90, max: 90 }}
              helperText="Enter latitude between -90 and 90"
            />

            <TextField
              label="Longitude"
              type="number"
              value={longitude}
              onChange={(e) => setLongitude(e.target.value)}
              fullWidth
              inputProps={{ step: 'any', min: -180, max: 180 }}
              helperText="Enter longitude between -180 and 180"
            />

            <Button
              variant="outlined"
              onClick={handleUseCurrentLocation}
              startIcon={<LocationOn />}
              fullWidth
            >
              Use Current Location
            </Button>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseEditDialog} startIcon={<Close />}>
            Cancel
          </Button>
          <Button
            onClick={handleSaveLocation}
            variant="contained"
            startIcon={<Check />}
            disabled={loading || !latitude || !longitude}
          >
            {loading ? 'Saving...' : 'Save'}
          </Button>
        </DialogActions>
      </Dialog>
    </>
  );
};

