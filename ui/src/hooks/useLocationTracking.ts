import { useState, useRef, useEffect } from 'react';
import { sessionApi } from '../services/api';

export const useLocationTracking = (sessionId: string | undefined) => {
  const [locationEnabled, setLocationEnabled] = useState(false);
  const [locationError, setLocationError] = useState<string | null>(null);
  const [currentLocation, setCurrentLocation] = useState<GeolocationPosition | null>(null);
  const [updatingLocation, setUpdatingLocation] = useState(false);
  const [showManualInput, setShowManualInput] = useState(false);
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
    
    // Check if error is due to insecure origin (HTTP instead of HTTPS)
    const isSecureOrigin = 
      window.location.protocol === 'https:' || 
      window.location.hostname === 'localhost' || 
      window.location.hostname === '127.0.0.1';
    
    if (error.code === error.PERMISSION_DENIED && 
        error.message.includes('secure origins') && 
        !isSecureOrigin) {
      errorMessage = 
        'Geolocation requires HTTPS. Please access the site using HTTPS (https://) instead of HTTP. ' +
        'You can manually enter your location coordinates instead.';
      if (isInitialRequest) {
        setLocationEnabled(false);
        setShowManualInput(true);
      }
    } else {
      switch (error.code) {
        case error.PERMISSION_DENIED:
          errorMessage = 'Location permission denied. Please enable location access in your browser settings, or manually enter your location.';
          if (isInitialRequest) {
            setLocationEnabled(false);
            setShowManualInput(true);
          }
          break;
        case error.POSITION_UNAVAILABLE:
          errorMessage = 'Location information is unavailable. This may happen if you are indoors or GPS signal is weak. You can manually enter your location instead.';
          if (isInitialRequest) {
            setShowManualInput(true);
          }
          break;
        case error.TIMEOUT:
          errorMessage = 'Location request timed out. You can manually enter your location instead.';
          if (isInitialRequest) {
            setShowManualInput(true);
          }
          break;
        default:
          errorMessage = `Unable to get location (error code: ${error.code}). You can manually enter your location instead.`;
          if (isInitialRequest) {
            setShowManualInput(true);
          }
          break;
      }
    }
    
    setLocationError(errorMessage);
    console.warn('Geolocation error:', {
      code: error.code,
      message: error.message,
      isInitialRequest,
      protocol: window.location.protocol,
      hostname: window.location.hostname,
    });
  };

  const startLocationTracking = () => {
    if (!navigator.geolocation) {
      setLocationError('Geolocation is not supported by your browser. Please manually enter your location.');
      setShowManualInput(true);
      return;
    }

    // Check if the context is secure (HTTPS)
    if (typeof window !== 'undefined' && !window.isSecureContext) {
      handleGeolocationError({
        code: GeolocationPositionError.PERMISSION_DENIED,
        message: 'Only secure origins are allowed.',
        PERMISSION_DENIED: GeolocationPositionError.PERMISSION_DENIED,
        POSITION_UNAVAILABLE: GeolocationPositionError.POSITION_UNAVAILABLE,
        TIMEOUT: GeolocationPositionError.TIMEOUT,
      }, true);
      return;
    }

    setLocationError(null);
    setShowManualInput(false);

    navigator.geolocation.getCurrentPosition(
      (position) => {
        setCurrentLocation(position);
        updateLocationToServer(position);
        setLocationEnabled(true);
        setShowManualInput(false);

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

  // 手动设置位置
  const setManualLocation = async (latitude: number, longitude: number) => {
    if (!sessionId) {
      setLocationError('Session ID is required');
      return;
    }

    try {
      setUpdatingLocation(true);
      setLocationError(null);
      
      const manualPosition = {
        coords: {
          latitude,
          longitude,
          accuracy: 100,
          altitude: null,
          altitudeAccuracy: null,
          heading: null,
          speed: null,
          toJSON: () => ({}),
        } as GeolocationCoordinates,
        timestamp: Date.now(),
        toJSON: () => ({}),
      } as GeolocationPosition;
      
      setCurrentLocation(manualPosition);
      await updateLocationToServer(manualPosition);
      setLocationEnabled(true);
      setShowManualInput(false);
    } catch (err: any) {
      console.error('Failed to set manual location:', err);
      setLocationError('Failed to set location. Please try again.');
    } finally {
      setUpdatingLocation(false);
    }
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
    setShowManualInput(false);
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
    showManualInput,
    handleLocationToggle,
    startLocationTracking,
    stopLocationTracking,
    setManualLocation,
    setShowManualInput,
  };
};

