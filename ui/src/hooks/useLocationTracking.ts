import { useState, useRef, useEffect } from 'react';
import { sessionApi } from '../services/api';
import { calculateHaversineDistance } from '../utils/distanceCalculator';

export const useLocationTracking = (sessionId: string | undefined, sessionStatus?: string) => {
  const [locationEnabled, setLocationEnabled] = useState(false);
  const [locationError, setLocationError] = useState<string | null>(null);
  const [currentLocation, setCurrentLocation] = useState<GeolocationPosition | null>(null);
  const [updatingLocation, setUpdatingLocation] = useState(false);
  const [showManualInput, setShowManualInput] = useState(false);
  const watchIdRef = useRef<number | null>(null);
  const locationUpdateIntervalRef = useRef<ReturnType<typeof setInterval> | null>(null);
  const latestPositionRef = useRef<GeolocationPosition | null>(null);
  const lastSavedLocationRef = useRef<{ latitude: number; longitude: number } | null>(null);
  const MIN_DISTANCE_METERS = 10; // 最小距离阈值（米）
  const MIN_DISTANCE_KM = MIN_DISTANCE_METERS / 1000; // 转换为公里

  const updateLocationToServer = async (position: GeolocationPosition, forceUpdate: boolean = false) => {
    if (!sessionId) {
      return;
    }

    // 如果 session 已结束，不更新位置
    if (sessionStatus === 'Ended') {
      return;
    }

    const newLat = position.coords.latitude;
    const newLon = position.coords.longitude;

    // 检查距离：如果上次保存的位置存在，且距离小于阈值，则跳过更新
    if (!forceUpdate && lastSavedLocationRef.current) {
      const distance = calculateHaversineDistance(
        lastSavedLocationRef.current.latitude,
        lastSavedLocationRef.current.longitude,
        newLat,
        newLon,
      );
      
      if (distance < MIN_DISTANCE_KM) {
        // 距离小于10米，不更新服务器，但更新本地显示
        setCurrentLocation(position);
        return;
      }
    }

    try {
      setUpdatingLocation(true);
      setLocationError(null);
      await sessionApi.updateLocation(sessionId, {
        latitude: newLat,
        longitude: newLon,
        accuracy: position.coords.accuracy,
      });
      setCurrentLocation(position);
      // 保存成功发送的位置
      lastSavedLocationRef.current = {
        latitude: newLat,
        longitude: newLon,
      };
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
    // 如果 session 已结束，不允许启动位置跟踪
    if (sessionStatus === 'Ended') {
      setLocationError('Cannot start location tracking for an ended session');
      return;
    }

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
        setLocationEnabled(true);
        setShowManualInput(false);

        watchIdRef.current = navigator.geolocation.watchPosition(
          (position) => {
            setCurrentLocation(position);
            latestPositionRef.current = position;
            setLocationError(null);
            if (!locationUpdateIntervalRef.current) {
              // 首次获取位置时强制更新（forceUpdate = true），避免重复调用
              updateLocationToServer(position, true);
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

  // enableTracking: false
  const setManualLocation = async (latitude: number, longitude: number, enableTracking: boolean = false) => {
    if (!sessionId) {
      setLocationError('Session ID is required');
      return;
    }

    // 如果 session 已结束，不允许手动设置位置
    if (sessionStatus === 'Ended') {
      setLocationError('Cannot update location for an ended session');
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
      // 手动设置位置时强制更新（forceUpdate = true）
      await updateLocationToServer(manualPosition, true);
      // 只有在明确要求启用跟踪时才启用，搜索位置时不自动启用跟踪 toggle
      if (enableTracking) {
        setLocationEnabled(true);
      }
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
    setShowManualInput(false);
  };

  const handleLocationToggle = (event: React.ChangeEvent<HTMLInputElement>) => {
    if (event.target.checked) {
      startLocationTracking();
    } else {
      stopLocationTracking();
    }
  };

  // 当 session 状态变为 'Ended' 时，自动停止位置跟踪（但保留最后的位置）
  useEffect(() => {
    if (sessionStatus === 'Ended' && locationEnabled) {
      if (watchIdRef.current !== null) {
        navigator.geolocation.clearWatch(watchIdRef.current);
        watchIdRef.current = null;
      }
      if (locationUpdateIntervalRef.current) {
        clearInterval(locationUpdateIntervalRef.current);
        locationUpdateIntervalRef.current = null;
      }
      setLocationEnabled(false);
      // 不清除 currentLocation，保留最后的位置显示
      latestPositionRef.current = null;
      // 不清除 lastSavedLocationRef，保留位置记录
      setShowManualInput(false);
    }
  }, [sessionStatus, locationEnabled]);

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

