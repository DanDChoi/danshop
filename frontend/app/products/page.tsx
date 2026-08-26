"use client";

import { Suspense, useEffect, useState, FormEvent } from "react";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { getProducts, ApiError, type Product } from "@/lib/api";

const PAGE_SIZE = 12;

export default function ProductsPage() {
  return (
    <Suspense fallback={null}>
      <ProductsShell />
    </Suspense>
  );
}

function ProductsShell() {
  const searchParams = useSearchParams();
  const page = Number(searchParams.get("page") ?? "0");
  const keyword = searchParams.get("keyword") ?? "";

  return <ProductsList key={`${page}:${keyword}`} page={page} keyword={keyword} />;
}

function ProductsList({ page, keyword }: { page: number; keyword: string }) {
  const router = useRouter();

  const [products, setProducts] = useState<Product[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;

    getProducts({ page, size: PAGE_SIZE, keyword: keyword || undefined })
      .then((data) => {
        if (cancelled) return;
        setProducts(data.content);
        setTotalPages(data.totalPages);
      })
      .catch((err) => {
        if (cancelled) return;
        setError(err instanceof ApiError ? err.message : "상품을 불러오지 못했습니다.");
      })
      .finally(() => {
        if (!cancelled) setIsLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [page, keyword]);

  const handleSearch = (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const formData = new FormData(e.currentTarget);
    const nextKeyword = String(formData.get("keyword") ?? "").trim();
    const params = new URLSearchParams();
    if (nextKeyword) params.set("keyword", nextKeyword);
    params.set("page", "0");
    router.push(`/products?${params.toString()}`);
  };

  const goToPage = (nextPage: number) => {
    const params = new URLSearchParams();
    if (keyword) params.set("keyword", keyword);
    params.set("page", String(nextPage));
    router.push(`/products?${params.toString()}`);
  };

  return (
    <main className="max-w-4xl mx-auto px-4 md:px-6 py-12 md:py-16">
      <h1 className="text-3xl font-extrabold text-gray-900 tracking-tight mb-6">상품</h1>

      <form onSubmit={handleSearch} className="flex gap-2 mb-8">
        <input
          type="text"
          name="keyword"
          defaultValue={keyword}
          placeholder="상품 검색"
          className="flex-1 rounded-lg border border-gray-200 px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-gray-900"
        />
        <button
          type="submit"
          className="rounded-lg bg-gray-900 text-white text-sm font-medium px-5 py-2.5 hover:bg-gray-700 transition-colors"
        >
          검색
        </button>
      </form>

      {error && <p className="text-sm text-red-500 mb-4">{error}</p>}

      {isLoading ? (
        <p className="text-sm text-gray-400">불러오는 중...</p>
      ) : products.length === 0 ? (
        <p className="text-sm text-gray-400">상품이 없습니다.</p>
      ) : (
        <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
          {products.map((product) => (
            <Link
              key={product.productNo}
              href={`/products/${product.productNo}`}
              className="block rounded-xl border border-gray-100 p-4 hover:border-gray-300 transition-colors"
            >
              <p className="text-sm font-semibold text-gray-900 mb-1">{product.productName}</p>
              <p className="text-sm text-gray-500">{product.price.toLocaleString()}원</p>
              <p className="text-xs text-gray-400 mt-1">재고 {product.stock}개</p>
            </Link>
          ))}
        </div>
      )}

      {totalPages > 1 && (
        <div className="flex items-center justify-center gap-2 mt-8">
          <button
            onClick={() => goToPage(page - 1)}
            disabled={page <= 0}
            className="text-sm px-3 py-1.5 rounded-lg border border-gray-200 disabled:opacity-40 hover:border-gray-400 transition-colors"
          >
            이전
          </button>
          <span className="text-sm text-gray-500">
            {page + 1} / {totalPages}
          </span>
          <button
            onClick={() => goToPage(page + 1)}
            disabled={page >= totalPages - 1}
            className="text-sm px-3 py-1.5 rounded-lg border border-gray-200 disabled:opacity-40 hover:border-gray-400 transition-colors"
          >
            다음
          </button>
        </div>
      )}
    </main>
  );
}
