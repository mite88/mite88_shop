package io.mite88.mite88shop.cart.repository;

import io.mite88.mite88shop.cart.entity.Cart;
import io.mite88.mite88shop.members.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartJpaRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByMember(Member member);
}
