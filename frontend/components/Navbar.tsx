"use client";

import Link from "next/link";
import { useAuth } from "@/lib/auth-context";

export default function Navbar() {
  const { userId, logout } = useAuth();

  return (
    <header className="border-b border-gray-100 bg-white">
      <div className="max-w-4xl mx-auto px-4 md:px-6 h-16 flex items-center justify-between">
        <Link href="/" className="text-lg font-extrabold tracking-tight text-gray-900">
          Danshop
        </Link>

        <nav className="flex items-center gap-4">
          <Link
            href="/products"
            className="text-sm font-medium text-gray-500 hover:text-gray-800 transition-colors"
          >
            상품
          </Link>
          <Link
            href="/cart"
            className="text-sm font-medium text-gray-500 hover:text-gray-800 transition-colors"
          >
            장바구니
          </Link>
          {userId ? (
            <>
              <span className="text-sm text-gray-500">{userId}님</span>
              <button
                onClick={logout}
                className="text-sm font-medium text-gray-500 hover:text-gray-800 transition-colors"
              >
                로그아웃
              </button>
            </>
          ) : (
            <>
              <Link
                href="/login"
                className="text-sm font-medium text-gray-500 hover:text-gray-800 transition-colors"
              >
                로그인
              </Link>
              <Link
                href="/signup"
                className="text-sm font-medium bg-gray-900 text-white rounded-lg px-3 py-1.5 hover:bg-gray-700 transition-colors"
              >
                회원가입
              </Link>
            </>
          )}
        </nav>
      </div>
    </header>
  );
}
