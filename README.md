# Danshop

Spring Boot 기반 이커머스 백엔드 프로젝트입니다.  
JWT 인증, Redis 캐싱, 동시성 제어, RBAC 등 실무에서 자주 쓰이는 기술을 직접 구현했습니다.

## 배포

- **API 서버**: https://danshop-production.up.railway.app
- **Swagger UI**: https://danshop-production.up.railway.app/swagger-ui/index.html
- **Health Check**: https://danshop-production.up.railway.app/actuator/health

## 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 4.0 |
| ORM | Spring Data JPA, QueryDSL |
| Database | PostgreSQL |
| Cache | Redis |
| Auth | JWT (Access + Refresh Token) |
| Docs | Swagger (SpringDoc OpenAPI) |
| Deploy | Railway |
| Test | JUnit 5, Mockito |

## 주요 기능

### 회원
- 회원가입 / 로그인 / 로그아웃
- Access Token (30분) + Refresh Token (7일) 발급 및 재발급
- 비밀번호 변경

### 상품
- 상품 등록 / 수정 / 삭제 (관리자 전용)
- Offset 페이지네이션 + 동적 정렬
- No-offset 커서 페이지네이션
- 키워드 / 카테고리 / 가격 범위 동적 검색 (QueryDSL)

### 주문
- 주문 생성 / 취소 / 조회
- 배송지 변경 (PENDING 상태만 가능)
- 주문 상태 관리 (관리자 전용)

### 쿠폰 (선착순 발급)
- Redis Lua Script로 중복 체크 + 수량 차감 원자적 처리
- `@Modifying` 쿼리로 DB `remainQuantity` 원자적 동기화
- 100명 동시 요청 동시성 테스트

### 장바구니
- Redis 기반 장바구니 (추가 / 수정 / 삭제 / 전체 비우기)
- 장바구니에서 바로 주문

### 포인트
- 주문 완료 시 포인트 적립, 주문 취소 시 차감
- 포인트 사용 내역 조회

### 위시리스트
- 위시리스트 추가 / 삭제 / 조회
- 위시리스트 → 장바구니 이동

### 리뷰
- 구매한 상품만 리뷰 작성 가능
- 리뷰 수정 / 삭제 (본인만)
- 상품별 리뷰 목록 + 평균 평점 조회

### 관리자
- 전체 주문 목록 조회 및 상태 변경
- 매출 통계 / 인기 상품 / 주문 상태 통계

## 기술적 구현 포인트

### JWT 인증
- Access Token + Refresh Token 이중 토큰 구조
- Refresh Token Redis 저장으로 무효화 가능
- `JwtFilter`에서 위조/만료 토큰 즉시 401 반환

### 동시성 제어
- 상품 재고 차감: `@Lock(PESSIMISTIC_WRITE)` 비관적 락
- 쿠폰 선착순 발급: Redis Lua Script (SADD + DECR 원자화)
- 포인트 차감: Redis 분산 락

### Spring Security RBAC
- URL 레벨 (`/admin/**`) + 메서드 레벨 (`@PreAuthorize`) 이중 방어
- 미인증 401, 권한 부족 403 명시적 반환

### 성능 최적화
- QueryDSL 동적 쿼리로 불필요한 조건 제거
- No-offset 커서 페이지네이션으로 대용량 조회 최적화
- Hibernate 슬로우 쿼리 탐지 (100ms 초과 자동 로깅)

### 로깅
- `HandlerInterceptor`로 모든 요청의 메서드 / URI / 상태코드 / 소요시간 기록

## 테스트

| 도메인 | 주요 테스트 |
|--------|------------|
| User | 회원가입, 로그인, 비밀번호 변경 |
| Product | 상품 검색, 동적 정렬 |
| Order | 주문 생성/취소, 쿠폰 적용, 포인트 적립/차감 |
| Coupon | 발급, 중복 방지, 수량 소진, 동시성 (100명) |
| Cart | 추가, 수정, 주문 연동, 위시리스트 이동 |
| Point | 적립/차감, 동시성 |
| Review | 작성, 수정, 삭제 |
| Admin | 주문 상태 변경, 매출 통계 |
| Security | RBAC (401/403/200), 위조 토큰 차단 |

## 로컬 실행

```bash
# PostgreSQL, Redis 실행 필요
./gradlew bootRun
```

환경변수 미설정 시 기본값으로 실행됩니다.

| 환경변수 | 기본값 |
|----------|--------|
| PGHOST | localhost |
| PGPORT | 5432 |
| PGDATABASE | danshop |
| REDIS_HOST | localhost |
| REDIS_PORT | 6379 |
