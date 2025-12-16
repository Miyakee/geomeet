import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { LocationTrackingSection } from '../LocationTrackingSection';

const mockGeolocationPosition: GeolocationPosition = {
  coords: {
    latitude: 37.7749,
    longitude: -122.4194,
    accuracy: 10,
    altitude: null,
    altitudeAccuracy: null,
    heading: null,
    speed: null,
  },
  timestamp: Date.now(),
} as GeolocationPosition;

describe('LocationTrackingSection', () => {
  it('should render location tracking section', () => {
    render(
      <LocationTrackingSection
        locationEnabled={false}
        locationError={null}
        currentLocation={null}
        updatingLocation={false}
        onToggle={vi.fn()}
        onRetry={vi.fn()}
      />,
    );

    expect(screen.getByText('Location Tracking')).toBeInTheDocument();
  });

  it('should show switch in disabled state when location is not enabled', () => {
    render(
      <LocationTrackingSection
        locationEnabled={false}
        locationError={null}
        currentLocation={null}
        updatingLocation={false}
        onToggle={vi.fn()}
        onRetry={vi.fn()}
      />,
    );

    const switchElement = screen.getByRole('checkbox');
    expect(switchElement).not.toBeChecked();
  });

  it('should show switch in enabled state when location is enabled', () => {
    render(
      <LocationTrackingSection
        locationEnabled={true}
        locationError={null}
        currentLocation={null}
        updatingLocation={false}
        onToggle={vi.fn()}
        onRetry={vi.fn()}
      />,
    );

    const switchElement = screen.getByRole('checkbox');
    expect(switchElement).toBeChecked();
  });

  it('should call onToggle when switch is clicked', async () => {
    const user = userEvent.setup();
    const onToggle = vi.fn();

    render(
      <LocationTrackingSection
        locationEnabled={false}
        locationError={null}
        currentLocation={null}
        updatingLocation={false}
        onToggle={onToggle}
        onRetry={vi.fn()}
      />,
    );

    const switchElement = screen.getByRole('checkbox');
    await user.click(switchElement);

    expect(onToggle).toHaveBeenCalledTimes(1);
  });

  it('should display location error when provided', () => {
    render(
      <LocationTrackingSection
        locationEnabled={false}
        locationError="Location permission denied"
        currentLocation={null}
        updatingLocation={false}
        onToggle={vi.fn()}
        onRetry={vi.fn()}
      />,
    );

    expect(screen.getByText('Location permission denied')).toBeInTheDocument();
  });

  it('should show retry button for permission denied errors', () => {
    const onRetry = vi.fn();

    render(
      <LocationTrackingSection
        locationEnabled={false}
        locationError="Location permission denied"
        currentLocation={null}
        updatingLocation={false}
        onToggle={vi.fn()}
        onRetry={onRetry}
      />,
    );

    const retryButton = screen.getByText('Retry');
    expect(retryButton).toBeInTheDocument();
  });

  it('should call onRetry when retry button is clicked', async () => {
    const user = userEvent.setup();
    const onRetry = vi.fn();

    render(
      <LocationTrackingSection
        locationEnabled={false}
        locationError="Location permission denied"
        currentLocation={null}
        updatingLocation={false}
        onToggle={vi.fn()}
        onRetry={onRetry}
      />,
    );

    const retryButton = screen.getByText('Retry');
    await user.click(retryButton);

    expect(onRetry).toHaveBeenCalledTimes(1);
  });

  it('should display current location when available', () => {
    render(
      <LocationTrackingSection
        locationEnabled={true}
        locationError={null}
        currentLocation={mockGeolocationPosition}
        updatingLocation={false}
        onToggle={vi.fn()}
        onRetry={vi.fn()}
      />,
    );

    expect(screen.getByText(/37\.774900/)).toBeInTheDocument();
    expect(screen.getByText(/-122\.419400/)).toBeInTheDocument();
    expect(screen.getByText(/Accuracy: ±10m/)).toBeInTheDocument();
  });

  it('should show loading message when location is enabled but not yet available', () => {
    render(
      <LocationTrackingSection
        locationEnabled={true}
        locationError={null}
        currentLocation={null}
        updatingLocation={false}
        onToggle={vi.fn()}
        onRetry={vi.fn()}
      />,
    );

    expect(screen.getByText('Getting your location...')).toBeInTheDocument();
  });
});

