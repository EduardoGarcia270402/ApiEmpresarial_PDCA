import {
  createContext,
  useCallback,
  useContext,
  useMemo,
  useState,
  type ReactNode,
} from 'react';
import { authApi } from '../api';
import { getToken, isAuthenticated, removeToken, setToken as saveToken } from '../utils/token';
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
  const [token, setToken] = useState<string | null>(() =>
    isAuthenticated() ? getToken() : null,
  );
  const loading = false;

  const login = useCallback(async (data: LoginRequest) => {
    const res = await authApi.login(data);
    saveToken(res.token);
    setToken(res.token);
  }, []);

  const register = useCallback(async (data: RegisterRequest) => {
    await authApi.register(data);
  }, []);

  const logout = useCallback(() => {
    removeToken();
    setToken(null);
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

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth(): AuthState {
  const ctx = useContext(AuthContext);
  if (ctx === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return ctx;
}
