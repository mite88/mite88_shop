# API 명세

모든 응답은 아래 공통 형식으로 래핑됩니다 (일부 예외 표기).

```json
{
  "success": true,
  "code": "S000",
  "message": "성공",
  "data": { ... }
}
```

인증이 필요한 API는 요청 헤더에 `Authorization: Bearer <accessToken>` 을 포함해야 합니다.

---

## 인증 (Auth)

기본 경로: `/api/v1/auth`

---

### 회원가입

`POST /api/v1/auth/signup`

**인증**: 불필요

**요청 Body**
```json
{
  "username": "string (4~10자)",
  "password": "string (8~12자)",
  "email": "string"
}
```

**응답 200**
```json
{
  "success": true,
  "code": "S000",
  "data": {
    "username": "user01",
    "email": "user@example.com",
    "role": "MEMBER",
    "signedAt": "2026-06-26T12:00:00"
  }
}
```

**에러**
| 코드 | 상황 |
|---|---|
| `M001` 409 | 이미 사용 중인 아이디 |
| `M002` 409 | 이미 사용 중인 이메일 |

---

### 로그인

`POST /api/v1/auth/login`

**인증**: 불필요

**요청 Body**
```json
{
  "username": "string",
  "password": "string"
}
```

**응답 200**
```json
{
  "success": true,
  "code": "S000",
  "data": {
    "accessToken": "eyJ...",
    "refreshToken": "eyJ..."
  }
}
```

**에러**
| 코드 | 상황 |
|---|---|
| `A002` 401 | 존재하지 않는 사용자 |
| `A001` 401 | 비밀번호 불일치 |

---

### 액세스 토큰 재발급

`POST /api/v1/auth/refresh`

**인증**: 불필요

**요청 Body**
```json
{
  "refreshToken": "eyJ..."
}
```

**응답 200**: 로그인 응답과 동일 (`TokenResponse`)

**에러**
| 코드 | 상황 |
|---|---|
| `A003` 401 | 유효하지 않은 리프레시 토큰 |
| `A004` 401 | 만료되었거나 로그아웃된 리프레시 토큰 |
| `A005` 401 | Redis 저장값과 불일치 |

---

### 로그아웃

`POST /api/v1/auth/logout`

**인증**: 필요

**요청 Body**: 없음

**응답**: `204 No Content`

---

## 상품 (Product)

기본 경로: `/api/products`

---

### 상품 목록 조회

`GET /api/products`

**인증**: 불필요

**쿼리 파라미터**
| 파라미터 | 필수 | 설명 |
|---|---|---|
| `category` | 선택 | 카테고리 필터 (예: `신발`, `의류`, `가방`, `액세서리`) |

**응답 200**
```json
[
  {
    "id": 1,
    "name": "나이키 운동화",
    "description": "편안한 운동화입니다.",
    "price": 89000,
    "stock": 100,
    "category": "신발",
    "createdAt": "2026-06-01T00:00:00"
  }
]
```

> `stock`이 0이면 재고없음 상태.

---

### 상품 단건 조회

`GET /api/products/{id}`

**인증**: 불필요

**응답 200**: 상품 목록 배열의 단건 형식과 동일

**에러**
| 코드 | 상황 |
|---|---|
| `PR001` 404 | 존재하지 않는 상품 |

---

### 상품 등록

`POST /api/products`

**인증**: ADMIN

**요청 Body**
```json
{
  "name": "나이키 운동화",
  "description": "편안한 운동화입니다.",
  "price": 89000,
  "stock": 100,
  "category": "신발"
}
```

**응답 201**: 등록된 상품 (단건 형식)

---

### 상품 수정

`PATCH /api/products/{id}`

**인증**: ADMIN

**요청 Body**: 등록과 동일 (전체 필드 교체)

**응답 200**: 수정된 상품 (단건 형식)

---

### 상품 삭제

`DELETE /api/products/{id}`

**인증**: ADMIN

**응답**: `204 No Content`

---

## 장바구니 (Cart)

기본 경로: `/api/cart` — 모든 엔드포인트 인증 필요

**공통 응답 형식 (`CartDescription`)**
```json
{
  "cartId": 1,
  "items": [
    {
      "cartItemId": 10,
      "productId": 1,
      "productName": "나이키 운동화",
      "price": 89000,
      "quantity": 2,
      "subtotal": 178000,
      "stock": 100
    }
  ],
  "totalPrice": 178000
}
```

