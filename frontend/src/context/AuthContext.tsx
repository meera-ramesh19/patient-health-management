import { createContext, useContext, useState, useEffect, type ReactNode } from 'react';
import { authApi } from '../api/auth';
import type { LoginRequest, RegisterRequest, LoginResponse, GoogleLoginRequest, SocialLoginRequest } from '../api/auth';

// The shape of our auth context — what data and functions are available
// Now includes social login methods alongside regular login/register
interface AuthContextType {
  user: LoginResponse | null;
  login: (data: LoginRequest) => Promise<void>;
  register: (data: RegisterRequest) => Promise<void>;
  googleLogin: (data: GoogleLoginRequest) => Promise<void>;
  socialLogin: (data: SocialLoginRequest) => Promise<void>;
  logout: () => void;
  isAuthenticated: boolean;
}

const AuthContext = createContext<AuthContextType | null>(null);

// Hook for any component to access auth state
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within AuthProvider');
  return context;
};

// Wraps the app — provides auth state to all child components
export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [user, setUser] = useState<LoginResponse | null>(null);

  // On first load, check if we have a saved session
  useEffect(() => {
    const saved = localStorage.getItem('user');
    if (saved) {
      setUser(JSON.parse(saved));
    }
  }, []);

  // Helper: save user data after any successful login
  // Used by all login methods (regular, Google, social)
  // The backend always returns the same LoginResponse shape:
  //   { token: "eyJ...", username: "john", role: "ROLE_PATIENT" }
  const saveUserData = (userData: LoginResponse) => {
    localStorage.setItem('token', userData.token);
    localStorage.setItem('user', JSON.stringify(userData));
    setUser(userData);
  };

  // Regular username/password login
  const login = async (data: LoginRequest) => {
    const response = await authApi.login(data);
    saveUserData(response.data);
  };

  const register = async (data: RegisterRequest) => {
    await authApi.register(data);
  };

  // Google Sign-In login
  // Flow: Google popup → user authenticates → Google gives us an ID token
  //       → we send that token to POST /api/auth/google
  //       → backend verifies with Google, finds/creates user, returns OUR JWT
  //       → from here on, it's identical to regular login
  const googleLogin = async (data: GoogleLoginRequest) => {
    const response = await authApi.googleLogin(data);
    saveUserData(response.data);
  };

  // Unified social login (Google, Facebook, GitHub)
  // Same flow as googleLogin, but uses POST /api/auth/social
  // with a "provider" field to tell the backend which service to verify with
  const socialLogin = async (data: SocialLoginRequest) => {
    const response = await authApi.socialLogin(data);
    saveUserData(response.data);
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{
      user, login, register, googleLogin, socialLogin, logout,
      isAuthenticated: !!user
    }}>
      {children}
    </AuthContext.Provider>
  );
};
