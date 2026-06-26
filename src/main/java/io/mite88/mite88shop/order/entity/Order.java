package io.mite88.mite88shop.order.entity;

import io.mite88.mite88shop.members.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private int totalPrice;
    private LocalDateTime createdAt;

    public static Order createFor(Member member) {
        Order order = new Order();
        order.member = member;
        order.status = OrderStatus.ORDERED;
        order.createdAt = LocalDateTime.now();
        return order;
    }

    public void addItem(OrderItem item) {
        orderItems.add(item);
        totalPrice += item.getPrice() * item.getQuantity();
    }

    public void cancel() {
        this.status = OrderStatus.CANCELLED;
    }
}
