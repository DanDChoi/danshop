"use client";

import { useEffect, useState, FormEvent } from "react";
import Link from "next/link";
import { useAuth } from "@/lib/auth-context";
import { useRequireAdmin } from "@/lib/use-require-admin";
import {
  getSalesStat,
  getTopProductsBySales,
  getOrderStatusStat,
  ApiError,
  type SalesStat,
  type ProductSales,
  type OrderStatusStat,
} from "@/lib/api";
import { ORDER_STATUS_LABELS } from "@/lib/order";

function toFromDateTime(date: string): string {
  return `${date}T00:00:00`;
}

function toToDateTime(date: string): string {
  return `${date}T23:59:59`;
}

export default function AdminStatsPage() {
  const isAdmin = useRequireAdmin();
  const { accessToken } = useAuth();

  const [fromDate, setFromDate] = useState("");
  const [toDate, setToDate] = useState("");
  const [range, setRange] = useState<{ from?: string; to?: string }>({});

  const [salesStat, setSalesStat] = useState<SalesStat | null>(null);
  const [salesLoading, setSalesLoading] = useState(true);
  const [salesError, setSalesError] = useState("");

  const [topProducts, setTopProducts] = useState<ProductSales[]>([]);
  const [topProductsLoading, setTopProductsLoading] = useState(true);
  const [topProductsError, setTopProductsError] = useState("");

  const [orderStatusStat, setOrderStatusStat] = useState<OrderStatusStat[]>([]);
  const [orderStatusLoading, setOrderStatusLoading] = useState(true);
  const [orderStatusError, setOrderStatusError] = useState("");

  useEffect(() => {
    if (!isAdmin || !accessToken) return;

    let cancelled = false;

    getSalesStat(accessToken, range)
      .then((data) => {
        if (!cancelled) setSalesStat(data);
      })
      .catch((err) => {
        if (!cancelled) {
          setSalesError(err instanceof ApiError ? err.message : "매출을 불러오지 못했습니다.");
        }
      })
      .finally(() => {
        if (!cancelled) setSalesLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [isAdmin, accessToken, range]);

  useEffect(() => {
    if (!isAdmin || !accessToken) return;

    let cancelled = false;

    getTopProductsBySales(accessToken, 5)
      .then((data) => {
        if (!cancelled) setTopProducts(data);
      })
      .catch((err) => {
        if (!cancelled) {
          setTopProductsError(
            err instanceof ApiError ? err.message : "인기 상품을 불러오지 못했습니다."
          );
        }
      })
      .finally(() => {
        if (!cancelled) setTopProductsLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [isAdmin, accessToken]);

  useEffect(() => {
    if (!isAdmin || !accessToken) return;

    let cancelled = false;

    getOrderStatusStat(accessToken)
      .then((data) => {
        if (!cancelled) setOrderStatusStat(data);
      })
      .catch((err) => {
        if (!cancelled) {
          setOrderStatusError(
            err instanceof ApiError ? err.message : "주문 상태 통계를 불러오지 못했습니다."
          );
        }
      })
      .finally(() => {
        if (!cancelled) setOrderStatusLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [isAdmin, accessToken]);

  const handleSearch = (e: FormEvent) => {
    e.preventDefault();
    setSalesLoading(true);
    setSalesError("");
    setRange({
      from: fromDate ? toFromDateTime(fromDate) : undefined,
      to: toDate ? toToDateTime(toDate) : undefined,
    });
  };

  if (!isAdmin) {
    return (
      <main className="max-w-3xl mx-auto px-4 md:px-6 py-16">
        <p className="text-sm text-gray-400">확인 중...</p>
      </main>
    );
  }

  const maxOrderStatusCount = Math.max(1, ...orderStatusStat.map((s) => s.count));

  return (
    <main className="max-w-3xl mx-auto px-4 md:px-6 py-12 md:py-16">
      <Link href="/admin" className="text-sm text-gray-400 hover:text-gray-700 transition-colors">
        ← 관리자
      </Link>

      <h1 className="text-3xl font-extrabold text-gray-900 tracking-tight mt-4 mb-6">통계</h1>

      <section className="mb-8">
        <h2 className="text-sm font-semibold text-gray-500 mb-3">매출</h2>

        <form onSubmit={handleSearch} className="flex items-center gap-2 mb-4">
          <input
            type="date"
            value={fromDate}
            onChange={(e) => setFromDate(e.target.value)}
            className="rounded-lg border border-gray-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-gray-900"
          />
          <span className="text-sm text-gray-400">~</span>
          <input
            type="date"
            value={toDate}
            onChange={(e) => setToDate(e.target.value)}
            className="rounded-lg border border-gray-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-gray-900"
          />
          <button
            type="submit"
            className="rounded-lg bg-gray-900 text-white text-sm font-medium px-4 py-2 hover:bg-gray-700 transition-colors"
          >
            조회
          </button>
        </form>

        {salesError && <p className="text-sm text-red-500">{salesError}</p>}

        {salesLoading ? (
          <p className="text-sm text-gray-400">불러오는 중...</p>
        ) : (
          salesStat && (
            <div className="flex gap-4">
              <div className="flex-1 rounded-xl border border-gray-100 p-4">
                <p className="text-xs text-gray-400 mb-1">총 매출</p>
                <p className="text-xl font-bold text-gray-900">
                  {salesStat.totalSales.toLocaleString()}원
                </p>
              </div>
              <div className="flex-1 rounded-xl border border-gray-100 p-4">
                <p className="text-xs text-gray-400 mb-1">주문 수</p>
                <p className="text-xl font-bold text-gray-900">
                  {salesStat.orderCount.toLocaleString()}건
                </p>
              </div>
            </div>
          )
        )}
      </section>

      <section className="mb-8">
        <h2 className="text-sm font-semibold text-gray-500 mb-3">인기 상품</h2>

        {topProductsError && <p className="text-sm text-red-500">{topProductsError}</p>}

        {topProductsLoading ? (
          <p className="text-sm text-gray-400">불러오는 중...</p>
        ) : topProducts.length === 0 ? (
          <p className="text-sm text-gray-400">판매 데이터가 없습니다.</p>
        ) : (
          <div className="flex flex-col gap-2">
            {topProducts.map((product, index) => (
              <div
                key={product.productId}
                className="flex items-center justify-between gap-4 rounded-xl border border-gray-100 p-3"
              >
                <div className="flex items-center gap-3 min-w-0">
                  <span className="text-sm font-semibold text-gray-400 w-5">{index + 1}</span>
                  <p className="text-sm text-gray-900 truncate">{product.productName}</p>
                </div>
                <div className="shrink-0 text-right">
                  <p className="text-sm font-medium text-gray-900">
                    {product.totalRevenue.toLocaleString()}원
                  </p>
                  <p className="text-xs text-gray-400">{product.totalQuantity}개 판매</p>
                </div>
              </div>
            ))}
          </div>
        )}
      </section>

      <section>
        <h2 className="text-sm font-semibold text-gray-500 mb-3">주문 상태</h2>

        {orderStatusError && <p className="text-sm text-red-500">{orderStatusError}</p>}

        {orderStatusLoading ? (
          <p className="text-sm text-gray-400">불러오는 중...</p>
        ) : orderStatusStat.length === 0 ? (
          <p className="text-sm text-gray-400">주문 데이터가 없습니다.</p>
        ) : (
          <div className="flex flex-col gap-2">
            {orderStatusStat.map((stat) => (
              <div key={stat.status} className="flex items-center gap-3">
                <span className="w-20 shrink-0 text-sm text-gray-500">
                  {ORDER_STATUS_LABELS[stat.status]}
                </span>
                <div className="flex-1 h-2 rounded-full bg-gray-100 overflow-hidden">
                  <div
                    className="h-full bg-gray-900"
                    style={{ width: `${(stat.count / maxOrderStatusCount) * 100}%` }}
                  />
                </div>
                <span className="w-10 shrink-0 text-sm text-right text-gray-900">
                  {stat.count}
                </span>
              </div>
            ))}
          </div>
        )}
      </section>
    </main>
  );
}
