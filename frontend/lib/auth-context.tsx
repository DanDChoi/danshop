"use client";

import { createContext, useContext, useSyncExternalStore, ReactNode } from "react";
import { registerAuthBridge, logout as logoutRequest, type TokenResponse } from "./api";
import { getRoleFromToken } from "./jwt";

const STORAGE_KEY = "danshop-auth";

type StoredAuth = {
  userId: string;
  accessToken: string;
  refreshToken: string;
};

type Listener = () => void;
const listeners = new Set<Listener>();
let cached: StoredAuth | null | undefined;

function readStored(): StoredAuth | null {
  const raw = localStorage.getItem(STORAGE_KEY);
  return raw ? (JSON.parse(raw) as StoredAuth) : null;
}

function getSnapshot(): StoredAuth | null {
  if (cached === undefined) {
    cached = readStored();
  }
  return cached;
}

function getServerSnapshot(): StoredAuth | null {
  return null;
}

function subscribe(listener: Listener) {
  listeners.add(listener);
  return () => listeners.delete(listener);
}

function setStored(value: StoredAuth | null) {
  cached = value;
  if (value) {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(value));
  } else {
    localStorage.removeItem(STORAGE_KEY);
  }
  listeners.forEach((listener) => listener());
}

// 액세스 토큰이 만료돼 401 이 나면 api.ts 가 refresh 토큰으로 재발급을 시도한다.
registerAuthBridge({
  getRefreshToken: () => (cached ?? readStored())?.refreshToken ?? null,
  onAccessTokenRefreshed: (accessToken) => {
    const current = cached ?? readStored();
    if (current) {
      setStored({ ...current, accessToken });
    }
  },
  onRefreshFailed: () => setStored(null),
});

type AuthState = {
  userId: string | null;
  accessToken: string | null;
  isAdmin: boolean;
  login: (userId: string, tokens: TokenResponse) => void;
  logout: () => void;
};

const AuthContext = createContext<AuthState | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const stored = useSyncExternalStore(subscribe, getSnapshot, getServerSnapshot);

  const login = (id: string, tokens: TokenResponse) => {
    setStored({ userId: id, accessToken: tokens.accessToken, refreshToken: tokens.refreshToken });
  };

  const logout = () => {
    const accessToken = stored?.accessToken;
    // 로컬 토큰 삭제는 서버 호출 성공 여부와 상관없이 항상 먼저/반드시 실행한다.
    setStored(null);
    if (accessToken) {
      logoutRequest(accessToken).catch(() => {
        /* 서버 쪽 refresh token 무효화가 실패해도 로컬 로그아웃은 이미 끝난 상태 */
      });
    }
  };

  return (
    <AuthContext.Provider
      value={{
        userId: stored?.userId ?? null,
        accessToken: stored?.accessToken ?? null,
        isAdmin: getRoleFromToken(stored?.accessToken ?? null) === "ROLE_ADMIN",
        login,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error("useAuth must be used within AuthProvider");
  }
  return ctx;
}
