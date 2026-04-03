import axios from 'axios';

// Central API client — every HTTP request goes through this.
// It automatically attaches the JWT token to every request
// so you don't have to do it manually each time.

const api = axios.create({
  baseURL: '/api',
});

// Request interceptor: runs BEFORE every request
// If we have a JWT token in localStorage, attach it
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Response interceptor: runs AFTER every response
// If we get a 401 (token expired/invalid), clear storage and redirect to login
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;
