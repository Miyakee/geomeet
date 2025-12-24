import { useState, useCallback } from 'react';
import { getErrorMessage } from '../utils/errorHandler';

interface UseAsyncOperationOptions {
  defaultErrorMessage?: string;
  onSuccess?: () => void;
  onError?: (error: unknown) => void;
}

/**
 * Generic hook for handling async operations with loading and error states
 * Reduces boilerplate code in components
 */
export const useAsyncOperation = <T extends (...args: any[]) => Promise<any>>(
  operation: T,
  options: UseAsyncOperationOptions = {},
) => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const execute = useCallback(
    async (...args: Parameters<T>): Promise<ReturnType<T> | undefined> => {
      setLoading(true);
      setError(null);

      try {
        const result = await operation(...args);
        options.onSuccess?.();
        return result;
      } catch (err: unknown) {
        const errorMessage = getErrorMessage(
          err,
          options.defaultErrorMessage || 'An error occurred. Please try again.',
        );
        setError(errorMessage);
        options.onError?.(err);
        return undefined;
      } finally {
        setLoading(false);
      }
    },
    [operation, options],
  );

  const clearError = useCallback(() => {
    setError(null);
  }, []);

  return {
    execute,
    loading,
    error,
    clearError,
  };
};

