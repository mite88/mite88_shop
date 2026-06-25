package io.mite88.mite88shop.order.controller;

import io.mite88.mite88shop.order.dto.OrderDescription;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import java.security.Principal;
import java.util.List;

public interface OrderApiDocs {

    @Operation(summary = "주문하기 (장바구니 → 주문)", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = OrderDescription.class)))
    ResponseEntity<OrderDescription> placeOrder(Principal principal);

    @Operation(summary = "내 주문 목록 조회", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation = OrderDescription.class))))
    ResponseEntity<List<OrderDescription>> findMyOrders(Principal principal);

    @Operation(summary = "주문 단건 조회", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = OrderDescription.class)))
    ResponseEntity<OrderDescription> findById(@PathVariable Long orderId, Principal principal);

    @Operation(summary = "주문 취소", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = OrderDescription.class)))
    ResponseEntity<OrderDescription> cancel(@PathVariable Long orderId, Principal principal);
}
