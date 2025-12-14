import { describe, it, expect, beforeEach, vi } from 'vitest';
import { renderHook, waitFor, act } from '@testing-library/react';
import { useSessionData } from '../useSessionData';
import { sessionApi } from '../../services/api';
import { SessionDetailResponse } from '../../types/session';

vi.mock('../../services/api', () => ({
  sessionApi: {
    getSessionDetails: vi.fn(),
  },
}));

describe('useSessionData', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should load session data successfully', async () => {
    const mockSession: SessionDetailResponse = {
      id: 1,
      sessionId: 'test-session-id',
      initiatorId: 1,
      initiatorUsername: 'testuser',
      status: 'Active',
      createdAt: '2024-01-01T00:00:00',
      participants: [],
      participantCount: 0,
    };

    vi.mocked(sessionApi.getSessionDetails).mockResolvedValue(mockSession);

    const { result } = renderHook(() => useSessionData('test-session-id'));

    expect(result.current.loading).toBe(true);
    expect(result.current.session).toBeNull();

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    expect(result.current.session).toEqual(mockSession);
    expect(result.current.error).toBeNull();
  });

  it('should handle 403 error', async () => {
    const mockError = {
      response: { status: 403 },
    };

    vi.mocked(sessionApi.getSessionDetails).mockRejectedValue(mockError);

    const { result } = renderHook(() => useSessionData('test-session-id'));

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    expect(result.current.session).toBeNull();
    expect(result.current.error).toBe('You do not have permission to view this session.');
  });

  it('should handle 404 error', async () => {
    const mockError = {
      response: { status: 404 },
    };

    vi.mocked(sessionApi.getSessionDetails).mockRejectedValue(mockError);

    const { result } = renderHook(() => useSessionData('test-session-id'));

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    expect(result.current.session).toBeNull();
    expect(result.current.error).toBe('Session not found.');
  });

  it('should handle generic error', async () => {
    const mockError = new Error('Network error');

    vi.mocked(sessionApi.getSessionDetails).mockRejectedValue(mockError);

    const { result } = renderHook(() => useSessionData('test-session-id'));

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    expect(result.current.session).toBeNull();
    expect(result.current.error).toBe('Failed to load session. Please try again.');
  });

  it('should set error when sessionId is undefined', () => {
    const { result } = renderHook(() => useSessionData(undefined));

    expect(result.current.loading).toBe(false);
    expect(result.current.error).toBe('Session ID is required');
    expect(result.current.session).toBeNull();
  });

  it('should update session when updateSession is called', async () => {
    const mockSession: SessionDetailResponse = {
      id: 1,
      sessionId: 'test-session-id',
      initiatorId: 1,
      initiatorUsername: 'testuser',
      status: 'Active',
      createdAt: '2024-01-01T00:00:00',
      participants: [],
      participantCount: 0,
    };

    vi.mocked(sessionApi.getSessionDetails).mockResolvedValue(mockSession);

    const { result } = renderHook(() => useSessionData('test-session-id'));

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    const updatedSession = { ...mockSession, participantCount: 1 };
    
    act(() => {
      result.current.updateSession(updatedSession);
    });

    expect(result.current.session).toEqual(updatedSession);
  });
});

