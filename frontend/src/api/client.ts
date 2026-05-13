import axios from 'axios';
import { getToken, removeToken } from '../utils/token';

/**
 * Instancia centralizada de Axios.
 *
 * - En desarrollo, Vite proxea `/api` al backend (vite.config.ts).
 * - En producción, VITE_API_URL apunta al dominio de Render.
 *
 * Interceptor de request : inyecta el JWT automáticamente.
 * Interceptor de response: ante 401 limpia sesión y redirige a /login.
 */
const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  headers: { 'Content-Type': 'application/json' },
});

/* ── Request interceptor ── */
apiClient.interceptors.request.use((config) => {
  const token = getToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

/* ── Response interceptor ── */
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (axios.isAxiosError(error) && error.response?.status === 401) {
      removeToken();
      // Evita loops: solo redirige si NO estamos ya en /login
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  },
);

export default apiClient;
