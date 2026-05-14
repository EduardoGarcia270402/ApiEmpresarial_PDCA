import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { AuthProvider } from '../context/AuthContext';
import RegisterPage from '../pages/RegisterPage';

vi.mock('../api', () => ({
  authApi: {
    register: vi.fn(),
  },
}));

import { authApi } from '../api';

function renderRegisterPage() {
  return render(
    <MemoryRouter initialEntries={['/register']}>
      <AuthProvider>
        <RegisterPage />
      </AuthProvider>
    </MemoryRouter>,
  );
}

describe('RegisterPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    sessionStorage.clear();
  });

  it('renders register form', () => {
    renderRegisterPage();
    expect(screen.getByText('Crear cuenta')).toBeInTheDocument();
    expect(screen.getByLabelText('Nombre')).toBeInTheDocument();
    expect(screen.getByLabelText('Correo electrónico')).toBeInTheDocument();
    expect(screen.getByLabelText('Contraseña')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Registrarse' })).toBeInTheDocument();
  });

  it('renders link to login page', () => {
    renderRegisterPage();
    expect(screen.getByText('Inicia sesión')).toHaveAttribute('href', '/login');
  });

  it('calls register on form submit', async () => {
    const user = userEvent.setup();
    vi.mocked(authApi.register).mockResolvedValue(undefined);

    renderRegisterPage();
    await user.type(screen.getByLabelText('Nombre'), 'Mateo');
    await user.type(screen.getByLabelText('Correo electrónico'), 'mateo@test.com');
    await user.type(screen.getByLabelText('Contraseña'), '123456');
    await user.click(screen.getByRole('button', { name: 'Registrarse' }));

    await waitFor(() => {
      expect(authApi.register).toHaveBeenCalledWith({
        name: 'Mateo',
        email: 'mateo@test.com',
        password: '123456',
      });
    });
  });

  it('shows error message on register failure', async () => {
    const user = userEvent.setup();
    vi.mocked(authApi.register).mockRejectedValue(new Error('Email ya registrado'));

    renderRegisterPage();
    await user.type(screen.getByLabelText('Nombre'), 'Test');
    await user.type(screen.getByLabelText('Correo electrónico'), 'dup@test.com');
    await user.type(screen.getByLabelText('Contraseña'), '123456');
    await user.click(screen.getByRole('button', { name: 'Registrarse' }));

    await waitFor(() => {
      expect(screen.getByRole('alert')).toHaveTextContent('Email ya registrado');
    });
  });
});
