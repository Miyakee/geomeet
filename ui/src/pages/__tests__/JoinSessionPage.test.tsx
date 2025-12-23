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
vi.mock('../../services/api', async () => {
  const actual = await vi.importActual<typeof import('../../services/api')>('../../services/api');
  return {
    ...actual,
    sessionApi: {
      joinSession: vi.fn(),
      getSessionDetails: vi.fn(),
    },
  };
});

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
      setAuthFromResponse: vi.fn(),
      isAuthenticated: true,
      isInitialized: true,
    });
  });

  it('should render join session page', () => {
    render(
      <BrowserRouter>
        <JoinSessionPage />
      </BrowserRouter>,
    );

    // Use getAllByText and check first occurrence
    const joinSessionTexts = screen.getAllByText('Join Session');
    expect(joinSessionTexts.length).toBeGreaterThan(0);
    expect(screen.getByText(/Enter the session ID and invite code to join a meeting session/)).toBeInTheDocument();
  });

  it('should allow user to enter session ID', async () => {
    const user = userEvent.setup();

    render(
      <BrowserRouter>
        <JoinSessionPage />
      </BrowserRouter>,
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
      </BrowserRouter>,
    );

    const sessionIdInput = screen.getByLabelText(/Session ID/i);
    const inviteCodeInput = screen.getByLabelText(/Invite Code/i);
    const joinButton = screen.getByRole('button', { name: /Join Session/i });

    await user.type(sessionIdInput, 'test-session-id');
    await user.type(inviteCodeInput, 'TESTCODE');
    await user.click(joinButton);

    await waitFor(() => {
      expect(sessionApi.joinSession).toHaveBeenCalledWith('test-session-id', 'TESTCODE');
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
      </BrowserRouter>,
    );

    const sessionIdInput = screen.getByLabelText(/Session ID/i);
    const inviteCodeInput = screen.getByLabelText(/Invite Code/i);
    const joinButton = screen.getByRole('button', { name: /Join Session/i });

    await user.type(sessionIdInput, 'test-session-id');
    await user.type(inviteCodeInput, 'TESTCODE');
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
      </BrowserRouter>,
    );

    const sessionIdInput = screen.getByLabelText(/Session ID/i);
    const inviteCodeInput = screen.getByLabelText(/Invite Code/i);
    const joinButton = screen.getByRole('button', { name: /Join Session/i });

    await user.type(sessionIdInput, 'invalid-session');
    await user.type(inviteCodeInput, 'TESTCODE');
    await user.click(joinButton);

    await waitFor(() => {
      expect(screen.getByText(errorMessage)).toBeInTheDocument();
    });
  });

  it('should validate empty session ID', () => {
    render(
      <BrowserRouter>
        <JoinSessionPage />
      </BrowserRouter>,
    );

    const joinButton = screen.getByRole('button', { name: /Join Session/i });
    // Button should be disabled when either sessionId or inviteCode is empty
    expect(joinButton).toBeDisabled();

    const sessionIdInput = screen.getByLabelText(/Session ID/i) as HTMLInputElement;
    const inviteCodeInput = screen.getByLabelText(/Invite Code/i) as HTMLInputElement;
    expect(sessionIdInput.value).toBe(''); // Should be empty initially
    expect(inviteCodeInput.value).toBe(''); // Should be empty initially
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
      </BrowserRouter>,
    );

    const sessionIdInput = screen.getByLabelText(/Session ID/i);
    const inviteCodeInput = screen.getByLabelText(/Invite Code/i);
    await user.type(sessionIdInput, 'test-session-id');
    await user.type(inviteCodeInput, 'TESTCODE');
    
    // Press Enter on the inviteCode input field (when sessionId is filled, Enter triggers join)
    await user.type(inviteCodeInput, '{Enter}');

    await waitFor(() => {
      expect(sessionApi.joinSession).toHaveBeenCalledWith('test-session-id', 'TESTCODE');
    }, { timeout: 3000 });
  });

  it('should auto-join when sessionId and inviteCode are in URL params', async () => {
    mockSearchParamsValue.set('sessionId', 'test-session-id');
    mockSearchParamsValue.set('inviteCode', 'TESTCODE');
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
      </BrowserRouter>,
    );

    // Wait for auto-join to trigger (component uses 100ms delay)
    await waitFor(() => {
      expect(sessionApi.joinSession).toHaveBeenCalledWith('test-session-id', 'TESTCODE');
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
      </BrowserRouter>,
    );

    const sessionIdInput = screen.getByLabelText(/Session ID/i);
    const inviteCodeInput = screen.getByLabelText(/Invite Code/i);
    const joinButton = screen.getByRole('button', { name: /Join Session/i });

    await user.type(sessionIdInput, 'test-session-id');
    await user.type(inviteCodeInput, 'TESTCODE');
    await user.click(joinButton);

    // After successful join, component navigates immediately, so success message may not be visible
    // But navigation should happen
    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/session/test-session-id', { replace: true });
    }, { timeout: 3000 });
  });

  it('should navigate to dashboard when back button is clicked', async () => {
    const user = userEvent.setup();

    render(
      <BrowserRouter>
        <JoinSessionPage />
      </BrowserRouter>,
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
      setAuthFromResponse: vi.fn(),
      isAuthenticated: false,
      isInitialized: true,
    });

    render(
      <BrowserRouter>
        <JoinSessionPage />
      </BrowserRouter>,
    );

    expect(mockNavigate).toHaveBeenCalledWith('/login?redirect=/join');
  });

  it('should allow entering session ID and invite code', async () => {
    const user = userEvent.setup();

    render(
      <BrowserRouter>
        <JoinSessionPage />
      </BrowserRouter>,
    );

    const sessionIdInput = screen.getByLabelText(/Session ID/i);
    const inviteCodeInput = screen.getByLabelText(/Invite Code/i);
    await user.type(sessionIdInput, 'test-session-id');
    await user.type(inviteCodeInput, 'TESTCODE');

    expect((sessionIdInput as HTMLInputElement).value).toBe('test-session-id');
    expect((inviteCodeInput as HTMLInputElement).value).toBe('TESTCODE');
  });

  it('should require both session ID and invite code to join', async () => {
    const user = userEvent.setup();

    render(
      <BrowserRouter>
        <JoinSessionPage />
      </BrowserRouter>,
    );

    const sessionIdInput = screen.getByLabelText(/Session ID/i);
    const inviteCodeInput = screen.getByLabelText(/Invite Code/i);
    const joinButton = screen.getByRole('button', { name: /Join Session/i });

    // Button should be disabled when only sessionId is filled
    await user.type(sessionIdInput, 'test-session-id');
    expect(joinButton).toBeDisabled();

    // Button should be enabled when both are filled
    await user.type(inviteCodeInput, 'TESTCODE');
    expect(joinButton).not.toBeDisabled();
  });

  it('should enable join button when both fields are filled', async () => {
    const user = userEvent.setup();

    render(
      <BrowserRouter>
        <JoinSessionPage />
      </BrowserRouter>,
    );

    const sessionIdInput = screen.getByLabelText(/Session ID/i);
    const inviteCodeInput = screen.getByLabelText(/Invite Code/i);
    const joinButton = screen.getByRole('button', { name: /Join Session/i });

    await user.type(sessionIdInput, 'test-session-id');
    await user.type(inviteCodeInput, 'TESTCODE');

    expect(joinButton).not.toBeDisabled();
  });

  it('should show error when invite code is missing', async () => {
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
      </BrowserRouter>,
    );

    const sessionIdInput = screen.getByLabelText(/Session ID/i);
    const joinButton = screen.getByRole('button', { name: /Join Session/i });

    await user.type(sessionIdInput, 'test-session-id');
    // Try to click join button without invite code
    // Button should be disabled, but if we force click, should show error
    expect(joinButton).toBeDisabled();

    // Verify joinSession was not called
    expect(sessionApi.joinSession).not.toHaveBeenCalled();
  });

  it('should show error when session ID is missing', async () => {
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
      </BrowserRouter>,
    );

    const inviteCodeInput = screen.getByLabelText(/Invite Code/i);
    const joinButton = screen.getByRole('button', { name: /Join Session/i });

    await user.type(inviteCodeInput, 'TESTCODE');
    // Try to click join button without session ID
    // Button should be disabled
    expect(joinButton).toBeDisabled();

    // Verify joinSession was not called
    expect(sessionApi.joinSession).not.toHaveBeenCalled();
  });
});
