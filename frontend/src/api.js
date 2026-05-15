import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
});

// Optionally add interceptors here if needed for auth tokens

export default api;
