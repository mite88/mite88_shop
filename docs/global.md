# global 도메인

모든 도메인에서 공유하는 공통 관심사를 담당한다.

## 공통 응답 (CommonResponse)

모든 REST API 응답의 래퍼 클래스:

```json
{
  "success": true,
  "code": "S000",
  "message": "성공",
  "data": { ... }
}
```

정적 팩토리 메서드:
- `CommonResponse.success(data)` — 성공 응답 (S000)
- `CommonResponse.fail(ResponseCode)` — ResponseCode 기본 메시지로 실패 응답
- `CommonResponse.fail(ResponseCode, message)` — 커스텀 메시지로 실패 응답

## 응답 코드 (ResponseCode)

HTTP 상태 + 코드 문자열 + 한국어 메시지로 구성된 enum.

| 분류 | 코드 범위 |
|---|---|
| 성공 | S000 |
| 회원 | M001~M003 |
| 인증 | A001~A006 |
| 공통 | C001 |
| AI 작업 | J001 |
| 게시글 | P001~P002 |
| 상품 | PR001~PR002 |
| 장바구니 | CA001 |
| 주문 | OR001~OR003 |
| 서버 오류 | E001 |

새 에러 코드 추가 시 `ResponseCode` enum에 항목 추가 후 `BusinessException(ResponseCode.XXX)` throw.

## 예외 처리 (GlobalExceptionHandler)

`@ControllerAdvice`로 전역 예외를 처리:

| 예외 | 처리 |
|---|---|
| `BusinessException` | ResponseCode의 HTTP 상태로 `CommonResponse.fail()` 반환 |
| `UsernameNotFoundException` | `USER_NOT_FOUND` (401) |
| `DataIntegrityViolationException` | `DUPLICATE_MEMBER` (409) — DB unique 제약 위반 |
| `Exception` (그 외) | `INTERNAL_SERVER_ERROR` (500) |

## AI 작업 큐

Redis 기반 비동기 fire-and-forget 처리:

### 주요 클래스

| 클래스 | 역할 |
|---|---|
| `JobService` | 작업 등록·조회·상태 갱신 |
| `AiJobWorker` | `@Scheduled`로 폴링하여 큐에서 작업 소비, `AiModelClient` 호출 |
| `AiModelClient` | WebFlux `WebClient`로 외부 AI 모델 HTTP 호출 |
| `AiJob` | Redis 저장용 작업 모델 (record) |
| `AiJobLog` | MySQL 저장용 상태 이력 엔티티 |

### 작업 흐름

```
POST /api/ai/jobs
    └─ JobService.submitJob()
           ├─ Redis: AiJob 객체 저장 (redisTemplate, Jackson JSON)
           ├─ Redis: jobId를 큐 리스트에 push (queueRedisTemplate, String)
           └─ MySQL: ai_job_log에 PENDING 이력 저장

AiJobWorker (@Scheduled)
    └─ 큐에서 jobId pop
           └─ AiModelClient.callModel() (WebClient)
                  ├─ 성공: 상태 DONE, 결과 저장
                  └─ 실패: 상태 FAILED, 에러 메시지 저장

GET /api/ai/jobs/{jobId}
    └─ JobService.getJob() → Redis에서 AiJob 조회
```

### Redis 빈 주입 주의

`JobService`는 `@Qualifier`로 두 개의 `RedisTemplate`을 구분하여 주입:
- `@Qualifier("redisTemplate")` → `RedisTemplate<String, AiJob>` (AiJob 저장)
- `@Qualifier("queueRedisTemplate")` → `RedisTemplate<String, String>` (jobId 큐)

## Swagger 설정 (SwaggerDocumentConfiguration)

`/swagger-ui/index.html`에서 접근 가능 (인증 불필요). `*ApiDocs` 인터페이스에 `@Operation`, `@ApiResponse` 어노테이션을 정의하고, 컨트롤러가 해당 인터페이스를 구현한다.
