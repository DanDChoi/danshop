const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

export class ApiError extends Error {
  code?: string;
  status: number;

  constructor(status: number, message: string, code?: string) {
    super(message);
    this.status = status;
    this.code = code;
  }
}

async function request<T>(
  path: string,
  options: RequestInit = {}
): Promise<T> {
  const res = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...options.headers,
    },
  });

  const isJson = res.headers.get("content-type")?.includes("application/json");
  const body = isJson ? await res.json() : await res.text();

  if (!res.ok) {
    const message =
      isJson && body && typeof body === "object" && "message" in body
        ? (body as { message: string }).message
        : typeof body === "string" && body
          ? body
          : "요청을 처리하지 못했습니다.";
    const code = isJson && body && typeof body === "object" && "code" in body
      ? (body as { code: string }).code
      : undefined;
    throw new ApiError(res.status, message, code);
  }

  return body as T;
}

export type SignupPayload = {
  userId: string;
  name: string;
  email: string;
  password: string;
};

export type LoginPayload = {
  userId: string;
  password: string;
};

export type TokenResponse = {
  accessToken: string;
  refreshToken: string;
};

export function signup(payload: SignupPayload): Promise<string> {
  return request<string>("/user/signup", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function login(payload: LoginPayload): Promise<TokenResponse> {
  return request<TokenResponse>("/user/login", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}
