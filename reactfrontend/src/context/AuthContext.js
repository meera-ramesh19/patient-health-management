import { createContext, useContext, useState, useEffect } from 'react';
import { authApi } from '../api/auth';

const AuthContext = createContext(null);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within AuthProvider');
  return context;
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);

  useEffect(() => {
    const saved = localStorage.getItem('user');
    if (saved) {
      setUser(JSON.parse(saved));
    }
  }, []);

  const saveUserData = (userData) => {
    localStorage.setItem('token', userData.token);
    localStorage.setItem('user', JSON.stringify(userData));
    setUser(userData);
  };

  const login = async (data) => {
    const response = await authApi.login(data);
    saveUserData(response.data);
  };

  const register = async (data) => {
    await authApi.register(data);
  };

  const googleLogin = async (data) => {
    const response = await authApi.googleLogin(data);
    saveUserData(response.data);
  };

  const socialLogin = async (data) => {
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
