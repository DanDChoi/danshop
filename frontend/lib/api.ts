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

// ─────────────────────────────────────────────
// 회원
// ─────────────────────────────────────────────

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

// ─────────────────────────────────────────────
// 식별(회원/게스트) 공통 헤더
// ─────────────────────────────────────────────

export type Identity = {
  token?: string | null;
  guestToken?: string | null;
};

function identityHeaders(identity: Identity): Record<string, string> {
  if (identity.token) {
    return { Authorization: `Bearer ${identity.token}` };
  }
  if (identity.guestToken) {
    return { "X-Guest-Token": identity.guestToken };
  }
  return {};
}

// ─────────────────────────────────────────────
// 상품 (조회는 비회원도 가능)
// ─────────────────────────────────────────────

export type Product = {
  productNo: number;
  productName: string;
  price: number;
  category: string | null;
  stock: number;
  description: string | null;
};

export type Page<T> = {
  content: T[];
  totalPages: number;
  totalElements: number;
  number: number;
  size: number;
};

export type GetProductsParams = {
  page?: number;
  size?: number;
  keyword?: string;
  category?: string;
  minPrice?: number;
  maxPrice?: number;
  sort?: string;
};

export function getProducts(params: GetProductsParams = {}): Promise<Page<Product>> {
  const query = new URLSearchParams();
  if (params.page !== undefined) query.set("page", String(params.page));
  if (params.size !== undefined) query.set("size", String(params.size));
  if (params.keyword) query.set("keyword", params.keyword);
  if (params.category) query.set("category", params.category);
  if (params.minPrice !== undefined) query.set("minPrice", String(params.minPrice));
  if (params.maxPrice !== undefined) query.set("maxPrice", String(params.maxPrice));
  if (params.sort) query.set("sort", params.sort);

  const qs = query.toString();
  return request<Page<Product>>(`/product${qs ? `?${qs}` : ""}`);
}

export function getProduct(productNo: number): Promise<Product> {
  return request<Product>(`/product/${productNo}`);
}

// ─────────────────────────────────────────────
// 장바구니 (회원/게스트 겸용)
// ─────────────────────────────────────────────

export type CartItem = {
  productId: number;
  productName: string;
  price: number;
  quantity: number;
  totalPrice: number;
};

export type Cart = {
  items: CartItem[];
  totalAmount: number;
};

export function getCart(identity: Identity): Promise<Cart> {
  return request<Cart>("/cart", { headers: identityHeaders(identity) });
}

export function addToCart(identity: Identity, productId: number, quantity = 1): Promise<string> {
  return request<string>(`/cart/${productId}?quantity=${quantity}`, {
    method: "POST",
    headers: identityHeaders(identity),
  });
}

export function updateCartQuantity(identity: Identity, productId: number, quantity: number): Promise<string> {
  return request<string>(`/cart/${productId}?quantity=${quantity}`, {
    method: "PATCH",
    headers: identityHeaders(identity),
  });
}

export function removeFromCart(identity: Identity, productId: number): Promise<string> {
  return request<string>(`/cart/${productId}`, {
    method: "DELETE",
    headers: identityHeaders(identity),
  });
}

export function clearCart(identity: Identity): Promise<string> {
  return request<string>("/cart", {
    method: "DELETE",
    headers: identityHeaders(identity),
  });
}

export type CheckoutPayload = {
  couponId?: number;
  usePoints?: number;
  postNo: string;
  baseAddr: string;
  detailAddr: string;
  // 비회원 checkout에서만 사용
  ordererName?: string;
  ordererEmail?: string;
  ordererPhone?: string;
};

export function checkoutCart(identity: Identity, payload: CheckoutPayload): Promise<{ orderId: number }> {
  return request<{ orderId: number }>("/cart/checkout", {
    method: "POST",
    headers: identityHeaders(identity),
    body: JSON.stringify(payload),
  });
}

// ─────────────────────────────────────────────
// 주문 (회원 전용)
// ─────────────────────────────────────────────

export type OrderStatus = "PENDING" | "PAID" | "SHIPPED" | "DELIVERED" | "CANCELLED";

export type OrderSummary = {
  orderId: number;
  status: OrderStatus;
  payAmount: number;
  postNo: string;
  baseAddr: string;
  detailAddr: string;
  userId: string | null;
};

export type OrderItemDetail = {
  productId: number;
  productName: string;
  price: number;
  quantity: number;
  totalPrice: number;
};

export type OrderDetail = {
  orderId: number;
  status: OrderStatus;
  payAmount: number;
  postNo: string;
  baseAddr: string;
  detailAddr: string;
  items: OrderItemDetail[];
};

export function getOrders(token: string, page = 0, size = 10): Promise<Page<OrderSummary>> {
  return request<Page<OrderSummary>>(`/orders?page=${page}&size=${size}`, {
    headers: { Authorization: `Bearer ${token}` },
  });
}

export function getOrder(token: string, orderId: number): Promise<OrderDetail> {
  return request<OrderDetail>(`/orders/${orderId}`, {
    headers: { Authorization: `Bearer ${token}` },
  });
}

export function cancelOrder(token: string, orderId: number): Promise<string> {
  return request<string>(`/orders/${orderId}/cancel`, {
    method: "PATCH",
    headers: { Authorization: `Bearer ${token}` },
  });
}

export type UpdateAddressPayload = {
  postNo: string;
  baseAddr: string;
  detailAddr: string;
};

export function updateOrderAddress(
  token: string,
  orderId: number,
  payload: UpdateAddressPayload
): Promise<string> {
  return request<string>(`/orders/${orderId}/address`, {
    method: "PATCH",
    headers: { Authorization: `Bearer ${token}` },
    body: JSON.stringify(payload),
  });
}

// ─────────────────────────────────────────────
// 비회원 주문 조회
// ─────────────────────────────────────────────

export function lookupGuestOrder(orderId: number, email: string): Promise<OrderDetail> {
  return request<OrderDetail>("/guest/orders/lookup", {
    method: "POST",
    body: JSON.stringify({ orderId, email }),
  });
}
