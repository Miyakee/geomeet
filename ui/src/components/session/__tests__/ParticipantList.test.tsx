import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { ParticipantList } from '../ParticipantList';
import { SessionDetailResponse } from '../../../types/session';

const mockSession: SessionDetailResponse = {
  id: 1,
  sessionId: 'test-session-id',
  initiatorId: 1,
  initiatorUsername: 'initiator',
  status: 'Active',
  createdAt: '2024-01-01T00:00:00',
  participants: [
    {
      participantId: 1,
      userId: 1,
      username: 'user1',
      email: 'user1@example.com',
      joinedAt: '2024-01-01T00:00:00',
    },
    {
      participantId: 2,
      userId: 2,
      username: 'user2',
      email: 'user2@example.com',
      joinedAt: '2024-01-01T00:05:00',
    },
  ],
  participantCount: 2,
};

describe('ParticipantList', () => {
  it('should render participant count', () => {
    render(
      <ParticipantList
        session={mockSession}
        participantAddresses={new Map()}
      />,
    );

    expect(screen.getByText(/Participants \(2\)/)).toBeInTheDocument();
  });

  it('should render all participants', () => {
    render(
      <ParticipantList
        session={mockSession}
        participantAddresses={new Map()}
      />,
    );

    expect(screen.getByText('user1')).toBeInTheDocument();
    expect(screen.getByText('user2')).toBeInTheDocument();
  });

  it('should show message when no participants', () => {
    const emptySession = {
      ...mockSession,
      participants: [],
      participantCount: 0,
    };

    render(
      <ParticipantList
        session={emptySession}
        participantAddresses={new Map()}
      />,
    );

    expect(screen.getByText(/No participants yet/)).toBeInTheDocument();
  });

  it('should display location data from participant', () => {
    const sessionWithLocation = {
      ...mockSession,
      participants: [
        {
          ...mockSession.participants[0],
          latitude: 37.7749,
          longitude: -122.4194,
          accuracy: 10.0,
          locationUpdatedAt: new Date().toISOString(),
        },
        mockSession.participants[1],
      ],
    };

    render(
      <ParticipantList
        session={sessionWithLocation}
        participantAddresses={new Map()}
      />,
    );

    expect(screen.getByText(/37\.774900/)).toBeInTheDocument();
  });

  it('should pass address data to participant items', () => {
    const sessionWithLocation = {
      ...mockSession,
      participants: [
        {
          ...mockSession.participants[0],
          latitude: 37.7749,
          longitude: -122.4194,
          accuracy: 10.0,
          locationUpdatedAt: new Date().toISOString(),
        },
        mockSession.participants[1],
      ],
    };
    const addresses = new Map<number, string>();
    addresses.set(1, 'Market Street, San Francisco');

    render(
      <ParticipantList
        session={sessionWithLocation}
        participantAddresses={addresses}
      />,
    );

    expect(screen.getByText(/Market Street, San Francisco/)).toBeInTheDocument();
  });
});

