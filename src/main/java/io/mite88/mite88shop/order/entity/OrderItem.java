package io.mite88.mite88shop.order.entity;

import io.mite88.mite88shop.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    //주문 당시 상품명/가격을 스냅샷으로 저장 (이후 상품 변경 이력과 무관하게 유지)
    private String productName;
    private int price;
    private int quantity;

    /**
     * 주문 항목 생성 - 현재 상품명·가격을 스냅샷으로 기록
     */
    public static OrderItem of(Order order, Product product, int quantity) {
        OrderItem item = new OrderItem();
        item.order = order;
        item.product = product;
        item.productName = product.getName();
        item.price = product.getPrice();
        item.quantity = quantity;
        return item;
    }
}
