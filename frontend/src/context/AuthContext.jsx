import React, { createContext, useContext, useEffect, useMemo, useState } from "react";
import { authApi, messageOf } from "../api";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem("jobpilot_token");
    if (!token) { setLoading(false); return; }
    authApi.me()
      .then(({ data }) => setUser(data))
      .catch(() => { localStorage.removeItem("jobpilot_token"); setUser(null); })
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    const handler = () => { localStorage.removeItem("jobpilot_token"); setUser(null); };
    window.addEventListener("jobpilot:unauthorized", handler);
    return () => window.removeEventListener("jobpilot:unauthorized", handler);
  }, []);

  const login = async (email, password) => {
    const { data } = await authApi.login({ email, password });
    if (!data?.token) throw new Error("Login response did not contain a token.");
    localStorage.setItem("jobpilot_token", data.token);
    const profile = await authApi.me();
    setUser(profile.data);
    return profile.data;
  };
  const register = async (name, email, password) => {
    await authApi.register({ name, email, password });
    return login(email, password);
  };
  const logout = () => {
    localStorage.removeItem("jobpilot_token");
    setUser(null);
  };

  const value = useMemo(() => ({ user, loading, login, register, logout }), [user, loading]);
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
export const useAuth = () => useContext(AuthContext);
