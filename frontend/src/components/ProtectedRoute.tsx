import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

/**
 * Wrapper de ruta protegida.
 * Si el usuario no está autenticado, redirige a /login.
 * Mientras se hidrata el estado de sesión muestra un spinner simple.
 */
export default function ProtectedRoute() {
  const { isLoggedIn, loading } = useAuth();

  if (loading) {
    return (
      <div className="flex h-screen items-center justify-center">
        <span className="text-gray-400 text-sm">Cargando…</span>
      </div>
    );
  }

  return isLoggedIn ? <Outlet /> : <Navigate to="/login" replace />;
}
