"use client";

import { useEffect, useState, FormEvent } from "react";
import Link from "next/link";
import { useRequireAdmin } from "@/lib/use-require-admin";
import { useAuth } from "@/lib/auth-context";
import {
  getProducts,
  createProduct,
  updateProduct,
  deleteProduct,
  ApiError,
  type Product,
  type ProductPayload,
} from "@/lib/api";

const PAGE_SIZE = 20;

type FormState = {
  productName: string;
  price: string;
  category: string;
  stock: string;
  description: string;
};

const EMPTY_FORM: FormState = {
  productName: "",
  price: "",
  category: "",
  stock: "",
  description: "",
};

function toPayload(form: FormState): ProductPayload {
  return {
    productName: form.productName.trim(),
    price: Number(form.price),
    category: form.category.trim() || undefined,
    stock: Number(form.stock) || 0,
    description: form.description.trim() || undefined,
  };
}

function toFormState(product: Product): FormState {
  return {
    productName: product.productName,
    price: String(product.price),
    category: product.category ?? "",
    stock: String(product.stock),
    description: product.description ?? "",
  };
}

const inputClass =
  "w-full rounded-lg border border-gray-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-gray-900";

function ProductFields({
  form,
  onChange,
}: {
  form: FormState;
  onChange: (next: FormState) => void;
}) {
  return (
    <div className="grid grid-cols-2 gap-2">
      <input
        type="text"
        required
        placeholder="상품명"
        value={form.productName}
        onChange={(e) => onChange({ ...form, productName: e.target.value })}
        className={`${inputClass} col-span-2`}
      />
      <input
        type="number"
        required
        min={0}
        placeholder="가격"
        value={form.price}
        onChange={(e) => onChange({ ...form, price: e.target.value })}
        className={inputClass}
      />
      <input
        type="number"
        required
        min={0}
        placeholder="재고"
        value={form.stock}
        onChange={(e) => onChange({ ...form, stock: e.target.value })}
        className={inputClass}
      />
      <input
        type="text"
        placeholder="카테고리"
        value={form.category}
        onChange={(e) => onChange({ ...form, category: e.target.value })}
        className={`${inputClass} col-span-2`}
      />
      <textarea
        placeholder="설명"
        value={form.description}
        onChange={(e) => onChange({ ...form, description: e.target.value })}
        rows={2}
        className={`${inputClass} col-span-2`}
      />
    </div>
  );
}

