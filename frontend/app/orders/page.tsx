"use client";

import { Suspense, useEffect, useState } from "react";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { getOrders, ApiError, type OrderStatus, type OrderSummary } from "@/lib/api";
import { useAuth } from "@/lib/auth-context";

const PAGE_SIZE = 10;

const STATUS_LABELS: Record<OrderStatus, string> = {
  PENDING: "결제 대기",
  PAID: "결제 완료",
  SHIPPED: "배송 중",
  DELIVERED: "배송 완료",
  CANCELLED: "취소됨",
};

export default function OrdersPage() {
  return (
    <Suspense fallback={null}>
      <OrdersShell />
    </Suspense>
  );
}

function OrdersShell() {
  const searchParams = useSearchParams();
  const page = Number(searchParams.get("page") ?? "0");

  return <OrdersList key={page} page={page} />;
}

function OrdersList({ page }: { page: number }) {
  const router = useRouter();
  const { accessToken } = useAuth();

  const [orders, setOrders] = useState<OrderSummary[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!accessToken) {
      router.replace("/login");
      return;
    }

    let cancelled = false;

    getOrders(accessToken, page, PAGE_SIZE)
      .then((data) => {
        if (cancelled) return;
        setOrders(data.content);
        setTotalPages(data.totalPages);
      })
      .catch((err) => {
        if (cancelled) return;
        setError(err instanceof ApiError ? err.message : "주문 내역을 불러오지 못했습니다.");
      })
      .finally(() => {
        if (!cancelled) setIsLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [accessToken, page, router]);

  if (!accessToken) {
    return (
      <main className="max-w-2xl mx-auto px-4 md:px-6 py-16">
        <p className="text-sm text-gray-400">로그인이 필요합니다. 로그인 페이지로 이동합니다...</p>
      </main>
    );
  }

  return (
    <main className="max-w-2xl mx-auto px-4 md:px-6 py-12 md:py-16">
      <h1 className="text-3xl font-extrabold text-gray-900 tracking-tight mb-6">주문 내역</h1>

      {error && <p className="text-sm text-red-500 mb-4">{error}</p>}

      {isLoading ? (
        <p className="text-sm text-gray-400">불러오는 중...</p>
      ) : orders.length === 0 ? (
        <div className="text-center py-16">
          <p className="text-sm text-gray-400 mb-4">주문 내역이 없습니다.</p>
          <Link href="/products" className="text-sm font-medium text-gray-900 hover:underline">
            상품 보러가기
          </Link>
        </div>
      ) : (
        <div className="flex flex-col gap-3">
          {orders.map((order) => (
            <div
              key={order.orderId}
              className="flex items-center justify-between gap-4 rounded-xl border border-gray-100 p-4"
            >
              <div className="min-w-0">
                <p className="text-sm font-semibold text-gray-900">주문번호 {order.orderId}</p>
                <p className="text-sm text-gray-500 truncate">
                  {order.baseAddr} {order.detailAddr}
                </p>
              </div>
              <div className="shrink-0 text-right">
                <p className="text-sm font-medium text-gray-900">
                  {order.payAmount.toLocaleString()}원
                </p>
                <span className="text-xs text-gray-400">{STATUS_LABELS[order.status]}</span>
              </div>
            </div>
          ))}
        </div>
      )}

      {totalPages > 1 && (
        <div className="flex items-center justify-center gap-2 mt-8">
          <button
            onClick={() => router.push(`/orders?page=${page - 1}`)}
            disabled={page <= 0}
            className="text-sm px-3 py-1.5 rounded-lg border border-gray-200 disabled:opacity-40 hover:border-gray-400 transition-colors"
          >
            이전
          </button>
          <span className="text-sm text-gray-500">
            {page + 1} / {totalPages}
          </span>
          <button
            onClick={() => router.push(`/orders?page=${page + 1}`)}
            disabled={page >= totalPages - 1}
            className="text-sm px-3 py-1.5 rounded-lg border border-gray-200 disabled:opacity-40 hover:border-gray-400 transition-colors"
          >
            다음
          </button>
        </div>
      )}
    </main>
  );
}
