package io.mite88.mite88shop.order.controller;

import io.mite88.mite88shop.order.dto.OrderDescription;
import io.mite88.mite88shop.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderApiController implements OrderApiDocs {

    private final OrderService orderService;

    /**
     * 주문 생성 - 장바구니 기반
     */
    @Override
    @PostMapping
    public ResponseEntity<OrderDescription> placeOrder(Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.placeOrder(principal.getName()));
    }

    /**
     * 내 주문 목록 조회
     */
    @Override
    @GetMapping
    public ResponseEntity<List<OrderDescription>> findMyOrders(Principal principal) {
        return ResponseEntity.ok(orderService.findMyOrders(principal.getName()));
    }

    /**
     * 주문 단건 조회
     */
    @Override
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDescription> findById(@PathVariable Long orderId, Principal principal) {
        return ResponseEntity.ok(orderService.findById(principal.getName(), orderId));
    }

    /**
     * 주문 취소
     */
    @Override
    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<OrderDescription> cancel(@PathVariable Long orderId, Principal principal) {
        return ResponseEntity.ok(orderService.cancel(principal.getName(), orderId));
    }
}
