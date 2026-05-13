import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react';
import { authApi } from '../api';
import { getToken, isAuthenticated, removeToken, setToken } from '../utils/token';
import type { LoginRequest, RegisterRequest } from '../types';

interface AuthState {
  token: string | null;
  isLoggedIn: boolean;
  loading: boolean;
  login: (data: LoginRequest) => Promise<void>;
  register: (data: RegisterRequest) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthState | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setTokenState] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  // Hidrata el estado desde sessionStorage al montar
  useEffect(() => {
    if (isAuthenticated()) {
      setTokenState(getToken());
    }
    setLoading(false);
  }, []);

  const login = useCallback(async (data: LoginRequest) => {
    const res = await authApi.login(data);
    setToken(res.token);
    setTokenState(res.token);
  }, []);

  const register = useCallback(async (data: RegisterRequest) => {
    await authApi.register(data);
  }, []);

  const logout = useCallback(() => {
    removeToken();
    setTokenState(null);
  }, []);

  const value = useMemo<AuthState>(
    () => ({
      token,
      isLoggedIn: token !== null,
      loading,
      login,
      register,
      logout,
    }),
    [token, loading, login, register, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthState {
  const ctx = useContext(AuthContext);
  if (ctx === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return ctx;
}
