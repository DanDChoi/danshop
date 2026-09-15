"use client";

import { useEffect } from "react";
import { usePathname, useRouter } from "next/navigation";
import { useAuth } from "./auth-context";

/**
 * 관리자 전용 페이지에서 사용. 비로그인이면 /login 으로(returnTo 포함),
 * 로그인은 했지만 ADMIN 이 아니면 홈으로 보낸다.
 * 이건 화면 노출만 막는 것이고, 실제 인가는 백엔드 @PreAuthorize 가 담당한다.
 */
export function useRequireAdmin(): boolean {
  const { accessToken, isAdmin } = useAuth();
  const router = useRouter();
  const pathname = usePathname();

  useEffect(() => {
    if (!accessToken) {
      router.replace(`/login?returnTo=${encodeURIComponent(pathname)}`);
      return;
    }
    if (!isAdmin) {
      router.replace("/");
    }
  }, [accessToken, isAdmin, pathname, router]);

  return Boolean(accessToken) && isAdmin;
}
