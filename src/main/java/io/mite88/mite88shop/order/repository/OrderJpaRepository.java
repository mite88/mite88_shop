package io.mite88.mite88shop.order.repository;

import io.mite88.mite88shop.members.entity.Member;
import io.mite88.mite88shop.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderJpaRepository extends JpaRepository<Order, Long> {
    List<Order> findByMemberOrderByCreatedAtDesc(Member member);
}
