package io.mite88.mite88shop.order.dto;

import io.mite88.mite88shop.order.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

public record OrderDescription(
        Long orderId,
        List<OrderItemDescription> items,
        int totalPrice,
        OrderStatus status,
        LocalDateTime createdAt
) {
}
