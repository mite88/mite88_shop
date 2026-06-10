package io.mite88.mite88shop.cart.controller;

import io.mite88.mite88shop.cart.dto.CartDescription;
import io.mite88.mite88shop.cart.dto.CartItemRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

public interface CartApiDocs {

    @Operation(summary = "내 장바구니 조회", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = CartDescription.class)))
    ResponseEntity<CartDescription> getMyCart(Principal principal);

    @Operation(summary = "장바구니 상품 추가", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = CartDescription.class)))
    ResponseEntity<CartDescription> addItem(@RequestBody CartItemRequest request, Principal principal);

    @Operation(summary = "장바구니 수량 변경", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = CartDescription.class)))
    ResponseEntity<CartDescription> updateItem(@PathVariable Long cartItemId, @RequestParam int quantity, Principal principal);

    @Operation(summary = "장바구니 상품 삭제", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = CartDescription.class)))
    ResponseEntity<CartDescription> removeItem(@PathVariable Long cartItemId, Principal principal);
}
