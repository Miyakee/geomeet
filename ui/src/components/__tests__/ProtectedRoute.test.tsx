import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import ProtectedRoute from '../ProtectedRoute';
import * as AuthContext from '../../contexts/AuthContext';

// Mock useAuth
vi.mock('../../contexts/AuthContext', () => ({
  useAuth: vi.fn(),
  AuthProvider: ({ children }: { children: React.ReactNode }) => <>{children}</>,
}));

const TestComponent = () => <div>Protected Content</div>;

const renderWithRouter = (isAuthenticated: boolean, isInitialized: boolean = true) => {
  vi.mocked(AuthContext.useAuth).mockReturnValue({
    user: isAuthenticated ? { id: 1, username: 'test', email: 'test@test.com' } : null,
    token: isAuthenticated ? 'test-token' : null,
    login: vi.fn(),
    logout: vi.fn(),
    isAuthenticated,
    isInitialized,
  });

  return render(
    <BrowserRouter>
      <ProtectedRoute>
        <TestComponent />
      </ProtectedRoute>
    </BrowserRouter>,
  );
};

describe('ProtectedRoute', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should render children when authenticated', () => {
    renderWithRouter(true);
    expect(screen.getByText('Protected Content')).toBeInTheDocument();
  });

  it('should redirect to login when not authenticated', () => {
    renderWithRouter(false);
    // Should redirect to /login
    expect(window.location.pathname).toBe('/login');
  });

  it('should not render children while initializing', () => {
    renderWithRouter(false, false);
    expect(screen.queryByText('Protected Content')).not.toBeInTheDocument();
  });
});

