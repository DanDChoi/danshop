// 화면 어디서든 띄울 수 있는 아주 작은 토스트 큐. cart-store 와 같은 pub/sub 형태.

export type Toast = { id: number; message: string };

type Listener = (toasts: Toast[]) => void;

const listeners = new Set<Listener>();
let toasts: Toast[] = [];
let nextId = 0;

function emit(): void {
  listeners.forEach((listener) => listener(toasts));
}

export function pushToast(message: string, durationMs = 4000): void {
  const id = ++nextId;
  toasts = [...toasts, { id, message }];
  emit();
  setTimeout(() => dismissToast(id), durationMs);
}

export function dismissToast(id: number): void {
  toasts = toasts.filter((toast) => toast.id !== id);
  emit();
}

export function subscribeToasts(listener: Listener): () => void {
  listeners.add(listener);
  return () => listeners.delete(listener);
}
