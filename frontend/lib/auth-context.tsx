"use client";

import { createContext, useContext, useSyncExternalStore, ReactNode } from "react";
import type { TokenResponse } from "./api";

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

type AuthState = {
  userId: string | null;
  accessToken: string | null;
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
    setStored(null);
  };

  return (
    <AuthContext.Provider
      value={{
        userId: stored?.userId ?? null,
        accessToken: stored?.accessToken ?? null,
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
