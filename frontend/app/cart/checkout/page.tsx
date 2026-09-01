"use client";

import { useEffect, useState, FormEvent } from "react";
import Link from "next/link";
import { useAuth } from "@/lib/auth-context";
import { getOrCreateGuestToken } from "@/lib/guest";
import { getCart, checkoutCart, ApiError, type Cart, type Identity } from "@/lib/api";

function resolveIdentity(accessToken: string | null): Identity {
  return accessToken ? { token: accessToken } : { guestToken: getOrCreateGuestToken() };
}

export default function CheckoutPage() {
  const { accessToken } = useAuth();
  const isGuest = !accessToken;

  const [postNo, setPostNo] = useState("");
  const [baseAddr, setBaseAddr] = useState("");
  const [detailAddr, setDetailAddr] = useState("");
  const [ordererName, setOrdererName] = useState("");
  const [ordererEmail, setOrdererEmail] = useState("");
  const [ordererPhone, setOrdererPhone] = useState("");

  const [error, setError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [result, setResult] = useState<{ orderId: number } | null>(null);

  const [cart, setCart] = useState<Cart | null>(null);
  const [cartError, setCartError] = useState("");

  useEffect(() => {
    let cancelled = false;

    getCart(resolveIdentity(accessToken))
      .then((data) => {
        if (cancelled) return;
        setCart(data);
      })
      .catch((err) => {
        if (cancelled) return;
        setCartError(err instanceof ApiError ? err.message : "장바구니를 불러오지 못했습니다.");
      });

    return () => {
      cancelled = true;
    };
  }, [accessToken]);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError("");
    setIsSubmitting(true);

    try {
      const identity = resolveIdentity(accessToken);
      const response = await checkoutCart(identity, {
        postNo,
        baseAddr,
        detailAddr,
        ...(isGuest ? { ordererName, ordererEmail, ordererPhone } : {}),
      });
      setResult(response);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "주문 생성에 실패했습니다.");
    } finally {
      setIsSubmitting(false);
    }
  };

  if (result) {
    return (
      <main className="max-w-sm mx-auto px-4 py-16 text-center">
        <h1 className="text-2xl font-bold text-gray-900 mb-4">주문이 완료되었습니다</h1>
        <p className="text-sm text-gray-500 mb-2">주문번호: {result.orderId}</p>
        {isGuest && (
          <p className="text-sm text-gray-500 mb-6 leading-relaxed">
            이 주문번호와 입력하신 이메일로 나중에 주문을 조회할 수 있습니다.
          </p>
        )}
        <div className="flex flex-col items-center gap-3">
          {!isGuest && (
            <Link
              href={`/orders/${result.orderId}`}
              className="w-full rounded-lg bg-gray-900 text-white text-sm font-medium py-2.5 hover:bg-gray-700 transition-colors"
            >
              주문 상세 보기
            </Link>
          )}
          <Link href="/products" className="text-sm font-medium text-gray-900 hover:underline">
            쇼핑 계속하기
          </Link>
        </div>
      </main>
    );
  }

  if (cartError) {
    return (
      <main className="max-w-sm mx-auto px-4 py-16">
        <p className="text-sm text-red-500">{cartError}</p>
      </main>
    );
  }

  if (!cart) {
    return (
      <main className="max-w-sm mx-auto px-4 py-16">
        <p className="text-sm text-gray-400">불러오는 중...</p>
      </main>
    );
  }

  if (cart.items.length === 0) {
    return (
      <main className="max-w-sm mx-auto px-4 py-16 text-center">
        <h1 className="text-2xl font-bold text-gray-900 mb-4">주문하기</h1>
        <p className="text-sm text-gray-400 mb-6">장바구니가 비어있어 주문할 수 없습니다.</p>
        <Link href="/products" className="text-sm font-medium text-gray-900 hover:underline">
          상품 보러가기
        </Link>
      </main>
    );
  }

  return (
    <main className="max-w-sm mx-auto px-4 py-16">
      <h1 className="text-2xl font-bold text-gray-900 mb-8 text-center">주문하기</h1>

      <form onSubmit={handleSubmit} className="flex flex-col gap-4">
        {isGuest && (
          <>
            <div>
              <label htmlFor="ordererName" className="block text-sm font-medium text-gray-700 mb-1">
                주문자 이름
              </label>
              <input
                id="ordererName"
                type="text"
                required
                value={ordererName}
                onChange={(e) => setOrdererName(e.target.value)}
                className="w-full rounded-lg border border-gray-200 px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-gray-900"
              />
            </div>

            <div>
              <label htmlFor="ordererEmail" className="block text-sm font-medium text-gray-700 mb-1">
                이메일
              </label>
              <input
                id="ordererEmail"
                type="email"
                required
                value={ordererEmail}
                onChange={(e) => setOrdererEmail(e.target.value)}
                className="w-full rounded-lg border border-gray-200 px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-gray-900"
              />
              <p className="mt-1 text-xs text-gray-400">나중에 주문 조회할 때 필요합니다</p>
            </div>

            <div>
              <label htmlFor="ordererPhone" className="block text-sm font-medium text-gray-700 mb-1">
                연락처
              </label>
              <input
                id="ordererPhone"
                type="text"
                required
                placeholder="010-0000-0000"
                value={ordererPhone}
                onChange={(e) => setOrdererPhone(e.target.value)}
                className="w-full rounded-lg border border-gray-200 px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-gray-900"
              />
            </div>

            <div className="h-px bg-gray-100 my-1" />
          </>
        )}

        <div>
          <label htmlFor="postNo" className="block text-sm font-medium text-gray-700 mb-1">
            우편번호
          </label>
          <input
            id="postNo"
            type="text"
            required
            value={postNo}
            onChange={(e) => setPostNo(e.target.value)}
            className="w-full rounded-lg border border-gray-200 px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-gray-900"
          />
        </div>

        <div>
          <label htmlFor="baseAddr" className="block text-sm font-medium text-gray-700 mb-1">
            기본 주소
          </label>
          <input
            id="baseAddr"
            type="text"
            required
            value={baseAddr}
            onChange={(e) => setBaseAddr(e.target.value)}
            className="w-full rounded-lg border border-gray-200 px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-gray-900"
          />
        </div>

        <div>
          <label htmlFor="detailAddr" className="block text-sm font-medium text-gray-700 mb-1">
            상세 주소
          </label>
          <input
            id="detailAddr"
            type="text"
            required
            value={detailAddr}
            onChange={(e) => setDetailAddr(e.target.value)}
            className="w-full rounded-lg border border-gray-200 px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-gray-900"
          />
        </div>

        {error && <p className="text-sm text-red-500">{error}</p>}

        <button
          type="submit"
          disabled={isSubmitting}
          className="mt-2 w-full rounded-lg bg-gray-900 text-white text-sm font-medium py-2.5 hover:bg-gray-700 transition-colors disabled:opacity-50"
        >
          {isSubmitting ? "주문 처리 중..." : "결제하기"}
        </button>
      </form>
    </main>
  );
}
