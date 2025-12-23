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
      // Optionally redirect to login page
      window.location.href = '/login';
    });
  }, [logout]);

  return null; // This component doesn't render anything
};

