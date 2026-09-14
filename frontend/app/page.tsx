"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { useAuth } from "@/lib/auth-context";
import { getProducts, type Product } from "@/lib/api";

export default function Home() {
  const { userId } = useAuth();

  const [newProducts, setNewProducts] = useState<Product[]>([]);

  useEffect(() => {
    let cancelled = false;

    getProducts({ sort: "latest", size: 4 })
      .then((data) => {
        if (!cancelled) setNewProducts(data.content);
      })
      .catch(() => {
        /* 신상품 섹션은 실패해도 조용히 숨긴다 */
      });

    return () => {
      cancelled = true;
    };
  }, []);

  return (
    <main className="max-w-4xl mx-auto px-4 md:px-6 py-16 md:py-24">
      <div className="text-center">
        <p className="font-mono text-xs text-gray-400 tracking-widest mb-3">
          SPRING BOOT + JPA E-COMMERCE
        </p>
        <h1 className="text-4xl md:text-5xl font-extrabold text-gray-900 tracking-tight leading-tight mb-4">
          Danshop
        </h1>
        <p className="text-gray-500 leading-relaxed max-w-lg mx-auto mb-8">
          JWT 인증, Redis 캐싱, 동시성 제어, RBAC까지 실무 수준으로 구현한
          이커머스 백엔드의 프론트엔드입니다.
        </p>

        <div className="flex items-center justify-center gap-3">
          {userId ? (
            <span className="text-sm text-gray-500">{userId}님, 환영합니다.</span>
          ) : (
            <>
              <Link
                href="/signup"
                className="text-sm font-medium bg-gray-900 text-white rounded-lg px-5 py-2.5 hover:bg-gray-700 transition-colors"
              >
                회원가입
              </Link>
              <Link
                href="/login"
                className="text-sm font-medium border border-gray-200 rounded-lg px-5 py-2.5 hover:border-gray-400 transition-colors"
              >
                로그인
              </Link>
            </>
          )}
        </div>
      </div>

      {newProducts.length > 0 && (
        <section className="mt-20">
          <div className="flex items-baseline justify-between mb-4">
            <h2 className="text-lg font-bold text-gray-900">신상품</h2>
            <Link
              href="/products"
              className="text-sm text-gray-500 hover:text-gray-800 transition-colors"
            >
              전체보기
            </Link>
          </div>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            {newProducts.map((product) => (
              <Link
                key={product.productNo}
                href={`/products/${product.productNo}`}
                className="block rounded-xl border border-gray-100 p-4 hover:border-gray-300 transition-colors"
              >
                <p className="text-sm font-semibold text-gray-900 mb-1 truncate">
                  {product.productName}
                </p>
                <p className="text-sm text-gray-500">{product.price.toLocaleString()}원</p>
              </Link>
            ))}
          </div>
        </section>
      )}
    </main>
  );
}
