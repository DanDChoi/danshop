import type { OrderStatus } from "./api";

export const ORDER_STATUS_LABELS: Record<OrderStatus, string> = {
  PENDING: "결제 대기",
  PAID: "결제 완료",
  SHIPPED: "배송 중",
  DELIVERED: "배송 완료",
  CANCELLED: "취소됨",
};
