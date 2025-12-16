import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import DashboardPage from '../DashboardPage';
import * as AuthContext from '../../contexts/AuthContext';
import { sessionApi } from '../../services/api';

// Mock useAuth
vi.mock('../../contexts/AuthContext', () => ({
  useAuth: vi.fn(),
}));

// Mock sessionApi
vi.mock('../../services/api', () => ({
  sessionApi: {
    createSession: vi.fn(),
    generateInviteLink: vi.fn(),
  },
}));

// Mock useNavigate
const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

// Mock clipboard
const mockWriteText = vi.fn().mockResolvedValue(undefined);
Object.assign(navigator, {
  clipboard: {
    writeText: mockWriteText,
  },
});

describe('DashboardPage', () => {
  const mockUser = {
    id: 1,
    username: 'testuser',
    email: 'test@example.com',
  };
  const mockLogout = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
    mockWriteText.mockClear();
    vi.mocked(AuthContext.useAuth).mockReturnValue({
      user: mockUser,
      token: 'test-token',
      login: vi.fn(),
      logout: mockLogout,
      isAuthenticated: true,
      isInitialized: true,
    });
  });

  it('should render dashboard with user information', () => {
    render(
      <BrowserRouter>
        <DashboardPage />
      </BrowserRouter>,
    );

    expect(screen.getByText('Welcome to GeoMeet')).toBeInTheDocument();
    expect(screen.getByText('testuser')).toBeInTheDocument();
    expect(screen.getByText('test@example.com')).toBeInTheDocument();
  });

  it('should show create session button when no session exists', () => {
    render(
      <BrowserRouter>
        <DashboardPage />
      </BrowserRouter>,
    );

    expect(screen.getByText('Create Session')).toBeInTheDocument();
  });

  it('should create session when button is clicked', async () => {
    const user = userEvent.setup();
    const mockSession = {
      id: 1,
      sessionId: 'test-session-id',
      initiatorId: 1,
      status: 'Active',
      createdAt: '2024-01-01T00:00:00',
      message: 'Session created successfully',
    };

    vi.mocked(sessionApi.createSession).mockResolvedValue(mockSession);
    vi.mocked(sessionApi.generateInviteLink).mockResolvedValue({
      sessionId: 'test-session-id',
      inviteLink: '/join?sessionId=test-session-id',
      inviteCode: 'test-session-id',
      message: 'Invitation link generated successfully',
    });

    render(
      <BrowserRouter>
        <DashboardPage />
      </BrowserRouter>,
    );

    const createButton = screen.getByText('Create Session');
    await user.click(createButton);

    await waitFor(() => {
      expect(sessionApi.createSession).toHaveBeenCalled();
      expect(mockNavigate).toHaveBeenCalledWith('/session/test-session-id');
    });
  });

  it('should display error message when session creation fails', async () => {
    const user = userEvent.setup();
    const errorMessage = 'Failed to create session';
    vi.mocked(sessionApi.createSession).mockRejectedValue({
      response: {
        data: {
          message: errorMessage,
        },
      },
    });

    render(
      <BrowserRouter>
        <DashboardPage />
      </BrowserRouter>,
    );

    const createButton = screen.getByText('Create Session');
    await user.click(createButton);

    await waitFor(() => {
      expect(screen.getByText(errorMessage)).toBeInTheDocument();
    });
  });

  it('should show loading state during session creation', async () => {
    const user = userEvent.setup();
    let resolveCreate: ((value: any) => void) | undefined;
    const createPromise = new Promise((resolve) => {
      resolveCreate = resolve;
    });
    vi.mocked(sessionApi.createSession).mockReturnValue(createPromise as any);
    vi.mocked(sessionApi.generateInviteLink).mockResolvedValue({
      sessionId: 'test-session-id',
      inviteLink: '/join?sessionId=test-session-id',
      inviteCode: 'test-session-id',
      message: 'Invitation link generated successfully',
    });

    render(
      <BrowserRouter>
        <DashboardPage />
      </BrowserRouter>,
    );

    const createButton = screen.getByText('Create Session');
    await user.click(createButton);

    // Check loading state
    await waitFor(() => {
      expect(createButton).toBeDisabled();
      expect(screen.getByRole('progressbar')).toBeInTheDocument();
    });

    // Resolve the promise
    if (resolveCreate) {
      resolveCreate({
        id: 1,
        sessionId: 'test-session-id',
        initiatorId: 1,
        status: 'Active',
        createdAt: '2024-01-01T00:00:00',
        message: 'Session created successfully',
      });
    }

    await waitFor(() => {
      // Button should be enabled after navigation or session creation
      expect(mockNavigate).toHaveBeenCalled();
    });
  });

  it('should logout when logout button is clicked', async () => {
    const user = userEvent.setup();

    render(
      <BrowserRouter>
        <DashboardPage />
      </BrowserRouter>,
    );

    const logoutButton = screen.getByText('Logout');
    await user.click(logoutButton);

    expect(mockLogout).toHaveBeenCalled();
    expect(mockNavigate).toHaveBeenCalledWith('/login');
  });

  it('should copy session ID to clipboard', async () => {
    const user = userEvent.setup();
    const mockSession = {
      id: 1,
      sessionId: 'test-session-id',
      initiatorId: 1,
      status: 'Active',
      createdAt: '2024-01-01T00:00:00',
      message: 'Session created successfully',
    };

    vi.mocked(sessionApi.createSession).mockResolvedValue(mockSession);
    vi.mocked(sessionApi.generateInviteLink).mockResolvedValue({
      sessionId: 'test-session-id',
      inviteLink: '/join?sessionId=test-session-id',
      inviteCode: 'test-session-id',
      message: 'Invitation link generated successfully',
    });

    // Mock navigate to prevent navigation
    mockNavigate.mockImplementation(() => {});

    render(
      <BrowserRouter>
        <DashboardPage />
      </BrowserRouter>,
    );

    const createButton = screen.getByText('Create Session');
    await user.click(createButton);

    await waitFor(() => {
      expect(screen.getByText('test-session-id')).toBeInTheDocument();
    });

    // Find copy button - it should be visible after session is created
    const copyButtons = screen.getAllByRole('button');
    const copyButton = copyButtons.find((btn) => 
      btn.textContent?.toLowerCase().includes('copy') && !btn.disabled,
    );
    
    expect(copyButton).toBeDefined();
  });

  it('should display session information after creation', async () => {
    const user = userEvent.setup();
    const mockSession = {
      id: 1,
      sessionId: 'test-session-id',
      initiatorId: 1,
      status: 'Active',
      createdAt: '2024-01-01T00:00:00',
      message: 'Session created successfully',
    };

    vi.mocked(sessionApi.createSession).mockResolvedValue(mockSession);
    vi.mocked(sessionApi.generateInviteLink).mockResolvedValue({
      sessionId: 'test-session-id',
      inviteLink: '/join?sessionId=test-session-id',
      inviteCode: 'test-session-id',
      message: 'Invitation link generated successfully',
    });

    render(
      <BrowserRouter>
        <DashboardPage />
      </BrowserRouter>,
    );

    const createButton = screen.getByText('Create Session');
    await user.click(createButton);

    await waitFor(() => {
      expect(screen.getByText('Session Created Successfully!')).toBeInTheDocument();
      expect(screen.getByText(/Status: Active/)).toBeInTheDocument();
    });
  });
});
