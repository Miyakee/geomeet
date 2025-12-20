import { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { authApi, LoginRequest } from '../services/api';

interface User {
  id: number;
  username: string;
  email: string;
}

interface AuthContextType {
  user: User | null;
  token: string | null;
  login: (credentials: LoginRequest) => Promise<void>;
  setAuthFromResponse: (token: string, username: string, email: string) => void;
  logout: () => void;
  isAuthenticated: boolean;
  isInitialized: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

// eslint-disable-next-line react-refresh/only-export-components
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

interface AuthProviderProps {
  children: ReactNode;
}

// Helper function to decode JWT token and extract user ID
const decodeToken = (token: string): { userId: number; username: string } | null => {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => `%${  (`00${  c.charCodeAt(0).toString(16)}`).slice(-2)}`)
        .join(''),
    );
    const decoded = JSON.parse(jsonPayload);
    return {
      userId: decoded.userId || decoded.user_id,
      username: decoded.sub || decoded.username,
    };
  } catch (error) {
    console.error('Failed to decode token:', error);
    return null;
  }
};

export const AuthProvider = ({ children }: AuthProviderProps) => {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isInitialized, setIsInitialized] = useState(false);

  useEffect(() => {
    // Check for stored token on mount (synchronous check)
    const storedToken = localStorage.getItem('token');
    const storedUser = localStorage.getItem('user');
    if (storedToken && storedUser) {
      setToken(storedToken);
      const parsedUser = JSON.parse(storedUser);
      // If user doesn't have id, try to decode from token
      if (!parsedUser.id && storedToken) {
        const decoded = decodeToken(storedToken);
        if (decoded) {
          parsedUser.id = decoded.userId;
          localStorage.setItem('user', JSON.stringify(parsedUser));
        }
      }
      setUser(parsedUser);
    }
    setIsInitialized(true);
  }, []);

  const login = async (credentials: LoginRequest) => {
    const response = await authApi.login(credentials);
    setToken(response.token);
    
    // Decode token to get user ID
    const decoded = decodeToken(response.token);
    const userData = {
      id: decoded?.userId || 0,
      username: response.username,
      email: response.email,
    };
    setUser(userData);
    localStorage.setItem('token', response.token);
    localStorage.setItem('user', JSON.stringify(userData));
  };

  const setAuthFromResponse = (token: string, username: string, email: string) => {
    setToken(token);
    
    // Decode token to get user ID
    const decoded = decodeToken(token);
    const userData = {
      id: decoded?.userId || 0,
      username,
      email,
    };
    setUser(userData);
    localStorage.setItem('token', token);
    localStorage.setItem('user', JSON.stringify(userData));
  };

  const logout = () => {
    setToken(null);
    setUser(null);
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        login,
        setAuthFromResponse,
        logout,
        isAuthenticated: !!token,
        isInitialized,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

