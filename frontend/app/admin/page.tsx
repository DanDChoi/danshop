"use client";

import Link from "next/link";
import { useRequireAdmin } from "@/lib/use-require-admin";

export default function AdminPage() {
  const isAdmin = useRequireAdmin();

  if (!isAdmin) {
    return (
      <main className="max-w-2xl mx-auto px-4 md:px-6 py-16">
        <p className="text-sm text-gray-400">확인 중...</p>
      </main>
    );
  }

  return (
    <main className="max-w-2xl mx-auto px-4 md:px-6 py-12 md:py-16">
      <h1 className="text-3xl font-extrabold text-gray-900 tracking-tight mb-6">관리자</h1>

      <div className="flex flex-col gap-3">
        <Link
          href="/admin/products"
          className="rounded-xl border border-gray-100 p-4 hover:border-gray-300 transition-colors"
        >
          <p className="text-sm font-semibold text-gray-900 mb-1">상품 관리</p>
          <p className="text-sm text-gray-500">상품 등록·수정·삭제</p>
        </Link>
        <Link
          href="/admin/orders"
          className="rounded-xl border border-gray-100 p-4 hover:border-gray-300 transition-colors"
        >
          <p className="text-sm font-semibold text-gray-900 mb-1">주문 관리</p>
          <p className="text-sm text-gray-500">전체 주문 조회 · 상태 변경</p>
        </Link>
      </div>
    </main>
  );
}
