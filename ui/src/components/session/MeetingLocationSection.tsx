import { useState, useMemo } from 'react';
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
import { Edit, LocationOn, Check, Close, Search } from '@mui/icons-material';
import { calculateHaversineDistance, formatDistance } from '../../utils/distanceCalculator';
import { forwardGeocode } from '../../services/geocodingService';

interface MeetingLocationSectionProps {
  meetingLocation: { latitude: number; longitude: number } | null;
  meetingLocationAddress?: string | null;
  loadingAddress?: boolean;
  currentUserLocation?: { latitude: number; longitude: number } | null;
  isInitiator: boolean;
  onUpdateLocation: (latitude: number, longitude: number) => Promise<void>;
  loading?: boolean;
  error?: string | null;
}

export const MeetingLocationSection = ({
  meetingLocation,
  meetingLocationAddress,
  loadingAddress = false,
  currentUserLocation,
  isInitiator,
  onUpdateLocation,
  loading = false,
  error = null,
}: MeetingLocationSectionProps) => {
  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const [latitude, setLatitude] = useState<string>('');
  const [longitude, setLongitude] = useState<string>('');
  const [addressSearch, setAddressSearch] = useState<string>('');
  const [searching, setSearching] = useState<boolean>(false);
  const [validationError, setValidationError] = useState<string | null>(null);

  const handleOpenEditDialog = () => {
    if (meetingLocation) {
      setLatitude(meetingLocation.latitude.toString());
      setLongitude(meetingLocation.longitude.toString());
    } else {
      setLatitude('');
      setLongitude('');
    }
    setAddressSearch('');
    setValidationError(null);
    setEditDialogOpen(true);
  };

  const handleCloseEditDialog = () => {
    setEditDialogOpen(false);
    setValidationError(null);
    setAddressSearch('');
  };

  const handleSearchAddress = async () => {
    if (!addressSearch || addressSearch.trim().length === 0) {
      setValidationError('Please enter a location name or address to search.');
      return;
    }

    setSearching(true);
    setValidationError(null);

    try {
      const coordinates = await forwardGeocode(addressSearch.trim());
      
      if (coordinates) {
        setLatitude(coordinates.latitude.toFixed(6));
        setLongitude(coordinates.longitude.toFixed(6));
        setValidationError(null);
      } else {
        setValidationError('Location not found. Please try a different location name or be more specific.');
      }
    } catch (error: any) {
      console.error('Address search error:', error);
      setValidationError('Failed to search location. Please try again.');
    } finally {
      setSearching(false);
    }
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
      const isSecureOrigin = 
        window.location.protocol === 'https:' || 
        window.location.hostname === 'localhost' || 
        window.location.hostname === '127.0.0.1';
      
      if (!isSecureOrigin) {
        setValidationError(
          'Geolocation requires HTTPS. Please access the site using HTTPS (https://) instead of HTTP. ' +
          'Alternatively, you can manually enter your location coordinates.',
        );
        return;
      }
      
      navigator.geolocation.getCurrentPosition(
        (position) => {
          setLatitude(position.coords.latitude.toFixed(6));
          setLongitude(position.coords.longitude.toFixed(6));
          setValidationError(null);
        },
        (err) => {
          if (err.code === err.PERMISSION_DENIED && err.message.includes('secure origins')) {
            setValidationError(
              'Geolocation requires HTTPS. Please access the site using HTTPS (https://) instead of HTTP. ' +
              'Alternatively, you can manually enter your location coordinates.',
            );
          } else {
            setValidationError(`Failed to get current location: ${err.message}`);
          }
        },
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
              <span>
                <IconButton
                  color="primary"
                  onClick={handleOpenEditDialog}
                  disabled={loading}
                  size="small"
                  aria-label="Edit meeting location"
                >
                  <Edit />
                </IconButton>
              </span>
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
            {meetingLocationAddress ? (
              <Typography variant="body1" sx={{ mb: 1 }}>
                <strong>Address:</strong> {meetingLocationAddress}
              </Typography>
            ) : loadingAddress ? (
              <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                Loading address...
              </Typography>
            ) : null}
            <Typography variant="body2" color="text.secondary" sx={{ mb: 0.5 }}>
              <strong>Coordinates:</strong>{' '}
              {meetingLocation.latitude.toFixed(6)}, {meetingLocation.longitude.toFixed(6)}
            </Typography>
            {currentUserLocation && (
              <DistanceDisplay
                currentLocation={currentUserLocation}
                meetingLocation={meetingLocation}
              />
            )}
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

            <Box sx={{ display: 'flex', gap: 1, alignItems: 'flex-start' }}>
              <TextField
                label="Search Location Name or Address"
                value={addressSearch}
                onChange={(e) => {
                  setAddressSearch(e.target.value);
                  setValidationError(null);
                }}
                fullWidth
                placeholder="e.g., Central Park, New York or 1600 Amphitheatre Parkway, Mountain View"
                helperText="Enter a location name or address to search"
                onKeyPress={(e) => {
                  if (e.key === 'Enter' && !searching) {
                    handleSearchAddress();
                  }
                }}
              />
              <Button
                variant="contained"
                onClick={handleSearchAddress}
                disabled={searching || !addressSearch.trim()}
                startIcon={<Search />}
                sx={{ mt: 0.5, minWidth: 100 }}
              >
                {searching ? 'Searching...' : 'Search'}
              </Button>
            </Box>

            <Box sx={{ display: 'flex', gap: 1, alignItems: 'center', my: 1 }}>
              <Box sx={{ flex: 1, height: 1, bgcolor: 'divider' }} />
              <Typography variant="body2" color="text.secondary" sx={{ px: 1 }}>
                OR
              </Typography>
              <Box sx={{ flex: 1, height: 1, bgcolor: 'divider' }} />
            </Box>

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

/**
 * Component to display distance from current user location to meeting location
 */
interface DistanceDisplayProps {
  currentLocation: { latitude: number; longitude: number };
  meetingLocation: { latitude: number; longitude: number };
}

const DistanceDisplay = ({ currentLocation, meetingLocation }: DistanceDisplayProps) => {
  const distance = useMemo(() => {
    return calculateHaversineDistance(
      currentLocation.latitude,
      currentLocation.longitude,
      meetingLocation.latitude,
      meetingLocation.longitude,
    );
  }, [currentLocation.latitude, currentLocation.longitude, meetingLocation.latitude, meetingLocation.longitude]);

  return (
    <Typography variant="body2" color="primary" sx={{ mt: 1, fontWeight: 'medium' }}>
      <LocationOn sx={{ fontSize: 16, verticalAlign: 'middle', mr: 0.5 }} />
      Distance from your location: {formatDistance(distance)}
    </Typography>
  );
};

