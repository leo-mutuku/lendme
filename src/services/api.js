import axios from 'axios'

// Shared axios instance for talking to the Java backend.
// In dev, '/api' is proxied to the backend by vite.config.js.
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  headers: { 'Content-Type': 'application/json' },
  timeout: 15000,
})

// Attach auth token (if any) to every request
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

export default api
