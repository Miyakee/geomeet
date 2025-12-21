import { useEffect, useState, useMemo } from 'react';
import { Box, Typography, Paper, CircularProgress } from '@mui/material';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet';

// Fix for default marker icons in React-Leaflet
import icon from 'leaflet/dist/images/marker-icon.png';
import iconShadow from 'leaflet/dist/images/marker-shadow.png';
import iconRetina from 'leaflet/dist/images/marker-icon-2x.png';

const DefaultIcon = L.icon({
  iconUrl: icon,
  shadowUrl: iconShadow,
  iconRetinaUrl: iconRetina,
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  tooltipAnchor: [16, -28],
  shadowSize: [41, 41],
});

L.Marker.prototype.options.icon = DefaultIcon;

// Custom icon for optimal location
const OptimalLocationIcon = L.divIcon({
  className: 'custom-marker-icon',
  html: `<div style="
    background-color: #facd07;
    width: 30px;
    height: 30px;
    border-radius: 50% 50% 50% 0;
    transform: rotate(-45deg);
    border: 3px solid #ffffff;
    box-shadow: 0 2px 4px rgba(0,0,0,0.3);
    display: flex;
    align-items: center;
    justify-content: center;
  "></div>`,
  iconSize: [30, 30],
  iconAnchor: [15, 30],
  popupAnchor: [0, -30],
});

// Custom icon for current user location (dark blue)
const CurrentUserIcon = L.divIcon({
  className: 'custom-marker-icon',
  html: `<div style="
    background-color: #1565c0;
    width: 30px;
    height: 30px;
    border-radius: 50% 50% 50% 0;
    transform: rotate(-45deg);
    border: 3px solid #ffffff;
    box-shadow: 0 2px 4px rgba(0,0,0,0.3);
  "></div>`,
  iconSize: [30, 30],
  iconAnchor: [15, 30],
  popupAnchor: [0, -30],
});

// Custom icon for participant locations (light blue)
const ParticipantIcon = L.divIcon({
  className: 'custom-marker-icon',
  html: `<div style="
    background-color: #64b5f6;
    width: 30px;
    height: 30px;
    border-radius: 50% 50% 50% 0;
    transform: rotate(-45deg);
    border: 3px solid #ffffff;
    box-shadow: 0 2px 4px rgba(0,0,0,0.3);
  "></div>`,
  iconSize: [30, 30],
  iconAnchor: [15, 30],
  popupAnchor: [0, -30],
});

// Custom icon for meeting location (set by initiator)
// Create a red marker icon for meeting location
const MeetingLocationIcon = L.divIcon({
  className: 'custom-marker-icon',
  html: `<div style="
    background-color: #d32f2f;
    width: 30px;
    height: 30px;
    border-radius: 50% 50% 50% 0;
    transform: rotate(-45deg);
    border: 3px solid #ffffff;
    box-shadow: 0 2px 4px rgba(0,0,0,0.3);
  "></div>`,
  iconSize: [30, 30],
  iconAnchor: [15, 30],
  popupAnchor: [0, -30],
});

interface ParticipantLocationData {
  latitude: number;
  longitude: number;
  userId: number;
  username?: string;
}

interface OptimalLocationMapProps {
  optimalLocation: {
    latitude: number;
    longitude: number;
    totalTravelDistance?: number;
    participantCount?: number;
  } | null;
  participantLocations: Map<number, ParticipantLocationData>;
  participantNames: Map<number, string>;
  currentUserLocation?: {
    latitude: number;
    longitude: number;
    userId: number;
  } | null;
  meetingLocation?: {
    latitude: number;
    longitude: number;
  } | null;
  meetingLocationAddress?: string | null;
}

// Component to automatically fit map bounds to show all markers
function MapBoundsFitter({ locations }: { locations: Array<[number, number]> }) {
  const map = useMap();

  // Create a stable string representation of locations for comparison
  const locationsKey = useMemo(
    () => locations.map(([lat, lng]) => `${lat.toFixed(6)},${lng.toFixed(6)}`).join('|'),
    [locations]
  );

  useEffect(() => {
    if (locations.length === 0) {
      return;
    }

    // Small delay to ensure map is fully rendered
    const timeoutId = setTimeout(() => {
      // If only one location, just center on it
      if (locations.length === 1) {
        map.setView(locations[0], 13);
        return;
      }

      // Calculate bounds to fit all locations
      const bounds = L.latLngBounds(locations);
      
      // Fit bounds with padding to ensure markers are not at the edge
      map.fitBounds(bounds, {
        padding: [50, 50], // Add 50px padding on all sides
        maxZoom: 15, // Limit max zoom to prevent too close view
      });
    }, 100);

    return () => clearTimeout(timeoutId);
  }, [map, locationsKey, locations]);

  return null;
}

