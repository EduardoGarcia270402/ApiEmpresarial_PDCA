import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { AuthProvider } from '../context/AuthContext';
import ProtectedRoute from '../components/ProtectedRoute';
import { setToken, removeToken } from '../utils/token';

function renderWithRouter(initialRoute: string) {
  return render(
    <MemoryRouter initialEntries={[initialRoute]}>
      <AuthProvider>
        <Routes>
          <Route element={<ProtectedRoute />}>
            <Route path="/dashboard" element={<p>Dashboard content</p>} />
          </Route>
          <Route path="/login" element={<p>Login page</p>} />
        </Routes>
      </AuthProvider>
    </MemoryRouter>,
  );
}

describe('ProtectedRoute', () => {
  it('redirects to /login when not authenticated', () => {
    removeToken();
    renderWithRouter('/dashboard');
    expect(screen.getByText('Login page')).toBeInTheDocument();
  });

  it('renders child route when authenticated', () => {
    setToken('fake-token');
    renderWithRouter('/dashboard');
    expect(screen.getByText('Dashboard content')).toBeInTheDocument();
    removeToken();
  });
});
