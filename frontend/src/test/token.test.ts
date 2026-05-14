import { describe, it, expect, beforeEach } from 'vitest';
import { getToken, setToken, removeToken, isAuthenticated } from '../utils/token';

describe('token utils', () => {
  beforeEach(() => {
    sessionStorage.clear();
  });

  it('returns null when no token is stored', () => {
    expect(getToken()).toBeNull();
  });

  it('stores and retrieves a token', () => {
    setToken('abc123');
    expect(getToken()).toBe('abc123');
  });

  it('removes the token', () => {
    setToken('abc123');
    removeToken();
    expect(getToken()).toBeNull();
  });

  it('isAuthenticated returns false when no token', () => {
    expect(isAuthenticated()).toBe(false);
  });

  it('isAuthenticated returns true when token exists', () => {
    setToken('abc123');
    expect(isAuthenticated()).toBe(true);
  });
});
