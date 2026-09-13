"use client";

import { Suspense, useEffect, useState, FormEvent } from "react";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import {
  getProducts,
  getWishlist,
  addToWishlist,
  removeFromWishlist,
  ApiError,
  type Product,
} from "@/lib/api";
import { useAuth } from "@/lib/auth-context";

const PAGE_SIZE = 12;

const SORT_OPTIONS = [
  { value: "latest", label: "최신순" },
  { value: "price_asc", label: "낮은 가격순" },
  { value: "price_desc", label: "높은 가격순" },
] as const;

type Filters = {
  keyword: string;
  category: string;
  minPrice: string;
  maxPrice: string;
  sort: string;
};

function buildProductsQuery(filters: Filters, page: number): string {
  const params = new URLSearchParams();
  if (filters.keyword) params.set("keyword", filters.keyword);
  if (filters.category) params.set("category", filters.category);
  if (filters.minPrice) params.set("minPrice", filters.minPrice);
  if (filters.maxPrice) params.set("maxPrice", filters.maxPrice);
  if (filters.sort && filters.sort !== "latest") params.set("sort", filters.sort);
  params.set("page", String(page));
  return params.toString();
}

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
  const filters: Filters = {
    keyword: searchParams.get("keyword") ?? "",
    category: searchParams.get("category") ?? "",
    minPrice: searchParams.get("minPrice") ?? "",
    maxPrice: searchParams.get("maxPrice") ?? "",
    sort: searchParams.get("sort") ?? "latest",
  };

  return (
    <ProductsList
      key={`${page}:${filters.keyword}:${filters.category}:${filters.minPrice}:${filters.maxPrice}:${filters.sort}`}
      page={page}
      filters={filters}
    />
  );
}

function ProductsList({ page, filters }: { page: number; filters: Filters }) {
  const router = useRouter();
  const { accessToken } = useAuth();

  const [products, setProducts] = useState<Product[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");

  const [wishedIds, setWishedIds] = useState<Set<number>>(new Set());

  useEffect(() => {
    let cancelled = false;

    getProducts({
      page,
      size: PAGE_SIZE,
      keyword: filters.keyword || undefined,
      category: filters.category || undefined,
      minPrice: filters.minPrice ? Number(filters.minPrice) : undefined,
      maxPrice: filters.maxPrice ? Number(filters.maxPrice) : undefined,
      sort: filters.sort,
    })
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
  }, [page, filters.keyword, filters.category, filters.minPrice, filters.maxPrice, filters.sort]);

  useEffect(() => {
    if (!accessToken) return;

    let cancelled = false;

    getWishlist(accessToken)
      .then((items) => {
        if (!cancelled) setWishedIds(new Set(items.map((item) => item.productId)));
      })
      .catch(() => {
        /* 찜 목록 조회 실패는 무시 */
      });

    return () => {
      cancelled = true;
    };
  }, [accessToken]);

  const toggleWish = async (productNo: number) => {
    if (!accessToken) return;
    const wasWished = wishedIds.has(productNo);

    setWishedIds((prev) => {
      const next = new Set(prev);
      if (wasWished) next.delete(productNo);
      else next.add(productNo);
      return next;
    });

    try {
      if (wasWished) await removeFromWishlist(accessToken, productNo);
      else await addToWishlist(accessToken, productNo);
    } catch {
      setWishedIds((prev) => {
        const next = new Set(prev);
        if (wasWished) next.add(productNo);
        else next.delete(productNo);
        return next;
      });
    }
  };

  const handleSearch = (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const formData = new FormData(e.currentTarget);
    const nextFilters: Filters = {
      keyword: String(formData.get("keyword") ?? "").trim(),
      category: String(formData.get("category") ?? "").trim(),
      minPrice: String(formData.get("minPrice") ?? "").trim(),
      maxPrice: String(formData.get("maxPrice") ?? "").trim(),
      sort: String(formData.get("sort") ?? "latest"),
    };
    router.push(`/products?${buildProductsQuery(nextFilters, 0)}`);
  };

  const goToPage = (nextPage: number) => {
    router.push(`/products?${buildProductsQuery(filters, nextPage)}`);
  };

  return (
    <main className="max-w-4xl mx-auto px-4 md:px-6 py-12 md:py-16">
      <h1 className="text-3xl font-extrabold text-gray-900 tracking-tight mb-6">상품</h1>

      <form onSubmit={handleSearch} className="flex flex-col gap-2 mb-8">
        <div className="flex gap-2">
          <input
            type="text"
            name="keyword"
            defaultValue={filters.keyword}
            placeholder="상품 검색"
            className="flex-1 rounded-lg border border-gray-200 px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-gray-900"
          />
          <button
            type="submit"
            className="rounded-lg bg-gray-900 text-white text-sm font-medium px-5 py-2.5 hover:bg-gray-700 transition-colors"
          >
            검색
          </button>
        </div>

        <div className="flex flex-wrap gap-2">
          <input
            type="text"
            name="category"
            defaultValue={filters.category}
            placeholder="카테고리"
            className="w-28 rounded-lg border border-gray-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-gray-900"
          />
          <input
            type="number"
            name="minPrice"
            defaultValue={filters.minPrice}
            placeholder="최소 가격"
            className="w-28 rounded-lg border border-gray-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-gray-900"
          />
          <input
            type="number"
            name="maxPrice"
            defaultValue={filters.maxPrice}
            placeholder="최대 가격"
            className="w-28 rounded-lg border border-gray-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-gray-900"
          />
          <select
            name="sort"
            defaultValue={filters.sort}
            className="rounded-lg border border-gray-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-gray-900"
          >
            {SORT_OPTIONS.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </div>
      </form>

      {error && <p className="text-sm text-red-500 mb-4">{error}</p>}

      {isLoading ? (
        <p className="text-sm text-gray-400">불러오는 중...</p>
      ) : products.length === 0 ? (
        <p className="text-sm text-gray-400">상품이 없습니다.</p>
      ) : (
        <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
          {products.map((product) => (
            <div key={product.productNo} className="relative">
              <Link
                href={`/products/${product.productNo}`}
                className="block rounded-xl border border-gray-100 p-4 hover:border-gray-300 transition-colors"
              >
                <p className="text-sm font-semibold text-gray-900 mb-1 pr-6">
                  {product.productName}
                </p>
                <p className="text-sm text-gray-500">{product.price.toLocaleString()}원</p>
                <p className="text-xs text-gray-400 mt-1">재고 {product.stock}개</p>
              </Link>
              {accessToken && (
                <button
                  onClick={() => toggleWish(product.productNo)}
                  aria-pressed={wishedIds.has(product.productNo)}
                  aria-label="찜"
                  className="absolute top-2 right-2 text-lg leading-none"
                >
                  <span
                    className={
                      wishedIds.has(product.productNo) ? "text-red-500" : "text-gray-300"
                    }
                  >
                    {wishedIds.has(product.productNo) ? "♥" : "♡"}
                  </span>
                </button>
              )}
            </div>
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
