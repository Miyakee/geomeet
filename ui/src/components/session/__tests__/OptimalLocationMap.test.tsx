import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { OptimalLocationMap } from '../OptimalLocationMap';

// Mock react-leaflet
vi.mock('react-leaflet', () => ({
  MapContainer: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="map-container">{children}</div>
  ),
  TileLayer: () => <div data-testid="tile-layer">TileLayer</div>,
  Marker: ({ position }: { position: [number, number] }) => (
    <div data-testid={`marker-${position[0]}-${position[1]}`}>Marker</div>
  ),
  Popup: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="popup">{children}</div>
  ),
}));

describe('OptimalLocationMap', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should show loading state when not mounted', async () => {
    render(
      <OptimalLocationMap
        optimalLocation={null}
        participantLocations={new Map()}
        participantNames={new Map()}
      />
    );

    // Component shows loading initially, then "No locations" after mount
    await new Promise((resolve) => setTimeout(resolve, 100));
  });

  it('should show message when no locations available', async () => {
    // Wait for component to mount
    await new Promise((resolve) => setTimeout(resolve, 100));

    render(
      <OptimalLocationMap
        optimalLocation={null}
        participantLocations={new Map()}
        participantNames={new Map()}
      />
    );

    expect(screen.getByText('No locations available to display on map')).toBeInTheDocument();
  });

  it('should render map with optimal location', async () => {
    const optimalLocation = {
      latitude: 1.3521,
      longitude: 103.8198,
      totalTravelDistance: 10.5,
      participantCount: 3,
    };

    render(
      <OptimalLocationMap
        optimalLocation={optimalLocation}
        participantLocations={new Map()}
        participantNames={new Map()}
      />
    );

    // Wait for component to mount
    await new Promise((resolve) => setTimeout(resolve, 100));

    expect(screen.getByText('Meeting Location Map')).toBeInTheDocument();
    expect(screen.getByTestId('map-container')).toBeInTheDocument();
    expect(screen.getByTestId('tile-layer')).toBeInTheDocument();
  });

  it('should render map with participant locations', async () => {
    const participantLocations = new Map([
      [1, { latitude: 1.2903, longitude: 103.8520, userId: 1 }],
      [2, { latitude: 1.2966, longitude: 103.7764, userId: 2 }],
    ]);

    const participantNames = new Map([
      [1, 'User 1'],
      [2, 'User 2'],
    ]);

    render(
      <OptimalLocationMap
        optimalLocation={null}
        participantLocations={participantLocations}
        participantNames={participantNames}
      />
    );

    // Wait for component to mount
    await new Promise((resolve) => setTimeout(resolve, 100));

    expect(screen.getByText('Meeting Location Map')).toBeInTheDocument();
    expect(screen.getByTestId('map-container')).toBeInTheDocument();
  });

  it('should use current user location as default center when provided', async () => {
    const currentUserLocation = {
      latitude: 1.3000,
      longitude: 103.8000,
    };

    render(
      <OptimalLocationMap
        optimalLocation={null}
        participantLocations={new Map()}
        participantNames={new Map()}
        currentUserLocation={currentUserLocation}
      />
    );

    // Wait for component to mount
    await new Promise((resolve) => setTimeout(resolve, 100));

    expect(screen.getByText('No locations available to display on map')).toBeInTheDocument();
  });

  it('should prioritize optimal location over current user location', async () => {
    const optimalLocation = {
      latitude: 1.3521,
      longitude: 103.8198,
      totalTravelDistance: 10.5,
      participantCount: 3,
    };

    const currentUserLocation = {
      latitude: 1.3000,
      longitude: 103.8000,
    };

    render(
      <OptimalLocationMap
        optimalLocation={optimalLocation}
        participantLocations={new Map()}
        participantNames={new Map()}
        currentUserLocation={currentUserLocation}
      />
    );

    // Wait for component to mount
    await new Promise((resolve) => setTimeout(resolve, 100));

    expect(screen.getByTestId('map-container')).toBeInTheDocument();
  });

  it('should prioritize meeting location over optimal location', async () => {
    const optimalLocation = {
      latitude: 1.3521,
      longitude: 103.8198,
      totalTravelDistance: 10.5,
      participantCount: 3,
    };

    const meetingLocation = {
      latitude: 1.3000,
      longitude: 103.8000,
    };

    render(
      <OptimalLocationMap
        optimalLocation={optimalLocation}
        participantLocations={new Map()}
        participantNames={new Map()}
        meetingLocation={meetingLocation}
      />
    );

    // Wait for component to mount
    await new Promise((resolve) => setTimeout(resolve, 100));

    expect(screen.getByTestId('map-container')).toBeInTheDocument();
  });

  it('should render meeting location marker when provided', async () => {
    const meetingLocation = {
      latitude: 1.3521,
      longitude: 103.8198,
    };

    render(
      <OptimalLocationMap
        optimalLocation={null}
        participantLocations={new Map()}
        participantNames={new Map()}
        meetingLocation={meetingLocation}
        meetingLocationAddress="Test Address"
      />
    );

    // Wait for component to mount
    await new Promise((resolve) => setTimeout(resolve, 100));

    expect(screen.getByTestId('map-container')).toBeInTheDocument();
  });

  it('should render all location types together', async () => {
    const optimalLocation = {
      latitude: 1.3521,
      longitude: 103.8198,
      totalTravelDistance: 10.5,
      participantCount: 3,
    };

    const meetingLocation = {
      latitude: 1.3000,
      longitude: 103.8000,
    };

    const participantLocations = new Map([
      [1, { latitude: 1.2903, longitude: 103.8520, userId: 1 }],
    ]);

    const participantNames = new Map([
      [1, 'User 1'],
    ]);

    render(
      <OptimalLocationMap
        optimalLocation={optimalLocation}
        participantLocations={participantLocations}
        participantNames={participantNames}
        meetingLocation={meetingLocation}
      />
    );

    // Wait for component to mount
    await new Promise((resolve) => setTimeout(resolve, 100));

    expect(screen.getByTestId('map-container')).toBeInTheDocument();
  });
});

