package io.mite88.mite88shop.order.mapper;

import io.mite88.mite88shop.order.dto.OrderDescription;
import io.mite88.mite88shop.order.dto.OrderItemDescription;
import io.mite88.mite88shop.order.entity.Order;
import io.mite88.mite88shop.order.entity.OrderItem;

import java.util.List;

public class OrderMapper {

    /**
     * Order 엔티티 → OrderDescription 변환
     */
    public static OrderDescription toDescription(Order order) {
        List<OrderItemDescription> items = order.getOrderItems().stream()
                .map(OrderMapper::toItemDescription)
                .toList();
        return new OrderDescription(order.getId(), items, order.getTotalPrice(), order.getStatus(), order.getCreatedAt());
    }

    /**
     * OrderItem 엔티티 → OrderItemDescription 변환 (소계 = 단가 × 수량)
     */
    public static OrderItemDescription toItemDescription(OrderItem item) {
        return new OrderItemDescription(
                item.getId(), item.getProduct().getId(), item.getProductName(),
                item.getPrice(), item.getQuantity(), item.getPrice() * item.getQuantity()
        );
    }
}
