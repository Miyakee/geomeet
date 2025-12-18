import { describe, it, expect, vi, beforeEach } from 'vitest';
import { authApi, sessionApi, ApiError } from '../api';

// Mock fetch globally
global.fetch = vi.fn();

describe('API Service', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  describe('authApi', () => {
    it('should login successfully', async () => {
      const mockResponse = {
        token: 'test-token',
        username: 'testuser',
        email: 'test@example.com',
        message: 'Login successful',
      };

      (global.fetch as any).mockResolvedValueOnce({
        ok: true,
        headers: new Headers({
          'content-type': 'application/json',
        }),
        json: async () => mockResponse,
      });

      const result = await authApi.login({
        usernameOrEmail: 'testuser',
        password: 'password123',
      });

      expect(result.token).toBe('test-token');
      expect(result.username).toBe('testuser');
      expect(result.email).toBe('test@example.com');
      expect(global.fetch).toHaveBeenCalledWith(
        '/api/auth/login',
        expect.objectContaining({
          method: 'POST',
          headers: expect.objectContaining({
            'Content-Type': 'application/json',
          }),
          body: JSON.stringify({
            usernameOrEmail: 'testuser',
            password: 'password123',
          }),
        }),
      );
    });

    it('should handle login error', async () => {
      const mockError = {
        message: 'Invalid credentials',
      };

      (global.fetch as any).mockResolvedValueOnce({
        ok: false,
        status: 401,
        json: async () => mockError,
      });

      await expect(
        authApi.login({
          usernameOrEmail: 'testuser',
          password: 'wrongpassword',
        }),
      ).rejects.toThrow();
    });
  });

  describe('sessionApi', () => {
    it('should create session successfully', async () => {
      const mockResponse = {
        id: 1,
        sessionId: 'test-session-id',
        initiatorId: 1,
        status: 'Active',
        createdAt: '2024-01-01T00:00:00Z',
        message: 'Session created',
      };

      (global.fetch as any).mockResolvedValueOnce({
        ok: true,
        headers: new Headers({
          'content-type': 'application/json',
        }),
        json: async () => mockResponse,
      });

      const result = await sessionApi.createSession();

      expect(result.sessionId).toBe('test-session-id');
      expect(global.fetch).toHaveBeenCalledWith(
        '/api/sessions',
        expect.objectContaining({
          method: 'POST',
        }),
      );
    });

    it('should get session details', async () => {
      const mockResponse = {
        sessionId: 'test-session-id',
        status: 'Active',
        participants: [],
      };

      (global.fetch as any).mockResolvedValueOnce({
        ok: true,
        headers: new Headers({
          'content-type': 'application/json',
        }),
        json: async () => mockResponse,
      });

      const result = await sessionApi.getSessionDetails('test-session-id');

      expect(result.sessionId).toBe('test-session-id');
      expect(global.fetch).toHaveBeenCalledWith(
        '/api/sessions/test-session-id',
        expect.objectContaining({
          method: 'GET',
        }),
      );
    });

    it('should handle API errors with status codes', async () => {
      (global.fetch as any).mockResolvedValueOnce({
        ok: false,
        status: 404,
        json: async () => ({ message: 'Session not found' }),
      });

      await expect(sessionApi.getSessionDetails('invalid-id')).rejects.toThrow();
    });
  });

  describe('ApiError', () => {
    it('should create ApiError with correct structure', () => {
      const error = new ApiError('Test error', 404, { message: 'Not found' });

      expect(error.message).toBe('Test error');
      expect(error.status).toBe(404);
      expect(error.response).toEqual({
        status: 404,
        data: { message: 'Not found' },
      });
    });
  });
});
