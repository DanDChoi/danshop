"use client";

import { useEffect, useState, FormEvent } from "react";
import Link from "next/link";
import { useAuth } from "@/lib/auth-context";
import { useRequireAdmin } from "@/lib/use-require-admin";
import { getSalesStat, ApiError, type SalesStat } from "@/lib/api";

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
        <p className="text-sm text-gray-400">준비 중</p>
      </section>

      <section>
        <h2 className="text-sm font-semibold text-gray-500 mb-3">주문 상태</h2>
        <p className="text-sm text-gray-400">준비 중</p>
      </section>
    </main>
  );
}
