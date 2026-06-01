package io.mite88.mite88shop.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ProductSaveRequest(
        @Schema(description = "상품명", example = "나이키 운동화") @NotBlank String name,
        @Schema(description = "상품 설명", example = "편안한 운동화입니다.") String description,
        @Schema(description = "가격", example = "89000") @Min(0) int price,
        @Schema(description = "재고", example = "100") @Min(0) int stock,
        @Schema(description = "카테고리", example = "신발") @NotBlank String category
) {
}
