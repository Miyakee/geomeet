import { describe, it, expect, beforeEach, vi } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { useEndSession } from '../useEndSession';
import { sessionApi } from '../../services/api';

vi.mock('../../services/api', async () => {
  const actual = await vi.importActual<typeof import('../../services/api')>('../../services/api');
  return {
    ...actual,
    sessionApi: {
      endSession: vi.fn(),
    },
  };
});

describe('useEndSession', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should initialize with empty state', () => {
    const { result } = renderHook(() => useEndSession('test-session-id'));

    expect(result.current.loading).toBe(false);
    expect(result.current.error).toBeNull();
    expect(typeof result.current.endSession).toBe('function');
  });

  it('should end session successfully', async () => {
    const mockResponse = {
      sessionId: 100,
      sessionIdString: 'test-session-id',
      status: 'Ended',
      endedAt: '2024-01-01T00:00:00',
      message: 'Session ended successfully',
    };

    vi.mocked(sessionApi.endSession).mockResolvedValue(mockResponse);

    const { result } = renderHook(() => useEndSession('test-session-id'));

    let endSessionResult: any;
    await act(async () => {
      endSessionResult = await result.current.endSession();
    });

    expect(endSessionResult).toEqual(mockResponse);
    expect(result.current.loading).toBe(false);
    expect(result.current.error).toBeNull();
    expect(sessionApi.endSession).toHaveBeenCalledWith('test-session-id');
  });

  it('should handle 403 error (permission denied)', async () => {
    const mockError = {
      response: {
        status: 403,
        data: { message: 'Only the session initiator can end the session' },
      },
    };

    vi.mocked(sessionApi.endSession).mockRejectedValue(mockError);

    const { result } = renderHook(() => useEndSession('test-session-id'));

    await act(async () => {
      try {
        await result.current.endSession();
      } catch (err) {
        // Expected to throw
      }
    });

    expect(result.current.loading).toBe(false);
    expect(result.current.error).toBe('You do not have permission to end this session.');
    expect(sessionApi.endSession).toHaveBeenCalledWith('test-session-id');
  });

  it('should handle 404 error (session not found)', async () => {
    const mockError = {
      response: {
        status: 404,
        data: { message: 'Session not found' },
      },
    };

    vi.mocked(sessionApi.endSession).mockRejectedValue(mockError);

    const { result } = renderHook(() => useEndSession('test-session-id'));

    await act(async () => {
      try {
        await result.current.endSession();
      } catch (err) {
        // Expected to throw
      }
    });

    expect(result.current.loading).toBe(false);
    expect(result.current.error).toBe('Session not found.');
    expect(sessionApi.endSession).toHaveBeenCalledWith('test-session-id');
  });

  it('should handle 400 error (validation error)', async () => {
    const mockError = {
      response: {
        status: 400,
        data: { message: 'Session is already ended' },
      },
    };

    vi.mocked(sessionApi.endSession).mockRejectedValue(mockError);

    const { result } = renderHook(() => useEndSession('test-session-id'));

    await act(async () => {
      try {
        await result.current.endSession();
      } catch (err) {
        // Expected to throw
      }
    });

    expect(result.current.loading).toBe(false);
    expect(result.current.error).toBe('Cannot end session. Session may already be ended.');
    expect(sessionApi.endSession).toHaveBeenCalledWith('test-session-id');
  });

  it('should handle 400 error without message', async () => {
    const mockError = {
      response: {
        status: 400,
        data: {},
      },
    };

    vi.mocked(sessionApi.endSession).mockRejectedValue(mockError);

    const { result } = renderHook(() => useEndSession('test-session-id'));

    await act(async () => {
      try {
        await result.current.endSession();
      } catch (err) {
        // Expected to throw
      }
    });

    expect(result.current.loading).toBe(false);
    expect(result.current.error).toBe('Cannot end session. Session may already be ended.');
    expect(sessionApi.endSession).toHaveBeenCalledWith('test-session-id');
  });

  it('should handle generic error', async () => {
    const mockError = new Error('Network error');
    vi.mocked(sessionApi.endSession).mockRejectedValue(mockError);

    const { result } = renderHook(() => useEndSession('test-session-id'));

    await act(async () => {
      try {
        await result.current.endSession();
      } catch (err) {
        // Expected to throw
      }
    });

    expect(result.current.loading).toBe(false);
    // getStatusErrorMessage will extract the error message from the Error object
    expect(result.current.error).toBe('Network error');
    expect(sessionApi.endSession).toHaveBeenCalledWith('test-session-id');
  });

  it('should set error when sessionId is undefined', async () => {
    const { result } = renderHook(() => useEndSession(undefined));

    await act(async () => {
      const endSessionResult = await result.current.endSession();
      expect(endSessionResult).toBeNull();
    });

    expect(result.current.error).toBe('Session ID is required');
    expect(sessionApi.endSession).not.toHaveBeenCalled();
  });

  it('should set loading state during API call', async () => {
    let resolvePromise: (value: any) => void;
    const promise = new Promise((resolve) => {
      resolvePromise = resolve;
    });

    vi.mocked(sessionApi.endSession).mockReturnValue(promise as any);

    const { result } = renderHook(() => useEndSession('test-session-id'));

    act(() => {
      result.current.endSession();
    });

    expect(result.current.loading).toBe(true);

    await act(async () => {
      resolvePromise!({
        sessionId: 100,
        sessionIdString: 'test-session-id',
        status: 'Ended',
        endedAt: '2024-01-01T00:00:00',
        message: 'Session ended successfully',
      });
      await promise;
    });

    expect(result.current.loading).toBe(false);
  });
});

