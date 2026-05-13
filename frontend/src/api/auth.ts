import apiClient from './client';
import type { LoginRequest, LoginResponse, RegisterRequest } from '../types';

/**
 * Endpoints de autenticación — §7.1 del contrato.
 */
export const authApi = {
  register(data: RegisterRequest): Promise<void> {
    return apiClient.post('/auth/register', data).then(() => undefined);
  },

  login(data: LoginRequest): Promise<LoginResponse> {
    return apiClient.post<LoginResponse>('/auth/login', data).then((r) => r.data);
  },
};
