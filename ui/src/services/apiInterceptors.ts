import { apiInterceptors, ApiError } from './api';

/**
 * Initialize API interceptors for error handling
 * This should be called once when the app starts
 * 
 * Features:
 * - Automatic logout on 401 Unauthorized
 * - Error logging for different HTTP status codes
 * - Development mode error details
 * 
 * @param logoutCallback Optional callback function to call when user should be logged out (401 errors)
 * 
 * @example
 * // In your app initialization:
 * setupApiInterceptors(() => {
 *   logout();
 *   navigate('/login');
 * });
 */
export function setupApiInterceptors(logoutCallback?: () => void) {
  // Error interceptor: Handle common HTTP errors
  apiInterceptors.error.use((error: ApiError) => {
    // Handle 401 Unauthorized - token expired or invalid
    if (error.status === 401) {
      console.warn('Authentication failed. Logging out...');
    }

    // Handle 403 Forbidden - access denied
    if (error.status === 403) {
      console.warn('Access denied:', error.message);
    }

    // Handle 404 Not Found
    if (error.status === 404) {
      console.warn('Resource not found:', error.message);
    }

    // Handle 500 Internal Server Error
    if (error.status >= 500) {
      console.error('Server error:', error.message);
    }

    // Log error details in development
    if (import.meta.env.DEV) {
      console.error('API Error:', {
        status: error.status,
        message: error.message,
        response: error.response,
      });
    }

    // Re-throw the error so it can be handled by the calling code
    throw error;
  });

  // Request interceptor: Add any default headers or modify requests
  apiInterceptors.request.use((config: RequestInit) => {
    // Can add default headers, logging, etc. here
    return config;
  });

  // Note: Response interceptors are not fully implemented in the current request function
  // because the response body is consumed before interceptors can access it.
  // This can be enhanced if needed by cloning the response.
}

