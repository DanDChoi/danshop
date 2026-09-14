"use client";

import { useEffect, useState } from "react";
import { useAuth } from "./auth-context";
import { getGuestToken } from "./guest";
import { getCart } from "./api";
import { subscribeCartChanged } from "./cart-store";

/** Navbar 뱃지용 장바구니 총 수량. 장바구니가 바뀔 때마다(cart-store) 자동 갱신된다. */
export function useCartCount(): number {
  const { accessToken } = useAuth();
  const [count, setCount] = useState(0);

  useEffect(() => {
    let cancelled = false;
    const identity = accessToken ? { token: accessToken } : { guestToken: getGuestToken() };

    const load = () => {
      getCart(identity)
        .then((cart) => {
          if (cancelled) return;
          setCount(cart.items.reduce((sum, item) => sum + item.quantity, 0));
        })
        .catch(() => {
          /* 게스트 토큰이 없거나 조회 실패 — 뱃지는 그대로 둔다 */
        });
    };

    load();
    const unsubscribe = subscribeCartChanged(load);

    return () => {
      cancelled = true;
      unsubscribe();
    };
  }, [accessToken]);

  return count;
}
