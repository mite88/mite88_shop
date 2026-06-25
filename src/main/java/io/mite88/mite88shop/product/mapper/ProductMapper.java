package io.mite88.mite88shop.product.mapper;

import io.mite88.mite88shop.product.dto.ProductDescription;
import io.mite88.mite88shop.product.entity.Product;

public class ProductMapper {

    /**
     * Product 엔티티 → ProductDescription 변환 (API 응답용)
     */
    public static ProductDescription toDescription(Product entity) {
        return new ProductDescription(
                entity.getId(), entity.getName(), entity.getDescription(),
                entity.getPrice(), entity.getStock(), entity.getCategory(), entity.getCreatedAt()
        );
    }
}
