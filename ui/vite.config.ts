import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import { nodePolyfills } from 'vite-plugin-node-polyfills'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    react(),
    nodePolyfills({
      include: ['buffer', 'process', 'util'],
      globals: {
        Buffer: true,
        global: true,
        process: true,
      },
    }),
  ],
  define: {
    global: 'globalThis',
    'process.env': '{}',
  },
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          'react-vendor': ['react', 'react-dom', 'react-router-dom'],
          'mui-vendor': ['@mui/material', '@mui/icons-material', '@emotion/react', '@emotion/styled'],
        },
        // Inject polyfill at the beginning of ALL chunks
        banner: `
// CRITICAL: Polyfill for Request/Response - MUST execute before any other code
if (typeof globalThis !== 'undefined') {
  if (typeof Request !== 'undefined' && !globalThis.Request) {
    Object.defineProperty(globalThis, 'Request', {
      value: Request,
      writable: true,
      enumerable: true,
      configurable: true,
    });
  }
  if (typeof Response !== 'undefined' && !globalThis.Response) {
    Object.defineProperty(globalThis, 'Response', {
      value: Response,
      writable: true,
      enumerable: true,
      configurable: true,
    });
  }
}
        `.trim(),
      },
    },
  },
  optimizeDeps: {
    include: ['@stomp/stompjs', 'sockjs-client'],
    esbuildOptions: {
      define: {
        global: 'globalThis',
      },
    },
  },
  server: {
    port: 3000,
    host: true, // Allow external connections
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
        ws: true, // Enable WebSocket proxying
      },
      '/ws': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
        ws: true, // Enable WebSocket proxying
      },
    },
  },
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: './src/test/setup.ts',
    css: true,
  },
})
