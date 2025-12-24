import { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Switch,
  FormControlLabel,
  Alert,
  Button,
  CircularProgress,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
} from '@mui/material';
import { LocationOn, MyLocation, EditLocation, Search } from '@mui/icons-material';
import { forwardGeocode } from '../../services/geocodingService';

interface LocationTrackingSectionProps {
  locationEnabled: boolean;
  locationError: string | null;
  currentLocation: GeolocationPosition | null;
  updatingLocation: boolean;
  showManualInput?: boolean;
  sessionStatus?: string;
  onToggle: (event: React.ChangeEvent<HTMLInputElement>) => void;
  onRetry: () => void;
  onSetManualLocation?: (latitude: number, longitude: number) => void;
  onCloseManualInput?: () => void;
}

export const LocationTrackingSection = ({
  locationEnabled,
  locationError,
  currentLocation,
  updatingLocation,
  showManualInput = false,
  sessionStatus,
  onToggle,
  onRetry,
  onSetManualLocation,
  onCloseManualInput,
}: LocationTrackingSectionProps) => {
  const isSessionEnded = sessionStatus === 'Ended';
  const [manualDialogOpen, setManualDialogOpen] = useState(false);
  const [addressSearch, setAddressSearch] = useState('');
  const [searching, setSearching] = useState(false);
  const [validationError, setValidationError] = useState<string | null>(null);

  // Close manual input dialog when session ends
  useEffect(() => {
    if (isSessionEnded && manualDialogOpen) {
      handleCloseManualDialog();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isSessionEnded]);

  const handleOpenManualDialog = () => {
    if (isSessionEnded) {
      return;
    }
    setAddressSearch('');
    setValidationError(null);
    setManualDialogOpen(true);
  };

  const handleCloseManualDialog = () => {
    setManualDialogOpen(false);
    setValidationError(null);
    if (onCloseManualInput) {
      onCloseManualInput();
    }
  };

  const handleSearchAddress = async () => {
    if (!addressSearch || addressSearch.trim().length === 0) {
      setValidationError('Please enter an address to search.');
      return;
    }

    setSearching(true);
    setValidationError(null);

    try {
      const coordinates = await forwardGeocode(addressSearch.trim());
      
      if (coordinates) {
        if (onSetManualLocation) {
          onSetManualLocation(coordinates.latitude, coordinates.longitude);
          handleCloseManualDialog();
        }
      } else {
        setValidationError('Address not found. Please try a different address or be more specific.');
      }
    } catch (error: any) {
      console.error('Address search error:', error);
      setValidationError('Failed to search address. Please try again.');
    } finally {
      setSearching(false);
    }
  };

  const handleAddressSearchChange = async (value: string) => {
    setAddressSearch(value);
    setValidationError(null);

    // Optional: Auto-search when user stops typing (debounced)
    // For now, we'll require explicit search button click
  };

  const handleUseCurrentLocationInDialog = () => {
    if (navigator.geolocation) {
      const isSecureOrigin =
        window.location.protocol === 'https:' ||
        window.location.hostname === 'localhost' ||
        window.location.hostname === '127.0.0.1';

      if (!isSecureOrigin) {
        setValidationError(
          'Geolocation requires HTTPS. Please access the site using HTTPS (https://) instead of HTTP.',
        );
        return;
      }

      setSearching(true);
      setValidationError(null);

      navigator.geolocation.getCurrentPosition(
        async (position) => {
          const lat = position.coords.latitude;
          const lon = position.coords.longitude;
          
          if (onSetManualLocation) {
            onSetManualLocation(lat, lon);
            handleCloseManualDialog();
          }
        },
        (err) => {
          setValidationError(`Failed to get current location: ${err.message}`);
          setSearching(false);
        },
      );
    } else {
      setValidationError('Geolocation is not supported by your browser');
    }
  };

  return (
    <>
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
                disabled={updatingLocation || isSessionEnded || manualDialogOpen || searching}
              />
            }
            label={locationEnabled ? 'Enabled' : 'Disabled'}
          />
        </Box>
        {locationError && (
          <Alert
            severity={locationError.includes('permission denied') || locationError.includes('HTTPS') ? 'error' : 'warning'}
            sx={{ mb: 2 }}
            action={
              <Box sx={{ display: 'flex', gap: 1 }}>
                {locationError.includes('permission denied') && (
                  <Button color="inherit" size="small" onClick={onRetry}>
                    Retry
                  </Button>
                )}
                {(showManualInput || locationError.includes('manually') || locationError.includes('HTTPS')) && !isSessionEnded && (
                  <Button
                    color="inherit"
                    size="small"
                    startIcon={<Search />}
                    onClick={handleOpenManualDialog}
                  >
                    Search Location
                  </Button>
                )}
              </Box>
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
            {!isSessionEnded && (
              <Button
                size="small"
                startIcon={<EditLocation />}
                onClick={handleOpenManualDialog}
                sx={{ ml: 2 }}
              >
                Update Location
              </Button>
            )}
          </Alert>
        )}
        {locationEnabled && !currentLocation && (
          <Alert severity="info" sx={{ mb: 2 }}>
            Getting your location...
            {(showManualInput || locationError) && !isSessionEnded && (
              <Button
                size="small"
                startIcon={<Search />}
                onClick={handleOpenManualDialog}
                sx={{ ml: 2 }}
              >
                Search Location
              </Button>
            )}
          </Alert>
        )}
        {!locationEnabled && !isSessionEnded && (
          <Button
            variant="outlined"
            startIcon={<Search />}
            onClick={handleOpenManualDialog}
            sx={{ mt: 1 }}
          >
            Search Location
          </Button>
        )}
      </Box>

      <Dialog open={manualDialogOpen && !isSessionEnded} onClose={handleCloseManualDialog} maxWidth="sm" fullWidth>
        <DialogTitle>Search Location</DialogTitle>
        <DialogContent>
          {validationError && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {validationError}
            </Alert>
          )}
          <Box sx={{ display: 'flex', gap: 1, mb: 2 }}>
            <TextField
              autoFocus
              margin="dense"
              id="address-search"
              label="Search Address"
              placeholder="e.g., Orchard Road, Singapore"
              fullWidth
              value={addressSearch}
              onChange={(e) => handleAddressSearchChange(e.target.value)}
              onKeyPress={(e) => {
                if (e.key === 'Enter' && !searching) {
                  handleSearchAddress();
                }
              }}
              helperText="Enter an address, place name, or landmark"
              disabled={searching}
            />
            <Button
              variant="contained"
              startIcon={<Search />}
              onClick={handleSearchAddress}
              disabled={searching || !addressSearch.trim()}
              sx={{ mt: 1, mb: 3, minWidth: 100 }}
            >
              {searching ? <CircularProgress size={20} /> : 'Search'}
            </Button>
          </Box>
          <Button
            startIcon={<MyLocation />}
            onClick={handleUseCurrentLocationInDialog}
            variant="outlined"
            fullWidth
            disabled={searching}
          >
            Use Current Location (if available)
          </Button>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseManualDialog} disabled={searching}>
            Cancel
          </Button>
        </DialogActions>
      </Dialog>
    </>
  );
};

