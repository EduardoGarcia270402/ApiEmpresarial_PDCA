import { createBrowserRouter } from 'react-router-dom';
import { ProtectedRoute } from '../components';
import { DashboardPage, LoginPage, RegisterPage } from '../pages';

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
    ],
  },
]);

export default router;
