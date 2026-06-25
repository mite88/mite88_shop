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

    /**
     * 회원 전용 주문 생성 - 초기 상태는 ORDERED
     */
    public static Order createFor(Member member) {
        Order order = new Order();
        order.member = member;
        order.status = OrderStatus.ORDERED;
        order.createdAt = LocalDateTime.now();
        return order;
    }

    /**
     * 주문 항목 추가 - 추가 시 총액에 소계 반영
     */
    public void addItem(OrderItem item) {
        orderItems.add(item);
        totalPrice += item.getPrice() * item.getQuantity();
    }

    /**
     * 주문 취소 처리
     */
    public void cancel() {
        this.status = OrderStatus.CANCELLED;
    }
}
