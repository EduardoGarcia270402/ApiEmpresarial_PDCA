import { useCallback, useEffect, useState } from 'react';
import { tasksApi } from '../api';
import { Alert, Button, Navbar } from '../components';
import type { Task, TaskStatus } from '../types';
import { extractErrorMessage } from '../utils/error';

const STATUS_LABELS: Record<TaskStatus, string> = {
  PENDING: 'Pendiente',
  IN_PROGRESS: 'En progreso',
  COMPLETED: 'Completada',
  CANCELLED: 'Cancelada',
};

const STATUS_COLORS: Record<TaskStatus, string> = {
  PENDING: 'bg-yellow-100 text-yellow-800',
  IN_PROGRESS: 'bg-blue-100 text-blue-800',
  COMPLETED: 'bg-green-100 text-green-800',
  CANCELLED: 'bg-gray-100 text-gray-500',
};

export default function DashboardPage() {
  const [tasks, setTasks] = useState<Task[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  // Form state para crear tarea
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [creating, setCreating] = useState(false);

  const fetchTasks = useCallback(async () => {
    try {
      const data = await tasksApi.getAll();
      setTasks(data);
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchTasks();
  }, [fetchTasks]);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim()) return;
    setCreating(true);
    setError(null);
    try {
      const task = await tasksApi.create({
        title: title.trim(),
        description: description.trim() || undefined,
      });
      setTasks((prev) => [task, ...prev]);
      setTitle('');
      setDescription('');
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setCreating(false);
    }
  };

  const handleStatusChange = async (id: number, status: TaskStatus) => {
    setError(null);
    try {
      const updated = await tasksApi.changeStatus(id, { status });
      setTasks((prev) => prev.map((t) => (t.id === id ? updated : t)));
    } catch (err) {
      setError(extractErrorMessage(err));
    }
  };

  const handleDelete = async (id: number) => {
    setError(null);
    try {
      await tasksApi.remove(id);
      setTasks((prev) => prev.filter((t) => t.id !== id));
    } catch (err) {
      setError(extractErrorMessage(err));
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />

      <main className="mx-auto max-w-3xl px-4 py-8 space-y-6">
        <h1 className="text-xl font-bold text-gray-900">Mis tareas</h1>

        {error && <Alert type="error" message={error} onClose={() => setError(null)} />}

        {/* ── Formulario crear tarea ── */}
        <form
          onSubmit={handleCreate}
          className="rounded-lg border border-gray-200 bg-white p-4 shadow-sm space-y-3"
        >
          <input
            type="text"
            required
            placeholder="Título de la tarea"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm
              focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
          />
          <textarea
            placeholder="Descripción (opcional)"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            rows={2}
            className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm resize-none
              focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
          />
          <Button type="submit" loading={creating}>
            Crear tarea
          </Button>
        </form>

        {/* ── Lista de tareas ── */}
        {loading ? (
          <p className="text-sm text-gray-400">Cargando tareas…</p>
        ) : tasks.length === 0 ? (
          <p className="text-sm text-gray-400">No tienes tareas aún. ¡Crea tu primera!</p>
        ) : (
          <ul className="space-y-3">
            {tasks.map((task) => (
              <li
                key={task.id}
                className="rounded-lg border border-gray-200 bg-white p-4 shadow-sm"
              >
                <div className="flex items-start justify-between gap-3">
                  <div className="min-w-0 flex-1">
                    <h3 className="font-medium text-gray-900 truncate">{task.title}</h3>
                    {task.description && (
                      <p className="mt-1 text-sm text-gray-500">{task.description}</p>
                    )}
                    <span
                      className={`mt-2 inline-block rounded-full px-2 py-0.5 text-xs font-medium ${STATUS_COLORS[task.status]}`}
                    >
                      {STATUS_LABELS[task.status]}
                    </span>
                  </div>

                  <div className="flex shrink-0 gap-2">
                    {task.status === 'PENDING' && (
                      <Button
                        variant="secondary"
                        onClick={() => handleStatusChange(task.id, 'IN_PROGRESS')}
                      >
                        Iniciar
                      </Button>
                    )}
                    {(task.status === 'PENDING' || task.status === 'IN_PROGRESS') && (
                      <Button
                        variant="primary"
                        onClick={() => handleStatusChange(task.id, 'COMPLETED')}
                      >
                        Completar
                      </Button>
                    )}
                    {(task.status === 'PENDING' || task.status === 'IN_PROGRESS') && (
                      <Button
                        variant="danger"
                        onClick={() => handleStatusChange(task.id, 'CANCELLED')}
                      >
                        Cancelar
                      </Button>
                    )}
                    <Button variant="secondary" onClick={() => handleDelete(task.id)}>
                      Eliminar
                    </Button>
                  </div>
                </div>
              </li>
            ))}
          </ul>
        )}
      </main>
    </div>
  );
}
