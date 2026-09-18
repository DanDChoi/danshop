"use client";

import { useOrderNotifications } from "@/lib/use-order-notifications";
import { useToasts } from "@/lib/use-toasts";
import { dismissToast } from "@/lib/toast-store";

export default function ToastHost() {
  useOrderNotifications();
  const toasts = useToasts();

  if (toasts.length === 0) return null;

  return (
    <div className="fixed bottom-4 right-4 z-50 flex flex-col gap-2 items-end">
      {toasts.map((toast) => (
        <button
          key={toast.id}
          onClick={() => dismissToast(toast.id)}
          className="max-w-xs text-left rounded-lg bg-gray-900 text-white text-sm px-4 py-3 shadow-lg hover:bg-gray-700 transition-colors"
        >
          {toast.message}
        </button>
      ))}
    </div>
  );
}
