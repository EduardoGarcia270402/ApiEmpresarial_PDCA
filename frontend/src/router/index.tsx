import { createBrowserRouter } from 'react-router-dom';
import { ProtectedRoute } from '../components';
import { DashboardPage, LoginPage, RegisterPage, TaskDetailPage } from '../pages';

/**
 * Router centralizado.
 * Rutas públicas : /login, /register
 * Rutas protegidas: / (dashboard)
 */
const router = createBrowserRouter([
  {
    path: '/login',
    element: <LoginPage />,
  },
  {
    path: '/register',
    element: <RegisterPage />,
  },
  {
    element: <ProtectedRoute />,
    children: [
      {
        path: '/',
        element: <DashboardPage />,
      },
      {
        path: '/task/:id',
        element: <TaskDetailPage />,
      },
    ],
  },
]);

export default router;
