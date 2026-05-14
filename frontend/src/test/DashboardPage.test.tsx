import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import DashboardPage from '../pages/DashboardPage';
import type { Task } from '../types';

const mockTasks: Task[] = [
  {
    id: 1,
    title: 'Tarea pendiente',
    description: 'Descripción uno',
    status: 'PENDING',
    createdAt: '2026-05-13T10:00:00',
    completedAt: null,
  },
  {
    id: 2,
    title: 'Tarea completada',
    description: null,
    status: 'COMPLETED',
    createdAt: '2026-05-12T08:00:00',
    completedAt: '2026-05-12T12:00:00',
  },
];

vi.mock('../api', () => ({
  tasksApi: {
    getAll: vi.fn(),
    create: vi.fn(),
    changeStatus: vi.fn(),
    remove: vi.fn(),
  },
}));

vi.mock('../components/Navbar', () => ({
  default: () => <nav data-testid="navbar">Navbar</nav>,
}));

import { tasksApi } from '../api';

function renderDashboard() {
  return render(
    <MemoryRouter>
      <DashboardPage />
    </MemoryRouter>,
  );
}

describe('DashboardPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('shows loading state then tasks', async () => {
    vi.mocked(tasksApi.getAll).mockResolvedValue(mockTasks);
    renderDashboard();

    expect(screen.getByText('Cargando tareas…')).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.getByText('Tarea pendiente')).toBeInTheDocument();
      expect(screen.getByText('Tarea completada')).toBeInTheDocument();
    });
  });

  it('shows empty state when no tasks', async () => {
    vi.mocked(tasksApi.getAll).mockResolvedValue([]);
    renderDashboard();

    await waitFor(() => {
      expect(screen.getByText(/No tienes tareas aún/)).toBeInTheDocument();
    });
  });

  it('creates a new task', async () => {
    const user = userEvent.setup();
    vi.mocked(tasksApi.getAll).mockResolvedValue([]);
    vi.mocked(tasksApi.create).mockResolvedValue({
      id: 3,
      title: 'Nueva tarea',
      description: 'Desc',
      status: 'PENDING',
      createdAt: '2026-05-13T15:00:00',
      completedAt: null,
    });

    renderDashboard();

    await waitFor(() => {
      expect(screen.getByText(/No tienes tareas aún/)).toBeInTheDocument();
    });

    await user.type(screen.getByPlaceholderText('Título de la tarea'), 'Nueva tarea');
    await user.type(screen.getByPlaceholderText('Descripción (opcional)'), 'Desc');
    await user.click(screen.getByRole('button', { name: 'Crear tarea' }));

    await waitFor(() => {
      expect(tasksApi.create).toHaveBeenCalledWith({
        title: 'Nueva tarea',
        description: 'Desc',
      });
      expect(screen.getByText('Nueva tarea')).toBeInTheDocument();
    });
  });

  it('shows status labels correctly', async () => {
    vi.mocked(tasksApi.getAll).mockResolvedValue(mockTasks);
    renderDashboard();

    await waitFor(() => {
      expect(screen.getByText('Pendiente')).toBeInTheDocument();
      expect(screen.getByText('Completada')).toBeInTheDocument();
    });
  });

  it('deletes a task', async () => {
    const user = userEvent.setup();
    vi.mocked(tasksApi.getAll).mockResolvedValue([mockTasks[0]]);
    vi.mocked(tasksApi.remove).mockResolvedValue(undefined);

    renderDashboard();

    await waitFor(() => {
      expect(screen.getByText('Tarea pendiente')).toBeInTheDocument();
    });

    await user.click(screen.getByRole('button', { name: 'Eliminar' }));

    await waitFor(() => {
      expect(tasksApi.remove).toHaveBeenCalledWith(1);
    });
  });

  it('shows error when fetch fails', async () => {
    vi.mocked(tasksApi.getAll).mockRejectedValue(new Error('Network Error'));
    renderDashboard();

    await waitFor(() => {
      expect(screen.getByRole('alert')).toHaveTextContent('Network Error');
    });
  });
});
