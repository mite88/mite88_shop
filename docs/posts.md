# posts 도메인

회원이 작성하는 블로그 게시글을 관리한다. 조회는 공개, 작성·수정·삭제는 로그인 필요.

## 주요 클래스

| 클래스 | 역할 |
|---|---|
| `PostService` | 게시글 CRUD 비즈니스 로직 |
| `Posts` | 게시글 엔티티 — `title`, `content`, `author(Member)` |
| `PostApiController` | REST API 엔드포인트 (`/posts`) |
| `PostsExceptionHandler` | posts 도메인 전용 예외 핸들러 (레거시) |

## API 엔드포인트

기본 경로: `/posts`

| 메서드 | 경로 | 인증 | 설명 |
|---|---|---|---|
| `GET` | `/posts` | 불필요 | 전체 게시글 목록 조회 |
| `GET` | `/posts/{id}` | 불필요 | 게시글 단건 조회 |
| `POST` | `/posts` | 필요 | 게시글 작성 |
| `PATCH` | `/posts/{id}` | 필요 | 게시글 수정 (작성자 본인만) |
| `DELETE` | `/posts/{id}` | 필요 | 게시글 삭제 |

## 주요 동작

### 게시글 수정 권한
- 수정 요청자의 `username`과 게시글 `author.username`을 비교
- 불일치 시 `BusinessException(ResponseCode.UNAUTHORIZED_POST_UPDATE)` throw

### 예외 핸들러 주의사항

`PostsExceptionHandler`는 레거시 코드로 `UnAuthorizedUpdateException`을 `String` 원문으로 반환한다 (`CommonResponse` 미사용). 현재 `PostService`는 이미 `BusinessException`을 사용하므로 `UnAuthorizedUpdateException`은 실제로 발생하지 않는다. 신규 기능은 `BusinessException` + `GlobalExceptionHandler` 패턴을 사용할 것.

## 에러 코드

| 코드 | 상황 |
|---|---|
| `P001` UNAUTHORIZED_POST_UPDATE | 본인 글이 아닌 수정 시도 (403) |
| `P002` POST_NOT_FOUND | 존재하지 않는 게시글 (404) |
