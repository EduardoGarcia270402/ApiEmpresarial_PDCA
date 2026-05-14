import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import Input from '../components/Input';

describe('Input', () => {
  it('renders label and input', () => {
    render(<Input label="Correo electrónico" />);
    expect(screen.getByLabelText('Correo electrónico')).toBeInTheDocument();
  });

  it('generates id from label text', () => {
    render(<Input label="Correo electrónico" />);
    const input = screen.getByLabelText('Correo electrónico');
    expect(input.id).toBe('correo-electrónico');
  });

  it('uses provided id over generated one', () => {
    render(<Input label="Email" id="custom-id" />);
    expect(screen.getByLabelText('Email').id).toBe('custom-id');
  });

  it('shows error message', () => {
    render(<Input label="Email" error="Campo requerido" />);
    expect(screen.getByRole('alert')).toHaveTextContent('Campo requerido');
  });

  it('sets aria-invalid when error is present', () => {
    render(<Input label="Email" error="Inválido" />);
    expect(screen.getByLabelText('Email')).toHaveAttribute('aria-invalid', 'true');
  });

  it('does not show error when none provided', () => {
    render(<Input label="Email" />);
    expect(screen.queryByRole('alert')).toBeNull();
  });

  it('accepts user input', async () => {
    const user = userEvent.setup();
    render(<Input label="Nombre" />);
    const input = screen.getByLabelText('Nombre');
    await user.type(input, 'Mateo');
    expect(input).toHaveValue('Mateo');
  });
});
