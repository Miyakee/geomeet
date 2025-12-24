import { ROUTES, QUERY_PARAMS } from '../constants/routes';

/**
 * Build join session URL with session ID and optional invite code
 */
export const buildJoinUrl = (sessionId: string, inviteCode?: string | null): string => {
  const baseUrl = `${ROUTES.JOIN}?${QUERY_PARAMS.SESSION_ID}=${sessionId}`;
  if (inviteCode) {
    return `${baseUrl}&${QUERY_PARAMS.INVITE_CODE}=${inviteCode}`;
  }
  return baseUrl;
};

/**
 * Get invite code from URL search params
 */
export const getInviteCodeFromUrl = (): string | null => {
  const urlParams = new URLSearchParams(window.location.search);
  return urlParams.get(QUERY_PARAMS.INVITE_CODE);
};

