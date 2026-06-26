package io.mite88.mite88shop.cart.service;

import io.mite88.mite88shop.global.code.ResponseCode;
import io.mite88.mite88shop.global.exception.BusinessException;
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

    /**
     * 내 장바구니 조회 - 없으면 빈 장바구니 자동 생성
     */
    public CartDescription getMyCart(String username) {
        Member member = memberService.findByUsername(username);
        Cart cart = cartRepository.findByMember(member)
                .orElseGet(() -> cartRepository.save(Cart.createFor(member)));
        return CartMapper.toDescription(cart);
    }

    /**
     * 장바구니 상품 추가 - 이미 담긴 상품이면 수량 합산
     */
    @Transactional
    public CartDescription addItem(String username, CartItemRequest request) {
        Member member = memberService.findByUsername(username);
        //장바구니 없으면 자동 생성
        Cart cart = cartRepository.findByMember(member)
                .orElseGet(() -> cartRepository.save(Cart.createFor(member)));
        Product product = productService.getProductOrThrow(request.productId());

        cartItemRepository.findByCartAndProduct(cart, product)
                .ifPresentOrElse(
                        existing -> {
                            // 이미 재고 한도까지 담겨 있으면 추가 불가
                            if (existing.getQuantity() >= product.getStock()) {
                                throw new BusinessException(ResponseCode.OUT_OF_STOCK);
                            }
                            // 재고 한도를 초과하는 요청은 한도까지만 채움
                            int newQuantity = Math.min(existing.getQuantity() + request.quantity(), product.getStock());
                            existing.updateQuantity(newQuantity);
                        },
                        () -> {
                            if (product.getStock() == 0) {
                                throw new BusinessException(ResponseCode.OUT_OF_STOCK);
                            }
                            // 재고보다 많은 수량 요청은 재고 수량으로 제한
                            int quantity = Math.min(request.quantity(), product.getStock());
                            cartItemRepository.save(CartItem.of(cart, product, quantity));
                        }
                );

        return CartMapper.toDescription(cartRepository.findByMember(member).orElseThrow());
    }

    /**
     * 장바구니 상품 수량 수정
     */
    @Transactional
    public CartDescription updateItem(String username, Long cartItemId, int quantity) {
        CartItem item = getCartItemOrThrow(cartItemId);
        validateOwner(username, item);
        if (quantity > item.getProduct().getStock()) {
            throw new BusinessException(ResponseCode.OUT_OF_STOCK);
        }
        item.updateQuantity(quantity);
        return CartMapper.toDescription(item.getCart());
    }

    /**
     * 장바구니 상품 삭제
     */
    @Transactional
    public CartDescription removeItem(String username, Long cartItemId) {
        CartItem item = getCartItemOrThrow(cartItemId);
        validateOwner(username, item);
        Cart cart = item.getCart();
        cartItemRepository.delete(item);
        return CartMapper.toDescription(cart);
    }

    /**
     * 장바구니 항목 조회 - 없으면 BusinessException 던짐
     */
    private CartItem getCartItemOrThrow(Long cartItemId) {
        return cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new BusinessException(ResponseCode.CART_ITEM_NOT_FOUND));
    }

    /**
     * 장바구니 항목 소유자 검증 - 타인 접근 차단 (정보 노출 방지를 위해 NOT_FOUND로 응답)
     */
    private void validateOwner(String username, CartItem item) {
        if (!item.getCart().getMember().getUsername().equals(username)) {
            throw new BusinessException(ResponseCode.CART_ITEM_NOT_FOUND);
        }
    }
}
