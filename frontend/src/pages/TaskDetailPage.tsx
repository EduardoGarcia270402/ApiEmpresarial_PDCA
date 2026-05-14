import { useCallback, useEffect, useState } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
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
  PENDING: 'bg-yellow-100 text-yellow-800 border-yellow-300',
  IN_PROGRESS: 'bg-blue-100 text-blue-800 border-blue-300',
  COMPLETED: 'bg-green-100 text-green-800 border-green-300',
  CANCELLED: 'bg-gray-100 text-gray-500 border-gray-300',
};

const STATUS_ICONS: Record<TaskStatus, string> = {
  PENDING: '⏳',
  IN_PROGRESS: '🔄',
  COMPLETED: '✅',
  CANCELLED: '🚫',
};

function formatDate(iso: string): string {
  return new Date(iso).toLocaleDateString('es-CO', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

export default function TaskDetailPage() {
  const { id } = useParams<{ id: string }>();
  const location = useLocation();
  const navigate = useNavigate();

  const [task, setTask] = useState<Task | null>(
    (location.state as { task?: Task } | null)?.task ?? null,
  );
  const [loading, setLoading] = useState(!task);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [actionLoading, setActionLoading] = useState(false);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);

  const taskId = Number(id);

  const fetchTask = useCallback(async () => {
    try {
      const found = await tasksApi.getById(taskId);
      if (!found) {
        setError('La tarea no existe o no tienes acceso a ella.');
        return;
      }
      setTask(found);
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, [taskId]);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    if (!task) void fetchTask();
  }, [task, fetchTask]);

  const handleStatusChange = async (status: TaskStatus) => {
    if (!task) return;
    setActionLoading(true);
    setError(null);
    setSuccess(null);
    try {
      const updated = await tasksApi.changeStatus(task.id, { status });
      setTask(updated);
      setSuccess(`Estado cambiado a "${STATUS_LABELS[status]}" correctamente.`);
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setActionLoading(false);
    }
  };

  const handleDelete = async () => {
    if (!task) return;
    setActionLoading(true);
    setError(null);
    try {
      await tasksApi.remove(task.id);
      navigate('/', { replace: true });
    } catch (err) {
      setError(extractErrorMessage(err));
      setActionLoading(false);
    }
  };

  const isTerminal = task?.status === 'COMPLETED' || task?.status === 'CANCELLED';

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />

      <main className="mx-auto max-w-2xl px-4 py-8">
        {/* ── Navegación ── */}
        <button
          onClick={() => navigate('/')}
          className="mb-6 inline-flex items-center gap-1 text-sm font-medium text-gray-500 hover:text-indigo-600 transition-colors"
        >
          <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" strokeWidth={2} stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" d="M15.75 19.5L8.25 12l7.5-7.5" />
          </svg>
          Volver a mis tareas
        </button>

        {error && <Alert type="error" message={error} onClose={() => setError(null)} />}
        {success && <Alert type="success" message={success} onClose={() => setSuccess(null)} />}

        {loading && (
          /* ── Skeleton ── */
          <div className="animate-pulse space-y-4">
            <div className="h-8 w-2/3 rounded bg-gray-200" />
            <div className="h-4 w-1/4 rounded bg-gray-200" />
            <div className="h-24 w-full rounded bg-gray-200" />
          </div>
        )}

        {!loading && task && (
          <div className="space-y-6">
            {/* ── Encabezado ── */}
            <div className="rounded-xl border border-gray-200 bg-white p-6 shadow-sm">
              <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
                <div className="min-w-0 flex-1">
                  <h1 className="text-2xl font-bold text-gray-900 break-words">{task.title}</h1>
                  <div className="mt-3">
                    <span
                      className={`inline-flex items-center gap-1.5 rounded-full border px-3 py-1 text-sm font-medium ${STATUS_COLORS[task.status]}`}
                    >
                      <span>{STATUS_ICONS[task.status]}</span>
                      {STATUS_LABELS[task.status]}
                    </span>
                  </div>
                </div>
                <span className="shrink-0 rounded-md bg-gray-100 px-3 py-1 text-xs font-mono text-gray-500">
                  ID #{task.id}
                </span>
              </div>
            </div>

            {/* ── Descripción ── */}
            <div className="rounded-xl border border-gray-200 bg-white p-6 shadow-sm">
              <h2 className="text-sm font-semibold uppercase tracking-wider text-gray-400">
                Descripción
              </h2>
              <p className="mt-2 text-gray-700 whitespace-pre-wrap leading-relaxed">
                {task.description || 'Sin descripción.'}
              </p>
            </div>

            {/* ── Fechas ── */}
            <div className="rounded-xl border border-gray-200 bg-white p-6 shadow-sm">
              <h2 className="text-sm font-semibold uppercase tracking-wider text-gray-400">
                Línea de tiempo
              </h2>
              <dl className="mt-3 grid grid-cols-1 gap-4 sm:grid-cols-2">
                <div>
                  <dt className="text-xs text-gray-500">Fecha de creación</dt>
                  <dd className="mt-0.5 text-sm font-medium text-gray-900">
                    {formatDate(task.createdAt)}
                  </dd>
                </div>
                {task.completedAt && (
                  <div>
                    <dt className="text-xs text-gray-500">Fecha de finalización</dt>
                    <dd className="mt-0.5 text-sm font-medium text-gray-900">
                      {formatDate(task.completedAt)}
                    </dd>
                  </div>
                )}
              </dl>
            </div>

            {/* ── Acciones ── */}
            <div className="rounded-xl border border-gray-200 bg-white p-6 shadow-sm">
              <h2 className="text-sm font-semibold uppercase tracking-wider text-gray-400">
                Acciones
              </h2>

              {isTerminal ? (
                <p className="mt-3 text-sm text-gray-500 italic">
                  Esta tarea está {STATUS_LABELS[task.status].toLowerCase()} y no admite más
                  transiciones de estado.
                </p>
              ) : (
                <div className="mt-4 flex flex-wrap gap-3">
                  {task.status === 'PENDING' && (
                    <Button
                      variant="secondary"
                      loading={actionLoading}
                      onClick={() => handleStatusChange('IN_PROGRESS')}
                    >
                      Iniciar tarea
                    </Button>
                  )}
                  <Button
                    variant="primary"
                    loading={actionLoading}
                    onClick={() => handleStatusChange('COMPLETED')}
                  >
                    Marcar como completada
                  </Button>
                  <Button
                    variant="danger"
                    loading={actionLoading}
                    onClick={() => handleStatusChange('CANCELLED')}
                  >
                    Cancelar tarea
                  </Button>
                </div>
              )}

              {/* ── Eliminar con confirmación ── */}
              <div className="mt-6 border-t border-gray-100 pt-4">
                {showDeleteConfirm ? (
                  <div className="flex items-center gap-3">
                    <span className="text-sm text-gray-600">¿Estás seguro?</span>
                    <Button
                      variant="danger"
                      loading={actionLoading}
                      onClick={handleDelete}
                    >
                      Sí, eliminar
                    </Button>
                    <Button
                      variant="secondary"
                      onClick={() => setShowDeleteConfirm(false)}
                    >
                      Cancelar
                    </Button>
                  </div>
                ) : (
                  <button
                    type="button"
                    onClick={() => setShowDeleteConfirm(true)}
                    className="text-sm font-medium text-red-600 hover:text-red-800 transition-colors"
                  >
                    Eliminar esta tarea permanentemente
                  </button>
                )}
              </div>
            </div>
          </div>
        )}
      </main>
    </div>
  );
}
