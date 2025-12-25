import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { ReactNode } from 'react';
import { ROUTES, QUERY_PARAMS } from '../constants/routes';

interface ProtectedRouteProps {
  children: ReactNode;
}

const ProtectedRoute = ({ children }: ProtectedRouteProps) => {
  const { isAuthenticated, isInitialized } = useAuth();
  const location = useLocation();

  // Wait for auth state to initialize before redirecting
  // This prevents false redirects when opening links in new tabs
  if (!isInitialized) {
    return null; // or a loading spinner
  }

  if (!isAuthenticated) {
    // Preserve the current path and search params (including sessionId) when redirecting to login
    const searchParams = new URLSearchParams(location.search);
    const sessionId = searchParams.get(QUERY_PARAMS.SESSION_ID);
    const inviteCode = searchParams.get(QUERY_PARAMS.INVITE_CODE);
    // Build redirect URL with current path and params
    let redirectTo = `${ROUTES.LOGIN}?${QUERY_PARAMS.REDIRECT}=${encodeURIComponent(location.pathname)}`;
    if (sessionId) {
      redirectTo += `&${QUERY_PARAMS.SESSION_ID}=${encodeURIComponent(sessionId)}`;
      if (inviteCode) {
        redirectTo += `&${QUERY_PARAMS.INVITE_CODE}=${encodeURIComponent(inviteCode)}`;
      }
    } else if (location.search) {
      // Preserve all other search params
      redirectTo += `&${location.search.substring(1)}`;
    }

    return <Navigate to={redirectTo} replace />;
  }

  return <>{children}</>;
};

export default ProtectedRoute;

