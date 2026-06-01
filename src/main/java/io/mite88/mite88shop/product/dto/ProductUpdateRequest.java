package io.mite88.mite88shop.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ProductUpdateRequest(
        @Schema(description = "상품명") @NotBlank String name,
        @Schema(description = "상품 설명") String description,
        @Schema(description = "가격") @Min(0) int price,
        @Schema(description = "재고") @Min(0) int stock,
        @Schema(description = "카테고리") @NotBlank String category
) {
}
