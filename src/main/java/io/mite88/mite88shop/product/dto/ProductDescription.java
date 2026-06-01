package io.mite88.mite88shop.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record ProductDescription(
        Long id,
        @Schema(description = "상품명") String name,
        @Schema(description = "상품 설명") String description,
        @Schema(description = "가격") int price,
        @Schema(description = "재고") int stock,
        @Schema(description = "카테고리") String category,
        @Schema(description = "등록일") LocalDateTime createdAt
) {
}
