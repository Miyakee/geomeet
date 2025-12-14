import {describe, it, expect, beforeEach, vi} from 'vitest';
import {renderHook, act} from '@testing-library/react';
import {AuthProvider, useAuth} from '../AuthContext';
import {authApi} from '../../services/api';
import {ReactNode} from 'react';

// Mock the API
vi.mock('../../services/api', () => ({
    authApi: {
        login: vi.fn(),
    },
}));

const wrapper = ({children}: { children: ReactNode }) => (
    <AuthProvider>{children}</AuthProvider>
);

describe('AuthContext', () => {
    beforeEach(() => {
        localStorage.clear();
        vi.clearAllMocks();
    });

    it('should initialize with no user and token', () => {
        const {result} = renderHook(() => useAuth(), {wrapper});

        expect(result.current.user).toBeNull();
        expect(result.current.token).toBeNull();
        expect(result.current.isAuthenticated).toBe(false);
        expect(result.current.isInitialized).toBe(true);
    });

    it('should load user and token from localStorage on mount', () => {
        const mockUser = {username: 'testuser', email: 'test@example.com'};
        localStorage.setItem('token', 'test-token');
        localStorage.setItem('user', JSON.stringify(mockUser));

        const {result} = renderHook(() => useAuth(), {wrapper});

        expect(result.current.token).toBe('test-token');
        expect(result.current.user).toEqual(mockUser);
        expect(result.current.isAuthenticated).toBe(true);
    });

    it('should login successfully', async () => {
        const mockResponse = {
            token: 'new-token',
            username: 'testuser',
            email: 'test@example.com',
            message: 'Login successful',
        };

        vi.mocked(authApi.login).mockResolvedValue(mockResponse);

        const {result} = renderHook(() => useAuth(), {wrapper});

        await act(async () => {
            await result.current.login({
                usernameOrEmail: 'testuser',
                password: 'password123',
            });
        });

        expect(result.current.token).toBe('new-token');
        expect(result.current.user).toEqual({
            id: 0,
            username: 'testuser',
            email: 'test@example.com',
        });
        expect(result.current.isAuthenticated).toBe(true);
        expect(localStorage.getItem('token')).toBe('new-token');
        expect(localStorage.getItem('user')).toBe(
            JSON.stringify({id: 0, username: 'testuser', email: 'test@example.com'})
        );
    });

    it('should logout successfully', () => {
        localStorage.setItem('token', 'test-token');
        localStorage.setItem('user', JSON.stringify({username: 'test', email: 'test@test.com'}));

        const {result} = renderHook(() => useAuth(), {wrapper});

        act(() => {
            result.current.logout();
        });

        expect(result.current.token).toBeNull();
        expect(result.current.user).toBeNull();
        expect(result.current.isAuthenticated).toBe(false);
        expect(localStorage.getItem('token')).toBeNull();
        expect(localStorage.getItem('user')).toBeNull();
    });

    it('should throw error on login failure', async () => {
        const mockError = new Error('Invalid credentials');
        vi.mocked(authApi.login).mockRejectedValue(mockError);

        const {result} = renderHook(() => useAuth(), {wrapper});

        await act(async () => {
            await expect(
                result.current.login({
                    usernameOrEmail: 'testuser',
                    password: 'wrongpassword',
                })
            ).rejects.toThrow('Invalid credentials');
        });

        expect(result.current.token).toBeNull();
        expect(result.current.user).toBeNull();
        expect(result.current.isAuthenticated).toBe(false);
    });
});

