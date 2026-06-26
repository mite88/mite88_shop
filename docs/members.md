# members 도메인

회원 가입, 로그인, 토큰 관리, Google OAuth2 인증을 담당한다.

## 주요 클래스

| 클래스 | 역할 |
|---|---|
| `MemberService` | 회원 가입, `UserDetailsService` 구현 (Spring Security 인증 연동) |
| `AuthService` | 로그인·로그아웃·토큰 갱신 비즈니스 로직 |
| `JwtTokenProvider` | jjwt 기반 액세스·리프레시 토큰 발급 및 검증 |
| `RefreshTokenService` | Redis에 리프레시 토큰 저장·조회·삭제 (`StringRedisTemplate` 사용) |
| `GoogleOAuth2MemberService` | Google OAuth2 로그인 시 회원 자동 생성·연동 |
| `TokenAuthenticationFilter` | 모든 요청에서 `Authorization: Bearer` 헤더를 파싱하여 SecurityContext에 인증 정보 주입 |
| `SecurityConfig` | Spring Security 필터 체인 설정 (URL 접근 권한, 필터 순서, OAuth2 조건부 등록) |

## API 엔드포인트

기본 경로: `/api/v1/auth`

| 메서드 | 경로 | 인증 | 설명 |
|---|---|---|---|
| `POST` | `/signup` | 불필요 | 회원 가입 |
| `POST` | `/login` | 불필요 | 일반 로그인 → `TokenResponse` 반환 |
| `POST` | `/refresh` | 불필요 | 리프레시 토큰으로 액세스 토큰 재발급 |
| `POST` | `/logout` | 필요 | Redis에서 리프레시 토큰 삭제 |

회원 정보 API: `/members`

| 메서드 | 경로 | 인증 | 설명 |
|---|---|---|---|
| `POST` | `/members` | 불필요 | 회원 생성 (가입과 동일) |
| `GET` | `/members/**` | 필요 | 회원 정보 조회 |

## 인증 흐름

### 일반 로그인
1. `POST /api/v1/auth/login` → `AuthService.login()`
2. `MemberService.loadUserByUsername()`으로 회원 조회
3. `PasswordEncoder.matches()`로 비밀번호 검증
4. `JwtTokenProvider`로 액세스·리프레시 토큰 발급
5. 리프레시 토큰을 Redis에 `username` 키로 저장

### 폼 로그인 (Spring Security 기본)
- `AuthenticationSuccessHandlerImpl`이 로그인 성공 후 토큰 발급 및 JSON 응답

### 토큰 갱신
1. `POST /api/v1/auth/refresh` → `AuthService.refresh()`
2. 리프레시 토큰 서명 검증 → Redis 저장값과 일치 여부 확인 (탈취 감지)
3. 일치하면 새 토큰 쌍 발급

### Google OAuth2
- `google` 프로파일 활성화 시에만 OAuth2 로그인 등록 (빈 조건부 등록)
- 신규 Google 계정: `GoogleOAuth2MemberService`가 자동으로 회원 생성
- 성공 후 `OAuth2SuccessHandler`에서 JWT 토큰 발급

## 회원 역할 (Role)

```
MEMBER  - 일반 회원 (기본값)
ADMIN   - 상품 등록/수정/삭제 권한
```

## 에러 코드

| 코드 | 상황 |
|---|---|
| `M001` DUPLICATE_USERNAME | 이미 사용 중인 아이디 |
| `M002` DUPLICATE_EMAIL | 이미 사용 중인 이메일 |
| `A001` INVALID_PASSWORD | 비밀번호 불일치 |
| `A002` USER_NOT_FOUND | 존재하지 않는 사용자 |
| `A003` INVALID_REFRESH_TOKEN | 유효하지 않은 리프레시 토큰 |
| `A004` REFRESH_TOKEN_EXPIRED | 만료되었거나 로그아웃된 리프레시 토큰 |
| `A005` REFRESH_TOKEN_MISMATCH | Redis 저장값과 불일치 (토큰 탈취 의심) |
| `A006` UNAUTHORIZED_ACCESS | 인증되지 않은 접근 (401) |
