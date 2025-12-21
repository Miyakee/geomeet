import '@testing-library/jest-dom';
import { cleanup } from '@testing-library/react';
import { afterEach, beforeAll, afterAll } from 'vitest';

// Define GeolocationPositionError constants for testing environment
// These are normally provided by the browser but need to be mocked in tests
if (typeof global.GeolocationPositionError === 'undefined') {
  (global as any).GeolocationPositionError = {
    PERMISSION_DENIED: 1,
    POSITION_UNAVAILABLE: 2,
    TIMEOUT: 3,
  };
}

// Suppress React warnings and expected error logs in tests
const originalError = console.error;
const originalWarn = console.warn;

beforeAll(() => {
  // Filter console.error
  console.error = (...args: any[]) => {
    const firstArg = args[0];
    const secondArg = args[1];
    
    // Filter out React DOM nesting warnings and other common test warnings
    if (typeof firstArg === 'string') {
      if (
        firstArg.includes('validateDOMNesting') ||
        firstArg.includes('Warning:') ||
        firstArg.includes('React Router Future Flag Warning') ||
        firstArg.includes('Failed to end session') ||
        firstArg.includes('Failed to calculate optimal location') ||
        firstArg.includes('Failed to update meeting location') ||
        firstArg.includes('Failed to update location') ||
        firstArg.includes('Failed to get current location')
      ) {
        return;
      }
    }
    
    // Filter out error objects that are part of expected test scenarios
    // Check if the message contains "Failed to" and the second arg is an error object
    if (
      typeof firstArg === 'string' &&
      firstArg.includes('Failed to') &&
      (secondArg instanceof Error || (secondArg && typeof secondArg === 'object' && 'response' in secondArg))
    ) {
      return;
    }
    
    // Filter out error objects with response.status (API errors in tests)
    if (
      firstArg &&
      typeof firstArg === 'object' &&
      (firstArg instanceof Error || 'response' in firstArg)
    ) {
      const errorObj = firstArg as any;
      if (
        errorObj.response?.status ||
        (errorObj.message && typeof errorObj.message === 'string' && 
         (errorObj.message.includes('Network error') ||
          errorObj.message.includes('Access denied') ||
          errorObj.message.includes('Session not found')))
      ) {
        return;
      }
    }
    
    originalError.call(console, ...args);
  };

  // Filter console.warn (for React Router warnings and geocoding errors)
  console.warn = (...args: any[]) => {
    const firstArg = args[0];
    
    // Filter out React Router Future Flag warnings, MUI warnings, and geocoding errors
    if (typeof firstArg === 'string') {
      if (
        firstArg.includes('React Router Future Flag Warning') ||
        firstArg.includes('v7_startTransition') ||
        firstArg.includes('v7_relativeSplatPath') ||
        firstArg.includes('React Router will begin') ||
        firstArg.includes('Reverse geocoding error') ||
        firstArg.includes('Forward geocoding error') ||
        firstArg.includes('Geolocation error') ||
        firstArg.includes('MUI: You are providing a disabled') ||
        firstArg.includes('A disabled element does not fire events') ||
        firstArg.includes('Tooltip needs to listen')
      ) {
        return;
      }
    }
    
    // Filter out geocoding errors (TypeError with "Cannot read properties")
    if (
      firstArg &&
      typeof firstArg === 'object' &&
      firstArg instanceof Error &&
      firstArg.message?.includes('Cannot read properties of undefined')
    ) {
      // Check if it's a geocoding-related error by looking at stack trace
      const stack = firstArg.stack || '';
      if (stack.includes('geocodingService') || stack.includes('reverseGeocode') || stack.includes('forwardGeocode')) {
        return;
      }
    }
    
    originalWarn.call(console, ...args);
  };
});

afterAll(() => {
  console.error = originalError;
  console.warn = originalWarn;
});

// Setup localStorage mock
const localStorageMock = (() => {
  let store: Record<string, string> = {};

  return {
    getItem: (key: string) => store[key] || null,
    setItem: (key: string, value: string) => {
      store[key] = value.toString();
    },
    removeItem: (key: string) => {
      delete store[key];
    },
    clear: () => {
      store = {};
    },
  };
})();

Object.defineProperty(window, 'localStorage', {
  value: localStorageMock,
});

// Cleanup after each test
afterEach(() => {
  cleanup();
  localStorageMock.clear();
});

