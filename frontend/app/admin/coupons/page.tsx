"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { useAuth } from "@/lib/auth-context";
import { useRequireAdmin } from "@/lib/use-require-admin";
import {
  getAvailableCoupons,
  createCoupon,
  ApiError,
  type Coupon,
  type DiscountType,
} from "@/lib/api";
import { formatDiscount } from "@/lib/coupon";
import { formatDate } from "@/lib/format";
import { Loading, ErrorMessage } from "@/components/PageState";

export default function AdminCouponsPage() {
  const isAdmin = useRequireAdmin();
  const { accessToken } = useAuth();

  const [coupons, setCoupons] = useState<Coupon[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");

  // 쿠폰 생성 폼 (UI만, 제출 연결은 다음 단계)
  const [showCreate, setShowCreate] = useState(false);
  const [name, setName] = useState("");
  const [discountType, setDiscountType] = useState<DiscountType>("RATE");
  const [discountValue, setDiscountValue] = useState("");
  const [minOrderAmount, setMinOrderAmount] = useState("");
  const [totalQuantity, setTotalQuantity] = useState("");
  const [expiresAt, setExpiresAt] = useState("");
  const [isCreating, setIsCreating] = useState(false);
  const [createError, setCreateError] = useState("");

  useEffect(() => {
    if (!isAdmin || !accessToken) return;

    let cancelled = false;

    getAvailableCoupons(accessToken)
      .then((data) => {
        if (!cancelled) setCoupons(data);
      })
      .catch((err) => {
        if (!cancelled) {
          setError(err instanceof ApiError ? err.message : "쿠폰을 불러오지 못했습니다.");
        }
      })
      .finally(() => {
        if (!cancelled) setIsLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [isAdmin, accessToken]);

  const handleCreate = async () => {
    if (!accessToken) return;
    setCreateError("");
    setIsCreating(true);
    try {
      await createCoupon(accessToken, {
        name,
        discountType,
        discountValue: Number(discountValue),
        minOrderAmount: Number(minOrderAmount),
        totalQuantity: Number(totalQuantity),
        expiresAt: expiresAt.length === 16 ? `${expiresAt}:00` : expiresAt,
      });
      setName("");
      setDiscountValue("");
      setMinOrderAmount("");
      setTotalQuantity("");
      setExpiresAt("");
      setShowCreate(false);
      const refreshed = await getAvailableCoupons(accessToken);
      setCoupons(refreshed);
    } catch (err) {
      setCreateError(err instanceof ApiError ? err.message : "쿠폰 생성에 실패했습니다.");
    } finally {
      setIsCreating(false);
    }
  };

  if (!isAdmin) {
    return (
      <main className="max-w-3xl mx-auto px-4 md:px-6 py-16">
        <p className="text-sm text-gray-400">확인 중...</p>
      </main>
    );
  }

  return (
    <main className="max-w-3xl mx-auto px-4 md:px-6 py-12 md:py-16">
      <Link href="/admin" className="text-sm text-gray-400 hover:text-gray-700 transition-colors">
        ← 관리자
      </Link>

      <div className="flex items-baseline justify-between mt-4 mb-6">
        <h1 className="text-3xl font-extrabold text-gray-900 tracking-tight">쿠폰 관리</h1>
        <button
          onClick={() => setShowCreate((v) => !v)}
          className="rounded-lg bg-gray-900 text-white text-sm font-medium px-4 py-2 hover:bg-gray-700 transition-colors"
        >
          {showCreate ? "취소" : "쿠폰 생성"}
        </button>
      </div>

      {showCreate && (
        <div className="rounded-xl border border-gray-100 p-4 mb-6 flex flex-col gap-3">
          <input
            type="text"
            placeholder="쿠폰 이름"
            value={name}
            onChange={(e) => setName(e.target.value)}
            className="w-full rounded-lg border border-gray-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-gray-900"
          />

          <div className="grid grid-cols-2 gap-2">
            <select
              value={discountType}
              onChange={(e) => setDiscountType(e.target.value as DiscountType)}
              className="rounded-lg border border-gray-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-gray-900"
            >
              <option value="RATE">정률(%)</option>
              <option value="AMOUNT">정액(원)</option>
            </select>
            <input
              type="number"
              placeholder="할인 값"
              value={discountValue}
              onChange={(e) => setDiscountValue(e.target.value)}
              className="rounded-lg border border-gray-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-gray-900"
            />
          </div>

          <div className="grid grid-cols-2 gap-2">
            <input
              type="number"
              placeholder="최소 주문금액"
              value={minOrderAmount}
              onChange={(e) => setMinOrderAmount(e.target.value)}
              className="rounded-lg border border-gray-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-gray-900"
            />
            <input
              type="number"
              placeholder="발급 수량"
              value={totalQuantity}
              onChange={(e) => setTotalQuantity(e.target.value)}
              className="rounded-lg border border-gray-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-gray-900"
            />
          </div>

          <div>
            <label htmlFor="expiresAt" className="block text-xs text-gray-400 mb-1">
              만료일시
            </label>
            <input
              id="expiresAt"
              type="datetime-local"
              value={expiresAt}
              onChange={(e) => setExpiresAt(e.target.value)}
              className="w-full rounded-lg border border-gray-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-gray-900"
            />
          </div>

          {createError && <p className="text-sm text-red-500">{createError}</p>}

          <button
            type="button"
            onClick={handleCreate}
            disabled={
              isCreating ||
              !name.trim() ||
              !discountValue ||
              !minOrderAmount ||
              !totalQuantity ||
              !expiresAt
            }
            className="self-start rounded-lg bg-gray-900 text-white text-sm font-medium px-4 py-2 hover:bg-gray-700 transition-colors disabled:opacity-50"
          >
            {isCreating ? "생성 중..." : "생성"}
          </button>
        </div>
      )}

      <ErrorMessage message={error} className="mb-4" />

      {isLoading ? (
        <Loading />
      ) : coupons.length === 0 ? (
        <p className="text-sm text-gray-400">발급 가능한 쿠폰이 없습니다.</p>
      ) : (
        <div className="flex flex-col gap-3">
          {coupons.map((coupon) => (
            <div key={coupon.id} className="rounded-xl border border-gray-100 p-4">
              <p className="text-sm font-semibold text-gray-900">{coupon.name}</p>
              <p className="text-sm text-gray-900">
                {formatDiscount(coupon.discountType, coupon.discountValue)}
              </p>
              <p className="text-xs text-gray-400 mt-1">
                {coupon.minOrderAmount.toLocaleString()}원 이상 · {coupon.remainQuantity}개 남음 ·
                ~{formatDate(coupon.expiresAt)}
              </p>
            </div>
          ))}
        </div>
      )}
    </main>
  );
}
