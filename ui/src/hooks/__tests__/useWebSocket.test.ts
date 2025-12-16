import { describe, it, expect, beforeEach, vi } from 'vitest';
import { renderHook } from '@testing-library/react';
import { useWebSocket } from '../useWebSocket';
import { SessionDetailResponse } from '../../types/session';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

// Mock SockJS and STOMP
vi.mock('sockjs-client', () => ({
  default: vi.fn(),
}));

vi.mock('@stomp/stompjs', () => ({
  Client: vi.fn(),
}));

// Mock localStorage
const localStorageMock = {
  getItem: vi.fn(),
  setItem: vi.fn(),
  removeItem: vi.fn(),
  clear: vi.fn(),
};
Object.defineProperty(window, 'localStorage', {
  value: localStorageMock,
});

describe('useWebSocket', () => {
  let mockClient: any;
  let mockSocket: any;
  let onConnectCallback: any;
  let subscribeCallbacks: Map<string, (message: any) => void>;

  beforeEach(() => {
    vi.clearAllMocks();
    vi.useFakeTimers();
    subscribeCallbacks = new Map();

    mockSocket = {};
    mockClient = {
      activate: vi.fn(),
      deactivate: vi.fn(),
      subscribe: vi.fn((destination, callback) => {
        subscribeCallbacks.set(destination, callback);
      }),
    };

    vi.mocked(SockJS).mockImplementation(() => mockSocket as any);
    vi.mocked(Client).mockImplementation((config: any) => {
      if (config.onConnect) {
        onConnectCallback = config.onConnect;
      }
      return mockClient;
    });

    localStorageMock.getItem.mockReturnValue('test-token');
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('should not setup WebSocket when sessionId is undefined', () => {
    const onSessionUpdate = vi.fn();
    const onLocationUpdate = vi.fn();
    const onAddressUpdate = vi.fn();

    renderHook(() =>
      useWebSocket({
        sessionId: undefined,
        onSessionUpdate,
        onLocationUpdate,
        onAddressUpdate,
      }),
    );

    expect(Client).not.toHaveBeenCalled();
  });

  it('should not setup WebSocket when token is not available', () => {
    localStorageMock.getItem.mockReturnValue(null);

    const onSessionUpdate = vi.fn();
    const onLocationUpdate = vi.fn();
    const onAddressUpdate = vi.fn();

    renderHook(() =>
      useWebSocket({
        sessionId: 'test-session-id',
        onSessionUpdate,
        onLocationUpdate,
        onAddressUpdate,
      }),
    );

    // Advance timer to trigger setup
    vi.advanceTimersByTime(500);

    expect(Client).not.toHaveBeenCalled();
  });

  it('should setup WebSocket connection when sessionId is provided', () => {
    const onSessionUpdate = vi.fn();
    const onLocationUpdate = vi.fn();
    const onAddressUpdate = vi.fn();

    renderHook(() =>
      useWebSocket({
        sessionId: 'test-session-id',
        onSessionUpdate,
        onLocationUpdate,
        onAddressUpdate,
      }),
    );

    // Advance timer to trigger setup
    vi.advanceTimersByTime(500);

    expect(SockJS).toHaveBeenCalled();
    expect(Client).toHaveBeenCalled();
    expect(mockClient.activate).toHaveBeenCalled();
  });

  it('should subscribe to session updates', () => {
    const onSessionUpdate = vi.fn();
    const onLocationUpdate = vi.fn();
    const onAddressUpdate = vi.fn();

    renderHook(() =>
      useWebSocket({
        sessionId: 'test-session-id',
        onSessionUpdate,
        onLocationUpdate,
        onAddressUpdate,
      }),
    );

    vi.advanceTimersByTime(500);

    // Simulate connection
    if (onConnectCallback) {
      onConnectCallback({});
    }

    expect(mockClient.subscribe).toHaveBeenCalledWith(
      '/topic/session/test-session-id',
      expect.any(Function),
    );
  });

  it('should handle session update messages', () => {
    const onSessionUpdate = vi.fn();
    const onLocationUpdate = vi.fn();
    const onAddressUpdate = vi.fn();

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

    renderHook(() =>
      useWebSocket({
        sessionId: 'test-session-id',
        onSessionUpdate,
        onLocationUpdate,
        onAddressUpdate,
      }),
    );

    vi.advanceTimersByTime(500);

    // Simulate connection
    if (onConnectCallback) {
      onConnectCallback({});
    }

    // Get the callback for session updates
    const sessionCallback = subscribeCallbacks.get('/topic/session/test-session-id');
    if (sessionCallback) {
      sessionCallback({
        body: JSON.stringify(mockSession),
      });
    }

    expect(onSessionUpdate).toHaveBeenCalledWith(mockSession);
  });

  it('should handle location update messages', () => {
    const onSessionUpdate = vi.fn();
    const onLocationUpdate = vi.fn();
    const onAddressUpdate = vi.fn();

    const mockLocationUpdate = {
      participantId: 1,
      sessionId: 1,
      sessionIdString: 'test-session-id',
      userId: 1,
      latitude: 1.3521,
      longitude: 103.8198,
      accuracy: 10.0,
      updatedAt: new Date().toISOString(),
    };

    renderHook(() =>
      useWebSocket({
        sessionId: 'test-session-id',
        onSessionUpdate,
        onLocationUpdate,
        onAddressUpdate,
      }),
    );

    vi.advanceTimersByTime(500);

    // Simulate connection
    if (onConnectCallback) {
      onConnectCallback({});
    }

    // Get the callback for location updates
    const locationCallback = subscribeCallbacks.get('/topic/session/test-session-id/locations');
    if (locationCallback) {
      locationCallback({
        body: JSON.stringify(mockLocationUpdate),
      });
    }

    expect(onLocationUpdate).toHaveBeenCalledWith(
      {
        latitude: 1.3521,
        longitude: 103.8198,
        accuracy: 10.0,
        updatedAt: mockLocationUpdate.updatedAt,
      },
      1,
    );
  });

  it('should subscribe to optimal location updates when callback is provided', () => {
    const onSessionUpdate = vi.fn();
    const onLocationUpdate = vi.fn();
    const onAddressUpdate = vi.fn();
    const onOptimalLocationUpdate = vi.fn();

    renderHook(() =>
      useWebSocket({
        sessionId: 'test-session-id',
        onSessionUpdate,
        onLocationUpdate,
        onAddressUpdate,
        onOptimalLocationUpdate,
      }),
    );

    vi.advanceTimersByTime(500);

    // Simulate connection
    if (onConnectCallback) {
      onConnectCallback({});
    }

    expect(mockClient.subscribe).toHaveBeenCalledWith(
      '/topic/session/test-session-id/optimal-location',
      expect.any(Function),
    );
  });

  it('should subscribe to meeting location updates when callback is provided', () => {
    const onSessionUpdate = vi.fn();
    const onLocationUpdate = vi.fn();
    const onAddressUpdate = vi.fn();
    const onMeetingLocationUpdate = vi.fn();

    renderHook(() =>
      useWebSocket({
        sessionId: 'test-session-id',
        onSessionUpdate,
        onLocationUpdate,
        onAddressUpdate,
        onMeetingLocationUpdate,
      }),
    );

    vi.advanceTimersByTime(500);

    // Simulate connection
    if (onConnectCallback) {
      onConnectCallback({});
    }

    expect(mockClient.subscribe).toHaveBeenCalledWith(
      '/topic/session/test-session-id/meeting-location',
      expect.any(Function),
    );
  });

  it('should subscribe to session end notifications when callback is provided', () => {
    const onSessionUpdate = vi.fn();
    const onLocationUpdate = vi.fn();
    const onAddressUpdate = vi.fn();
    const onSessionEnd = vi.fn();

    renderHook(() =>
      useWebSocket({
        sessionId: 'test-session-id',
        onSessionUpdate,
        onLocationUpdate,
        onAddressUpdate,
        onSessionEnd,
      }),
    );

    vi.advanceTimersByTime(500);

    // Simulate connection
    if (onConnectCallback) {
      onConnectCallback({});
    }

    expect(mockClient.subscribe).toHaveBeenCalledWith(
      '/topic/session/test-session-id/end',
      expect.any(Function),
    );
  });

  it('should handle session end notification', () => {
    const onSessionUpdate = vi.fn();
    const onLocationUpdate = vi.fn();
    const onAddressUpdate = vi.fn();
    const onSessionEnd = vi.fn();

    const mockSessionEndNotification = {
      sessionId: 100,
      sessionIdString: 'test-session-id',
      status: 'Ended',
      endedAt: '2024-01-01T00:00:00',
      message: 'Session ended successfully',
      hasMeetingLocation: true,
      meetingLocationLatitude: 1.3521,
      meetingLocationLongitude: 103.8198,
    };

    renderHook(() =>
      useWebSocket({
        sessionId: 'test-session-id',
        onSessionUpdate,
        onLocationUpdate,
        onAddressUpdate,
        onSessionEnd,
      }),
    );

    vi.advanceTimersByTime(500);

    // Simulate connection
    if (onConnectCallback) {
      onConnectCallback({});
    }

    // Get the callback for session end notifications
    const sessionEndCallback = subscribeCallbacks.get('/topic/session/test-session-id/end');
    if (sessionEndCallback) {
      sessionEndCallback({
        body: JSON.stringify(mockSessionEndNotification),
      });
    }

    expect(onSessionEnd).toHaveBeenCalledWith(mockSessionEndNotification);
  });

  it('should not subscribe to session end when callback is not provided', () => {
    const onSessionUpdate = vi.fn();
    const onLocationUpdate = vi.fn();
    const onAddressUpdate = vi.fn();

    renderHook(() =>
      useWebSocket({
        sessionId: 'test-session-id',
        onSessionUpdate,
        onLocationUpdate,
        onAddressUpdate,
      }),
    );

    vi.advanceTimersByTime(500);

    // Simulate connection
    if (onConnectCallback) {
      onConnectCallback({});
    }

    // Should not subscribe to session end topic
    const subscribeCalls = vi.mocked(mockClient.subscribe).mock.calls;
    const sessionEndSubscribe = subscribeCalls.find(
      (call) => call[0] === '/topic/session/test-session-id/end',
    );
    expect(sessionEndSubscribe).toBeUndefined();
  });

  it('should cleanup WebSocket connection on unmount', () => {
    const onSessionUpdate = vi.fn();
    const onLocationUpdate = vi.fn();
    const onAddressUpdate = vi.fn();

    const { unmount } = renderHook(() =>
      useWebSocket({
        sessionId: 'test-session-id',
        onSessionUpdate,
        onLocationUpdate,
        onAddressUpdate,
      }),
    );

    vi.advanceTimersByTime(500);

    unmount();

    expect(mockClient.deactivate).toHaveBeenCalled();
  });

  it('should handle invalid JSON in messages gracefully', () => {
    const onSessionUpdate = vi.fn();
    const onLocationUpdate = vi.fn();
    const onAddressUpdate = vi.fn();

    const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {});

    renderHook(() =>
      useWebSocket({
        sessionId: 'test-session-id',
        onSessionUpdate,
        onLocationUpdate,
        onAddressUpdate,
      }),
    );

    vi.advanceTimersByTime(500);

    // Simulate connection
    if (onConnectCallback) {
      onConnectCallback({});
    }

    // Get the callback for session updates
    const sessionCallback = subscribeCallbacks.get('/topic/session/test-session-id');
    if (sessionCallback) {
      sessionCallback({
        body: 'invalid json',
      });
    }

    expect(consoleErrorSpy).toHaveBeenCalled();
    expect(onSessionUpdate).not.toHaveBeenCalled();

    consoleErrorSpy.mockRestore();
  });

  it('should deactivate existing connection before creating new one', () => {
    const onSessionUpdate = vi.fn();
    const onLocationUpdate = vi.fn();
    const onAddressUpdate = vi.fn();

    const { rerender } = renderHook(
      ({ sessionId }) =>
        useWebSocket({
          sessionId,
          onSessionUpdate,
          onLocationUpdate,
          onAddressUpdate,
        }),
      {
        initialProps: { sessionId: 'session-1' },
      },
    );

    vi.advanceTimersByTime(500);

    // Change sessionId
    rerender({ sessionId: 'session-2' });
    vi.advanceTimersByTime(500);

    // Should deactivate old connection before creating new one
    expect(mockClient.deactivate).toHaveBeenCalled();
  });
});

