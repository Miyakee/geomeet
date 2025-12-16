import { useState } from 'react';
import { sessionApi } from '../services/api';

export const useInviteLink = (sessionId: string | undefined) => {
  const [inviteLink, setInviteLink] = useState<string | null>(null);
  const [inviteCode, setInviteCode] = useState<string | null>(null);
  const [copied, setCopied] = useState(false);
  const [loadingInvite, setLoadingInvite] = useState(false);

  const loadInviteLink = async () => {
    if (!sessionId) {
      return;
    }
    
    try {
      setLoadingInvite(true);
      const invite = await sessionApi.generateInviteLink(sessionId);
      setInviteLink(`${window.location.origin}${invite.inviteLink}`);
      setInviteCode(invite.inviteCode);
    } catch (err: any) {
      console.error('Failed to load invite link:', err);
    } finally {
      setLoadingInvite(false);
    }
  };

  const handleCopyInviteLink = () => {
    if (inviteLink) {
      navigator.clipboard.writeText(inviteLink);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  const handleCopyInviteCode = () => {
    if (inviteCode) {
      navigator.clipboard.writeText(inviteCode);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  return {
    inviteLink,
    inviteCode,
    copied,
    loadingInvite,
    loadInviteLink,
    handleCopyInviteLink,
    handleCopyInviteCode,
  };
};

