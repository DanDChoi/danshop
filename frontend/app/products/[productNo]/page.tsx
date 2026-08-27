"use client";

import { useParams } from "next/navigation";
import { useEffect, useState } from "react";
import { useAuth } from "@/lib/auth-context";
import { getOrCreateGuestToken } from "@/lib/guest";
import { getProduct, addToCart, ApiError, type Product } from "@/lib/api";

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

    return () => {
      cancelled = true;
    };
  }, [productNo]);

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

      <button
        onClick={handleAddToCart}
        disabled={addStatus === "loading" || product.stock === 0}
        className="rounded-lg bg-gray-900 text-white text-sm font-medium px-5 py-2.5 hover:bg-gray-700 transition-colors disabled:opacity-50"
      >
        {product.stock === 0 ? "품절" : addStatus === "loading" ? "담는 중..." : "장바구니 담기"}
      </button>

      {addStatus === "success" && (
        <p className="text-sm text-green-600 mt-3">장바구니에 담았습니다.</p>
      )}
      {addStatus === "error" && <p className="text-sm text-red-500 mt-3">{addError}</p>}
    </main>
  );
}
