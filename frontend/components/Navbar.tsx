"use client";

import Link from "next/link";
import { useAuth } from "@/lib/auth-context";
import { useCartCount } from "@/lib/use-cart-count";

export default function Navbar() {
  const { userId, isAdmin, logout } = useAuth();
  const cartCount = useCartCount();

  return (
    <header className="border-b border-gray-100 bg-white">
      <div className="max-w-4xl mx-auto px-4 md:px-6 h-16 flex items-center gap-4">
        <Link
          href="/"
          className="shrink-0 text-lg font-extrabold tracking-tight text-gray-900"
        >
          Danshop
        </Link>

        {/* 로그인 상태(특히 관리자)일 때 항목이 많아져 좁은 화면에서 넘칠 수 있음 —
            페이지 전체가 아니라 nav 안에서만 가로 스크롤되게 한다. */}
        <nav className="min-w-0 flex items-center gap-4 overflow-x-auto">
          <Link
            href="/products"
            className="shrink-0 text-sm font-medium text-gray-500 hover:text-gray-800 transition-colors"
          >
            상품
          </Link>
          <Link
            href="/cart"
            className="relative shrink-0 text-sm font-medium text-gray-500 hover:text-gray-800 transition-colors"
          >
            장바구니
            {cartCount > 0 && (
              <span className="absolute -top-2 -right-3 flex h-4 min-w-4 items-center justify-center rounded-full bg-gray-900 px-1 text-[10px] font-semibold text-white">
                {cartCount > 99 ? "99+" : cartCount}
              </span>
            )}
          </Link>
          {userId ? (
            <>
              <Link
                href="/orders"
                className="shrink-0 text-sm font-medium text-gray-500 hover:text-gray-800 transition-colors"
              >
                주문내역
              </Link>
              <Link
                href="/coupons"
                className="shrink-0 text-sm font-medium text-gray-500 hover:text-gray-800 transition-colors"
              >
                쿠폰함
              </Link>
              <Link
                href="/wishlist"
                className="shrink-0 text-sm font-medium text-gray-500 hover:text-gray-800 transition-colors"
              >
                위시리스트
              </Link>
              {isAdmin && (
                <Link
                  href="/admin"
                  className="shrink-0 text-sm font-medium text-gray-500 hover:text-gray-800 transition-colors"
                >
                  관리자
                </Link>
              )}
              <Link
                href="/mypage"
                className="shrink-0 whitespace-nowrap text-sm text-gray-500 hover:text-gray-800 transition-colors"
              >
                {userId}님
              </Link>
              <button
                onClick={logout}
                className="shrink-0 text-sm font-medium text-gray-500 hover:text-gray-800 transition-colors"
              >
                로그아웃
              </button>
            </>
          ) : (
            <>
              <Link
                href="/login"
                className="shrink-0 text-sm font-medium text-gray-500 hover:text-gray-800 transition-colors"
              >
                로그인
              </Link>
              <Link
                href="/signup"
                className="shrink-0 whitespace-nowrap text-sm font-medium bg-gray-900 text-white rounded-lg px-3 py-1.5 hover:bg-gray-700 transition-colors"
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
