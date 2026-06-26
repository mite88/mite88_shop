package io.mite88.mite88shop.cart.mapper;

import io.mite88.mite88shop.cart.dto.CartDescription;
import io.mite88.mite88shop.cart.dto.CartItemDescription;
import io.mite88.mite88shop.cart.entity.Cart;
import io.mite88.mite88shop.cart.entity.CartItem;

import java.util.List;

public class CartMapper {

    /**
     * Cart 엔티티 → CartDescription 변환 (항목 소계 합산하여 총액 계산)
     */
    public static CartDescription toDescription(Cart cart) {
        List<CartItemDescription> items = cart.getCartItems().stream()
                .map(CartMapper::toItemDescription)
                .toList();
        //전체 항목 소계 합산
        int total = items.stream().mapToInt(CartItemDescription::subtotal).sum();
        return new CartDescription(cart.getId(), items, total);
    }

    /**
     * CartItem 엔티티 → CartItemDescription 변환 (소계 = 단가 × 수량)
     */
    public static CartItemDescription toItemDescription(CartItem item) {
        return new CartItemDescription(
                item.getId(), item.getProduct().getId(), item.getProduct().getName(),
                item.getProduct().getPrice(), item.getQuantity(),
                item.getProduct().getPrice() * item.getQuantity(),
                item.getProduct().getStock()
        );
    }
}
