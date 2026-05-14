import { describe, it, expect } from 'vitest';
import { AxiosError, AxiosHeaders } from 'axios';
import { extractErrorMessage } from '../utils/error';

describe('extractErrorMessage', () => {
  it('extracts message from axios response data', () => {
    const error = new AxiosError('fail', '400', undefined, undefined, {
      data: { message: 'Email ya registrado' },
      status: 400,
      statusText: 'Bad Request',
      headers: {},
      config: { headers: new AxiosHeaders() },
    });
    expect(extractErrorMessage(error)).toBe('Email ya registrado');
  });

  it('falls back to statusText when no message in data', () => {
    const error = new AxiosError('fail', '500', undefined, undefined, {
      data: {},
      status: 500,
      statusText: 'Internal Server Error',
      headers: {},
      config: { headers: new AxiosHeaders() },
    });
    expect(extractErrorMessage(error)).toBe('Internal Server Error');
  });

  it('falls back to error.message for network errors', () => {
    const error = new AxiosError('Network Error');
    expect(extractErrorMessage(error)).toBe('Network Error');
  });

  it('handles plain Error instances', () => {
    expect(extractErrorMessage(new Error('boom'))).toBe('boom');
  });

  it('returns generic message for unknown errors', () => {
    expect(extractErrorMessage('something')).toBe('Ocurrió un error inesperado.');
  });
});
