"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { useAuth } from "@/lib/auth-context";
import { getOrCreateGuestToken } from "@/lib/guest";
import {
  getCart,
  updateCartQuantity,
  removeFromCart,
  clearCart,
  ApiError,
  type Cart,
  type Identity,
} from "@/lib/api";

function resolveIdentity(accessToken: string | null): Identity {
  return accessToken ? { token: accessToken } : { guestToken: getOrCreateGuestToken() };
}

export default function CartPage() {
  const { accessToken } = useAuth();

  const [cart, setCart] = useState<Cart | null>(null);
  const [error, setError] = useState("");
  const [actionError, setActionError] = useState("");

  useEffect(() => {
    let cancelled = false;

    getCart(resolveIdentity(accessToken))
      .then((data) => {
        if (cancelled) return;
        setCart(data);
      })
      .catch((err) => {
        if (cancelled) return;
        setError(err instanceof ApiError ? err.message : "장바구니를 불러오지 못했습니다.");
      });

    return () => {
      cancelled = true;
    };
  }, [accessToken]);

  const refreshCart = () => {
    getCart(resolveIdentity(accessToken))
      .then(setCart)
      .catch((err) => {
        setActionError(err instanceof ApiError ? err.message : "장바구니를 새로고침하지 못했습니다.");
      });
  };

  const handleQuantityChange = async (productId: number, quantity: number) => {
    setActionError("");
    try {
      await updateCartQuantity(resolveIdentity(accessToken), productId, quantity);
      refreshCart();
    } catch (err) {
      setActionError(err instanceof ApiError ? err.message : "수량 변경에 실패했습니다.");
    }
  };

  const handleRemove = async (productId: number) => {
    setActionError("");
    try {
      await removeFromCart(resolveIdentity(accessToken), productId);
      refreshCart();
    } catch (err) {
      setActionError(err instanceof ApiError ? err.message : "삭제에 실패했습니다.");
    }
  };

  const handleClear = async () => {
    setActionError("");
    try {
      await clearCart(resolveIdentity(accessToken));
      refreshCart();
    } catch (err) {
      setActionError(err instanceof ApiError ? err.message : "비우기에 실패했습니다.");
    }
  };

  if (error) {
    return (
      <main className="max-w-2xl mx-auto px-4 md:px-6 py-16">
        <p className="text-sm text-red-500">{error}</p>
      </main>
    );
  }

  if (!cart) {
    return (
      <main className="max-w-2xl mx-auto px-4 md:px-6 py-16">
        <p className="text-sm text-gray-400">불러오는 중...</p>
      </main>
    );
  }

  return (
    <main className="max-w-2xl mx-auto px-4 md:px-6 py-12 md:py-16">
      <h1 className="text-3xl font-extrabold text-gray-900 tracking-tight mb-6">장바구니</h1>

      {actionError && <p className="text-sm text-red-500 mb-4">{actionError}</p>}

      {cart.items.length === 0 ? (
        <div className="text-center py-16">
          <p className="text-sm text-gray-400 mb-4">장바구니가 비어있습니다.</p>
          <Link
            href="/products"
            className="text-sm font-medium text-gray-900 hover:underline"
          >
            상품 보러가기
          </Link>
        </div>
      ) : (
        <>
          <div className="flex flex-col gap-3 mb-6">
            {cart.items.map((item) => (
              <div
                key={item.productId}
                className="flex items-center justify-between gap-4 rounded-xl border border-gray-100 p-4"
              >
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-semibold text-gray-900 truncate">
                    {item.productName}
                  </p>
                  <p className="text-sm text-gray-500">{item.price.toLocaleString()}원</p>
                </div>

                <input
                  type="number"
                  min={1}
                  value={item.quantity}
                  onChange={(e) =>
                    handleQuantityChange(item.productId, Math.max(1, Number(e.target.value) || 1))
                  }
                  className="w-16 rounded-lg border border-gray-200 px-2 py-1.5 text-sm text-center focus:outline-none focus:ring-2 focus:ring-gray-900"
                />

                <p className="w-24 text-right text-sm font-medium text-gray-900">
                  {item.totalPrice.toLocaleString()}원
                </p>

                <button
                  onClick={() => handleRemove(item.productId)}
                  className="text-sm text-gray-400 hover:text-red-500 transition-colors"
                >
                  삭제
                </button>
              </div>
            ))}
          </div>

          <div className="flex items-center justify-between border-t border-gray-100 pt-4 mb-8">
            <button
              onClick={handleClear}
              className="text-sm text-gray-400 hover:text-gray-700 transition-colors"
            >
              전체 비우기
            </button>
            <p className="text-lg font-bold text-gray-900">
              총 {cart.totalAmount.toLocaleString()}원
            </p>
          </div>

          <Link
            href="/cart/checkout"
            className="block text-center rounded-lg bg-gray-900 text-white text-sm font-medium py-3 hover:bg-gray-700 transition-colors"
          >
            주문하기
          </Link>
        </>
      )}
    </main>
  );
}
