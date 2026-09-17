"use client";

import { useEffect, useState, FormEvent } from "react";
import Link from "next/link";
import { useRequireAdmin } from "@/lib/use-require-admin";
import { useAuth } from "@/lib/auth-context";
import {
  getAllOrdersAdmin,
  updateOrderStatusAdmin,
  ApiError,
  type OrderStatus,
  type OrderSummary,
} from "@/lib/api";
import { ORDER_STATUS_LABELS } from "@/lib/order";

const PAGE_SIZE = 20;

const STATUS_OPTIONS = Object.entries(ORDER_STATUS_LABELS) as [OrderStatus, string][];

export default function AdminOrdersPage() {
  const isAdmin = useRequireAdmin();
  const { accessToken } = useAuth();

  const [orders, setOrders] = useState<OrderSummary[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [page, setPage] = useState(0);
  const [statusFilter, setStatusFilter] = useState<OrderStatus | "">("");
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");
  const [refreshKey, setRefreshKey] = useState(0);

  const [updatingId, setUpdatingId] = useState<number | null>(null);
  const [statusError, setStatusError] = useState("");

  useEffect(() => {
    if (!isAdmin || !accessToken) return;

    let cancelled = false;

    getAllOrdersAdmin(accessToken, {
      page,
      size: PAGE_SIZE,
      status: statusFilter || undefined,
    })
      .then((data) => {
        if (cancelled) return;
        setOrders(data.content);
        setTotalPages(data.totalPages);
      })
      .catch((err) => {
        if (cancelled) return;
        setError(err instanceof ApiError ? err.message : "주문을 불러오지 못했습니다.");
      })
      .finally(() => {
        if (!cancelled) setIsLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [isAdmin, accessToken, page, statusFilter, refreshKey]);

  const handleStatusChange = async (e: FormEvent<HTMLFormElement>, orderId: number) => {
    e.preventDefault();
    if (!accessToken) return;

    const formData = new FormData(e.currentTarget);
    const status = String(formData.get("status")) as OrderStatus;

    setStatusError("");
    setUpdatingId(orderId);
    try {
      await updateOrderStatusAdmin(accessToken, orderId, status);
      setRefreshKey((k) => k + 1);
    } catch (err) {
      setStatusError(err instanceof ApiError ? err.message : "상태 변경에 실패했습니다.");
    } finally {
      setUpdatingId(null);
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
        <h1 className="text-3xl font-extrabold text-gray-900 tracking-tight">주문 관리</h1>
        <select
          value={statusFilter}
          onChange={(e) => {
            setStatusFilter(e.target.value as OrderStatus | "");
            setPage(0);
          }}
          className="rounded-lg border border-gray-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-gray-900"
        >
          <option value="">전체 상태</option>
          {STATUS_OPTIONS.map(([value, label]) => (
            <option key={value} value={value}>
              {label}
            </option>
          ))}
        </select>
      </div>

      {error && <p className="text-sm text-red-500 mb-4">{error}</p>}
      {statusError && <p className="text-sm text-red-500 mb-4">{statusError}</p>}

      {isLoading ? (
        <p className="text-sm text-gray-400">불러오는 중...</p>
      ) : orders.length === 0 ? (
        <p className="text-sm text-gray-400">주문이 없습니다.</p>
      ) : (
        <div className="flex flex-col gap-3">
          {orders.map((order) => (
            <div
              key={order.orderId}
              className="flex items-center justify-between gap-4 rounded-xl border border-gray-100 p-4"
            >
              <div className="min-w-0">
                <p className="text-sm font-semibold text-gray-900">
                  #{order.orderId} · {order.userId ?? "비회원"}
                </p>
                <p className="text-sm text-gray-500 truncate">
                  {order.baseAddr} {order.detailAddr}
                </p>
                <p className="text-sm text-gray-500 mt-0.5">
                  {order.payAmount.toLocaleString()}원 · {ORDER_STATUS_LABELS[order.status]}
                </p>
              </div>

              <form
                onSubmit={(e) => handleStatusChange(e, order.orderId)}
                className="shrink-0 flex items-center gap-2"
              >
                <select
                  name="status"
                  defaultValue={order.status}
                  className="rounded-lg border border-gray-200 px-2 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-gray-900"
                >
                  {STATUS_OPTIONS.map(([value, label]) => (
                    <option key={value} value={value}>
                      {label}
                    </option>
                  ))}
                </select>
                <button
                  type="submit"
                  disabled={updatingId === order.orderId}
                  className="rounded-lg bg-gray-900 text-white text-sm font-medium px-3 py-1.5 hover:bg-gray-700 transition-colors disabled:opacity-50"
                >
                  {updatingId === order.orderId ? "변경 중..." : "변경"}
                </button>
              </form>
            </div>
          ))}
        </div>
      )}

      {totalPages > 1 && (
        <div className="flex items-center justify-center gap-2 mt-8">
          <button
            onClick={() => setPage((p) => p - 1)}
            disabled={page <= 0}
            className="text-sm px-3 py-1.5 rounded-lg border border-gray-200 disabled:opacity-40 hover:border-gray-400 transition-colors"
          >
            이전
          </button>
          <span className="text-sm text-gray-500">
            {page + 1} / {totalPages}
          </span>
          <button
            onClick={() => setPage((p) => p + 1)}
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
