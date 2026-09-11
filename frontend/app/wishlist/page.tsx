"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import {
  getWishlist,
  removeFromWishlist,
  moveWishlistToCart,
  ApiError,
  type WishlistItem,
} from "@/lib/api";
import { useRequireAuth } from "@/lib/use-require-auth";

export default function WishlistPage() {
  const accessToken = useRequireAuth();

  const [items, setItems] = useState<WishlistItem[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");
  const [actionError, setActionError] = useState("");
  const [notice, setNotice] = useState("");
  const [busyId, setBusyId] = useState<number | null>(null);

  useEffect(() => {
    if (!accessToken) return;

    let cancelled = false;

    getWishlist(accessToken)
      .then((data) => {
        if (!cancelled) setItems(data);
      })
      .catch((err) => {
        if (!cancelled) {
          setError(err instanceof ApiError ? err.message : "위시리스트를 불러오지 못했습니다.");
        }
      })
      .finally(() => {
        if (!cancelled) setIsLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [accessToken]);

  const handleRemove = async (productId: number) => {
    if (!accessToken) return;
    setActionError("");
    setNotice("");
    setBusyId(productId);
    try {
      await removeFromWishlist(accessToken, productId);
      setItems((prev) => prev.filter((item) => item.productId !== productId));
    } catch (err) {
      setActionError(err instanceof ApiError ? err.message : "삭제에 실패했습니다.");
    } finally {
      setBusyId(null);
    }
  };

  const handleMoveToCart = async (productId: number) => {
    if (!accessToken) return;
    setActionError("");
    setNotice("");
    setBusyId(productId);
    try {
      await moveWishlistToCart(accessToken, productId);
      setItems((prev) => prev.filter((item) => item.productId !== productId));
      setNotice("장바구니에 담았습니다.");
    } catch (err) {
      setActionError(err instanceof ApiError ? err.message : "장바구니 이동에 실패했습니다.");
    } finally {
      setBusyId(null);
    }
  };

  if (!accessToken) {
    return (
      <main className="max-w-2xl mx-auto px-4 md:px-6 py-16">
        <p className="text-sm text-gray-400">로그인이 필요합니다. 로그인 페이지로 이동합니다...</p>
      </main>
    );
  }

  return (
    <main className="max-w-2xl mx-auto px-4 md:px-6 py-12 md:py-16">
      <h1 className="text-3xl font-extrabold text-gray-900 tracking-tight mb-6">위시리스트</h1>

      {error && <p className="text-sm text-red-500 mb-4">{error}</p>}
      {actionError && <p className="text-sm text-red-500 mb-4">{actionError}</p>}
      {notice && <p className="text-sm text-green-600 mb-4">{notice}</p>}

      {isLoading ? (
        <p className="text-sm text-gray-400">불러오는 중...</p>
      ) : items.length === 0 ? (
        <div className="text-center py-16">
          <p className="text-sm text-gray-400 mb-4">위시리스트가 비어있습니다.</p>
          <Link href="/products" className="text-sm font-medium text-gray-900 hover:underline">
            상품 보러가기
          </Link>
        </div>
      ) : (
        <div className="flex flex-col gap-3">
          {items.map((item) => (
            <div
              key={item.productId}
              className="flex items-center justify-between gap-4 rounded-xl border border-gray-100 p-4"
            >
              <Link href={`/products/${item.productId}`} className="min-w-0">
                <p className="text-sm font-semibold text-gray-900 truncate">
                  {item.productName}
                </p>
                <p className="text-sm text-gray-500">{item.price.toLocaleString()}원</p>
                {item.avgRating != null && (
                  <p className="text-xs text-gray-400 mt-0.5">★ {item.avgRating.toFixed(1)}</p>
                )}
              </Link>

              <div className="flex shrink-0 gap-2">
                <button
                  onClick={() => handleMoveToCart(item.productId)}
                  disabled={busyId === item.productId}
                  className="rounded-lg bg-gray-900 text-white text-sm font-medium px-3 py-2 hover:bg-gray-700 transition-colors disabled:opacity-50"
                >
                  담기
                </button>
                <button
                  onClick={() => handleRemove(item.productId)}
                  disabled={busyId === item.productId}
                  className="text-sm text-gray-400 hover:text-red-500 transition-colors disabled:opacity-50"
                >
                  삭제
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </main>
  );
}
