# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 빌드 및 테스트 명령어

```bash
# 전체 클린 빌드 (결과물: build/libs/mite88shop-0.0.1-SNAPSHOT.jar)
./gradlew clean build

# 테스트 제외 빌드
./gradlew build -x test

# 전체 테스트 실행
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests "io.mite88.mite88shop.cart.service.CartServiceTest"

# 특정 테스트 메서드 실행
./gradlew test --tests "io.mite88.mite88shop.product.service.ProductServiceTest.save_Success"
```

CI/CD (`.github/workflows/deployment-workflow.yml`)는 self-hosted runner에서 실행: Gradle 빌드 → Docker 이미지 → `deployment.sh my-app-image my-container 8080`.

스택: **Spring Boot 4.1.0**, **Java 25**, Gradle

## 활성 프로파일

`application.yml` 기본값: `mysql,redis,dev`

| 프로파일 | 설정 파일 | 역할 |
|---|---|---|
| `mysql` | `application-mysql.yml` | MySQL 데이터소스; Flyway 활성화; `ddl-auto: none` |
| `redis` | `application-redis.yml` | Redis(Lettuce) 연결 |
| `h2` | `application-h2.yml` | 인메모리 H2 (로컬/테스트용); `ddl-auto: ${JPA_DDL_AUTO:create}` |
| `google` | `application-google.yml` | Google OAuth2 (선택) |
| `dev` | `application-dev.properties` | 로컬 개발용 환경변수 |
| `prod` | `application-prod.properties` | 운영 환경변수 |

MySQL/Redis 없이 로컬 개발 시 `h2` 프로파일 활성화, `mysql` 제거.

> `JPA_DDL_AUTO` 환경변수는 `h2` 프로파일에서만 사용됨. `mysql` 프로파일은 항상 `ddl-auto: none` — 스키마 변경은 Flyway가 단독 관리.

## 필수 환경변수

`application-dev.properties` (개발) 또는 `application-prod.properties` (운영)에 정의:

| 변수 | 용도 |
|---|---|
| `JWT_APP_KEY` | JWT 서명용 HMAC 시크릿 키 |
| `JWT_EXPIRATION` | 액세스 토큰 유효시간(ms, 기본 900000) |
| `JWT_REFRESH_EXPIRATION` | 리프레시 토큰 유효시간(ms, 기본 604800000) |
| `REDIS_HOST` / `REDIS_PORT` / `REDIS_PASSWORD` | Redis 연결 |
| `MYSQL_HOST` / `MYSQL_PORT` / `MYSQL_DATABASE` / `MYSQL_USERNAME` / `MYSQL_PASSWORD` | MySQL 연결 |
| `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` | Google OAuth2 (선택) |
| `AI_MODEL_URL` / `AI_MODEL_ENDPOINT` | 외부 AI 모델 서비스 |
| `AI_JOB_QUEUE_KEY` / `AI_JOB_QUEUE_PREFIX` / `AI_JOB_QUEUE_TTL` / `AI_JOB_WORKER_DELAY` | 비동기 작업 큐 설정 |

## 아키텍처 개요

### 패키지 구조

루트 패키지: `io.mite88.mite88shop`. 도메인 패키지는 `entity / dto / mapper / repository / service / controller` 레이어 구조를 따름.

각 도메인 상세 문서는 `docs/` 폴더 참조.

- `product` — 상품 카탈로그 (쓰기: ADMIN 전용, 읽기: 공개)
- `cart` — 회원별 장바구니 (첫 접근 시 자동 생성)
- `order` — 주문 생성 및 취소
- `posts` — Q&A 문의게시판 (문의 작성: 로그인 회원, 수정·삭제·답변: ADMIN 전용, 조회 공개). API 경로: `/qna`, `/qna/{id}`, `/qna/{id}/comments`. 목록은 10개 페이징(`GET /qna?page=0`). 답변(Comment)에 `isAdminAuthor` 플래그 포함.
- `members` — 인증, JWT, OAuth2, 회원 CRUD. JWT 클레임 키: `username`, `role`, `sid`. `MemberDetails.getAuthorities()`는 `ROLE_` 접두사 포함(`ROLE_ADMIN`, `ROLE_MEMBER`).
- `view` — Thymeleaf 서버 렌더링 페이지 (`ViewController`)
- `global` — 공통 관심사: `CommonResponse`, `ResponseCode`, `BusinessException`, `GlobalExceptionHandler`, AI 작업 큐, Redis 설정

### 인증 흐름

JWT + 선택적 세션 (`SessionCreationPolicy.IF_REQUIRED`):

