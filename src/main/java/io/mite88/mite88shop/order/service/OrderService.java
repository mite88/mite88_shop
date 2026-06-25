package io.mite88.mite88shop.order.service;

import io.mite88.mite88shop.global.code.ResponseCode;
import io.mite88.mite88shop.global.exception.BusinessException;
import io.mite88.mite88shop.cart.entity.Cart;
import io.mite88.mite88shop.cart.entity.CartItem;
import io.mite88.mite88shop.cart.repository.CartJpaRepository;
import io.mite88.mite88shop.members.entity.Member;
import io.mite88.mite88shop.members.service.MemberService;
import io.mite88.mite88shop.order.dto.OrderDescription;
import io.mite88.mite88shop.order.entity.Order;
import io.mite88.mite88shop.order.entity.OrderItem;
import io.mite88.mite88shop.order.entity.OrderStatus;
import io.mite88.mite88shop.order.mapper.OrderMapper;
import io.mite88.mite88shop.order.repository.OrderJpaRepository;
import io.mite88.mite88shop.product.entity.Product;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderJpaRepository orderRepository;
    private final CartJpaRepository cartRepository;
    private final MemberService memberService;

    /**
     * 장바구니 기반 주문 생성 - 재고 차감 및 장바구니 초기화까지 한 트랜잭션으로 처리
     */
    @Transactional
    public OrderDescription placeOrder(String username) {
        Member member = memberService.findByUsername(username);
        Cart cart = cartRepository.findByMember(member)
                .orElseThrow(() -> new BusinessException(ResponseCode.EMPTY_CART));

        if (cart.getCartItems().isEmpty()) {
            throw new BusinessException(ResponseCode.EMPTY_CART);
        }

        Order order = Order.createFor(member);
        orderRepository.save(order);

        for (CartItem cartItem : cart.getCartItems()) {
            Product product = cartItem.getProduct();
            //재고 부족 시 주문 불가
            if (product.getStock() < cartItem.getQuantity()) {
                throw new BusinessException(ResponseCode.OUT_OF_STOCK);
            }
            product.decreaseStock(cartItem.getQuantity());
            order.addItem(OrderItem.of(order, product, cartItem.getQuantity()));
        }

        //주문 완료 후 장바구니 비우기 (orphanRemoval로 DB 삭제)
        cart.getCartItems().clear();

        return OrderMapper.toDescription(order);
    }

    /**
     * 내 주문 목록 조회 - 최신순 정렬
     */
    public List<OrderDescription> findMyOrders(String username) {
        Member member = memberService.findByUsername(username);
        return orderRepository.findByMemberOrderByCreatedAtDesc(member).stream()
                .map(OrderMapper::toDescription)
                .toList();
    }

    /**
     * 주문 단건 조회 - 본인 주문이 아닌 경우 NOT_FOUND로 응답 (정보 노출 방지)
     */
    public OrderDescription findById(String username, Long orderId) {
        Order order = getOrderOrThrow(orderId);
        if (!order.getMember().getUsername().equals(username)) {
            throw new BusinessException(ResponseCode.ORDER_NOT_FOUND);
        }
        return OrderMapper.toDescription(order);
    }

    /**
     * 주문 취소 - ORDERED 상태일 때만 가능
     */
    @Transactional
    public OrderDescription cancel(String username, Long orderId) {
        Order order = getOrderOrThrow(orderId);
        if (!order.getMember().getUsername().equals(username)) {
            throw new BusinessException(ResponseCode.ORDER_NOT_FOUND);
        }
        //ORDERED 외 상태(이미 취소됨 등)는 취소 불가
        if (order.getStatus() != OrderStatus.ORDERED) {
            throw new BusinessException(ResponseCode.ORDER_CANCEL_NOT_ALLOWED);
        }
        order.cancel();
        return OrderMapper.toDescription(order);
    }

    /**
     * 주문 조회 - 없으면 BusinessException 던짐
     */
    private Order getOrderOrThrow(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ResponseCode.ORDER_NOT_FOUND));
    }
}
