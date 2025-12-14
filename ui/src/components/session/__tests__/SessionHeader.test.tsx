import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { SessionHeader } from '../SessionHeader';
import { SessionDetailResponse } from '../../../types/session';

const mockSession: SessionDetailResponse = {
  id: 1,
  sessionId: 'test-session-id-12345678',
  initiatorId: 1,
  initiatorUsername: 'testuser',
  status: 'Active',
  createdAt: '2024-01-01T00:00:00',
  participants: [],
  participantCount: 0,
};

describe('SessionHeader', () => {
  it('should render session header with session ID', () => {
    render(<SessionHeader session={mockSession} isInitiator={false} />);
    
    expect(screen.getByText(/Session: test-ses/)).toBeInTheDocument();
    expect(screen.getByText(/Status: Active/)).toBeInTheDocument();
  });

  it('should show initiator chip when user is initiator', () => {
    render(<SessionHeader session={mockSession} isInitiator={true} />);
    
    expect(screen.getByText('Initiator')).toBeInTheDocument();
  });

  it('should not show initiator chip when user is not initiator', () => {
    render(<SessionHeader session={mockSession} isInitiator={false} />);
    
    expect(screen.queryByText('Initiator')).not.toBeInTheDocument();
  });

  it('should show correct status color for Active session', () => {
    render(<SessionHeader session={mockSession} isInitiator={false} />);
    
    const statusChip = screen.getByText(/Status: Active/);
    expect(statusChip).toBeInTheDocument();
  });

  it('should show correct status for Ended session', () => {
    const endedSession = { ...mockSession, status: 'Ended' };
    render(<SessionHeader session={endedSession} isInitiator={false} />);
    
    expect(screen.getByText(/Status: Ended/)).toBeInTheDocument();
  });
});

