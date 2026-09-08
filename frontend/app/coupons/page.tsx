"use client";

import { useEffect, useState } from "react";
import {
  getAvailableCoupons,
  getMyCoupons,
  issueCoupon,
  ApiError,
  type Coupon,
  type MyCoupon,
} from "@/lib/api";
import { formatDiscount, formatCouponDate } from "@/lib/coupon";
import { useRequireAuth } from "@/lib/use-require-auth";

export default function CouponsPage() {
  const accessToken = useRequireAuth();

  const [available, setAvailable] = useState<Coupon[]>([]);
  const [mine, setMine] = useState<MyCoupon[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");

  const [issuingId, setIssuingId] = useState<number | null>(null);
  const [issueError, setIssueError] = useState<{ id: number; message: string } | null>(null);

  useEffect(() => {
    if (!accessToken) return;

    let cancelled = false;

    Promise.all([getAvailableCoupons(accessToken), getMyCoupons(accessToken)])
      .then(([availableCoupons, myCoupons]) => {
        if (cancelled) return;
        setAvailable(availableCoupons);
        setMine(myCoupons);
      })
      .catch((err) => {
        if (cancelled) return;
        setError(err instanceof ApiError ? err.message : "쿠폰을 불러오지 못했습니다.");
      })
      .finally(() => {
        if (!cancelled) setIsLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [accessToken]);

  const handleIssue = async (couponId: number) => {
    if (!accessToken) return;
    setIssuingId(couponId);
    setIssueError(null);
    try {
      await issueCoupon(accessToken, couponId);
      const [availableCoupons, myCoupons] = await Promise.all([
        getAvailableCoupons(accessToken),
        getMyCoupons(accessToken),
      ]);
      setAvailable(availableCoupons);
      setMine(myCoupons);
    } catch (err) {
      setIssueError({
        id: couponId,
        message: err instanceof ApiError ? err.message : "쿠폰 발급에 실패했습니다.",
      });
    } finally {
      setIssuingId(null);
    }
  };

  if (!accessToken) {
    return (
      <main className="max-w-2xl mx-auto px-4 md:px-6 py-16">
        <p className="text-sm text-gray-400">로그인이 필요합니다. 로그인 페이지로 이동합니다...</p>
      </main>
    );
  }

  const ownedIds = new Set(mine.map((c) => c.couponId));

  return (
    <main className="max-w-2xl mx-auto px-4 md:px-6 py-12 md:py-16">
      <h1 className="text-3xl font-extrabold text-gray-900 tracking-tight mb-6">쿠폰함</h1>

      {error && <p className="text-sm text-red-500 mb-4">{error}</p>}

      {isLoading ? (
        <p className="text-sm text-gray-400">불러오는 중...</p>
      ) : (
        <>
          <section className="mb-10">
            <h2 className="text-sm font-semibold text-gray-500 mb-3">발급 가능한 쿠폰</h2>
            {available.length === 0 ? (
              <p className="text-sm text-gray-400">발급 가능한 쿠폰이 없습니다.</p>
            ) : (
              <div className="flex flex-col gap-3">
                {available.map((coupon) => {
                  const owned = ownedIds.has(coupon.id);
                  return (
                    <div key={coupon.id} className="rounded-xl border border-gray-100 p-4">
                      <div className="flex items-start justify-between gap-4">
                        <div className="min-w-0">
                          <p className="text-sm font-semibold text-gray-900">{coupon.name}</p>
                          <p className="text-sm text-gray-900">
                            {formatDiscount(coupon.discountType, coupon.discountValue)}
                          </p>
                          <p className="text-xs text-gray-400 mt-1">
                            {coupon.minOrderAmount.toLocaleString()}원 이상 · {coupon.remainQuantity}개 남음
                            · ~{formatCouponDate(coupon.expiresAt)}
                          </p>
                        </div>
                        <button
                          onClick={() => handleIssue(coupon.id)}
                          disabled={owned || issuingId === coupon.id}
                          className="shrink-0 rounded-lg bg-gray-900 text-white text-sm font-medium px-4 py-2 hover:bg-gray-700 transition-colors disabled:opacity-40"
                        >
                          {owned ? "발급 완료" : issuingId === coupon.id ? "발급 중..." : "발급받기"}
                        </button>
                      </div>
                      {issueError?.id === coupon.id && (
                        <p className="text-sm text-red-500 mt-2">{issueError.message}</p>
                      )}
                    </div>
                  );
                })}
              </div>
            )}
          </section>

          <section>
            <h2 className="text-sm font-semibold text-gray-500 mb-3">내 쿠폰</h2>
            {mine.length === 0 ? (
              <p className="text-sm text-gray-400">보유한 쿠폰이 없습니다.</p>
            ) : (
              <div className="flex flex-col gap-3">
                {mine.map((coupon) => (
                  <div
                    key={coupon.couponId}
                    className="flex items-center justify-between gap-4 rounded-xl border border-gray-100 p-4"
                  >
                    <div className="min-w-0">
                      <p
                        className={`text-sm font-semibold ${
                          coupon.used ? "text-gray-400 line-through" : "text-gray-900"
                        }`}
                      >
                        {coupon.name}
                      </p>
                      <p className="text-xs text-gray-400 mt-1">
                        {formatDiscount(coupon.discountType, coupon.discountValue)} ·{" "}
                        {coupon.minOrderAmount.toLocaleString()}원 이상 · ~
                        {formatCouponDate(coupon.expiresAt)}
                      </p>
                    </div>
                    {coupon.used && (
                      <span className="shrink-0 text-xs text-gray-400">사용됨</span>
                    )}
                  </div>
                ))}
              </div>
            )}
          </section>
        </>
      )}
    </main>
  );
}
