# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build and test commands

```bash
# Full clean build (produces build/libs/mite88shop-0.0.1-SNAPSHOT.jar)
./gradlew clean build

# Build without tests
./gradlew build -x test

# Run all tests
./gradlew test

# Run a specific test class
./gradlew test --tests "io.mite88.mite88shop.product.service.ProductServiceTest"

# Run a specific test method
./gradlew test --tests "io.mite88.mite88shop.product.service.ProductServiceTest.save_Success"
```

CI/CD (`.github/workflows/deployment-workflow.yml`) runs on a self-hosted runner: Gradle build → Docker image → `deployment.sh my-app-image my-container 8080`.

## Active profiles

Default in `application.yml`: `mysql,redis,dev`

- `mysql` — MySQL datasource (`application-mysql.yml`)
- `redis` — Redis connection (`application-redis.yml`)
- `h2` — In-memory H2 for local/test use (`application-h2.yml`)
- `google` — Google OAuth2 (`application-google.yml`)
- `dev` — loads `application-dev.properties` with secrets via env vars

For local development without MySQL/Redis, activate `h2` and disable `mysql`.

## Required environment variables

Loaded from `src/main/resources/application-dev.properties` when the `dev` profile is active:

| Variable | Purpose |
|---|---|
| `JWT_APP_KEY` | HMAC secret key for JWT signing |
| `JWT_EXPIRATION` | Access token TTL (ms, default 900000) |
| `JWT_REFRESH_EXPIRATION` | Refresh token TTL (ms, default 604800000) |
| `REDIS_HOST` / `REDIS_PORT` / `REDIS_PASSWORD` | Redis connection |
| `MYSQL_HOST` / `MYSQL_PORT` / `MYSQL_DATABASE` / `MYSQL_USERNAME` / `MYSQL_PASSWORD` | MySQL connection |
| `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` | Google OAuth2 (optional — see Security section) |
| `AI_MODEL_URL` / `AI_MODEL_ENDPOINT` | External AI model service |
| `AI_JOB_QUEUE_KEY` / `AI_JOB_QUEUE_PREFIX` / `AI_JOB_QUEUE_TTL` / `AI_JOB_WORKER_DELAY` | Async job queue config |

## Architecture overview

### Package structure

Root package: `io.mite88.mite88shop`. Domain packages follow a `entity / dto / mapper / repository / service / controller` layer layout:

- `product` — product catalog (ADMIN-only write; public read)
- `cart` — per-member shopping cart (lazy-created on first access)
- `order` — order placement and cancellation
- `posts` — blog posts (member-owned; public read)
- `members` — authentication, JWT, OAuth2, member CRUD
- `view` — Thymeleaf server-rendered pages (`ViewController`)
- `mite88shop.global` — shared concerns: `CommonResponse`, `ResponseCode`, `BusinessException`, `GlobalExceptionHandler`, AI job queue

> Note: the global package has a redundant nesting — the path is `io.mite88.mite88shop.mite88shop.global`.

### Authentication flow

JWT + optional session (`SessionCreationPolicy.IF_REQUIRED`):

1. `TokenAuthenticationFilter` intercepts every request and validates `Authorization: Bearer <token>` via `JwtTokenProvider` (jjwt HMAC).
2. On form-login success, `AuthenticationSuccessHandlerImpl` issues access + refresh tokens.
3. Refresh tokens are stored in Redis via `RefreshTokenService` (uses `StringRedisTemplate`). Token renewal: `POST /api/v1/auth/refresh`.
4. Google OAuth2 is conditionally registered in `SecurityConfig` only when a `ClientRegistrationRepository` bean is present. Flow: `OAuth2SuccessHandler` → `GoogleOAuth2MemberService`.
5. `AuthenticationEntryPointImpl` returns a `CommonResponse` JSON on 401 instead of redirecting.

### Order flow (cart → order)

`OrderService.placeOrder()` is a single `@Transactional` operation that:
1. Loads the authenticated member's `Cart`.
2. Validates stock for each `CartItem` and calls `product.decreaseStock()`.
3. Creates an `Order` with `OrderItem` entries derived from cart contents.
4. Clears `cart.getCartItems()` (cascade removes items via orphanRemoval).

Order cancellation is only allowed when status is `ORDERED`; `CANCELLED` orders cannot be re-cancelled.

### AI job queue

Redis-backed async fire-and-forget:

1. `POST /api/ai/jobs` → `JobService.submitJob()` stores an `AiJob` in Redis (`AI_JOB_QUEUE_PREFIX + jobId`) and pushes the job ID to a Redis list (`AI_JOB_QUEUE_KEY`).
2. `AiJobWorker` polls the queue with a fixed `@Scheduled` delay, calls `AiModelClient.callModel()` via WebFlux `WebClient`, and updates job state (`PENDING → PROCESSING → DONE/FAILED`).
3. `GET /api/ai/jobs/{jobId}` for client polling.
4. `RedisConfig` defines two `RedisTemplate` beans: one serializes `AiJob` objects as Jackson JSON, the other handles plain `String` queue entries. Using the wrong template causes serialization errors.
5. All state transitions are logged to MySQL (`ai_job_log` table) via `AiJobLogRepository`.

### Response convention

All API responses are wrapped in `CommonResponse<T>` using `ResponseCode` enum (HTTP status + code string + Korean message). Throw `BusinessException(ResponseCode.XXX)` for domain errors — `GlobalExceptionHandler` and `PostsExceptionHandler` catch them and serialize to the response envelope.

### Database migrations

Flyway manages schema under `src/main/resources/db/migration/`:
- `V1` — `ai_job_log`
- `V2` — `member`
- `V3` — `posts`
- `V4` — member unique constraints
- `V5` — `product`
- `V6` — `cart`, `cart_item`
- `V7` — `orders`, `order_item`
- `V8` — sample product data

### Testing conventions

Service tests use `@ExtendWith(MockitoExtension.class)` (pure Mockito, no Spring context). Because JPA entities use `@GeneratedValue` IDs with no-args protected constructors, tests use `ReflectionTestUtils.setField(entity, "id", value)` to inject IDs after construction.

Controller tests (`PostApiControllerTest`, etc.) use `@WebMvcTest` with `spring-security-test`.

### API documentation

Swagger UI at `/swagger-ui/index.html` (no auth required). Endpoint annotations are defined on `*ApiDocs` interfaces (e.g. `ProductApiDocs`, `PostApiDocs`) and implemented by the corresponding controllers.
