import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { AuthProvider } from '../context/AuthContext';
import LoginPage from '../pages/LoginPage';

// Mock the auth API
vi.mock('../api', () => ({
  authApi: {
    login: vi.fn(),
  },
}));

import { authApi } from '../api';

function renderLoginPage() {
  return render(
    <MemoryRouter initialEntries={['/login']}>
      <AuthProvider>
        <LoginPage />
      </AuthProvider>
    </MemoryRouter>,
  );
}

describe('LoginPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    sessionStorage.clear();
  });

  it('renders login form', () => {
    renderLoginPage();
    expect(screen.getByText('Iniciar sesión')).toBeInTheDocument();
    expect(screen.getByLabelText('Correo electrónico')).toBeInTheDocument();
    expect(screen.getByLabelText('Contraseña')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Ingresar' })).toBeInTheDocument();
  });

  it('renders link to register page', () => {
    renderLoginPage();
    expect(screen.getByText('Regístrate')).toHaveAttribute('href', '/register');
  });

  it('calls login on form submit', async () => {
    const user = userEvent.setup();
    vi.mocked(authApi.login).mockResolvedValue({ token: 'jwt123', expiresIn: 7200 });

    renderLoginPage();
    await user.type(screen.getByLabelText('Correo electrónico'), 'test@test.com');
    await user.type(screen.getByLabelText('Contraseña'), '123456');
    await user.click(screen.getByRole('button', { name: 'Ingresar' }));

    await waitFor(() => {
      expect(authApi.login).toHaveBeenCalledWith({
        email: 'test@test.com',
        password: '123456',
      });
    });
  });

  it('shows error message on login failure', async () => {
    const user = userEvent.setup();
    vi.mocked(authApi.login).mockRejectedValue(new Error('Credenciales inválidas'));

    renderLoginPage();
    await user.type(screen.getByLabelText('Correo electrónico'), 'bad@test.com');
    await user.type(screen.getByLabelText('Contraseña'), 'wrong');
    await user.click(screen.getByRole('button', { name: 'Ingresar' }));

    await waitFor(() => {
      expect(screen.getByRole('alert')).toHaveTextContent('Credenciales inválidas');
    });
  });
});
