import { ApiError, ErrorResponse } from '../services/api';

/**
 * Extract error message from an API error
 * Uses the standard ErrorResponse structure from backend
 * @param error - The error object (ApiError or any error with response)
 * @param defaultMessage - Default message to use if no specific message is found
 * @returns The error message string
 */
export function getErrorMessage(error: unknown, defaultMessage: string = 'An error occurred'): string {
  if (error instanceof ApiError) {
    // ApiError.message is already set from ErrorResponse.message or ErrorResponse.error
    return error.message || defaultMessage;
  }

  if (error && typeof error === 'object' && 'response' in error) {
    const apiError = error as { response?: { data?: ErrorResponse; status?: number }; message?: string };
    const data = apiError.response?.data;
    
    // Use standard ErrorResponse structure: message field takes priority
    if (data?.message) {
      return data.message;
    }
    // Fallback to error field if message is not available
    if (data?.error) {
      return data.error;
    }
    
    // Fallback to error object's message
    if (apiError.message) {
      return apiError.message;
    }
  }

  // Handle standard Error objects
  if (error instanceof Error) {
    return error.message || defaultMessage;
  }

  return defaultMessage;
}

/**
 * Get user-friendly error message based on HTTP status code
 * @param error - The error object
 * @param statusMessages - Custom messages for specific status codes
 * @param defaultMessage - Default message if no status-specific message is found
 * @returns The error message string
 */
export function getStatusErrorMessage(
  error: unknown,
  statusMessages: Record<number, string> = {},
  defaultMessage: string = 'An error occurred. Please try again.',
): string {
  let status: number | undefined;

  if (error instanceof ApiError) {
    status = error.status;
  } else if (error && typeof error === 'object' && 'response' in error) {
    const apiError = error as { response?: { status?: number } };
    status = apiError.response?.status;
  } else if (error && typeof error === 'object' && 'status' in error) {
    status = (error as { status: number }).status;
  }

  // Check for custom status messages
  if (status && statusMessages[status]) {
    return statusMessages[status];
  }

  // Default status-based messages
  if (status === 401) {
    return 'Authentication failed. Please login again.';
  }
  if (status === 403) {
    return 'You do not have permission to perform this action.';
  }
  if (status === 404) {
    return 'Resource not found.';
  }
  if (status === 400) {
    return getErrorMessage(error, 'Invalid request. Please check your input.');
  }
  if (status && status >= 500) {
    return 'Server error. Please try again later.';
  }

  // Fallback to generic error message extraction
  return getErrorMessage(error, defaultMessage);
}

