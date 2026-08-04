"use client";

import Link from "next/link";
import { useAuth } from "@/lib/auth-context";

export default function Home() {
  const { userId } = useAuth();

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
    </main>
  );
}
