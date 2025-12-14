import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MeetingLocationSection } from '../MeetingLocationSection';

// Mock geolocation API
const mockGeolocation = {
  getCurrentPosition: vi.fn(),
};

Object.defineProperty(global.navigator, 'geolocation', {
  value: mockGeolocation,
  writable: true,
});

describe('MeetingLocationSection', () => {
  const mockOnUpdateLocation = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should render meeting location section', () => {
    render(
      <MeetingLocationSection
        meetingLocation={null}
        isInitiator={false}
        onUpdateLocation={mockOnUpdateLocation}
      />
    );

    expect(screen.getByText('Meeting Location')).toBeInTheDocument();
  });

  it('should show edit button when user is initiator', () => {
    render(
      <MeetingLocationSection
        meetingLocation={null}
        isInitiator={true}
        onUpdateLocation={mockOnUpdateLocation}
      />
    );

    const editButton = screen.getByRole('button', { name: /edit/i });
    expect(editButton).toBeInTheDocument();
  });

  it('should not show edit button when user is not initiator', () => {
    render(
      <MeetingLocationSection
        meetingLocation={null}
        isInitiator={false}
        onUpdateLocation={mockOnUpdateLocation}
      />
    );

    const editButton = screen.queryByRole('button', { name: /edit/i });
    expect(editButton).not.toBeInTheDocument();
  });

  it('should display meeting location when provided', () => {
    const meetingLocation = {
      latitude: 1.3521,
      longitude: 103.8198,
    };

    render(
      <MeetingLocationSection
        meetingLocation={meetingLocation}
        isInitiator={false}
        onUpdateLocation={mockOnUpdateLocation}
      />
    );

    expect(screen.getByText(/1\.352100/)).toBeInTheDocument();
    expect(screen.getByText(/103\.819800/)).toBeInTheDocument();
  });

  it('should display meeting location address when provided', () => {
    const meetingLocation = {
      latitude: 1.3521,
      longitude: 103.8198,
    };

    render(
      <MeetingLocationSection
        meetingLocation={meetingLocation}
        meetingLocationAddress="Orchard Road, Singapore"
        isInitiator={false}
        onUpdateLocation={mockOnUpdateLocation}
      />
    );

    expect(screen.getByText(/Orchard Road, Singapore/)).toBeInTheDocument();
  });

  it('should show loading message when address is loading', () => {
    const meetingLocation = {
      latitude: 1.3521,
      longitude: 103.8198,
    };

    render(
      <MeetingLocationSection
        meetingLocation={meetingLocation}
        loadingAddress={true}
        isInitiator={false}
        onUpdateLocation={mockOnUpdateLocation}
      />
    );

    expect(screen.getByText('Loading address...')).toBeInTheDocument();
  });

  it('should display distance from current user location', () => {
    const meetingLocation = {
      latitude: 1.3521,
      longitude: 103.8198,
    };

    const currentUserLocation = {
      latitude: 1.2903,
      longitude: 103.8520,
    };

    render(
      <MeetingLocationSection
        meetingLocation={meetingLocation}
        currentUserLocation={currentUserLocation}
        isInitiator={false}
        onUpdateLocation={mockOnUpdateLocation}
      />
    );

    expect(screen.getByText(/Distance from your location/)).toBeInTheDocument();
  });

  it('should show message when no meeting location is set (initiator)', () => {
    render(
      <MeetingLocationSection
        meetingLocation={null}
        isInitiator={true}
        onUpdateLocation={mockOnUpdateLocation}
      />
    );

    expect(screen.getByText(/No meeting location set/)).toBeInTheDocument();
    expect(screen.getByText(/Click the edit button to set a location/)).toBeInTheDocument();
  });

  it('should show message when no meeting location is set (non-initiator)', () => {
    render(
      <MeetingLocationSection
        meetingLocation={null}
        isInitiator={false}
        onUpdateLocation={mockOnUpdateLocation}
      />
    );

    expect(screen.getByText(/Meeting location has not been set/)).toBeInTheDocument();
  });

  it('should open edit dialog when edit button is clicked', async () => {
    const user = userEvent.setup();
    const meetingLocation = {
      latitude: 1.3521,
      longitude: 103.8198,
    };

    render(
      <MeetingLocationSection
        meetingLocation={meetingLocation}
        isInitiator={true}
        onUpdateLocation={mockOnUpdateLocation}
      />
    );

    const editButton = screen.getByRole('button', { name: /edit/i });
    await user.click(editButton);

    expect(screen.getByText('Edit Meeting Location')).toBeInTheDocument();
  });

  it('should pre-fill coordinates in edit dialog', async () => {
    const user = userEvent.setup();
    const meetingLocation = {
      latitude: 1.3521,
      longitude: 103.8198,
    };

    render(
      <MeetingLocationSection
        meetingLocation={meetingLocation}
        isInitiator={true}
        onUpdateLocation={mockOnUpdateLocation}
      />
    );

    const editButton = screen.getByRole('button', { name: /edit/i });
    await user.click(editButton);

    const latitudeInput = screen.getByLabelText(/Latitude/i) as HTMLInputElement;
    const longitudeInput = screen.getByLabelText(/Longitude/i) as HTMLInputElement;

    expect(latitudeInput.value).toBe('1.3521');
    expect(longitudeInput.value).toBe('103.8198');
  });

  it('should update location when save is clicked', async () => {
    const user = userEvent.setup();
    mockOnUpdateLocation.mockResolvedValue(undefined);

    render(
      <MeetingLocationSection
        meetingLocation={null}
        isInitiator={true}
        onUpdateLocation={mockOnUpdateLocation}
      />
    );

    const editButton = screen.getByRole('button', { name: /edit/i });
    await user.click(editButton);

    const latitudeInput = screen.getByLabelText(/Latitude/i);
    const longitudeInput = screen.getByLabelText(/Longitude/i);

    await user.clear(latitudeInput);
    await user.type(latitudeInput, '1.3521');
    await user.clear(longitudeInput);
    await user.type(longitudeInput, '103.8198');

    const saveButton = screen.getByRole('button', { name: /save/i });
    await user.click(saveButton);

    await waitFor(() => {
      expect(mockOnUpdateLocation).toHaveBeenCalledWith(1.3521, 103.8198);
    });
  });

  it('should validate latitude range', async () => {
    const user = userEvent.setup();

    render(
      <MeetingLocationSection
        meetingLocation={null}
        isInitiator={true}
        onUpdateLocation={mockOnUpdateLocation}
      />
    );

    const editButton = screen.getByRole('button', { name: /edit/i });
    await user.click(editButton);

    const latitudeInput = screen.getByLabelText(/Latitude/i);
    await user.clear(latitudeInput);
    await user.type(latitudeInput, '91'); // Invalid latitude

    const longitudeInput = screen.getByLabelText(/Longitude/i);
    await user.clear(longitudeInput);
    await user.type(longitudeInput, '0'); // Valid longitude

    const saveButton = screen.getByRole('button', { name: /save/i });
    // Button should be enabled now that both fields have values
    expect(saveButton).not.toBeDisabled();
    await user.click(saveButton);

    expect(screen.getByText(/Latitude must be between -90 and 90/)).toBeInTheDocument();
    expect(mockOnUpdateLocation).not.toHaveBeenCalled();
  });

  it('should validate longitude range', async () => {
    const user = userEvent.setup();

    render(
      <MeetingLocationSection
        meetingLocation={null}
        isInitiator={true}
        onUpdateLocation={mockOnUpdateLocation}
      />
    );

    const editButton = screen.getByRole('button', { name: /edit/i });
    await user.click(editButton);

    const latitudeInput = screen.getByLabelText(/Latitude/i);
    await user.clear(latitudeInput);
    await user.type(latitudeInput, '0'); // Valid latitude

    const longitudeInput = screen.getByLabelText(/Longitude/i);
    await user.clear(longitudeInput);
    await user.type(longitudeInput, '181'); // Invalid longitude

    const saveButton = screen.getByRole('button', { name: /save/i });
    // Button should be enabled now that both fields have values
    expect(saveButton).not.toBeDisabled();
    await user.click(saveButton);

    expect(screen.getByText(/Longitude must be between -180 and 180/)).toBeInTheDocument();
    expect(mockOnUpdateLocation).not.toHaveBeenCalled();
  });

  it('should use current location when button is clicked', async () => {
    const user = userEvent.setup();
    const mockPosition = {
      coords: {
        latitude: 1.3521,
        longitude: 103.8198,
        accuracy: 10,
      },
    };

    mockGeolocation.getCurrentPosition.mockImplementation((success) => {
      success(mockPosition as GeolocationPosition);
    });

    render(
      <MeetingLocationSection
        meetingLocation={null}
        isInitiator={true}
        onUpdateLocation={mockOnUpdateLocation}
      />
    );

    const editButton = screen.getByRole('button', { name: /edit/i });
    await user.click(editButton);

    const useCurrentLocationButton = screen.getByRole('button', { name: /Use Current Location/i });
    await user.click(useCurrentLocationButton);

    await waitFor(() => {
      const latitudeInput = screen.getByLabelText(/Latitude/i) as HTMLInputElement;
      expect(latitudeInput.value).toBe('1.352100');
    });
  });

  it('should handle geolocation error', async () => {
    const user = userEvent.setup();
    const mockError = {
      code: 1,
      message: 'User denied geolocation',
    };

    mockGeolocation.getCurrentPosition.mockImplementation((success, error) => {
      if (error) {
        error(mockError as GeolocationPositionError);
      }
    });

    render(
      <MeetingLocationSection
        meetingLocation={null}
        isInitiator={true}
        onUpdateLocation={mockOnUpdateLocation}
      />
    );

    const editButton = screen.getByRole('button', { name: /edit/i });
    await user.click(editButton);

    const useCurrentLocationButton = screen.getByRole('button', { name: /Use Current Location/i });
    await user.click(useCurrentLocationButton);

    await waitFor(() => {
      expect(screen.getByText(/Failed to get current location/)).toBeInTheDocument();
    });
  });

  it('should display error message when provided', () => {
    render(
      <MeetingLocationSection
        meetingLocation={null}
        isInitiator={true}
        onUpdateLocation={mockOnUpdateLocation}
        error="Failed to update location"
      />
    );

    expect(screen.getByText('Failed to update location')).toBeInTheDocument();
  });

  it('should disable save button when loading', () => {
    render(
      <MeetingLocationSection
        meetingLocation={null}
        isInitiator={true}
        onUpdateLocation={mockOnUpdateLocation}
        loading={true}
      />
    );

    // When loading, edit button should be disabled
    const editButton = screen.getByRole('button', { name: /edit/i });
    expect(editButton).toBeDisabled();
  });

  it('should close dialog when cancel is clicked', async () => {
    const user = userEvent.setup();

    render(
      <MeetingLocationSection
        meetingLocation={null}
        isInitiator={true}
        onUpdateLocation={mockOnUpdateLocation}
      />
    );

    const editButton = screen.getByRole('button', { name: /edit/i });
    await user.click(editButton);

    expect(screen.getByText('Edit Meeting Location')).toBeInTheDocument();

    const cancelButton = screen.getByRole('button', { name: /cancel/i });
    await user.click(cancelButton);

    await waitFor(() => {
      expect(screen.queryByText('Edit Meeting Location')).not.toBeInTheDocument();
    });
  });
});

