"use client";

import Link from "next/link";
import { useRequireAdmin } from "@/lib/use-require-admin";

export default function AdminStatsPage() {
  const isAdmin = useRequireAdmin();

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
        <p className="text-sm text-gray-400">준비 중</p>
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
