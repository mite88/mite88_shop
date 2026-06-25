package io.mite88.mite88shop.cart.controller;

import io.mite88.mite88shop.cart.dto.CartDescription;
import io.mite88.mite88shop.cart.dto.CartItemRequest;
import io.mite88.mite88shop.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartApiController implements CartApiDocs {

    private final CartService cartService;

    /**
     * 내 장바구니 조회
     */
    @Override
    @GetMapping
    public ResponseEntity<CartDescription> getMyCart(Principal principal) {
        return ResponseEntity.ok(cartService.getMyCart(principal.getName()));
    }

    /**
     * 장바구니 상품 추가
     */
    @Override
    @PostMapping("/items")
    public ResponseEntity<CartDescription> addItem(@Valid @RequestBody CartItemRequest request, Principal principal) {
        return ResponseEntity.ok(cartService.addItem(principal.getName(), request));
    }

    /**
     * 장바구니 상품 수량 수정
     */
    @Override
    @PatchMapping("/items/{cartItemId}")
    public ResponseEntity<CartDescription> updateItem(@PathVariable Long cartItemId, @RequestParam int quantity, Principal principal) {
        return ResponseEntity.ok(cartService.updateItem(principal.getName(), cartItemId, quantity));
    }

    /**
     * 장바구니 상품 삭제
     */
    @Override
    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<CartDescription> removeItem(@PathVariable Long cartItemId, Principal principal) {
        return ResponseEntity.ok(cartService.removeItem(principal.getName(), cartItemId));
    }
}
