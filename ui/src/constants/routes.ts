/**
 * Application route paths
 */
export const ROUTES = {
  LOGIN: '/login',
  DASHBOARD: '/dashboard',
  JOIN: '/join',
  SESSION: (sessionId: string) => `/session/${sessionId}`,
} as const;

/**
 * Route parameter names
 */
export const ROUTE_PARAMS = {
  SESSION_ID: 'sessionId',
} as const;

/**
 * Query parameter names
 */
export const QUERY_PARAMS = {
  REDIRECT: 'redirect',
  SESSION_ID: 'sessionId',
  INVITE_CODE: 'inviteCode',
} as const;