export default function AdminProductsPage() {
  const isAdmin = useRequireAdmin();
  const { accessToken } = useAuth();

  const [products, setProducts] = useState<Product[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [page, setPage] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");
  const [refreshKey, setRefreshKey] = useState(0);

  const [showCreate, setShowCreate] = useState(false);
  const [createForm, setCreateForm] = useState<FormState>(EMPTY_FORM);
  const [createError, setCreateError] = useState("");
  const [isCreating, setIsCreating] = useState(false);

  const [editingId, setEditingId] = useState<number | null>(null);
  const [editForm, setEditForm] = useState<FormState>(EMPTY_FORM);
  const [editError, setEditError] = useState("");
  const [isSavingEdit, setIsSavingEdit] = useState(false);

  const [confirmDeleteId, setConfirmDeleteId] = useState<number | null>(null);
  const [deletingId, setDeletingId] = useState<number | null>(null);
  const [deleteError, setDeleteError] = useState("");

  useEffect(() => {
    if (!isAdmin) return;

    let cancelled = false;

    getProducts({ page, size: PAGE_SIZE, sort: "latest" })
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
  }, [isAdmin, page, refreshKey]);

  const handleCreate = async (e: FormEvent) => {
    e.preventDefault();
    if (!accessToken) return;
    setCreateError("");
    setIsCreating(true);
    try {
      await createProduct(accessToken, toPayload(createForm));
      setCreateForm(EMPTY_FORM);
      setShowCreate(false);
      setRefreshKey((k) => k + 1);
    } catch (err) {
      setCreateError(err instanceof ApiError ? err.message : "상품 등록에 실패했습니다.");
    } finally {
      setIsCreating(false);
    }
  };

  const startEdit = (product: Product) => {
    setEditingId(product.productNo);
    setEditForm(toFormState(product));
    setEditError("");
  };

  const handleEditSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (!accessToken || editingId === null) return;
    setEditError("");
    setIsSavingEdit(true);
    try {
      await updateProduct(accessToken, editingId, toPayload(editForm));
      setEditingId(null);
      setRefreshKey((k) => k + 1);
    } catch (err) {
      setEditError(err instanceof ApiError ? err.message : "상품 수정에 실패했습니다.");
    } finally {
      setIsSavingEdit(false);
    }
  };

  const handleDelete = async (productNo: number) => {
    if (!accessToken) return;
    setDeleteError("");
    setDeletingId(productNo);
    try {
      await deleteProduct(accessToken, productNo);
      setConfirmDeleteId(null);
      setRefreshKey((k) => k + 1);
    } catch (err) {
      setDeleteError(err instanceof ApiError ? err.message : "상품 삭제에 실패했습니다.");
    } finally {
      setDeletingId(null);
    }
  };

  if (!isAdmin) {
    return (
      <main className="max-w-3xl mx-auto px-4 md:px-6 py-16">
        <p className="text-sm text-gray-400">확인 중...</p>
      </main>
    );
  }

  return (
    <main className="max-w-3xl mx-auto px-4 md:px-6 py-12 md:py-16">
      <Link href="/admin" className="text-sm text-gray-400 hover:text-gray-700 transition-colors">
        ← 관리자
      </Link>

      <div className="flex items-baseline justify-between mt-4 mb-6">
        <h1 className="text-3xl font-extrabold text-gray-900 tracking-tight">상품 관리</h1>
        <button
          onClick={() => setShowCreate((v) => !v)}
          className="rounded-lg bg-gray-900 text-white text-sm font-medium px-4 py-2 hover:bg-gray-700 transition-colors"
        >
          {showCreate ? "취소" : "상품 등록"}
        </button>
      </div>

      {showCreate && (
        <form
          onSubmit={handleCreate}
          className="rounded-xl border border-gray-100 p-4 mb-6 flex flex-col gap-3"
        >
          <ProductFields form={createForm} onChange={setCreateForm} />
          {createError && <p className="text-sm text-red-500">{createError}</p>}
          <button
            type="submit"
            disabled={isCreating}
            className="rounded-lg bg-gray-900 text-white text-sm font-medium px-4 py-2 hover:bg-gray-700 transition-colors disabled:opacity-50 self-start"
          >
            {isCreating ? "등록 중..." : "등록"}
          </button>
        </form>
      )}

      {error && <p className="text-sm text-red-500 mb-4">{error}</p>}
      {deleteError && <p className="text-sm text-red-500 mb-4">{deleteError}</p>}

      {isLoading ? (
        <p className="text-sm text-gray-400">불러오는 중...</p>
      ) : products.length === 0 ? (
        <p className="text-sm text-gray-400">상품이 없습니다.</p>
      ) : (
        <div className="flex flex-col gap-3">
          {products.map((product) =>
            editingId === product.productNo ? (
              <form
                key={product.productNo}
                onSubmit={handleEditSubmit}
                className="rounded-xl border border-gray-300 p-4 flex flex-col gap-3"
              >
                <ProductFields form={editForm} onChange={setEditForm} />
                {editError && <p className="text-sm text-red-500">{editError}</p>}
                <div className="flex gap-2">
                  <button
                    type="submit"
                    disabled={isSavingEdit}
                    className="rounded-lg bg-gray-900 text-white text-sm font-medium px-4 py-2 hover:bg-gray-700 transition-colors disabled:opacity-50"
                  >
                    {isSavingEdit ? "저장 중..." : "저장"}
                  </button>
                  <button
                    type="button"
                    onClick={() => setEditingId(null)}
                    disabled={isSavingEdit}
                    className="rounded-lg border border-gray-200 text-sm font-medium px-4 py-2 hover:border-gray-400 transition-colors disabled:opacity-50"
                  >
                    취소
                  </button>
                </div>
              </form>
            ) : (
              <div
                key={product.productNo}
                className="rounded-xl border border-gray-100 p-4 flex items-center justify-between gap-4"
              >
                <div className="min-w-0">
                  <p className="text-sm font-semibold text-gray-900 truncate">
                    #{product.productNo} {product.productName}
                  </p>
                  <p className="text-sm text-gray-500">
                    {product.price.toLocaleString()}원 · 재고 {product.stock}개
                    {product.category ? ` · ${product.category}` : ""}
                  </p>
                </div>

                <div className="shrink-0 flex items-center gap-2">
                  {confirmDeleteId === product.productNo ? (
                    <>
                      <span className="text-sm text-gray-500">삭제할까요?</span>
                      <button
                        onClick={() => handleDelete(product.productNo)}
                        disabled={deletingId === product.productNo}
                        className="rounded-lg bg-red-500 text-white text-sm font-medium px-3 py-1.5 hover:bg-red-600 transition-colors disabled:opacity-50"
                      >
                        {deletingId === product.productNo ? "삭제 중..." : "삭제"}
                      </button>
                      <button
                        onClick={() => setConfirmDeleteId(null)}
                        disabled={deletingId === product.productNo}
                        className="rounded-lg border border-gray-200 text-sm font-medium px-3 py-1.5 hover:border-gray-400 transition-colors disabled:opacity-50"
                      >
                        취소
                      </button>
                    </>
                  ) : (
                    <>
                      <button
                        onClick={() => startEdit(product)}
                        className="rounded-lg border border-gray-200 text-sm font-medium px-3 py-1.5 hover:border-gray-400 transition-colors"
                      >
                        수정
                      </button>
                      <button
                        onClick={() => setConfirmDeleteId(product.productNo)}
                        className="text-sm text-gray-400 hover:text-red-500 transition-colors"
                      >
                        삭제
                      </button>
                    </>
                  )}
                </div>
              </div>
            )
          )}
        </div>
      )}

      {totalPages > 1 && (
        <div className="flex items-center justify-center gap-2 mt-8">
          <button
            onClick={() => setPage((p) => p - 1)}
            disabled={page <= 0}
            className="text-sm px-3 py-1.5 rounded-lg border border-gray-200 disabled:opacity-40 hover:border-gray-400 transition-colors"
          >
            이전
          </button>
          <span className="text-sm text-gray-500">
            {page + 1} / {totalPages}
          </span>
          <button
            onClick={() => setPage((p) => p + 1)}
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
