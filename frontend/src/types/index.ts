/**
 * Tipos del dominio alineados con el modelo de datos del backend.
 * Fuente: Especificación Técnica §8 (Modelo de datos).
 */

export type TaskStatus = 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';

export interface Task {
  id: number;
  title: string;
  description: string | null;
  status: TaskStatus;
  createdAt: string;
  completedAt: string | null;
}

export interface User {
  id: number;
  email: string;
  name: string;
}

/* ── DTOs de request / response alineados con §7 (APIs y endpoints) ── */

export interface RegisterRequest {
  email: string;
  password: string;
  name: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  expiresIn: number;
}

export interface CreateTaskRequest {
  title: string;
  description?: string;
}

export interface ChangeStatusRequest {
  status: TaskStatus;
}

/* ── Respuesta genérica de error del backend (GlobalExceptionHandler) ── */

export interface ApiError {
  message: string;
  status: number;
  timestamp?: string;
}
