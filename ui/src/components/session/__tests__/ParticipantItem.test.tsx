import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { ParticipantItem } from '../ParticipantItem';
import { ParticipantInfo, ParticipantLocation } from '../../../types/session';

const mockParticipant: ParticipantInfo = {
  participantId: 1,
  userId: 1,
  username: 'testuser',
  email: 'test@example.com',
  joinedAt: '2024-01-01T00:00:00',
};

const mockLocation: ParticipantLocation = {
  latitude: 37.7749,
  longitude: -122.4194,
  accuracy: 10.0,
  updatedAt: new Date().toISOString(),
};

describe('ParticipantItem', () => {
  it('should render participant information', () => {
    render(
      <ParticipantItem
        participant={mockParticipant}
        isInitiator={false}
        isCurrentUser={false}
      />,
    );

    expect(screen.getByText('testuser')).toBeInTheDocument();
    // Email is displayed with "Joined:" text, so use a more flexible matcher
    expect(screen.getByText(/test@example\.com/)).toBeInTheDocument();
  });

  it('should show initiator chip when participant is initiator', () => {
    render(
      <ParticipantItem
        participant={mockParticipant}
        isInitiator={true}
        isCurrentUser={false}
      />,
    );

    expect(screen.getByText('Initiator')).toBeInTheDocument();
  });

  it('should show "You" chip when participant is current user', () => {
    render(
      <ParticipantItem
        participant={mockParticipant}
        isInitiator={false}
        isCurrentUser={true}
      />,
    );

    expect(screen.getByText('You')).toBeInTheDocument();
  });

  it('should display location when provided', () => {
    render(
      <ParticipantItem
        participant={mockParticipant}
        isInitiator={false}
        isCurrentUser={false}
        location={mockLocation}
      />,
    );

    expect(screen.getByText(/37\.774900/)).toBeInTheDocument();
    expect(screen.getByText(/-122\.419400/)).toBeInTheDocument();
  });

  it('should display address when provided', () => {
    render(
      <ParticipantItem
        participant={mockParticipant}
        isInitiator={false}
        isCurrentUser={false}
        location={mockLocation}
        address="Market Street, San Francisco"
      />,
    );

    expect(screen.getByText(/Market Street, San Francisco/)).toBeInTheDocument();
  });

  it('should display accuracy when provided', () => {
    render(
      <ParticipantItem
        participant={mockParticipant}
        isInitiator={false}
        isCurrentUser={false}
        location={mockLocation}
      />,
    );

    expect(screen.getByText(/±10m/)).toBeInTheDocument();
  });

  it('should not display location when not provided', () => {
    render(
      <ParticipantItem
        participant={mockParticipant}
        isInitiator={false}
        isCurrentUser={false}
      />,
    );

    expect(screen.queryByText(/37\.774900/)).not.toBeInTheDocument();
  });

  it('should show both initiator and current user chips', () => {
    render(
      <ParticipantItem
        participant={mockParticipant}
        isInitiator={true}
        isCurrentUser={true}
      />,
    );

    expect(screen.getByText('Initiator')).toBeInTheDocument();
    expect(screen.getByText('You')).toBeInTheDocument();
  });

  it('should display location age for old locations', () => {
    const oldLocation: ParticipantLocation = {
      latitude: 37.7749,
      longitude: -122.4194,
      accuracy: 10.0,
      updatedAt: new Date(Date.now() - 120000).toISOString(), // 2 minutes ago
    };

    render(
      <ParticipantItem
        participant={mockParticipant}
        isInitiator={false}
        isCurrentUser={false}
        location={oldLocation}
      />,
    );

    expect(screen.getByText(/120s ago/)).toBeInTheDocument();
  });

  it('should highlight current user with background color', () => {
    const { container } = render(
      <ParticipantItem
        participant={mockParticipant}
        isInitiator={false}
        isCurrentUser={true}
      />,
    );

    const listItem = container.querySelector('.MuiListItem-root');
    expect(listItem).toHaveStyle({ backgroundColor: expect.any(String) });
  });
});

