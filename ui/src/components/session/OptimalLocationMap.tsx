import { useEffect, useState, useMemo, useRef } from 'react';
import { Box, Typography, Paper, CircularProgress } from '@mui/material';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import 'leaflet.markercluster/dist/MarkerCluster.css';
import 'leaflet.markercluster/dist/MarkerCluster.Default.css';
import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet';
import 'leaflet.markercluster';
import { calculateHaversineDistance } from '../../utils/distanceCalculator';

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

// Combined icon for when meeting location and optimal location overlap
const CombinedLocationIcon = L.divIcon({
  className: 'custom-marker-icon',
  html: `<div style="
    position: relative;
    width: 40px;
    height: 40px;
  ">
    <div style="
      background-color: #d32f2f;
      width: 30px;
      height: 30px;
      border-radius: 50% 50% 50% 0;
      transform: rotate(-45deg);
      border: 3px solid #ffffff;
      box-shadow: 0 2px 4px rgba(0,0,0,0.3);
      position: absolute;
      top: 0;
      left: 0;
    "></div>
    <div style="
      background-color: #facd07;
      width: 24px;
      height: 24px;
      border-radius: 50% 50% 50% 0;
      transform: rotate(-45deg);
      border: 2px solid #ffffff;
      box-shadow: 0 2px 4px rgba(0,0,0,0.3);
      position: absolute;
      top: 8px;
      left: 8px;
    "></div>
  </div>`,
  iconSize: [40, 40],
  iconAnchor: [20, 40],
  popupAnchor: [0, -40],
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
    userId?: number;
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
    [locations],
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

// Component to handle marker clustering for participant locations
function MarkerClusterComponent({
  currentUserLocation,
  participantLocations,
  participantNames,
  CurrentUserIcon,
  ParticipantIcon,
}: {
  currentUserLocation?: {
    latitude: number;
    longitude: number;
    userId?: number;
  } | null;
  participantLocations: Map<number, { latitude: number; longitude: number; userId: number }>;
  participantNames: Map<number, string>;
  CurrentUserIcon: L.DivIcon;
  ParticipantIcon: L.DivIcon;
}) {
  const map = useMap();
  const clusterGroupRef = useRef<any>(null);

  useEffect(() => {
    if (!map) return;

    // Create marker cluster group with custom styling
    // @ts-ignore - leaflet.markercluster extends L.LayerGroup but types may not be fully compatible
    const clusterGroup = (L as any).markerClusterGroup({
      chunkedLoading: true,
      maxClusterRadius: 50,
      iconCreateFunction: (cluster) => {
        const count = cluster.getChildCount();
        return L.divIcon({
          html: `<div style="
            background-color: #64b5f6;
            width: 40px;
            height: 40px;
            border-radius: 50%;
            border: 3px solid #ffffff;
            box-shadow: 0 2px 4px rgba(0,0,0,0.3);
            display: flex;
            align-items: center;
            justify-content: center;
            color: white;
            font-weight: bold;
            font-size: 14px;
          ">${count}</div>`,
          className: 'marker-cluster-custom',
          iconSize: L.point(40, 40),
        });
      },
    });

    // Add current user location marker
    if (currentUserLocation) {
      const currentUserMarker = L.marker(
        [currentUserLocation.latitude, currentUserLocation.longitude],
        { icon: CurrentUserIcon }
      );
      
      const popupContent = document.createElement('div');
      popupContent.innerHTML = `
        <div>
          <div style="font-weight: bold; color: #1976d2; margin-bottom: 4px;">Your Current Location</div>
          <div style="font-size: 12px;">${currentUserLocation.latitude.toFixed(6)}, ${currentUserLocation.longitude.toFixed(6)}</div>
        </div>
      `;
      currentUserMarker.bindPopup(popupContent);
      clusterGroup.addLayer(currentUserMarker);
    }

    // Add participant location markers
    participantLocations.forEach((location, userId) => {
      if (userId !== currentUserLocation?.userId) {
        const marker = L.marker(
          [location.latitude, location.longitude],
          { icon: ParticipantIcon }
        );
        
        const popupContent = document.createElement('div');
        const username = participantNames.get(userId) || `Participant ${userId}`;
        popupContent.innerHTML = `
          <div>
            <div style="font-weight: bold; margin-bottom: 4px;">${username}</div>
            <div style="font-size: 12px;">${location.latitude.toFixed(6)}, ${location.longitude.toFixed(6)}</div>
          </div>
        `;
        marker.bindPopup(popupContent);
        clusterGroup.addLayer(marker);
      }
    });

    clusterGroup.addTo(map);
    clusterGroupRef.current = clusterGroup;

    return () => {
      if (clusterGroupRef.current) {
        map.removeLayer(clusterGroupRef.current);
        clusterGroupRef.current = null;
      }
    };
  }, [map, currentUserLocation, participantLocations, participantNames, CurrentUserIcon, ParticipantIcon]);

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
  // Also exclude current user from participantLocations to avoid duplicate markers
  const allLocations: Array<[number, number]> = useMemo(() => {
    const locations: Array<[number, number]> = [];
    if (meetingLocation) {
      locations.push([meetingLocation.latitude, meetingLocation.longitude]);
    }
    if (currentUserLocation) {
      locations.push([currentUserLocation.latitude, currentUserLocation.longitude]);
    }
    // Always include optimal location if it exists (for reference)
    if (optimalLocation) {
      locations.push([optimalLocation.latitude, optimalLocation.longitude]);
    }
    // Exclude current user from participant locations to avoid duplicate markers
    const currentUserId = currentUserLocation?.userId;
    participantLocations.forEach((location, userId) => {
      if (userId !== currentUserId) {
        locations.push([location.latitude, location.longitude]);
      }
    });
    return locations;
  }, [meetingLocation, currentUserLocation, optimalLocation, participantLocations]);

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

          {/* Check if meeting location and optimal location overlap (within 10 meters) */}
          {(() => {
            const OVERLAP_THRESHOLD_KM = 0.01; // 10 meters in kilometers
            const locationsOverlap = meetingLocation && optimalLocation && 
              calculateHaversineDistance(
                meetingLocation.latitude,
                meetingLocation.longitude,
                optimalLocation.latitude,
                optimalLocation.longitude
              ) < OVERLAP_THRESHOLD_KM;

            if (locationsOverlap && meetingLocation && optimalLocation) {
              // Show combined marker when locations overlap
              return (
                <Marker
                  position={[meetingLocation.latitude, meetingLocation.longitude]}
                  icon={CombinedLocationIcon}
                >
                  <Popup>
                    <Box>
                      <Typography variant="subtitle2" fontWeight="bold" color="error">
                        Meeting Location & Optimal Location
                      </Typography>
                      <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5, mb: 1 }}>
                        Both locations are at the same point
                      </Typography>
                      {meetingLocationAddress ? (
                        <Typography variant="body2" sx={{ mt: 0.5, mb: 0.5 }}>
                          {meetingLocationAddress}
                        </Typography>
                      ) : null}
                      <Typography variant="body2" color="text.secondary">
                        {meetingLocation.latitude.toFixed(6)}, {meetingLocation.longitude.toFixed(6)}
                      </Typography>
                      {optimalLocation.totalTravelDistance !== undefined && (
                        <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
                          Total distance: {optimalLocation.totalTravelDistance.toFixed(2)} km
                        </Typography>
                      )}
                      {optimalLocation.participantCount !== undefined && (
                        <Typography variant="body2" color="text.secondary">
                          Based on {optimalLocation.participantCount} participant(s)
                        </Typography>
                      )}
                      <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
                        Set by session initiator
                      </Typography>
                    </Box>
                  </Popup>
                </Marker>
              );
            }

            // Show separate markers when they don't overlap
            return (
              <>
                {/* Optimal location marker (yellow) - always show if calculated */}
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
              </>
            );
          })()}

          {/* Marker Cluster Group for participant locations to handle overlapping markers */}
          <MarkerClusterComponent
            currentUserLocation={currentUserLocation}
            participantLocations={participantLocations}
            participantNames={participantNames}
            CurrentUserIcon={CurrentUserIcon}
            ParticipantIcon={ParticipantIcon}
          />
        </MapContainer>
      </Paper>
    </Box>
  );
};
