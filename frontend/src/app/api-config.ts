// Dynamically detect environment to connect to local backend during development
// and production backend on Render when deployed.
const isLocal = typeof window !== 'undefined' && 
  (window.location.hostname === 'localhost' || 
   window.location.hostname === '127.0.0.1' || 
   window.location.hostname.startsWith('192.168.'));

export const API_BASE_URL = isLocal ? 'http://localhost:8080' : 'https://flexzone-backend.onrender.com';
