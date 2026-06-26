package io.mite88.mite88shop.cart.entity;

import io.mite88.mite88shop.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private int quantity;

    public static CartItem of(Cart cart, Product product, int quantity) {
        CartItem item = new CartItem();
        item.cart = cart;
        item.product = product;
        item.quantity = quantity;
        return item;
    }

    public void updateQuantity(int quantity) {
        this.quantity = quantity;
    }
}
