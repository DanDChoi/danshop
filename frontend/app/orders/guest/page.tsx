"use client";

import { useState, FormEvent } from "react";
import Link from "next/link";
import { lookupGuestOrder, ApiError, type OrderDetail } from "@/lib/api";
import { ORDER_STATUS_LABELS } from "@/lib/order";

export default function GuestOrderLookupPage() {
  const [orderId, setOrderId] = useState("");
  const [email, setEmail] = useState("");
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [order, setOrder] = useState<OrderDetail | null>(null);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError("");
    setIsLoading(true);
    try {
      const data = await lookupGuestOrder(Number(orderId), email);
      setOrder(data);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "주문을 조회하지 못했습니다.");
    } finally {
      setIsLoading(false);
    }
  };

  if (order) {
    return (
      <main className="max-w-2xl mx-auto px-4 md:px-6 py-12 md:py-16">
        <button
          onClick={() => setOrder(null)}
          className="text-sm text-gray-400 hover:text-gray-700 transition-colors"
        >
          ← 다시 조회
        </button>

        <div className="flex items-baseline justify-between mt-4 mb-6">
          <h1 className="text-3xl font-extrabold text-gray-900 tracking-tight">
            주문번호 {order.orderId}
          </h1>
          <span className="text-sm text-gray-500">{ORDER_STATUS_LABELS[order.status]}</span>
        </div>

        <div className="flex flex-col gap-3 mb-6">
          {order.items.map((item) => (
            <div
              key={item.productId}
              className="flex items-center justify-between gap-4 rounded-xl border border-gray-100 p-4"
            >
              <div className="min-w-0">
                <p className="text-sm font-semibold text-gray-900 truncate">{item.productName}</p>
                <p className="text-sm text-gray-500">
                  {item.price.toLocaleString()}원 × {item.quantity}
                </p>
              </div>
              <p className="shrink-0 text-sm font-medium text-gray-900">
                {item.totalPrice.toLocaleString()}원
              </p>
            </div>
          ))}
        </div>

        <div className="border-t border-gray-100 pt-4 space-y-2">
          <div className="flex justify-between text-sm">
            <span className="text-gray-500">배송지</span>
            <span className="text-gray-900 text-right">
              ({order.postNo}) {order.baseAddr} {order.detailAddr}
            </span>
          </div>
          <div className="flex justify-between">
            <span className="text-sm text-gray-500">결제 금액</span>
            <span className="text-lg font-bold text-gray-900">
              {order.payAmount.toLocaleString()}원
            </span>
          </div>
        </div>
      </main>
    );
  }

  return (
    <main className="max-w-sm mx-auto px-4 py-16">
      <h1 className="text-2xl font-bold text-gray-900 mb-2 text-center">비회원 주문 조회</h1>
      <p className="text-sm text-gray-400 mb-8 text-center">
        주문 시 입력한 주문번호와 이메일을 입력해주세요.
      </p>

      <form onSubmit={handleSubmit} className="flex flex-col gap-4">
        <div>
          <label htmlFor="orderId" className="block text-sm font-medium text-gray-700 mb-1">
            주문번호
          </label>
          <input
            id="orderId"
            type="number"
            required
            value={orderId}
            onChange={(e) => setOrderId(e.target.value)}
            className="w-full rounded-lg border border-gray-200 px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-gray-900"
          />
        </div>

        <div>
          <label htmlFor="email" className="block text-sm font-medium text-gray-700 mb-1">
            이메일
          </label>
          <input
            id="email"
            type="email"
            required
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className="w-full rounded-lg border border-gray-200 px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-gray-900"
          />
        </div>

        {error && <p className="text-sm text-red-500">{error}</p>}

        <button
          type="submit"
          disabled={isLoading}
          className="mt-2 w-full rounded-lg bg-gray-900 text-white text-sm font-medium py-2.5 hover:bg-gray-700 transition-colors disabled:opacity-50"
        >
          {isLoading ? "조회 중..." : "주문 조회"}
        </button>
      </form>

      <p className="mt-6 text-center text-sm text-gray-500">
        회원이신가요?{" "}
        <Link href="/login" className="font-medium text-gray-900 hover:underline">
          로그인
        </Link>
      </p>
    </main>
  );
}
