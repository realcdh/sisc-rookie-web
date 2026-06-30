import { createContext, useContext, useState } from 'react';
import { setToken, clearToken } from './api';

const USER_KEY = 'sisc_user';
const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? JSON.parse(raw) : null;
  });

  // 로그인 응답(LoginResponse)을 받아 토큰과 사용자 정보를 저장한다.
  function login(loginResponse) {
    setToken(loginResponse.accessToken);
    const profile = {
      memberId: loginResponse.memberId,
      name: loginResponse.name,
      email: loginResponse.email,
      role: loginResponse.role,
    };
    localStorage.setItem(USER_KEY, JSON.stringify(profile));
    setUser(profile);
  }

  function logout() {
    clearToken();
    localStorage.removeItem(USER_KEY);
    setUser(null);
  }

  return (
    <AuthContext.Provider value={{ user, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}

// 역할 판별 헬퍼
export const isAdmin = (user) => user?.role === 'ADMIN';
export const isStaff = (user) => user?.role === 'STAFF' || user?.role === 'ADMIN';
