import { describe, it, expect, beforeEach, vi } from 'vitest';
import { authApi, sessionApi } from '../api';
import axios from 'axios';

// Mock axios module
vi.mock('axios', () => {
  const mockPost = vi.fn();
  const mockGet = vi.fn();
  const mockPut = vi.fn();
  const mockDelete = vi.fn();
  const mockAxiosInstance = {
    post: mockPost,
    get: mockGet,
    put: mockPut,
    delete: mockDelete,
    interceptors: {
      request: { use: vi.fn() },
    },
  };
  return {
    default: {
      create: vi.fn(() => mockAxiosInstance),
    },
  };
});

describe('API Service', () => {
  let mockPost: any;
  let mockGet: any;
  let _mockPut: any;
  let mockDelete: any;

  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
    const axiosInstance = (axios.create as any)();
    mockPost = axiosInstance.post;
    mockGet = axiosInstance.get;
    _mockPut = axiosInstance.put;
    mockDelete = axiosInstance.delete;
  });

  describe('authApi', () => {
    it('should login successfully', async () => {
      const mockResponse = {
        data: {
          token: 'test-token',
          username: 'testuser',
          email: 'test@example.com',
          message: 'Login successful',
        },
      };

      mockPost.mockResolvedValue(mockResponse);

      const result = await authApi.login({
        usernameOrEmail: 'testuser',
        password: 'password123',
      });

      expect(result.token).toBe('test-token');
      expect(result.username).toBe('testuser');
      expect(result.email).toBe('test@example.com');
      expect(mockPost).toHaveBeenCalledWith('/api/auth/login', {
        usernameOrEmail: 'testuser',
        password: 'password123',
      });
    });

    it('should handle login error', async () => {
      const mockError = {
        response: {
          data: {
            message: 'Invalid credentials',
          },
        },
      };

      mockPost.mockRejectedValue(mockError);

      await expect(
        authApi.login({
          usernameOrEmail: 'testuser',
          password: 'wrongpassword',
        }),
      ).rejects.toEqual(mockError);
    });
  });

  describe('sessionApi', () => {
    it('should create session successfully', async () => {
      const mockResponse = {
        data: {
          id: 1,
          sessionId: 'test-session-id',
          initiatorId: 1,
          status: 'Active',
          createdAt: '2024-01-01T00:00:00',
          message: 'Session created successfully',
        },
      };

      mockPost.mockResolvedValue(mockResponse);

      const result = await sessionApi.createSession();

      expect(result.id).toBe(1);
      expect(result.sessionId).toBe('test-session-id');
      expect(result.status).toBe('Active');
      expect(mockPost).toHaveBeenCalledWith('/api/sessions', {});
    });

    it('should join session successfully', async () => {
      const mockResponse = {
        data: {
          participantId: 1,
          sessionId: 100,
          sessionIdString: 'test-session-id',
          userId: 1,
          joinedAt: '2024-01-01T00:00:00',
          message: 'Successfully joined the session',
        },
      };

      mockPost.mockResolvedValue(mockResponse);

      const result = await sessionApi.joinSession('test-session-id');

      expect(result.participantId).toBe(1);
      expect(result.sessionIdString).toBe('test-session-id');
      expect(result.message).toBe('Successfully joined the session');
      expect(mockPost).toHaveBeenCalledWith('/api/sessions/join', {
        sessionId: 'test-session-id',
      });
    });

    it('should generate invite link successfully', async () => {
      const mockResponse = {
        data: {
          sessionId: 'test-session-id',
          inviteLink: '/join?sessionId=test-session-id',
          inviteCode: 'test-session-id',
          message: 'Invitation link generated successfully',
        },
      };

      mockGet.mockResolvedValue(mockResponse);

      const result = await sessionApi.generateInviteLink('test-session-id');

      expect(result.sessionId).toBe('test-session-id');
      expect(result.inviteLink).toBe('/join?sessionId=test-session-id');
      expect(result.inviteCode).toBe('test-session-id');
      expect(mockGet).toHaveBeenCalledWith('/api/sessions/test-session-id/invite');
    });

    it('should end session successfully', async () => {
      const mockResponse = {
        data: {
          sessionId: 100,
          sessionIdString: 'test-session-id',
          status: 'Ended',
          endedAt: '2024-01-01T12:00:00',
          message: 'Session ended successfully',
        },
      };

      mockDelete.mockResolvedValue(mockResponse);

      const result = await sessionApi.endSession('test-session-id');

      expect(result.sessionId).toBe(100);
      expect(result.sessionIdString).toBe('test-session-id');
      expect(result.status).toBe('Ended');
      expect(result.endedAt).toBe('2024-01-01T12:00:00');
      expect(result.message).toBe('Session ended successfully');
      expect(mockDelete).toHaveBeenCalledWith('/api/sessions/test-session-id');
    });

    it('should handle end session error', async () => {
      const mockError = {
        response: {
          status: 403,
          data: {
            message: 'Only the session initiator can end the session',
          },
        },
      };

      mockDelete.mockRejectedValue(mockError);

      await expect(sessionApi.endSession('test-session-id')).rejects.toEqual(mockError);
    });
  });
});

