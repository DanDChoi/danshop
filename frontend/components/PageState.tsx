/** 페이지 데이터 로딩 중 공용 문구. */
export function Loading() {
  return <p className="text-sm text-gray-400">불러오는 중...</p>;
}

/** 페이지 데이터 조회 실패 공용 문구. message가 비어있으면 아무것도 렌더링하지 않는다. */
export function ErrorMessage({
  message,
  className = "",
}: {
  message: string;
  className?: string;
}) {
  if (!message) return null;
  return <p className={`text-sm text-red-500 ${className}`.trim()}>{message}</p>;
}
