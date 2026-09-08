import type { DiscountType } from "./api";

export function formatDiscount(type: DiscountType, value: number): string {
  return type === "RATE" ? `${value}% 할인` : `${value.toLocaleString()}원 할인`;
}

export function formatCouponDate(iso: string): string {
  return iso.slice(0, 10).split("-").join(".");
}
