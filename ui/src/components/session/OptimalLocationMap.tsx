import { useEffect, useState } from 'react';
import { Box, Typography, Paper, CircularProgress } from '@mui/material';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

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
const OptimalLocationIcon = L.icon({
  iconUrl: icon,
  shadowUrl: iconShadow,
  iconRetinaUrl: iconRetina,
  iconSize: [35, 51],
  iconAnchor: [17, 51],
  popupAnchor: [1, -34],
  tooltipAnchor: [16, -28],
  shadowSize: [51, 51],
});

// Custom icon for participant locations
const ParticipantIcon = L.icon({
  iconUrl: icon,
  shadowUrl: iconShadow,
  iconRetinaUrl: iconRetina,
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  tooltipAnchor: [16, -28],
  shadowSize: [41, 41],
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
  } | null;
}

// Import react-leaflet components directly
// Using dynamic import in useEffect to avoid SSR issues
import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';

export const OptimalLocationMap = ({
  optimalLocation,
  participantLocations,
  participantNames,
  currentUserLocation,
}: OptimalLocationMapProps) => {
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
  }, []);

  // Default center: use current user location, then optimal location, then Singapore
  const defaultCenter: [number, number] = [1.3521, 103.8198];
  const center: [number, number] = optimalLocation
    ? [optimalLocation.latitude, optimalLocation.longitude]
    : currentUserLocation
      ? [currentUserLocation.latitude, currentUserLocation.longitude]
      : defaultCenter;

  // Calculate bounds to fit all markers
  const allLocations: Array<[number, number]> = [];
  if (optimalLocation) {
    allLocations.push([optimalLocation.latitude, optimalLocation.longitude]);
  }
  participantLocations.forEach((location) => {
    allLocations.push([location.latitude, location.longitude]);
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

          {/* Optimal location marker */}
          {optimalLocation && (
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

          {/* Participant location markers */}
          {Array.from(participantLocations.entries()).map(([userId, location]) => (
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
