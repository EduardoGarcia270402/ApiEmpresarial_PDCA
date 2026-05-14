import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import Alert from '../components/Alert';

describe('Alert', () => {
  it('renders error alert with message', () => {
    render(<Alert type="error" message="Algo salió mal" />);
    expect(screen.getByRole('alert')).toHaveTextContent('Algo salió mal');
  });

  it('renders success alert with message', () => {
    render(<Alert type="success" message="Operación exitosa" />);
    expect(screen.getByRole('alert')).toHaveTextContent('Operación exitosa');
  });

  it('calls onClose when close button is clicked', async () => {
    const user = userEvent.setup();
    let closed = false;
    render(<Alert type="error" message="Error" onClose={() => { closed = true; }} />);
    await user.click(screen.getByLabelText('Cerrar alerta'));
    expect(closed).toBe(true);
  });

  it('hides after close button is clicked', async () => {
    const user = userEvent.setup();
    render(<Alert type="error" message="Error" onClose={() => {}} />);
    await user.click(screen.getByLabelText('Cerrar alerta'));
    expect(screen.queryByRole('alert')).toBeNull();
  });

  it('does not show close button without onClose', () => {
    render(<Alert type="error" message="Error" />);
    expect(screen.queryByLabelText('Cerrar alerta')).toBeNull();
  });
});
