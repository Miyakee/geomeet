import {Navigate, useLocation} from 'react-router-dom';
import {useAuth} from '../contexts/AuthContext';
import {ReactNode} from 'react';

interface ProtectedRouteProps {
    children: ReactNode;
}

const ProtectedRoute = ({children}: ProtectedRouteProps) => {
    const {isAuthenticated, isInitialized} = useAuth();
    const location = useLocation();

    // Wait for auth state to initialize before redirecting
    // This prevents false redirects when opening links in new tabs
    if (!isInitialized) {
        return null; // or a loading spinner
    }

    if (!isAuthenticated) {
        // Preserve the current path and search params (including sessionId) when redirecting to login
        const searchParams = new URLSearchParams(location.search);
        const sessionId = searchParams.get('sessionId');
        const inviteCode = searchParams.get('inviteCode');
        // Build redirect URL with current path and params
        let redirectTo = `/login?redirect=${encodeURIComponent(location.pathname)}`;
        if (sessionId) {
            redirectTo += `&sessionId=${encodeURIComponent(sessionId)}`;
            if (inviteCode) {
                redirectTo += `&inviteCode=${encodeURIComponent(inviteCode)}`;
            }
        } else if (location.search) {
            // Preserve all other search params
            redirectTo += `&${location.search.substring(1)}`;
        }

        return <Navigate to={redirectTo} replace/>;
    }

    return <>{children}</>;
};

export default ProtectedRoute;

