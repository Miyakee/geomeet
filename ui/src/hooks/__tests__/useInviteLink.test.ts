import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { useInviteLink } from '../useInviteLink';
import { sessionApi } from '../../services/api';

vi.mock('../../services/api', () => ({
  sessionApi: {
    generateInviteLink: vi.fn(),
  },
}));

// Mock navigator.clipboard
Object.assign(navigator, {
  clipboard: {
    writeText: vi.fn(),
  },
});

describe('useInviteLink', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('should initialize with empty state', () => {
    const { result } = renderHook(() => useInviteLink('test-session-id'));

    expect(result.current.inviteLink).toBeNull();
    expect(result.current.inviteCode).toBeNull();
    expect(result.current.copied).toBe(false);
    expect(result.current.loadingInvite).toBe(false);
  });

  it('should load invite link successfully', async () => {
    const mockInvite = {
      sessionId: 'test-session-id',
      inviteLink: '/join?sessionId=test-session-id',
      inviteCode: 'test-session-id',
      message: 'Invitation link generated successfully',
    };

    vi.mocked(sessionApi.generateInviteLink).mockResolvedValue(mockInvite);

    const { result } = renderHook(() => useInviteLink('test-session-id'));

    await act(async () => {
      await result.current.loadInviteLink();
    });

    expect(result.current.inviteLink).toBe('http://localhost:3000/join?sessionId=test-session-id');
    expect(result.current.inviteCode).toBe('test-session-id');
    expect(result.current.loadingInvite).toBe(false);
  });

  it('should handle error when loading invite link', async () => {
    const mockError = new Error('Failed to generate invite link');
    vi.mocked(sessionApi.generateInviteLink).mockRejectedValue(mockError);

    const { result } = renderHook(() => useInviteLink('test-session-id'));

    await act(async () => {
      await result.current.loadInviteLink();
    });

    expect(result.current.inviteLink).toBeNull();
    expect(result.current.inviteCode).toBeNull();
    expect(result.current.loadingInvite).toBe(false);
  });

  it('should copy invite link to clipboard', async () => {
    const mockInvite = {
      sessionId: 'test-session-id',
      inviteLink: '/join?sessionId=test-session-id',
      inviteCode: 'test-session-id',
      message: 'Invitation link generated successfully',
    };

    vi.mocked(sessionApi.generateInviteLink).mockResolvedValue(mockInvite);
    vi.mocked(navigator.clipboard.writeText).mockResolvedValue();

    const { result } = renderHook(() => useInviteLink('test-session-id'));

    await act(async () => {
      await result.current.loadInviteLink();
    });

    await act(async () => {
      result.current.handleCopyInviteLink();
    });

    expect(navigator.clipboard.writeText).toHaveBeenCalledWith(
      'http://localhost:3000/join?sessionId=test-session-id',
    );
    expect(result.current.copied).toBe(true);

    act(() => {
      vi.advanceTimersByTime(2000);
    });

    expect(result.current.copied).toBe(false);
  });

  it('should copy invite code to clipboard', async () => {
    const mockInvite = {
      sessionId: 'test-session-id',
      inviteLink: '/join?sessionId=test-session-id',
      inviteCode: 'test-session-id',
      message: 'Invitation link generated successfully',
    };

    vi.mocked(sessionApi.generateInviteLink).mockResolvedValue(mockInvite);
    vi.mocked(navigator.clipboard.writeText).mockResolvedValue();

    const { result } = renderHook(() => useInviteLink('test-session-id'));

    await act(async () => {
      await result.current.loadInviteLink();
    });

    await act(async () => {
      result.current.handleCopyInviteCode();
    });

    expect(navigator.clipboard.writeText).toHaveBeenCalledWith('test-session-id');
    expect(result.current.copied).toBe(true);
  });

  it('should not copy when invite link is null', () => {
    const { result } = renderHook(() => useInviteLink('test-session-id'));

    act(() => {
      result.current.handleCopyInviteLink();
    });

    expect(navigator.clipboard.writeText).not.toHaveBeenCalled();
  });
});

