import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import Button from '../components/Button';

describe('Button', () => {
  it('renders children text', () => {
    render(<Button>Click me</Button>);
    expect(screen.getByRole('button', { name: 'Click me' })).toBeInTheDocument();
  });

  it('is disabled when loading', () => {
    render(<Button loading>Enviando</Button>);
    expect(screen.getByRole('button')).toBeDisabled();
  });

  it('is disabled when disabled prop is set', () => {
    render(<Button disabled>No</Button>);
    expect(screen.getByRole('button')).toBeDisabled();
  });

  it('shows spinner when loading', () => {
    const { container } = render(<Button loading>Cargando</Button>);
    expect(container.querySelector('svg.animate-spin')).toBeInTheDocument();
  });

  it('does not show spinner when not loading', () => {
    const { container } = render(<Button>Normal</Button>);
    expect(container.querySelector('svg.animate-spin')).toBeNull();
  });

  it('fires onClick handler', async () => {
    const user = userEvent.setup();
    let clicked = false;
    render(<Button onClick={() => { clicked = true; }}>Go</Button>);
    await user.click(screen.getByRole('button'));
    expect(clicked).toBe(true);
  });

  it('applies variant classes', () => {
    const { rerender } = render(<Button variant="primary">P</Button>);
    expect(screen.getByRole('button')).toHaveClass('bg-indigo-600');

    rerender(<Button variant="danger">D</Button>);
    expect(screen.getByRole('button')).toHaveClass('bg-red-600');

    rerender(<Button variant="secondary">S</Button>);
    expect(screen.getByRole('button')).toHaveClass('bg-white');
  });
});
