package io.mite88.mite88shop.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record CartDescription(
        Long cartId,
        @Schema(description = "장바구니 상품 목록") List<CartItemDescription> items,
        @Schema(description = "총 금액") int totalPrice
) {
}
