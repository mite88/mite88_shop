package io.mite88.mite88shop.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

public record CartItemRequest(
        @Schema(description = "상품 ID") Long productId,
        @Schema(description = "수량", example = "1") @Min(1) int quantity
) {
}
