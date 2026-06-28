# qna 도메인 (Q&A 문의게시판)

관리자(ADMIN)가 문의를 작성하고 답변한다. 조회는 공개, 작성·수정·삭제·답변 모두 ADMIN 전용.

## 주요 클래스

| 클래스 | 역할 |
|---|---|
| `QnaService` | 문의 CRUD 비즈니스 로직 (페이징: 10개) |
| `Qna` | 문의 엔티티 — `title`, `content`, `author(Member)`, DB 테이블: `posts` |
| `Comment` | 답변 엔티티 — `post`, `author(Member)`, `content` |
| `QnaController` | REST API 엔드포인트 (`/qna`) |
| `CommentController` | 답변 API 엔드포인트 (`/qna/{id}/comments`) |
| `QnaExceptionHandler` | 레거시 예외 핸들러 (현재 미사용) |

## API 엔드포인트

기본 경로: `/qna`

| 메서드 | 경로 | 인증 | 설명 |
|---|---|---|---|
| `GET` | `/qna?page=0` | 불필요 | 문의 목록 페이징 조회 (10개씩) |
| `GET` | `/qna/{id}` | 불필요 | 문의 단건 조회 |
| `POST` | `/qna` | ADMIN | 문의 작성 |
| `PATCH` | `/qna/{id}` | ADMIN | 문의 수정 |
| `DELETE` | `/qna/{id}` | ADMIN | 문의 삭제 |
| `GET` | `/qna/{id}/comments` | 불필요 | 답변 목록 조회 |
| `POST` | `/qna/{id}/comments` | ADMIN | 답변 등록 |
| `DELETE` | `/qna/{id}/comments/{commentId}` | ADMIN | 답변 삭제 |

## 주요 동작

### 문의 수정 권한
- 수정 요청자의 `username`과 문의 `author.username`을 비교
- 불일치 시 `BusinessException(ResponseCode.UNAUTHORIZED_POST_UPDATE)` throw

### 답변(Comment)
- `isAdminAuthor` 필드 포함 — 프론트에서 "관리자" 뱃지 표시에 사용

### 엔티티 참고사항
`Qna` 엔티티는 `@Table(name = "posts")`로 기존 DB 테이블(`posts`)을 그대로 사용.

### 예외 핸들러 주의사항
`QnaExceptionHandler`는 레거시 코드로 현재 실제로 호출되지 않음. 모든 오류는 `BusinessException` + `GlobalExceptionHandler` 패턴으로 처리.

## 에러 코드

| 코드 | 상황 |
|---|---|
| `P001` UNAUTHORIZED_POST_UPDATE | 본인 글이 아닌 수정 시도 (403) |
| `P002` POST_NOT_FOUND | 존재하지 않는 문의 (404) |
| `C002` COMMENT_NOT_FOUND | 존재하지 않는 답변 (404) |
| `C003` UNAUTHORIZED_COMMENT_DELETE | 본인 답변이 아닌 삭제 시도 (403) |
