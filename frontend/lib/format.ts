/** ISO 날짜/날짜시간 문자열의 날짜 부분만 "YYYY.MM.DD"로 표시한다. */
export function formatDate(iso: string): string {
  return iso.slice(0, 10).split("-").join(".");
}
