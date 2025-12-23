import { useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { setupApiInterceptors } from '../services/apiInterceptors';

/**
 * Component to set up API interceptors with auth context
 * This should be rendered once at the app root level
 */
export const ApiInterceptorSetup = () => {
  const { logout } = useAuth();

  useEffect(() => {
    // Set up interceptors with logout callback
    setupApiInterceptors(() => {
      logout();
      // Only redirect to login page if not already on login page
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    });
  }, [logout]);

  return null;
};

