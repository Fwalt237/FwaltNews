import axios from 'axios';
import qs from 'qs';

const api = axios.create({
  baseURL: process.env.REACT_APP_API_URL || '/api',
  paramsSerializer: params => qs.stringify(params, { arrayFormat: 'repeat' }),
  headers: { 'Content-Type': 'application/json' },
});

api.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

api.interceptors.response.use(
  res => res,
  err => {
    if (err.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
    }
    return Promise.reject(err);
  }
);

export default api;