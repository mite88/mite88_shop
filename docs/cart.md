# cart 도메인

회원별 장바구니를 관리한다. 첫 조회 시 자동으로 생성된다.

## 주요 클래스

| 클래스 | 역할 |
|---|---|
| `CartService` | 장바구니 조회·상품 추가·수량 수정·상품 삭제 |
| `Cart` | 장바구니 엔티티 — `Member`와 1:1 |
| `CartItem` | 장바구니 항목 엔티티 — `Cart`, `Product`와 ManyToOne |
| `CartApiController` | REST API 엔드포인트 (`/api/cart`) |

## API 엔드포인트

기본 경로: `/api/cart` (모두 인증 필요)

| 메서드 | 경로 | 설명 |
|---|---|---|
| `GET` | `/api/cart` | 내 장바구니 조회 (없으면 자동 생성) |
| `POST` | `/api/cart/items` | 상품 추가 |
| `PATCH` | `/api/cart/items/{cartItemId}` | 수량 수정 (`?quantity=N`) |
| `DELETE` | `/api/cart/items/{cartItemId}` | 상품 삭제 |

## 주요 동작

### 장바구니 자동 생성
- `getMyCart()` 또는 `addItem()` 호출 시 장바구니가 없으면 자동으로 생성하여 저장

### 상품 추가 (addItem)
- 이미 담긴 상품이면 수량 합산 (새 행 추가 안 함)
- 재고 초과 검증: 합산 후 수량이 `product.stock`을 넘으면 `OUT_OF_STOCK` throw
- 재고 0인 상품은 어떤 수량도 담을 수 없음

### 수량 수정 (updateItem)
- 변경 수량이 `product.stock`을 넘으면 `OUT_OF_STOCK` throw
- 타인의 장바구니 항목 접근 시 `CART_ITEM_NOT_FOUND`로 응답 (정보 노출 방지)

### 상품 삭제 (removeItem)
- 타인의 장바구니 항목 접근 시 `CART_ITEM_NOT_FOUND`로 응답

## 엔티티 관계

```
Member (1) ──── (1) Cart (1) ──── (N) CartItem (N) ──── (1) Product
```

- `Cart` → `CartItem`: `orphanRemoval = true` — Cart에서 항목을 제거하면 DB에서도 삭제
- 주문 완료 후 `cart.getCartItems().clear()`로 장바구니 초기화 (OrderService에서 호출)

## 에러 코드

| 코드 | 상황 |
|---|---|
| `CA001` CART_ITEM_NOT_FOUND | 장바구니 항목 없음 또는 타인 항목 접근 |
| `PR002` OUT_OF_STOCK | 재고 부족 (추가·수량 수정 시 검증) |

## Thymeleaf 페이지

- `GET /cart` → `cart/index` 템플릿 렌더링 (`ViewController`)