export const OptimalLocationMap = ({
  optimalLocation,
  participantLocations,
  participantNames,
  currentUserLocation,
  meetingLocation,
  meetingLocationAddress,
}: OptimalLocationMapProps) => {
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
  }, []);

  // Default center: prioritize meeting location, then optimal location, then current user location, then Singapore
  const defaultCenter: [number, number] = [1.3521, 103.8198];
  const center: [number, number] = meetingLocation
    ? [meetingLocation.latitude, meetingLocation.longitude]
    : optimalLocation
      ? [optimalLocation.latitude, optimalLocation.longitude]
      : currentUserLocation
        ? [currentUserLocation.latitude, currentUserLocation.longitude]
        : defaultCenter;

  // Calculate bounds to fit all markers (including current user location)
  // Note: If meeting location is set, we don't show optimal location (meeting location takes priority)
  // Also exclude current user from participantLocations to avoid duplicate markers
  const allLocations: Array<[number, number]> = [];
  if (meetingLocation) {
    allLocations.push([meetingLocation.latitude, meetingLocation.longitude]);
  }
  if (currentUserLocation) {
    allLocations.push([currentUserLocation.latitude, currentUserLocation.longitude]);
  }
  // Only include optimal location if meeting location is not set
  if (optimalLocation && !meetingLocation) {
    allLocations.push([optimalLocation.latitude, optimalLocation.longitude]);
  }
  // Exclude current user from participant locations to avoid duplicate markers
  const currentUserId = currentUserLocation?.userId;
  participantLocations.forEach((location, userId) => {
    if (userId !== currentUserId) {
      allLocations.push([location.latitude, location.longitude]);
    }
  });

  if (!mounted || typeof window === 'undefined') {
    return (
      <Paper elevation={2} sx={{ p: 2, mb: 3 }}>
        <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '400px' }}>
          <CircularProgress size={24} />
          <Typography variant="body2" color="text.secondary" sx={{ ml: 2 }}>
            Loading map...
          </Typography>
        </Box>
      </Paper>
    );
  }

  if (allLocations.length === 0) {
    return (
      <Paper elevation={2} sx={{ p: 2, mb: 3 }}>
        <Typography variant="body2" color="text.secondary" align="center">
          No locations available to display on map
        </Typography>
      </Paper>
    );
  }

  return (
    <Box sx={{ mb: 3 }}>
      <Typography variant="h6" gutterBottom>
        Meeting Location Map
      </Typography>
      <Paper elevation={2} sx={{ overflow: 'hidden', borderRadius: 2 }}>
        <MapContainer
          center={center}
          zoom={allLocations.length > 1 ? 12 : 13}
          style={{ height: '400px', width: '100%', zIndex: 0 }}
          scrollWheelZoom={true}
        >
          <TileLayer
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          />
          
          {/* Auto-fit bounds to show all markers */}
          <MapBoundsFitter locations={allLocations} />

          {/* Meeting location marker (set by initiator) */}
          {meetingLocation && (
            <Marker
              position={[meetingLocation.latitude, meetingLocation.longitude]}
              icon={MeetingLocationIcon}
            >
              <Popup>
                <Box>
                  <Typography variant="subtitle2" fontWeight="bold" color="error">
                    Meeting Location
                  </Typography>
                  {meetingLocationAddress ? (
                    <Typography variant="body2" sx={{ mt: 0.5, mb: 0.5 }}>
                      {meetingLocationAddress}
                    </Typography>
                  ) : null}
                  <Typography variant="body2" color="text.secondary">
                    {meetingLocation.latitude.toFixed(6)}, {meetingLocation.longitude.toFixed(6)}
                  </Typography>
                  <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
                    Set by session initiator
                  </Typography>
                </Box>
              </Popup>
            </Marker>
          )}

          {/* Optimal location marker - only show if meeting location is not set */}
          {optimalLocation && !meetingLocation && (
            <Marker
              position={[optimalLocation.latitude, optimalLocation.longitude]}
              icon={OptimalLocationIcon}
            >
              <Popup>
                <Box>
                  <Typography variant="subtitle2" fontWeight="bold">
                    Optimal Meeting Location
                  </Typography>
                  <Typography variant="body2">
                    {optimalLocation.latitude.toFixed(6)}, {optimalLocation.longitude.toFixed(6)}
                  </Typography>
                  {optimalLocation.totalTravelDistance !== undefined && (
                    <Typography variant="body2" color="text.secondary">
                      Total distance: {optimalLocation.totalTravelDistance.toFixed(2)} km
                    </Typography>
                  )}
                  {optimalLocation.participantCount !== undefined && (
                    <Typography variant="body2" color="text.secondary">
                      Based on {optimalLocation.participantCount} participant(s)
                    </Typography>
                  )}
                </Box>
              </Popup>
            </Marker>
          )}

          {/* Current user location marker (dark blue) */}
          {currentUserLocation && (
            <Marker
              position={[currentUserLocation.latitude, currentUserLocation.longitude]}
              icon={CurrentUserIcon}
            >
              <Popup>
                <Box>
                  <Typography variant="subtitle2" fontWeight="bold" color="primary">
                    Your Current Location
                  </Typography>
                  <Typography variant="body2">
                    {currentUserLocation.latitude.toFixed(6)}, {currentUserLocation.longitude.toFixed(6)}
                  </Typography>
                </Box>
              </Popup>
            </Marker>
          )}

          {/* Participant location markers (light blue) - exclude current user */}
          {Array.from(participantLocations.entries())
            .filter(([userId]) => userId !== currentUserLocation?.userId)
            .map(([userId, location]) => (
              <Marker
                key={userId}
                position={[location.latitude, location.longitude]}
                icon={ParticipantIcon}
              >
                <Popup>
                  <Box>
                    <Typography variant="subtitle2" fontWeight="bold">
                      {participantNames.get(userId) || `Participant ${userId}`}
                    </Typography>
                    <Typography variant="body2">
                      {location.latitude.toFixed(6)}, {location.longitude.toFixed(6)}
                    </Typography>
                  </Box>
                </Popup>
              </Marker>
            ))}
        </MapContainer>
      </Paper>
    </Box>
  );
};
