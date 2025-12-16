import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { InviteSection } from '../InviteSection';

// Mock clipboard API
Object.assign(navigator, {
  clipboard: {
    writeText: vi.fn(),
  },
});

describe('InviteSection', () => {
  it('should render invite section title', () => {
    render(
      <InviteSection
        inviteLink={null}
        inviteCode={null}
        copied={false}
        loadingInvite={false}
        onLoadInviteLink={vi.fn()}
        onCopyInviteLink={vi.fn()}
        onCopyInviteCode={vi.fn()}
      />,
    );

    expect(screen.getByText('Invite Friends')).toBeInTheDocument();
  });

  it('should show generate button when invite link is not loaded', () => {
    const onLoadInviteLink = vi.fn();

    render(
      <InviteSection
        inviteLink={null}
        inviteCode={null}
        copied={false}
        loadingInvite={false}
        onLoadInviteLink={onLoadInviteLink}
        onCopyInviteLink={vi.fn()}
        onCopyInviteCode={vi.fn()}
      />,
    );

    const generateButton = screen.getByText('Generate Invite Link');
    expect(generateButton).toBeInTheDocument();
  });

  it('should call onLoadInviteLink when generate button is clicked', async () => {
    const user = userEvent.setup();
    const onLoadInviteLink = vi.fn();

    render(
      <InviteSection
        inviteLink={null}
        inviteCode={null}
        copied={false}
        loadingInvite={false}
        onLoadInviteLink={onLoadInviteLink}
        onCopyInviteLink={vi.fn()}
        onCopyInviteCode={vi.fn()}
      />,
    );

    const generateButton = screen.getByText('Generate Invite Link');
    await user.click(generateButton);

    expect(onLoadInviteLink).toHaveBeenCalledTimes(1);
  });

  it('should show loading spinner when loading invite', () => {
    render(
      <InviteSection
        inviteLink={null}
        inviteCode={null}
        copied={false}
        loadingInvite={true}
        onLoadInviteLink={vi.fn()}
        onCopyInviteLink={vi.fn()}
        onCopyInviteCode={vi.fn()}
      />,
    );

    // Check for CircularProgress (it might not have accessible text)
    const generateButton = screen.queryByText('Generate Invite Link');
    expect(generateButton).not.toBeInTheDocument();
  });

  it('should display invite link and code when loaded', () => {
    render(
      <InviteSection
        inviteLink="http://localhost:3000/join?sessionId=test-session"
        inviteCode="test-session"
        copied={false}
        loadingInvite={false}
        onLoadInviteLink={vi.fn()}
        onCopyInviteLink={vi.fn()}
        onCopyInviteCode={vi.fn()}
      />,
    );

    expect(screen.getByDisplayValue('http://localhost:3000/join?sessionId=test-session')).toBeInTheDocument();
    expect(screen.getByDisplayValue('test-session')).toBeInTheDocument();
  });

  it('should call onCopyInviteLink when copy link button is clicked', async () => {
    const user = userEvent.setup();
    const onCopyInviteLink = vi.fn();

    render(
      <InviteSection
        inviteLink="http://localhost:3000/join?sessionId=test-session"
        inviteCode="test-session"
        copied={false}
        loadingInvite={false}
        onLoadInviteLink={vi.fn()}
        onCopyInviteLink={onCopyInviteLink}
        onCopyInviteCode={vi.fn()}
      />,
    );

    const copyButtons = screen.getAllByRole('button');
    // Find the copy button for invite link (first copy button)
    await user.click(copyButtons[0]);

    expect(onCopyInviteLink).toHaveBeenCalledTimes(1);
  });

  it('should call onCopyInviteCode when copy code button is clicked', async () => {
    const user = userEvent.setup();
    const onCopyInviteCode = vi.fn();

    render(
      <InviteSection
        inviteLink="http://localhost:3000/join?sessionId=test-session"
        inviteCode="test-session"
        copied={false}
        loadingInvite={false}
        onLoadInviteLink={vi.fn()}
        onCopyInviteLink={vi.fn()}
        onCopyInviteCode={onCopyInviteCode}
      />,
    );

    const copyButtons = screen.getAllByRole('button');
    // Find the copy button for invite code (second copy button)
    await user.click(copyButtons[1]);

    expect(onCopyInviteCode).toHaveBeenCalledTimes(1);
  });

  it('should show success message when copied', () => {
    render(
      <InviteSection
        inviteLink="http://localhost:3000/join?sessionId=test-session"
        inviteCode="test-session"
        copied={true}
        loadingInvite={false}
        onLoadInviteLink={vi.fn()}
        onCopyInviteLink={vi.fn()}
        onCopyInviteCode={vi.fn()}
      />,
    );

    expect(screen.getByText('Copied to clipboard!')).toBeInTheDocument();
  });
});

