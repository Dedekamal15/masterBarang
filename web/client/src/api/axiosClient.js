import axios from 'axios';

const api = axios.create({
  baseURL: '/api/v1',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Response interceptor — extract data
api.interceptors.response.use(
  (response) => response.data,
  (error) => {
    const message = error.response?.data?.error || error.message || 'Terjadi kesalahan';
    console.error('[API Error]', message);
    return Promise.reject(new Error(message));
  }
);

export default api;
