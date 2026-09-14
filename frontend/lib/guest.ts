const GUEST_TOKEN_KEY = "danshop-guest-token";

/** 이미 발급된 게스트 토큰이 있으면 반환, 없으면 새로 만들지 않고 null. */
export function getGuestToken(): string | null {
  return localStorage.getItem(GUEST_TOKEN_KEY);
}

export function getOrCreateGuestToken(): string {
  const existing = localStorage.getItem(GUEST_TOKEN_KEY);
  if (existing) {
    return existing;
  }
  const token = crypto.randomUUID();
  localStorage.setItem(GUEST_TOKEN_KEY, token);
  return token;
}
