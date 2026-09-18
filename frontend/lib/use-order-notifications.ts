"use client";

import { useEffect } from "react";
import { useAuth } from "./auth-context";
import { API_BASE_URL } from "./api";
import { pushToast } from "./toast-store";

type NotificationPayload = {
  type: string; // ORDER_CREATED, ORDER_CANCELLED, POINT_EARNED, POINT_USED
  message: string;
  data?: unknown;
};

function parseSseFrame(frame: string): { event: string; data: string } {
  let event = "message";
  const dataLines: string[] = [];

  for (const line of frame.split("\n")) {
    if (line.startsWith("event:")) event = line.slice(6).trim();
    else if (line.startsWith("data:")) dataLines.push(line.slice(5).trim());
  }

  return { event, data: dataLines.join("\n") };
}

/**
 * 백엔드 SSE(GET /sse/connect, Authorization 헤더 필요)를 구독해 주문/포인트
 * 알림을 토스트로 띄운다. EventSource는 커스텀 헤더를 못 보내서 fetch 스트림을
 * 직접 파싱한다.
 */
export function useOrderNotifications(): void {
  const { accessToken } = useAuth();

  useEffect(() => {
    if (!accessToken) return;

    const controller = new AbortController();
    let cancelled = false;

    async function connect() {
      try {
        const res = await fetch(`${API_BASE_URL}/sse/connect`, {
          headers: { Authorization: `Bearer ${accessToken}` },
          signal: controller.signal,
        });
        if (!res.ok || !res.body) return;

        const reader = res.body.getReader();
        const decoder = new TextDecoder();
        let buffer = "";

        while (!cancelled) {
          const { done, value } = await reader.read();
          if (done) break;
          buffer += decoder.decode(value, { stream: true });

          let separatorIndex: number;
          while ((separatorIndex = buffer.indexOf("\n\n")) !== -1) {
            const frame = buffer.slice(0, separatorIndex);
            buffer = buffer.slice(separatorIndex + 2);

            const { event, data } = parseSseFrame(frame);
            if (event === "CONNECT" || !data) continue;

            try {
              const payload = JSON.parse(data) as NotificationPayload;
              pushToast(payload.message);
            } catch {
              // 파싱 실패한 알림은 조용히 무시
            }
          }
        }
      } catch {
        // 연결 끊김/중단 — 로그인 유지 중이면 다음 마운트나 새로고침 시 재연결된다
      }
    }

    connect();

    return () => {
      cancelled = true;
      controller.abort();
    };
  }, [accessToken]);
}
