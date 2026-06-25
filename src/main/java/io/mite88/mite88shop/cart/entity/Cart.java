package io.mite88.mite88shop.cart.entity;

import io.mite88.mite88shop.members.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //회원 1명당 장바구니 1개
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    //항목 삭제 시 orphanRemoval로 DB에서도 자동 삭제
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> cartItems = new ArrayList<>();

    /**
     * 회원 전용 장바구니 생성
     */
    public static Cart createFor(Member member) {
        Cart cart = new Cart();
        cart.member = member;
        return cart;
    }
}
