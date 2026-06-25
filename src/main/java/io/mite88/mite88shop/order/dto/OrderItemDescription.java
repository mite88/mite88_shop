package io.mite88.mite88shop.order.dto;

public record OrderItemDescription(
        Long orderItemId,
        Long productId,
        String productName,
        int price,
        int quantity,
        int subtotal
) {
}
