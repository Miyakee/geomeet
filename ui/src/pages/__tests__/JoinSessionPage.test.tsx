import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import JoinSessionPage from '../JoinSessionPage';
import * as AuthContext from '../../contexts/AuthContext';
import { sessionApi } from '../../services/api';

// Mock useAuth
vi.mock('../../contexts/AuthContext', () => ({
  useAuth: vi.fn(),
}));

// Mock sessionApi
vi.mock('../../services/api', () => ({
  sessionApi: {
    joinSession: vi.fn(),
  },
}));

// Mock useNavigate and useSearchParams
const mockNavigate = vi.fn();
let mockSearchParamsValue = new URLSearchParams();

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
    useSearchParams: () => [mockSearchParamsValue],
  };
});

describe('JoinSessionPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockSearchParamsValue = new URLSearchParams();
    vi.mocked(AuthContext.useAuth).mockReturnValue({
      user: { id: 1, username: 'testuser', email: 'test@example.com' },
      token: 'test-token',
      login: vi.fn(),
      logout: vi.fn(),
      isAuthenticated: true,
      isInitialized: true,
    });
  });

  it('should render join session page', () => {
    render(
      <BrowserRouter>
        <JoinSessionPage />
      </BrowserRouter>
    );

    // Use getAllByText and check first occurrence
    const joinSessionTexts = screen.getAllByText('Join Session');
    expect(joinSessionTexts.length).toBeGreaterThan(0);
    expect(screen.getByText(/Enter the session ID or invitation code/)).toBeInTheDocument();
  });

  it('should allow user to enter session ID', async () => {
    const user = userEvent.setup();

    render(
      <BrowserRouter>
        <JoinSessionPage />
      </BrowserRouter>
    );

    const sessionIdInput = screen.getByLabelText(/Session ID/i) as HTMLInputElement;
    await user.type(sessionIdInput, 'test-session-id');

    expect(sessionIdInput.value).toBe('test-session-id');
  });

  it('should join session when button is clicked', async () => {
    const user = userEvent.setup();
    const mockResponse = {
      participantId: 1,
      sessionId: 100,
      sessionIdString: 'test-session-id',
      userId: 1,
      joinedAt: '2024-01-01T00:00:00',
      message: 'Successfully joined the session',
    };

    vi.mocked(sessionApi.joinSession).mockResolvedValue(mockResponse);

    render(
      <BrowserRouter>
        <JoinSessionPage />
      </BrowserRouter>
    );

    const sessionIdInput = screen.getByLabelText(/Session ID/i);
    const joinButton = screen.getByRole('button', { name: /Join Session/i });

    await user.type(sessionIdInput, 'test-session-id');
    await user.click(joinButton);

    await waitFor(() => {
      expect(sessionApi.joinSession).toHaveBeenCalledWith('test-session-id');
    });
  });

  it('should navigate to session page after successful join', async () => {
    const user = userEvent.setup();
    const mockResponse = {
      participantId: 1,
      sessionId: 100,
      sessionIdString: 'test-session-id',
      userId: 1,
      joinedAt: '2024-01-01T00:00:00',
      message: 'Successfully joined the session',
    };

    vi.mocked(sessionApi.joinSession).mockResolvedValue(mockResponse);

    render(
      <BrowserRouter>
        <JoinSessionPage />
      </BrowserRouter>
    );

    const sessionIdInput = screen.getByLabelText(/Session ID/i);
    const joinButton = screen.getByRole('button', { name: /Join Session/i });

    await user.type(sessionIdInput, 'test-session-id');
    await user.click(joinButton);

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/session/test-session-id', { replace: true });
    }, { timeout: 3000 });
  });

  it('should display error message when join fails', async () => {
    const user = userEvent.setup();
    const errorMessage = 'Session not found';
    vi.mocked(sessionApi.joinSession).mockRejectedValue({
      response: {
        data: {
          message: errorMessage,
        },
      },
    });

    render(
      <BrowserRouter>
        <JoinSessionPage />
      </BrowserRouter>
    );

    const sessionIdInput = screen.getByLabelText(/Session ID/i);
    const joinButton = screen.getByRole('button', { name: /Join Session/i });

    await user.type(sessionIdInput, 'invalid-session');
    await user.click(joinButton);

    await waitFor(() => {
      expect(screen.getByText(errorMessage)).toBeInTheDocument();
    });
  });

  it('should validate empty session ID', () => {
    render(
      <BrowserRouter>
        <JoinSessionPage />
      </BrowserRouter>
    );

    const joinButton = screen.getByRole('button', { name: /Join Session/i });
    expect(joinButton).toBeDisabled();

    const sessionIdInput = screen.getByLabelText(/Session ID/i) as HTMLInputElement;
    expect(sessionIdInput.value).toBe(''); // Should be empty initially
    expect(joinButton).toBeDisabled();
  });

  it('should join session when Enter key is pressed', async () => {
    const user = userEvent.setup();
    const mockResponse = {
      participantId: 1,
      sessionId: 100,
      sessionIdString: 'test-session-id',
      userId: 1,
      joinedAt: '2024-01-01T00:00:00',
      message: 'Successfully joined the session',
    };

    vi.mocked(sessionApi.joinSession).mockResolvedValue(mockResponse);

    render(
      <BrowserRouter>
        <JoinSessionPage />
      </BrowserRouter>
    );

    const sessionIdInput = screen.getByLabelText(/Session ID/i);
    await user.type(sessionIdInput, 'test-session-id');
    
    // Press Enter on the input field
    await user.type(sessionIdInput, '{Enter}');

    await waitFor(() => {
      expect(sessionApi.joinSession).toHaveBeenCalledWith('test-session-id');
    }, { timeout: 3000 });
  });

  it('should auto-join when sessionId is in URL params', async () => {
    mockSearchParamsValue.set('sessionId', 'test-session-id');
    const mockResponse = {
      participantId: 1,
      sessionId: 100,
      sessionIdString: 'test-session-id',
      userId: 1,
      joinedAt: '2024-01-01T00:00:00',
      message: 'Successfully joined the session',
    };

    vi.mocked(sessionApi.joinSession).mockResolvedValue(mockResponse);

    render(
      <BrowserRouter>
        <JoinSessionPage />
      </BrowserRouter>
    );

    // Wait for auto-join to trigger (component uses 100ms delay)
    await waitFor(() => {
      expect(sessionApi.joinSession).toHaveBeenCalledWith('test-session-id');
    }, { timeout: 2000 });
  });

  it('should show success message after joining', async () => {
    const user = userEvent.setup();
    const mockResponse = {
      participantId: 1,
      sessionId: 100,
      sessionIdString: 'test-session-id',
      userId: 1,
      joinedAt: '2024-01-01T00:00:00',
      message: 'Successfully joined the session',
    };

    vi.mocked(sessionApi.joinSession).mockResolvedValue(mockResponse);

    render(
      <BrowserRouter>
        <JoinSessionPage />
      </BrowserRouter>
    );

    const sessionIdInput = screen.getByLabelText(/Session ID/i);
    const joinButton = screen.getByRole('button', { name: /Join Session/i });

    await user.type(sessionIdInput, 'test-session-id');
    await user.click(joinButton);

    await waitFor(() => {
      expect(screen.getByText(/Successfully joined the session/)).toBeInTheDocument();
    }, { timeout: 3000 });
  });

  it('should navigate to dashboard when back button is clicked', async () => {
    const user = userEvent.setup();

    render(
      <BrowserRouter>
        <JoinSessionPage />
      </BrowserRouter>
    );

    const backButton = screen.getByRole('button', { name: /Back to Dashboard/i });
    expect(backButton).toBeInTheDocument();
    await user.click(backButton);

    expect(mockNavigate).toHaveBeenCalledWith('/dashboard');
  });

  it('should redirect to login when not authenticated', () => {
    vi.mocked(AuthContext.useAuth).mockReturnValue({
      user: null,
      token: null,
      login: vi.fn(),
      logout: vi.fn(),
      isAuthenticated: false,
      isInitialized: true,
    });

    render(
      <BrowserRouter>
        <JoinSessionPage />
      </BrowserRouter>
    );

    expect(mockNavigate).toHaveBeenCalledWith('/login?redirect=/join');
  });
});
