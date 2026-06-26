# product 도메인

상품 카탈로그를 관리한다. 조회는 누구나 가능하고, 등록·수정·삭제는 ADMIN 역할만 가능하다.

## 주요 클래스

| 클래스 | 역할 |
|---|---|
| `ProductService` | 상품 CRUD 비즈니스 로직, `getProductOrThrow()` 제공 (다른 도메인에서 재사용) |
| `ProductApiController` | REST API 엔드포인트 (`/api/products`) |
| `Product` | 상품 엔티티 — `name`, `description`, `price`, `stock`, `category` |

## API 엔드포인트

기본 경로: `/api/products`

| 메서드 | 경로 | 인증 | 설명 |
|---|---|---|---|
| `POST` | `/api/products` | ADMIN | 상품 등록 |
| `GET` | `/api/products` | 불필요 | 전체 목록 조회 (또는 카테고리별) |
| `GET` | `/api/products/{id}` | 불필요 | 상품 단건 조회 |
| `PATCH` | `/api/products/{id}` | ADMIN | 상품 수정 |
| `DELETE` | `/api/products/{id}` | ADMIN | 상품 삭제 |

카테고리 필터: `GET /api/products?category=신발`

## 재고 관리

- `Product.stock` 필드로 재고 수량 관리
- `product.decreaseStock(quantity)` — 주문 시 호출하여 재고 차감
- 재고 0인 상품은 장바구니 담기/수량 증가 불가 (CartService에서 사전 차단)
- 주문 시 재고 부족 최종 검증 (OrderService.placeOrder)

## 다른 도메인과의 연관

- `cart` 도메인: 장바구니 담기 시 `ProductService.getProductOrThrow()`로 상품 존재 확인 및 재고 검증
- `order` 도메인: 주문 생성 시 재고 차감 (`product.decreaseStock()`) 및 재고 부족 검증

## 에러 코드

| 코드 | 상황 |
|---|---|
| `PR001` PRODUCT_NOT_FOUND | 존재하지 않는 상품 |
| `PR002` OUT_OF_STOCK | 재고 부족 (장바구니 담기·수량 변경·주문 시 모두 검증) |

## Thymeleaf 페이지

- `GET /products/{id}` → `products/detail` 템플릿 렌더링 (`ViewController`)
- 상품 목록: `GET /` (메인 페이지에서 표시)
