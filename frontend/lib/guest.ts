const GUEST_TOKEN_KEY = "danshop-guest-token";

export function getOrCreateGuestToken(): string {
  const existing = localStorage.getItem(GUEST_TOKEN_KEY);
  if (existing) {
    return existing;
  }
  const token = crypto.randomUUID();
  localStorage.setItem(GUEST_TOKEN_KEY, token);
  return token;
}
