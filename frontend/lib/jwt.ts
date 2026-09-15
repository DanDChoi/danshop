/**
 * JWT payload 를 서명 검증 없이 디코드한다. 화면에 role 등을 표시/분기하는 용도로만
 * 쓰고, 실제 인가는 항상 백엔드(@PreAuthorize, SecurityConfig)가 담당한다.
 */
export function decodeJwtPayload<T = Record<string, unknown>>(token: string): T | null {
  try {
    const payload = token.split(".")[1];
    if (!payload) return null;

    const base64 = payload.replace(/-/g, "+").replace(/_/g, "/");
    const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), "=");
    const json = decodeURIComponent(
      atob(padded)
        .split("")
        .map((c) => "%" + c.charCodeAt(0).toString(16).padStart(2, "0"))
        .join("")
    );

    return JSON.parse(json) as T;
  } catch {
    return null;
  }
}

/** 백엔드 Role enum: ROLE_USER | ROLE_ADMIN */
export function getRoleFromToken(token: string | null): string | null {
  if (!token) return null;
  return decodeJwtPayload<{ role?: string }>(token)?.role ?? null;
}
