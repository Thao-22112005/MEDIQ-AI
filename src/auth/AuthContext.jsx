import React, { createContext, useContext, useState, useEffect } from "react";
import { mockAuthService } from "../services/mockAuthService";

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const session = mockAuthService.getCurrentSession();
    if (session && session.user) {
      setUser(session.user);
      setToken(session.token);
    }
    setLoading(false);
  }, []);

  const login = async (email, password) => {
    const authData = await mockAuthService.login(email, password);
    setUser(authData.user);
    setToken(authData.token);
    return authData.user;
  };

  const logout = () => {
    mockAuthService.logout();
    setUser(null);
    setToken(null);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated: !!user,
        loading,
        login,
        logout
      }}
    >
      {!loading && children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth phải được sử dụng trong AuthProvider");
  }
  return context;
};