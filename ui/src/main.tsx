// Polyfill for global (needed by sockjs-client)
if (typeof global === 'undefined') {
  (window as any).global = window;
}

// CRITICAL: Import polyfills FIRST - before any other imports
import './polyfills';

import { StrictMode } from 'react';
import ReactDOM from 'react-dom/client';
import App from './App.tsx';
import './index.css';

const rootElement = document.getElementById('root');
if (!rootElement) {
  throw new Error('Root element not found');
}

ReactDOM.createRoot(rootElement).render(
  <StrictMode>
    <App />
  </StrictMode>,
);
