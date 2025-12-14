// Polyfill for global (needed by sockjs-client)
if (typeof global === 'undefined') {
  (window as any).global = window;
}

import { StrictMode } from 'react'
import ReactDOM from 'react-dom/client'
import App from './App.tsx'
import './index.css'

ReactDOM.createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
