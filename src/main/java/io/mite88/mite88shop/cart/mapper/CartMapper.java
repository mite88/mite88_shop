package io.mite88.mite88shop.cart.mapper;

import io.mite88.mite88shop.cart.dto.CartDescription;
import io.mite88.mite88shop.cart.dto.CartItemDescription;
import io.mite88.mite88shop.cart.entity.Cart;
import io.mite88.mite88shop.cart.entity.CartItem;

import java.util.List;

public class CartMapper {

    public static CartDescription toDescription(Cart cart) {
        List<CartItemDescription> items = cart.getCartItems().stream()
                .map(CartMapper::toItemDescription)
                .toList();
        int total = items.stream().mapToInt(CartItemDescription::subtotal).sum();
        return new CartDescription(cart.getId(), items, total);
    }

    public static CartItemDescription toItemDescription(CartItem item) {
        return new CartItemDescription(
                item.getId(), item.getProduct().getId(), item.getProduct().getName(),
                item.getProduct().getPrice(), item.getQuantity(),
                item.getProduct().getPrice() * item.getQuantity()
        );
    }
}
