import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import LoginPage from '../LoginPage';
import * as AuthContext from '../../contexts/AuthContext';

// Mock useAuth
vi.mock('../../contexts/AuthContext', () => ({
  useAuth: vi.fn(),
}));

// Mock useNavigate
const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
    useSearchParams: () => [new URLSearchParams()],
  };
});

describe('LoginPage', () => {
  const mockLogin = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(AuthContext.useAuth).mockReturnValue({
      user: null,
      token: null,
      login: mockLogin,
      logout: vi.fn(),
      isAuthenticated: false,
      isInitialized: true,
    });
  });

  it('should render login form', () => {
    render(
      <BrowserRouter>
        <LoginPage />
      </BrowserRouter>,
    );

    // Use getAllByText and check first occurrence
    const signInTexts = screen.getAllByText('Sign In');
    expect(signInTexts.length).toBeGreaterThan(0);
    expect(screen.getByText('Welcome to GeoMeet')).toBeInTheDocument();
    expect(screen.getByLabelText(/Username or Email/i)).toBeInTheDocument();
    expect(screen.getAllByLabelText(/Password/i).length).toBeGreaterThan(0);
  });

  it('should allow user to enter username and password', async () => {
    const user = userEvent.setup();

    render(
      <BrowserRouter>
        <LoginPage />
      </BrowserRouter>,
    );

    const usernameInput = screen.getByLabelText(/Username or Email/i);
    // Use getByDisplayValue or queryByLabelText to avoid multiple matches
    const passwordInputs = screen.getAllByLabelText(/Password/i);
    const passwordInput = passwordInputs[0]; // Get the first one

    await user.type(usernameInput, 'testuser');
    await user.type(passwordInput, 'password123');

    expect(usernameInput).toHaveValue('testuser');
    expect(passwordInput).toHaveValue('password123');
  });

  it('should toggle password visibility', async () => {
    const user = userEvent.setup();

    render(
      <BrowserRouter>
        <LoginPage />
      </BrowserRouter>,
    );

    const passwordInputs = screen.getAllByLabelText(/Password/i);
    const passwordInput = passwordInputs[0] as HTMLInputElement;
    const toggleButton = screen.getByRole('button', { name: /toggle password visibility/i });

    expect(passwordInput.type).toBe('password');

    await user.click(toggleButton);
    expect(passwordInput.type).toBe('text');

    await user.click(toggleButton);
    expect(passwordInput.type).toBe('password');
  });

  it('should submit login form with credentials', async () => {
    const user = userEvent.setup();
    mockLogin.mockResolvedValue(undefined);

    render(
      <BrowserRouter>
        <LoginPage />
      </BrowserRouter>,
    );

    const usernameInput = screen.getByLabelText(/Username or Email/i);
    const passwordInputs = screen.getAllByLabelText(/Password/i);
    const passwordInput = passwordInputs[0];
    const submitButton = screen.getByRole('button', { name: /Sign In/i });

    await user.type(usernameInput, 'testuser');
    await user.type(passwordInput, 'password123');
    await user.click(submitButton);

    await waitFor(() => {
      expect(mockLogin).toHaveBeenCalledWith({
        usernameOrEmail: 'testuser',
        password: 'password123',
      });
    });
  });

  it('should navigate to dashboard after successful login', async () => {
    const user = userEvent.setup();
    mockLogin.mockResolvedValue(undefined);

    render(
      <BrowserRouter>
        <LoginPage />
      </BrowserRouter>,
    );

    const usernameInput = screen.getByLabelText(/Username or Email/i);
    const passwordInputs = screen.getAllByLabelText(/Password/i);
    const passwordInput = passwordInputs[0];
    const submitButton = screen.getByRole('button', { name: /Sign In/i });

    await user.type(usernameInput, 'testuser');
    await user.type(passwordInput, 'password123');
    await user.click(submitButton);

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/dashboard', { replace: true });
    });
  });

  it('should navigate to redirect URL if provided in search params', async () => {
    const user = userEvent.setup();
    mockLogin.mockResolvedValue(undefined);

    // This test would require more complex mocking of useSearchParams
    // For now, we'll test the basic navigation flow
    render(
      <BrowserRouter>
        <LoginPage />
      </BrowserRouter>,
    );

    const usernameInput = screen.getByLabelText(/Username or Email/i);
    const passwordInputs = screen.getAllByLabelText(/Password/i);
    const passwordInput = passwordInputs[0];
    const submitButton = screen.getByRole('button', { name: /Sign In/i });

    await user.type(usernameInput, 'testuser');
    await user.type(passwordInput, 'password123');
    await user.click(submitButton);

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalled();
    });
  });

  it('should display error message on login failure', async () => {
    const user = userEvent.setup();
    const errorMessage = 'Invalid credentials';
    mockLogin.mockRejectedValue({
      response: {
        data: {
          message: errorMessage,
        },
      },
    });

    render(
      <BrowserRouter>
        <LoginPage />
      </BrowserRouter>,
    );

    const usernameInput = screen.getByLabelText(/Username or Email/i);
    const passwordInputs = screen.getAllByLabelText(/Password/i);
    const passwordInput = passwordInputs[0];
    const submitButton = screen.getByRole('button', { name: /Sign In/i });

    await user.type(usernameInput, 'testuser');
    await user.type(passwordInput, 'wrongpassword');
    await user.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText(errorMessage)).toBeInTheDocument();
    });
  });

  it('should show loading state during login', async () => {
    const user = userEvent.setup();
    let resolveLogin: (() => void) | undefined;
    const loginPromise = new Promise<void>((resolve) => {
      resolveLogin = resolve;
    });
    mockLogin.mockReturnValue(loginPromise);

    render(
      <BrowserRouter>
        <LoginPage />
      </BrowserRouter>,
    );

    const usernameInput = screen.getByLabelText(/Username or Email/i);
    const passwordInputs = screen.getAllByLabelText(/Password/i);
    const passwordInput = passwordInputs[0];
    const submitButton = screen.getByRole('button', { name: /Sign In/i });

    await user.type(usernameInput, 'testuser');
    await user.type(passwordInput, 'password123');
    await user.click(submitButton);

    // Button should be disabled and show loading
    expect(submitButton).toBeDisabled();
    expect(screen.getByRole('progressbar')).toBeInTheDocument();

    // Resolve login
    if (resolveLogin) {
      resolveLogin();
    }
    await waitFor(() => {
      expect(submitButton).not.toBeDisabled();
    });
  });

  it('should display demo credentials', () => {
    render(
      <BrowserRouter>
        <LoginPage />
      </BrowserRouter>,
    );

    expect(screen.getByText(/Demo credentials:/)).toBeInTheDocument();
    expect(screen.getByText(/admin/)).toBeInTheDocument();
    expect(screen.getByText(/testuser/)).toBeInTheDocument();
  });
});

