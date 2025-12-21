import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: './src/test/setup.ts',
    css: true,
    testTimeout: 10000, // 10 seconds default timeout for all tests
    hookTimeout: 10000, // 10 seconds timeout for hooks
    // Use dot reporter: only show dots for passing tests, full details for failures
    reporter: ['verbose'],
    // Suppress console output unless test fails
    silent: false,
  },
});

