# Danshop Frontend

[danshop 백엔드](../README.md)(Spring Boot + JPA + Redis 이커머스 API)를 위한 Next.js 프론트엔드입니다.

## 기술 스택

| 분류 | 기술 |
|------|------|
| Framework | Next.js (App Router) |
| Language | TypeScript |
| Styling | Tailwind CSS |
| 상태 관리 | React Context + `useSyncExternalStore` (전역 라이브러리 없이 직접 구현) |
| 인증 | JWT Access + Refresh Token (401 시 자동 재발급) |
| 실시간 알림 | SSE (Server-Sent Events, fetch 스트림 직접 파싱) |

## 주요 기능

### 쇼핑
- 상품 목록(검색·카테고리·가격·정렬 필터) / 상세, 장바구니, 위시리스트
- 회원/비회원(게스트 토큰) 겸용 장바구니 · 주문
- 체크아웃 시 쿠폰 선택 + 포인트 사용, 최종 결제금액 실시간 계산

### 주문
- 주문 내역 / 상세, 취소·배송지 변경(대기중 주문만)
- 비회원 주문 조회 (주문번호 + 이메일)

### 회원
- 회원가입 / 로그인 / 마이페이지(프로필, 비밀번호 변경)
- 액세스 토큰 만료 시 백그라운드에서 refresh 토큰으로 자동 재발급
- 로그인 필요한 페이지는 공통 가드로 처리, 로그인 후 원래 페이지로 복귀

### 쿠폰 · 포인트 · 위시리스트
- 쿠폰함(선착순 발급), 포인트 잔액 확인
- 위시리스트 담기/빼기, 장바구니로 이동

### 관리자 (ADMIN)
- 상품 등록 / 수정 / 삭제
- 전체 주문 조회 및 상태 변경
- JWT의 role claim으로 화면 노출만 제어 (실제 인가는 항상 백엔드 `@PreAuthorize`)

### 실시간 알림
- 주문/포인트 이벤트를 SSE로 구독해 토스트로 표시

## 기술적으로 눈여겨볼 부분

- **인증 상태 관리**: 별도 상태관리 라이브러리 없이 모듈 스코프 스토어 + `useSyncExternalStore`로 로그인 상태를 구현 (`lib/auth-context.tsx`)
- **401 자동 재발급**: `lib/api.ts`의 `request()`가 Bearer 요청이 401을 받으면 refresh 토큰으로 재발급 후 원 요청을 1회 재시도. 동시에 여러 요청이 401을 받아도 refresh 호출은 한 번만 나가도록 in-flight 프로미스를 공유
- **회원/게스트 겸용 장바구니**: `Identity` 타입(`{token}` | `{guestToken}`)으로 API 함수를 분기 없이 공용으로 사용
- **SSE는 EventSource를 안 씀**: 알림 구독 엔드포인트가 Authorization 헤더를 요구하는데, 브라우저 `EventSource`는 커스텀 헤더를 못 보냄 → `fetch` + `ReadableStream`으로 SSE 프레임을 직접 파싱 (`lib/use-order-notifications.ts`)
- **장바구니 뱃지 갱신**: 전역 상태 없이 아주 작은 pub/sub(`lib/cart-store.ts`)로 장바구니를 바꾸는 API 호출들이 성공 시 알리고, Navbar가 구독해서 총 수량을 다시 조회

## 로컬 실행

```bash
npm install
npm run dev
```

http://localhost:3000 에서 확인할 수 있습니다. 백엔드(danshop API 서버)가 먼저 실행 중이어야 데이터가 보입니다.

### 환경변수

`.env.local`:

```
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
```

미설정 시 `http://localhost:8080`을 기본값으로 사용합니다.

## 스크린샷

_준비 중 — 백엔드까지 함께 배포된 뒤 실제 화면으로 채울 예정입니다._

## 배포

Vercel 배포 예정. 배포 URL이 확정되면 이 섹션과 [루트 README](../README.md)에 함께 추가합니다.
