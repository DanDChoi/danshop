"use client";

import { useEffect, useState, FormEvent } from "react";
import {
  getMe,
  changePassword,
  getPointHistory,
  ApiError,
  type UserProfile,
  type PointHistoryEntry,
  type PointType,
} from "@/lib/api";
import { useRequireAuth } from "@/lib/use-require-auth";

function formatDate(iso: string): string {
  return iso.slice(0, 10).split("-").join(".");
}

const POINT_TYPE_LABELS: Record<PointType, string> = {
  EARN: "적립",
  USE: "사용",
  EARN_CANCEL: "적립 취소",
  REFUND: "환불",
};

export default function MyPage() {
  const accessToken = useRequireAuth();

  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [error, setError] = useState("");

  const [pointHistory, setPointHistory] = useState<PointHistoryEntry[]>([]);

  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [pwError, setPwError] = useState("");
  const [pwSuccess, setPwSuccess] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (!accessToken) return;

    let cancelled = false;

    getMe(accessToken)
      .then((data) => {
        if (!cancelled) setProfile(data);
      })
      .catch((err) => {
        if (!cancelled) {
          setError(err instanceof ApiError ? err.message : "프로필을 불러오지 못했습니다.");
        }
      });

    getPointHistory(accessToken)
      .then((data) => {
        if (!cancelled) setPointHistory(data);
      })
      .catch(() => {
        /* 포인트 내역 조회 실패는 무시 — 섹션을 그냥 안 보여준다 */
      });

    return () => {
      cancelled = true;
    };
  }, [accessToken]);

  const handleChangePassword = async (e: FormEvent) => {
    e.preventDefault();
    if (!accessToken) return;

    setPwError("");
    setPwSuccess(false);
    setIsSubmitting(true);
    try {
      await changePassword(accessToken, { currentPassword, newPassword });
      setPwSuccess(true);
      setCurrentPassword("");
      setNewPassword("");
    } catch (err) {
      setPwError(err instanceof ApiError ? err.message : "비밀번호 변경에 실패했습니다.");
    } finally {
      setIsSubmitting(false);
    }
  };

  if (!accessToken) {
    return (
      <main className="max-w-sm mx-auto px-4 py-16 md:py-24">
        <p className="text-sm text-gray-400">로그인이 필요합니다. 로그인 페이지로 이동합니다...</p>
      </main>
    );
  }

  if (error) {
    return (
      <main className="max-w-sm mx-auto px-4 py-16 md:py-24">
        <p className="text-sm text-red-500">{error}</p>
      </main>
    );
  }

  if (!profile) {
    return (
      <main className="max-w-sm mx-auto px-4 py-16 md:py-24">
        <p className="text-sm text-gray-400">불러오는 중...</p>
      </main>
    );
  }

  return (
    <main className="max-w-sm mx-auto px-4 py-16 md:py-24">
      <h1 className="text-2xl font-bold text-gray-900 mb-8 text-center">마이페이지</h1>

      <div className="rounded-xl border border-gray-100 p-4 mb-10 space-y-2">
        <div className="flex justify-between text-sm">
          <span className="text-gray-500">아이디</span>
          <span className="text-gray-900">{profile.userId}</span>
        </div>
        <div className="flex justify-between text-sm">
          <span className="text-gray-500">이름</span>
          <span className="text-gray-900">{profile.name}</span>
        </div>
        <div className="flex justify-between text-sm">
          <span className="text-gray-500">이메일</span>
          <span className="text-gray-900">{profile.email}</span>
        </div>
        <div className="flex justify-between text-sm">
          <span className="text-gray-500">포인트</span>
          <span className="text-gray-900">{profile.pointBalance.toLocaleString()}P</span>
        </div>
      </div>

      {pointHistory.length > 0 && (
        <div className="mb-10">
          <h2 className="text-sm font-semibold text-gray-500 mb-3">포인트 내역</h2>
          <div className="flex flex-col gap-2">
            {pointHistory.map((entry) => (
              <div
                key={entry.id}
                className="flex items-center justify-between gap-4 rounded-xl border border-gray-100 p-3"
              >
                <div className="min-w-0">
                  <p className="text-sm text-gray-900 truncate">{entry.description}</p>
                  <p className="text-xs text-gray-400">
                    {POINT_TYPE_LABELS[entry.type]} · {formatDate(entry.createdAt)}
                  </p>
                </div>
                <p className="shrink-0 text-sm font-medium text-gray-900">
                  {entry.signedAmount > 0 ? "+" : ""}
                  {entry.signedAmount.toLocaleString()}P
                </p>
              </div>
            ))}
          </div>
        </div>
      )}

      <h2 className="text-sm font-semibold text-gray-500 mb-3">비밀번호 변경</h2>
      <form onSubmit={handleChangePassword} className="flex flex-col gap-4">
        <div>
          <label htmlFor="currentPassword" className="block text-sm font-medium text-gray-700 mb-1">
            현재 비밀번호
          </label>
          <input
            id="currentPassword"
            type="password"
            required
            value={currentPassword}
            onChange={(e) => setCurrentPassword(e.target.value)}
            className="w-full rounded-lg border border-gray-200 px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-gray-900"
          />
        </div>

        <div>
          <label htmlFor="newPassword" className="block text-sm font-medium text-gray-700 mb-1">
            새 비밀번호
          </label>
          <input
            id="newPassword"
            type="password"
            required
            minLength={8}
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
            className="w-full rounded-lg border border-gray-200 px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-gray-900"
          />
          <p className="mt-1 text-xs text-gray-400">8자 이상</p>
        </div>

        {pwError && <p className="text-sm text-red-500">{pwError}</p>}
        {pwSuccess && <p className="text-sm text-green-600">비밀번호가 변경되었습니다.</p>}

        <button
          type="submit"
          disabled={isSubmitting}
          className="mt-2 w-full rounded-lg bg-gray-900 text-white text-sm font-medium py-2.5 hover:bg-gray-700 transition-colors disabled:opacity-50"
        >
          {isSubmitting ? "변경 중..." : "비밀번호 변경"}
        </button>
      </form>
    </main>
  );
}
