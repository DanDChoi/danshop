"use client";

import { useParams } from "next/navigation";
import { useEffect, useState } from "react";
import { useAuth } from "@/lib/auth-context";
import { getOrCreateGuestToken } from "@/lib/guest";
import {
  getProduct,
  addToCart,
  isWished,
  addToWishlist,
  removeFromWishlist,
  getProductReviews,
  ApiError,
  type Product,
  type ProductReviews,
} from "@/lib/api";

function formatDate(iso: string): string {
  return iso.slice(0, 10).split("-").join(".");
}

export default function ProductDetailPage() {
  const params = useParams<{ productNo: string }>();
  const productNo = Number(params.productNo);

  return <ProductDetail key={productNo} productNo={productNo} />;
}

function ProductDetail({ productNo }: { productNo: number }) {
  const { accessToken } = useAuth();

  const [product, setProduct] = useState<Product | null>(null);
  const [error, setError] = useState("");
  const [quantity, setQuantity] = useState(1);
  const [addStatus, setAddStatus] = useState<"idle" | "loading" | "success" | "error">("idle");
  const [addError, setAddError] = useState("");

  const [wished, setWished] = useState<boolean | null>(null);
  const [wishBusy, setWishBusy] = useState(false);

  const [productReviews, setProductReviews] = useState<ProductReviews | null>(null);

  useEffect(() => {
    let cancelled = false;

    getProduct(productNo)
      .then((data) => {
        if (cancelled) return;
        setProduct(data);
      })
      .catch((err) => {
        if (cancelled) return;
        setError(err instanceof ApiError ? err.message : "상품을 불러오지 못했습니다.");
      });

    if (accessToken) {
      isWished(accessToken, productNo)
        .then((res) => {
          if (!cancelled) setWished(res.wished);
        })
        .catch(() => {
          /* 찜 여부 조회 실패는 무시 */
        });

      // 리뷰 조회도 로그인이 필요한 엔드포인트라 비회원에게는 섹션을 아예 숨긴다.
      getProductReviews(accessToken, productNo)
        .then((data) => {
          if (!cancelled) setProductReviews(data);
        })
        .catch(() => {
          /* 리뷰 조회 실패는 무시 — 섹션을 그냥 안 보여준다 */
        });
    }

    return () => {
      cancelled = true;
    };
  }, [productNo, accessToken]);

  const handleToggleWish = async () => {
    if (!accessToken) return;
    setWishBusy(true);
    try {
      if (wished) {
        await removeFromWishlist(accessToken, productNo);
        setWished(false);
      } else {
        await addToWishlist(accessToken, productNo);
        setWished(true);
      }
    } catch {
      /* 실패 시 상태 유지 */
    } finally {
      setWishBusy(false);
    }
  };

  const handleAddToCart = async () => {
    setAddStatus("loading");
    setAddError("");
    try {
      const identity = accessToken
        ? { token: accessToken }
        : { guestToken: getOrCreateGuestToken() };
      await addToCart(identity, productNo, quantity);
      setAddStatus("success");
    } catch (err) {
      setAddStatus("error");
      setAddError(err instanceof ApiError ? err.message : "장바구니 담기에 실패했습니다.");
    }
  };

  if (error) {
    return (
      <main className="max-w-2xl mx-auto px-4 md:px-6 py-16">
        <p className="text-sm text-red-500">{error}</p>
      </main>
    );
  }

  if (!product) {
    return (
      <main className="max-w-2xl mx-auto px-4 md:px-6 py-16">
        <p className="text-sm text-gray-400">불러오는 중...</p>
      </main>
    );
  }

  return (
    <main className="max-w-2xl mx-auto px-4 md:px-6 py-12 md:py-16">
      <p className="font-mono text-xs text-gray-400 tracking-widest mb-2">
        {product.category ?? "일반"}
      </p>
      <h1 className="text-2xl font-extrabold text-gray-900 tracking-tight mb-2">
        {product.productName}
      </h1>
      <p className="text-xl text-gray-900 font-semibold mb-1">
        {product.price.toLocaleString()}원
      </p>
      <p className="text-sm text-gray-400 mb-6">재고 {product.stock}개</p>

      {accessToken && productReviews && (
        <p className="text-sm text-gray-500 mb-6">
          {productReviews.reviewCount > 0
            ? `★ ${(productReviews.avgRating ?? 0).toFixed(1)} · 리뷰 ${productReviews.reviewCount}개`
            : "아직 리뷰가 없습니다"}
        </p>
      )}

      {product.description && (
        <p className="text-sm text-gray-600 leading-relaxed mb-8">{product.description}</p>
      )}

      <div className="flex items-center gap-3 mb-4">
        <label htmlFor="quantity" className="text-sm text-gray-700">
          수량
        </label>
        <input
          id="quantity"
          type="number"
          min={1}
          max={product.stock}
          value={quantity}
          onChange={(e) =>
            setQuantity(Math.max(1, Math.min(product.stock, Number(e.target.value) || 1)))
          }
          className="w-20 rounded-lg border border-gray-200 px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-gray-900"
        />
      </div>

      <div className="flex items-center gap-3">
        <button
          onClick={handleAddToCart}
          disabled={addStatus === "loading" || product.stock === 0}
          className="rounded-lg bg-gray-900 text-white text-sm font-medium px-5 py-2.5 hover:bg-gray-700 transition-colors disabled:opacity-50"
        >
          {product.stock === 0 ? "품절" : addStatus === "loading" ? "담는 중..." : "장바구니 담기"}
        </button>

        {accessToken && (
          <button
            onClick={handleToggleWish}
            disabled={wishBusy}
            aria-pressed={wished === true}
            className="rounded-lg border border-gray-200 px-3 py-2.5 text-sm hover:border-gray-400 transition-colors disabled:opacity-50"
          >
            <span className={wished ? "text-red-500" : "text-gray-400"}>
              {wished ? "♥" : "♡"}
            </span>
            <span className="ml-1.5 text-gray-600">찜</span>
          </button>
        )}
      </div>

      {addStatus === "success" && (
        <p className="text-sm text-green-600 mt-3">장바구니에 담았습니다.</p>
      )}
      {addStatus === "error" && <p className="text-sm text-red-500 mt-3">{addError}</p>}

      {accessToken && productReviews && productReviews.reviews.length > 0 && (
        <div className="mt-10 pt-8 border-t border-gray-100">
          <h2 className="text-sm font-semibold text-gray-500 mb-3">
            리뷰 {productReviews.reviewCount}개
          </h2>
          <div className="flex flex-col gap-3">
            {productReviews.reviews.map((review) => (
              <div key={review.id} className="rounded-xl border border-gray-100 p-4">
                <div className="flex items-center justify-between gap-4 mb-1">
                  <p className="text-sm font-semibold text-gray-900">{review.userName}</p>
                  <span className="text-xs text-gray-400">{formatDate(review.createdAt)}</span>
                </div>
                <p className="text-sm text-gray-500 mb-1">
                  {"★".repeat(review.rating)}
                  {"☆".repeat(5 - review.rating)}
                </p>
                <p className="text-sm text-gray-700 leading-relaxed">{review.content}</p>
              </div>
            ))}
          </div>
        </div>
      )}
    </main>
  );
}
