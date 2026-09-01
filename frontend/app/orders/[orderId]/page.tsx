"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { useParams, useRouter } from "next/navigation";
import { getOrder, ApiError, type OrderDetail } from "@/lib/api";
import { ORDER_STATUS_LABELS } from "@/lib/order";
import { useAuth } from "@/lib/auth-context";

export default function OrderDetailPage() {
  const params = useParams<{ orderId: string }>();
  const orderId = Number(params.orderId);

  return <OrderDetailView key={orderId} orderId={orderId} />;
}

function OrderDetailView({ orderId }: { orderId: number }) {
  const router = useRouter();
  const { accessToken } = useAuth();

  const [order, setOrder] = useState<OrderDetail | null>(null);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!accessToken) {
      router.replace("/login");
      return;
    }

    let cancelled = false;

    getOrder(accessToken, orderId)
      .then((data) => {
        if (cancelled) return;
        setOrder(data);
      })
      .catch((err) => {
        if (cancelled) return;
        setError(err instanceof ApiError ? err.message : "주문을 불러오지 못했습니다.");
      });

    return () => {
      cancelled = true;
    };
  }, [accessToken, orderId, router]);

  if (!accessToken) {
    return (
      <main className="max-w-2xl mx-auto px-4 md:px-6 py-16">
        <p className="text-sm text-gray-400">로그인이 필요합니다. 로그인 페이지로 이동합니다...</p>
      </main>
    );
  }

  if (error) {
    return (
      <main className="max-w-2xl mx-auto px-4 md:px-6 py-16">
        <p className="text-sm text-red-500 mb-4">{error}</p>
        <Link href="/orders" className="text-sm font-medium text-gray-900 hover:underline">
          주문 내역으로
        </Link>
      </main>
    );
  }

  if (!order) {
    return (
      <main className="max-w-2xl mx-auto px-4 md:px-6 py-16">
        <p className="text-sm text-gray-400">불러오는 중...</p>
      </main>
    );
  }

  return (
    <main className="max-w-2xl mx-auto px-4 md:px-6 py-12 md:py-16">
      <Link href="/orders" className="text-sm text-gray-400 hover:text-gray-700 transition-colors">
        ← 주문 내역
      </Link>

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

      <div className="border-t border-gray-100 pt-4 mb-8 space-y-2">
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
