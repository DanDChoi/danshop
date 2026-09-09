import type { DiscountType, MyCoupon } from "./api";

export function formatDiscount(type: DiscountType, value: number): string {
  return type === "RATE" ? `${value}% 할인` : `${value.toLocaleString()}원 할인`;
}

export function formatCouponDate(iso: string): string {
  return iso.slice(0, 10).split("-").join(".");
}

/** 주문 금액에 쿠폰을 적용했을 때 깎이는 금액. 백엔드 OrderService 계산식과 일치. */
export function couponDiscount(coupon: MyCoupon, amount: number): number {
  if (amount < coupon.minOrderAmount) return 0;
  if (coupon.discountType === "AMOUNT") {
    return Math.min(coupon.discountValue, amount);
  }
  return amount - Math.floor(amount * (1 - coupon.discountValue / 100));
}

/** 이 쿠폰을 지금 이 주문 금액에 쓸 수 있는지 (미사용 / 만료 전 / 최소주문금액 충족) */
export function isCouponUsable(coupon: MyCoupon, amount: number, now = Date.now()): boolean {
  return (
    !coupon.used &&
    new Date(coupon.expiresAt).getTime() > now &&
    amount >= coupon.minOrderAmount
  );
}
