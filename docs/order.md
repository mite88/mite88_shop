# order 도메인

장바구니 기반 주문 생성, 조회, 취소를 담당한다.

## 주요 클래스

| 클래스 | 역할 |
|---|---|
| `OrderService` | 주문 생성·조회·취소 비즈니스 로직 |
| `Order` | 주문 엔티티 — `Member`, `OrderStatus`, `OrderItem` 목록 포함 |
| `OrderItem` | 주문 항목 엔티티 — `Order`, `Product`, 수량, 단가 스냅샷 |
| `OrderStatus` | 주문 상태 enum (`ORDERED`, `CANCELLED`) |
| `OrderApiController` | REST API 엔드포인트 (`/api/orders`) |

## API 엔드포인트

기본 경로: `/api/orders` (모두 인증 필요)

| 메서드 | 경로 | 설명 |
|---|---|---|
| `POST` | `/api/orders` | 주문 생성 (장바구니 기반) |
| `GET` | `/api/orders` | 내 주문 목록 조회 (최신순) |
| `GET` | `/api/orders/{orderId}` | 주문 단건 조회 |
| `PATCH` | `/api/orders/{orderId}/cancel` | 주문 취소 |

## 주문 생성 흐름 (placeOrder)

`@Transactional` 하나로 모든 처리를 완료:

1. 회원의 `Cart` 조회 — 없거나 비어있으면 `EMPTY_CART` throw
2. 각 `CartItem`에 대해:
   - `product.stock < cartItem.quantity`이면 `OUT_OF_STOCK` throw
   - `product.decreaseStock(quantity)` 호출하여 재고 차감
   - `OrderItem` 생성하여 주문에 추가
3. `cart.getCartItems().clear()` — 장바구니 비우기 (`orphanRemoval`로 DB 삭제)

재고 차감은 `@Transactional` 안에서 일어나므로 주문 실패 시 전체 롤백.

## 주문 취소

- `ORDERED` 상태일 때만 취소 가능
- `CANCELLED` 상태에서 재취소 시도 시 `ORDER_CANCEL_NOT_ALLOWED` throw
- 취소 시 재고 복구 없음 (현재 미구현)
- 타인의 주문 조회·취소 시 `ORDER_NOT_FOUND`로 응답 (정보 노출 방지)

## 에러 코드

| 코드 | 상황 |
|---|---|
| `OR001` ORDER_NOT_FOUND | 존재하지 않는 주문 또는 타인 주문 접근 |
| `OR002` ORDER_CANCEL_NOT_ALLOWED | 취소 불가 상태 (이미 취소됨 등) |
| `OR003` EMPTY_CART | 장바구니가 없거나 비어있음 |
| `PR002` OUT_OF_STOCK | 재고 부족 (주문 시 최종 검증) |

## Thymeleaf 페이지

- `GET /orders` → `orders/index` 템플릿 렌더링 (`ViewController`)
