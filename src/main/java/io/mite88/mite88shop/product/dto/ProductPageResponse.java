package io.mite88.mite88shop.product.dto;

import java.util.List;

public record ProductPageResponse(
        List<ProductDescription> content,
        int totalPages,
        long totalElements,
        int number
) {}
