import apiClient from './client';
import type {
  ChangeStatusRequest,
  CreateTaskRequest,
  Task,
  TaskStatus,
} from '../types';

/**
 * Endpoints de tareas — §7.2 del contrato.
 */
export const tasksApi = {
  getAll(status?: TaskStatus): Promise<Task[]> {
    const params = status ? { status } : undefined;
    return apiClient.get<Task[]>('/tasks', { params }).then((r) => r.data);
  },

  create(data: CreateTaskRequest): Promise<Task> {
    return apiClient.post<Task>('/tasks', data).then((r) => r.data);
  },

  changeStatus(id: number, data: ChangeStatusRequest): Promise<Task> {
    return apiClient
      .patch<Task>(`/tasks/${id}/status`, data)
      .then((r) => r.data);
  },

  remove(id: number): Promise<void> {
    return apiClient.delete(`/tasks/${id}`).then(() => undefined);
  },
};
