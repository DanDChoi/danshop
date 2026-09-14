// 장바구니가 바뀌었음을 알리는 아주 작은 pub/sub.
// api.ts 의 장바구니 변경 함수들이 성공 시 notifyCartChanged() 를 호출하고,
// useCartCount() 같은 훅이 subscribeCartChanged() 로 구독해 다시 조회한다.

type Listener = () => void;
const listeners = new Set<Listener>();

export function notifyCartChanged(): void {
  listeners.forEach((listener) => listener());
}

export function subscribeCartChanged(listener: Listener): () => void {
  listeners.add(listener);
  return () => listeners.delete(listener);
}