1. `TokenAuthenticationFilter`가 모든 요청을 가로채 `Authorization: Bearer <token>`을 `JwtTokenProvider`(jjwt HMAC)로 검증.
2. 토큰의 `sid` 클레임을 Redis `session:{username}` 값과 비교 — 불일치 시 인증 거부 (중복 로그인 방지).
3. 폼 로그인 성공 시 `AuthenticationSuccessHandlerImpl`이 액세스·리프레시 토큰 발급 (UUID `sid` 포함).
4. 리프레시 토큰은 `RefreshTokenService`를 통해 Redis에 저장(`StringRedisTemplate` 사용). 갱신: `POST /api/v1/auth/refresh`.
5. Google OAuth2는 `ClientRegistrationRepository` 빈이 존재할 때만 `SecurityConfig`에 조건부 등록. 흐름: `OAuth2SuccessHandler` → `GoogleOAuth2MemberService`.
6. `AuthenticationEntryPointImpl`은 401 시 리다이렉트 대신 `CommonResponse` JSON 반환.

**중복 로그인 방지**: 새 로그인 시 기존 세션 ID를 덮어쓰므로 이전 기기의 토큰은 자동 무효화된다.

**프론트엔드 세션 타임아웃**: `SessionManager`(auth.js)가 비활동 1시간 후 자동 로그아웃, 잔여 10분 이하 시 연장 여부 모달 표시. 세션 만료 시각은 `localStorage.sessionExpiryTime`에 저장. 자세한 내용은 `docs/members.md` 참조.

### URL 접근 권한 (SecurityConfig)

- 공개: `/`, `/login`, `/signup`, Swagger(`/swagger-ui/**`, `/v3/api-docs/**`), OAuth2 리다이렉트, Actuator(`/actuator/**` — Prometheus 스크레이핑 포함)
- 공개 GET: `GET /qna/**`, `GET /products/**`, `GET /api/products/**`
- 인증 필요: `/api/cart/**`, `/api/orders/**`, `/api/v1/auth/logout`
- 인증 필요(MEMBER 이상): `POST /qna`(문의 작성)
- ADMIN 전용: `POST/PATCH/DELETE /api/products/**`, `PATCH/DELETE /qna/**`, `POST/DELETE /qna/{id}/comments`(수정·삭제·답변)
- 나머지 요청은 기본 `permitAll()`

### Redis 빈 구분 (RedisConfig)

세 가지 빈이 존재하며 혼용 시 직렬화 오류 발생:

| 빈 이름 | 타입 | 사용처 |
|---|---|---|
| `redisTemplate` | `RedisTemplate<String, AiJob>` | `JobService` — AiJob 객체를 Jackson JSON으로 저장 |
| `queueRedisTemplate` | `RedisTemplate<String, String>` | `JobService` — 작업 큐에 jobId push/pop |
| `stringRedisTemplate` | `StringRedisTemplate` | `RefreshTokenService` — 리프레시 토큰 문자열 저장 |

### 응답 규약

모든 API 응답은 `CommonResponse<T>`로 래핑하며 `ResponseCode` enum(HTTP 상태 + 코드 문자열 + 한국어 메시지)을 사용.

도메인 오류 발생 시: `BusinessException(ResponseCode.XXX)` throw → `GlobalExceptionHandler`가 응답 직렬화.

**예외**: `posts` 도메인에 레거시 `PostsExceptionHandler`와 `UnAuthorizedUpdateException`이 남아있으나 현재 사용되지 않음. 모든 오류는 `BusinessException` + `GlobalExceptionHandler` 패턴으로 처리.

### 데이터베이스 마이그레이션

Flyway가 `src/main/resources/db/migration/`의 스크립트로 스키마 관리. 스키마 변경 시 다음 번호의 `V{n}__설명.sql` 파일 추가 (기존 파일 수정 절대 금지).

현재 마이그레이션: V1 `ai_job_log`, V2 `member`, V3 `posts`, V4 회원 유니크 제약, V5 `product`, V6 `cart`/`cart_item`, V7 `orders`/`order_item`, V8 샘플 상품 데이터, V9 `cart.member_id` UNIQUE 제약, V10 `cart_item(cart_id, product_id)` UNIQUE 제약, V11 `comment` 테이블, V12 admin 계정 초기 데이터 (아이디: `admin`, 비밀번호: `admin1234`, role: `ADMIN`).

### 테스트 규약

- 서비스 테스트: `@ExtendWith(MockitoExtension.class)` — 순수 Mockito, Spring 컨텍스트 없음.
- 엔티티 ID 주입: `@GeneratedValue` + protected 생성자 구조로 인해 `ReflectionTestUtils.setField(entity, "id", value)` 사용.
- 컨트롤러 테스트: `@WebMvcTest` + `spring-security-test`.

### API 문서

Swagger UI: `/swagger-ui/index.html` (인증 불필요). 엔드포인트 어노테이션은 `*ApiDocs` 인터페이스(`ProductApiDocs`, `PostApiDocs` 등)에 정의하고 컨트롤러가 구현.
