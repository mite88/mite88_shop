package io.mite88.mite88shop.product.mapper;

import io.mite88.mite88shop.product.dto.ProductDescription;
import io.mite88.mite88shop.product.entity.Product;

public class ProductMapper {

    public static ProductDescription toDescription(Product entity) {
        return new ProductDescription(
                entity.getId(), entity.getName(), entity.getDescription(),
                entity.getPrice(), entity.getStock(), entity.getCategory(), entity.getCreatedAt()
        );
    }
}
