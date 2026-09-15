"use client";

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
      <h1 className="text-3xl font-extrabold text-gray-900 tracking-tight mb-2">관리자</h1>
      <p className="text-sm text-gray-500">상품 관리, 주문 관리 기능이 여기에 추가될 예정입니다.</p>
    </main>
  );
}
