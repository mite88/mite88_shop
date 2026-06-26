package io.mite88.mite88shop.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record CartItemDescription(
        Long cartItemId,
        Long productId,
        @Schema(description = "상품명") String productName,
        @Schema(description = "가격") int price,
        @Schema(description = "수량") int quantity,
        @Schema(description = "소계") int subtotal,
        @Schema(description = "현재 재고") int stock
) {
}
