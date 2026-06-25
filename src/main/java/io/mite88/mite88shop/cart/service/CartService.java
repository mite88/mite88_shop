package io.mite88.mite88shop.cart.service;

import io.mite88.mite88shop.mite88shop.global.code.ResponseCode;
import io.mite88.mite88shop.mite88shop.global.exception.BusinessException;
import io.mite88.mite88shop.cart.dto.CartDescription;
import io.mite88.mite88shop.cart.dto.CartItemRequest;
import io.mite88.mite88shop.cart.entity.Cart;
import io.mite88.mite88shop.cart.entity.CartItem;
import io.mite88.mite88shop.cart.mapper.CartMapper;
import io.mite88.mite88shop.cart.repository.CartItemJpaRepository;
import io.mite88.mite88shop.cart.repository.CartJpaRepository;
import io.mite88.mite88shop.members.entity.Member;
import io.mite88.mite88shop.members.service.MemberService;
import io.mite88.mite88shop.product.entity.Product;
import io.mite88.mite88shop.product.service.ProductService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartJpaRepository cartRepository;
    private final CartItemJpaRepository cartItemRepository;
    private final MemberService memberService;
    private final ProductService productService;

    public CartDescription getMyCart(String username) {
        Member member = memberService.findByUsername(username);
        Cart cart = cartRepository.findByMember(member)
                .orElseGet(() -> cartRepository.save(Cart.createFor(member)));
        return CartMapper.toDescription(cart);
    }

    @Transactional
    public CartDescription addItem(String username, CartItemRequest request) {
        Member member = memberService.findByUsername(username);
        Cart cart = cartRepository.findByMember(member)
                .orElseGet(() -> cartRepository.save(Cart.createFor(member)));
        Product product = productService.getProductOrThrow(request.productId());

        cartItemRepository.findByCartAndProduct(cart, product)
                .ifPresentOrElse(
                        existing -> existing.updateQuantity(existing.getQuantity() + request.quantity()),
                        () -> cartItemRepository.save(CartItem.of(cart, product, request.quantity()))
                );

        return CartMapper.toDescription(cartRepository.findByMember(member).orElseThrow());
    }

    @Transactional
    public CartDescription updateItem(String username, Long cartItemId, int quantity) {
        CartItem item = getCartItemOrThrow(cartItemId);
        validateOwner(username, item);
        item.updateQuantity(quantity);
        return CartMapper.toDescription(item.getCart());
    }

    @Transactional
    public CartDescription removeItem(String username, Long cartItemId) {
        CartItem item = getCartItemOrThrow(cartItemId);
        validateOwner(username, item);
        Cart cart = item.getCart();
        cartItemRepository.delete(item);
        return CartMapper.toDescription(cart);
    }

    private CartItem getCartItemOrThrow(Long cartItemId) {
        return cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new BusinessException(ResponseCode.CART_ITEM_NOT_FOUND));
    }

    private void validateOwner(String username, CartItem item) {
        if (!item.getCart().getMember().getUsername().equals(username)) {
            throw new BusinessException(ResponseCode.CART_ITEM_NOT_FOUND);
        }
    }
}
