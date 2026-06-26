package io.mite88.mite88shop.order.service;

import io.mite88.mite88shop.mite88shop.global.code.ResponseCode;
import io.mite88.mite88shop.mite88shop.global.exception.BusinessException;
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

    @Transactional
    public OrderDescription placeOrder(String username) {
        Member member = memberService.findByUsername(username);
        Cart cart = cartRepository.findFirstByMember(member)
                .orElseThrow(() -> new BusinessException(ResponseCode.EMPTY_CART));

        if (cart.getCartItems().isEmpty()) {
            throw new BusinessException(ResponseCode.EMPTY_CART);
        }

        Order order = Order.createFor(member);
        orderRepository.save(order);

        for (CartItem cartItem : cart.getCartItems()) {
            Product product = cartItem.getProduct();
            if (product.getStock() < cartItem.getQuantity()) {
                throw new BusinessException(ResponseCode.OUT_OF_STOCK);
            }
            product.decreaseStock(cartItem.getQuantity());
            order.addItem(OrderItem.of(order, product, cartItem.getQuantity()));
        }

        cart.getCartItems().clear();

        return OrderMapper.toDescription(order);
    }

    public List<OrderDescription> findMyOrders(String username) {
        Member member = memberService.findByUsername(username);
        return orderRepository.findByMemberOrderByCreatedAtDesc(member).stream()
                .map(OrderMapper::toDescription)
                .toList();
    }

    public OrderDescription findById(String username, Long orderId) {
        Order order = getOrderOrThrow(orderId);
        if (!order.getMember().getUsername().equals(username)) {
            throw new BusinessException(ResponseCode.ORDER_NOT_FOUND);
        }
        return OrderMapper.toDescription(order);
    }

    @Transactional
    public OrderDescription cancel(String username, Long orderId) {
        Order order = getOrderOrThrow(orderId);
        if (!order.getMember().getUsername().equals(username)) {
            throw new BusinessException(ResponseCode.ORDER_NOT_FOUND);
        }
        if (order.getStatus() != OrderStatus.ORDERED) {
            throw new BusinessException(ResponseCode.ORDER_CANCEL_NOT_ALLOWED);
        }
        order.cancel();
        return OrderMapper.toDescription(order);
    }

    private Order getOrderOrThrow(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ResponseCode.ORDER_NOT_FOUND));
    }
}
