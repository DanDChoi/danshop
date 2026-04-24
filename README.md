## Spring Boot 기반 이커머스 백엔드 사이드 프로젝트
### Claude Code가 전체 커리큘럼을 짜주고 방향성 제시 & 코드 리뷰 & 피드백 해주는 형식으로 진행.

### 전체 커리큘럼
| 단계 | 기능 | 핵심 학습 포인트 |
| --- | --- | --- |
| 1 | 프로젝트 세팅 | 패키지 구조, 레이어 분리, 의존성 설정 |
| 2 | 회원가입/로그인 | Spring Security, JWT, BCrypt, 예외처리 |
| 3 | 상품 CRUD | JPA 기초, 페이지네이션, 검색 필터 |
| 4 | 주문/결제 흐름 | 트랜잭션, 동시성(재고 처리), 낙관적/비관적 락 |
| 5 | 성능 최적화 | N+1 해결, 인덱스 설계, 쿼리 튜닝 |
| 6 | 부가 기능 | Redis 캐싱, 예외처리 전략, API 문서화(Swagger) |

### 기술 스택
- Language: Java21
- Framework: Spring Boot 4.0.5
- ORM: Spring Data JPA (Hibernate)
- Database: PostgreSQL
- Security: Spring Security, JWT (jjwt 0.12.6)
- Built Tool: Gradle
- Etc: Lombok
