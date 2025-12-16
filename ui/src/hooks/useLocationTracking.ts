import { useState, useRef, useEffect } from 'react';
import { sessionApi } from '../services/api';

export const useLocationTracking = (sessionId: string | undefined) => {
  const [locationEnabled, setLocationEnabled] = useState(false);
  const [locationError, setLocationError] = useState<string | null>(null);
  const [currentLocation, setCurrentLocation] = useState<GeolocationPosition | null>(null);
  const [updatingLocation, setUpdatingLocation] = useState(false);
  const watchIdRef = useRef<number | null>(null);
  const locationUpdateIntervalRef = useRef<ReturnType<typeof setInterval> | null>(null);
  const latestPositionRef = useRef<GeolocationPosition | null>(null);

  const updateLocationToServer = async (position: GeolocationPosition) => {
    if (!sessionId) {
      return;
    }

    try {
      setUpdatingLocation(true);
      setLocationError(null);
      await sessionApi.updateLocation(sessionId, {
        latitude: position.coords.latitude,
        longitude: position.coords.longitude,
        accuracy: position.coords.accuracy,
      });
      setCurrentLocation(position);
    } catch (err: any) {
      console.error('Failed to update location:', err);
      setLocationError('Failed to update location. Please try again.');
    } finally {
      setUpdatingLocation(false);
    }
  };

  const handleGeolocationError = (error: GeolocationPositionError, isInitialRequest: boolean = false) => {
    let errorMessage = '';
    
    switch (error.code) {
      case error.PERMISSION_DENIED:
        errorMessage = 'Location permission denied. Please enable location access in your browser settings.';
        if (isInitialRequest) {
          setLocationEnabled(false);
        }
        break;
      case error.POSITION_UNAVAILABLE:
        errorMessage = 'Location information is unavailable. This may happen if you are indoors or GPS signal is weak. The app will keep trying to get your location.';
        break;
      case error.TIMEOUT:
        errorMessage = 'Location request timed out. The app will keep trying to get your location.';
        break;
      default:
        errorMessage = `Unable to get location (error code: ${error.code}). The app will keep trying.`;
        break;
    }
    
    setLocationError(errorMessage);
    console.warn('Geolocation error:', {
      code: error.code,
      message: error.message,
      isInitialRequest,
    });
  };

  const startLocationTracking = () => {
    if (!navigator.geolocation) {
      setLocationError('Geolocation is not supported by your browser.');
      return;
    }

    setLocationError(null);

    navigator.geolocation.getCurrentPosition(
      (position) => {
        setCurrentLocation(position);
        updateLocationToServer(position);
        setLocationEnabled(true);

        watchIdRef.current = navigator.geolocation.watchPosition(
          (position) => {
            setCurrentLocation(position);
            latestPositionRef.current = position;
            setLocationError(null);
            if (!locationUpdateIntervalRef.current) {
              updateLocationToServer(position);
              locationUpdateIntervalRef.current = setInterval(() => {
                if (latestPositionRef.current) {
                  updateLocationToServer(latestPositionRef.current);
                }
              }, 5000);
            }
          },
          (error) => {
            handleGeolocationError(error, false);
          },
          {
            enableHighAccuracy: true,
            timeout: 15000,
            maximumAge: 30000,
          },
        );
      },
      (error) => {
        console.error('Geolocation error (initial request):', error);
        handleGeolocationError(error, true);
        if (error.code === error.PERMISSION_DENIED) {
          setLocationEnabled(false);
        }
      },
      {
        enableHighAccuracy: true,
        timeout: 15000,
        maximumAge: 30000,
      },
    );
  };

  const stopLocationTracking = () => {
    if (watchIdRef.current !== null) {
      navigator.geolocation.clearWatch(watchIdRef.current);
      watchIdRef.current = null;
    }
    if (locationUpdateIntervalRef.current) {
      clearInterval(locationUpdateIntervalRef.current);
      locationUpdateIntervalRef.current = null;
    }
    setLocationEnabled(false);
    setCurrentLocation(null);
    latestPositionRef.current = null;
  };

  const handleLocationToggle = (event: React.ChangeEvent<HTMLInputElement>) => {
    if (event.target.checked) {
      startLocationTracking();
    } else {
      stopLocationTracking();
    }
  };

  useEffect(() => {
    return () => {
      stopLocationTracking();
    };
  }, []);

  return {
    locationEnabled,
    locationError,
    currentLocation,
    updatingLocation,
    handleLocationToggle,
    startLocationTracking,
    stopLocationTracking,
  };
};

