import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import TaskDetailPage from '../pages/TaskDetailPage';
import type { Task } from '../types';

const mockTask: Task = {
  id: 5,
  title: 'Tarea de prueba',
  description: 'Descripción detallada',
  status: 'PENDING',
  createdAt: '2026-05-13T10:00:00',
  completedAt: null,
};

vi.mock('../api', () => ({
  tasksApi: {
    getAll: vi.fn(),
    getById: vi.fn(),
    changeStatus: vi.fn(),
    remove: vi.fn(),
  },
}));

vi.mock('../components/Navbar', () => ({
  default: () => <nav data-testid="navbar">Navbar</nav>,
}));

import { tasksApi } from '../api';

function renderTaskDetail(task?: Task) {
  const state = task ? { task } : undefined;
  return render(
    <MemoryRouter initialEntries={[{ pathname: '/task/5', state }]}>
      <Routes>
        <Route path="/task/:id" element={<TaskDetailPage />} />
        <Route path="/" element={<p>Dashboard</p>} />
      </Routes>
    </MemoryRouter>,
  );
}

describe('TaskDetailPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders task details from router state (zero-request)', () => {
    renderTaskDetail(mockTask);
    expect(screen.getByText('Tarea de prueba')).toBeInTheDocument();
    expect(screen.getByText('Descripción detallada')).toBeInTheDocument();
    expect(screen.getByText('Pendiente')).toBeInTheDocument();
    expect(screen.getByText('ID #5')).toBeInTheDocument();
  });

  it('fetches task from API when no state', async () => {
    vi.mocked(tasksApi.getById).mockResolvedValue(mockTask);
    renderTaskDetail();

    await waitFor(() => {
      expect(screen.getByText('Tarea de prueba')).toBeInTheDocument();
    });
    expect(tasksApi.getById).toHaveBeenCalledWith(5);
  });

  it('shows error when task not found', async () => {
    vi.mocked(tasksApi.getById).mockResolvedValue(undefined);
    renderTaskDetail();

    await waitFor(() => {
      expect(screen.getByRole('alert')).toHaveTextContent('La tarea no existe');
    });
  });

  it('shows "Iniciar tarea" button for PENDING tasks', () => {
    renderTaskDetail(mockTask);
    expect(screen.getByRole('button', { name: 'Iniciar tarea' })).toBeInTheDocument();
  });

  it('changes status to IN_PROGRESS', async () => {
    const user = userEvent.setup();
    vi.mocked(tasksApi.changeStatus).mockResolvedValue({
      ...mockTask,
      status: 'IN_PROGRESS',
    });

    renderTaskDetail(mockTask);
    await user.click(screen.getByRole('button', { name: 'Iniciar tarea' }));

    await waitFor(() => {
      expect(tasksApi.changeStatus).toHaveBeenCalledWith(5, { status: 'IN_PROGRESS' });
      expect(screen.getByText('En progreso')).toBeInTheDocument();
    });
  });

  it('changes status to COMPLETED', async () => {
    const user = userEvent.setup();
    vi.mocked(tasksApi.changeStatus).mockResolvedValue({
      ...mockTask,
      status: 'COMPLETED',
      completedAt: '2026-05-13T15:00:00',
    });

    renderTaskDetail(mockTask);
    await user.click(screen.getByRole('button', { name: 'Marcar como completada' }));

    await waitFor(() => {
      expect(tasksApi.changeStatus).toHaveBeenCalledWith(5, { status: 'COMPLETED' });
      expect(screen.getByText('Completada')).toBeInTheDocument();
    });
  });

  it('shows delete confirmation and deletes task', async () => {
    const user = userEvent.setup();
    vi.mocked(tasksApi.remove).mockResolvedValue(undefined);

    renderTaskDetail(mockTask);

    // Click "Eliminar esta tarea permanentemente"
    await user.click(screen.getByText('Eliminar esta tarea permanentemente'));

    // Confirmation appears
    expect(screen.getByText('¿Estás seguro?')).toBeInTheDocument();

    // Confirm delete
    await user.click(screen.getByRole('button', { name: 'Sí, eliminar' }));

    await waitFor(() => {
      expect(tasksApi.remove).toHaveBeenCalledWith(5);
      // Should navigate to dashboard
      expect(screen.getByText('Dashboard')).toBeInTheDocument();
    });
  });

  it('does not show action buttons for COMPLETED tasks', () => {
    renderTaskDetail({ ...mockTask, status: 'COMPLETED', completedAt: '2026-05-13T12:00:00' });
    expect(screen.queryByRole('button', { name: 'Iniciar tarea' })).toBeNull();
    expect(screen.queryByRole('button', { name: 'Marcar como completada' })).toBeNull();
    expect(screen.getByText(/no admite más transiciones/)).toBeInTheDocument();
  });

  it('has back navigation button', () => {
    renderTaskDetail(mockTask);
    expect(screen.getByText('Volver a mis tareas')).toBeInTheDocument();
  });
});