> `stock`: 현재 상품 재고. `quantity > stock`이면 재고 부족 상태.

---

### 장바구니 조회

`GET /api/cart`

장바구니가 없으면 빈 장바구니를 자동 생성하여 반환.

**응답 200**: `CartDescription`

---

### 상품 추가

`POST /api/cart/items`

이미 담긴 상품이면 수량 합산. 재고를 초과하는 요청은 재고 한도까지만 담음.

**요청 Body**
```json
{
  "productId": 1,
  "quantity": 2
}
```

**응답 200**: `CartDescription`

**에러**
| 코드 | 상황 |
|---|---|
| `PR001` 404 | 존재하지 않는 상품 |
| `PR002` 400 | 재고 0 또는 이미 재고 한도까지 담긴 상태 |

---

### 수량 수정

`PATCH /api/cart/items/{cartItemId}?quantity={N}`

**쿼리 파라미터**
| 파라미터 | 필수 | 설명 |
|---|---|---|
| `quantity` | 필수 | 변경할 수량 (1 이상) |

**응답 200**: `CartDescription`

**에러**
| 코드 | 상황 |
|---|---|
| `CA001` 404 | 항목 없음 또는 타인 항목 |
| `PR002` 400 | 재고 초과 수량 |

---

### 상품 삭제

`DELETE /api/cart/items/{cartItemId}`

**응답 200**: `CartDescription` (삭제 후 장바구니)

---

## 주문 (Order)

기본 경로: `/api/orders` — 모든 엔드포인트 인증 필요

**공통 응답 형식 (`OrderDescription`)**
```json
{
  "orderId": 1,
  "items": [
    {
      "orderItemId": 1,
      "productId": 1,
      "productName": "나이키 운동화",
      "price": 89000,
      "quantity": 2,
      "subtotal": 178000
    }
  ],
  "totalPrice": 178000,
  "status": "ORDERED",
  "createdAt": "2026-06-26T12:00:00"
}
```

> `status`: `ORDERED` (주문완료) / `CANCELLED` (취소)

---

### 주문 생성

`POST /api/orders`

장바구니의 모든 상품을 주문. 재고 차감 및 장바구니 초기화가 한 트랜잭션으로 처리됨.

**요청 Body**: 없음

**응답 201**: `OrderDescription`

**에러**
| 코드 | 상황 |
|---|---|
| `OR003` 400 | 장바구니가 없거나 비어있음 |
| `PR002` 400 | 재고 부족 상품 존재 |

---

### 내 주문 목록 조회

`GET /api/orders`

최신순 정렬.

**응답 200**: `OrderDescription[]`

---

### 주문 단건 조회

`GET /api/orders/{orderId}`

타인 주문 접근 시 `OR001` 반환 (정보 노출 방지).

**응답 200**: `OrderDescription`

**에러**
| 코드 | 상황 |
|---|---|
| `OR001` 404 | 존재하지 않는 주문 또는 타인 주문 |

---

### 주문 취소

`PATCH /api/orders/{orderId}/cancel`

`ORDERED` 상태일 때만 취소 가능. 취소 시 주문 항목 수량만큼 재고 즉시 복구.

**요청 Body**: 없음

**응답 200**: `OrderDescription` (status: `CANCELLED`)

**에러**
| 코드 | 상황 |
|---|---|
| `OR001` 404 | 존재하지 않는 주문 또는 타인 주문 |
| `OR002` 400 | 취소 불가 상태 (이미 취소됨) |

---

## Q&A 문의게시판 (QnA)

기본 경로: `/qna`

모든 쓰기(작성·수정·삭제·답변) 는 **ADMIN 전용**, 조회는 공개.

---

### 문의 목록 조회 (페이징)

`GET /qna?page=0`

**인증**: 불필요

**쿼리 파라미터**: `page` (0-indexed, 기본값 0) — 10개씩 최신순

**응답 200**
```json
{
  "content": [
    {
      "id": 1,
      "title": "문의 제목",
      "content": "문의 내용",
      "authorName": "admin",
      "createdAt": "2026-06-26T12:00:00"
    }
  ],
  "totalPages": 3,
  "totalElements": 25,
  "number": 0
}
```

