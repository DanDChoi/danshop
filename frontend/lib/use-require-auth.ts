"use client";

import { useEffect } from "react";
import { usePathname, useRouter } from "next/navigation";
import { useAuth } from "./auth-context";

/**
 * 로그인이 필요한 페이지에서 사용. 토큰이 없으면 현재 경로를 returnTo 로 붙여
 * /login 으로 보낸다. 반환값이 null 인 동안에는 로딩/안내 UI를 렌더하면 된다.
 */
export function useRequireAuth(): string | null {
  const { accessToken } = useAuth();
  const router = useRouter();
  const pathname = usePathname();

  useEffect(() => {
    if (!accessToken) {
      router.replace(`/login?returnTo=${encodeURIComponent(pathname)}`);
    }
  }, [accessToken, pathname, router]);

  return accessToken;
}