---

### 문의 단건 조회

`GET /qna/{id}`

**인증**: 불필요

**응답 200**: `PostDescription` 단건

**에러**
| 코드 | 상황 |
|---|---|
| `P002` 404 | 존재하지 않는 문의 |

---

### 문의 작성

`POST /qna`

**인증**: ADMIN

**요청 Body**
```json
{
  "title": "string",
  "content": "string"
}
```

**응답 200**: `PostDescription`

---

### 문의 수정

`PATCH /qna/{id}`

**인증**: ADMIN

**요청 Body**: 작성과 동일

**응답 200**: `PostDescription`

**에러**
| 코드 | 상황 |
|---|---|
| `P002` 404 | 존재하지 않는 문의 |
| `P001` 403 | 본인 글이 아님 |

---

### 문의 삭제

`DELETE /qna/{id}`

**인증**: ADMIN

**응답**: `204 No Content`

---

### 답변 목록 조회

`GET /qna/{id}/comments`

**인증**: 불필요

**응답 200**
```json
[
  {
    "id": 1,
    "postId": 1,
    "content": "답변 내용",
    "authorName": "admin",
    "createdAt": "2026-06-26T12:00:00",
    "isAdminAuthor": true
  }
]
```

---

### 답변 등록

`POST /qna/{id}/comments`

**인증**: ADMIN

**요청 Body**
```json
{ "content": "string" }
```

**응답 201**: `CommentDescription`

---

### 답변 삭제

`DELETE /qna/{id}/comments/{commentId}`

**인증**: ADMIN

**응답**: `204 No Content`

---

## AI 작업 큐 (AI Job)

기본 경로: `/api/ai`

---

### 작업 제출

`POST /api/ai/jobs`

**인증**: 불필요

**요청 Body**
```json
{
  "input": "분석할 텍스트 또는 데이터"
}
```

**응답 202 Accepted**
```json
{
  "success": true,
  "code": "S000",
  "data": {
    "jobId": "550e8400-e29b-41d4-a716-446655440000"
  }
}
```

---

### 작업 상태 조회

`GET /api/ai/jobs/{jobId}`

클라이언트가 폴링하여 결과를 확인.

**응답 200**
```json
{
  "success": true,
  "code": "S000",
  "data": {
    "jobId": "550e8400-...",
    "status": "DONE",
    "input": "입력값",
    "result": { ... },
    "errorMessage": null,
    "createdAt": "2026-06-26T12:00:00",
    "updatedAt": "2026-06-26T12:00:05"
  }
}
```

> `status`: `PENDING` → `PROCESSING` → `DONE` / `FAILED`

**에러**
| 코드 | 상황 |
|---|---|
| `J001` 404 | 존재하지 않는 작업 (만료 포함) |

---

## 공통 에러 코드 목록

| 코드 | HTTP | 메시지 |
|---|---|---|
| `S000` | 200 | 성공 |
| `M001` | 409 | 이미 사용 중인 아이디 |
| `M002` | 409 | 이미 사용 중인 이메일 |
| `M003` | 409 | 이미 존재하는 회원 정보 |
| `A001` | 401 | 비밀번호가 일치하지 않습니다 |
| `A002` | 401 | 존재하지 않는 사용자 |
| `A003` | 401 | 유효하지 않은 RefreshToken |
| `A004` | 401 | 만료되었거나 로그아웃된 RefreshToken |
| `A005` | 401 | RefreshToken 불일치 |
| `A006` | 401 | 인증되지 않은 접근 |
| `C001` | 400 | 입력값이 필요합니다 |
| `J001` | 404 | 작업을 찾을 수 없습니다 |
| `P001` | 403 | 본인 글이 아니면 수정할 수 없습니다 |
| `P002` | 404 | 게시글을 찾을 수 없습니다 |
| `PR001` | 404 | 상품을 찾을 수 없습니다 |
| `PR002` | 400 | 재고가 부족합니다 |
| `CA001` | 404 | 장바구니 항목을 찾을 수 없습니다 |
| `OR001` | 404 | 주문을 찾을 수 없습니다 |
| `OR002` | 400 | 취소할 수 없는 주문 상태 |
| `OR003` | 400 | 장바구니가 비어있습니다 |
| `E001` | 500 | 서버 내부 오류 |
