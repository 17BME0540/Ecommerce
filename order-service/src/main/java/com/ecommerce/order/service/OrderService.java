package com.ecommerce.order.service;

import com.ecommerce.order.client.CartClient;
import com.ecommerce.order.client.CartItemDto;
import com.ecommerce.order.client.ProductClient;
import com.ecommerce.order.client.ProductDto;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderItem;
import com.ecommerce.order.model.OrderStatus;
import com.ecommerce.order.repository.OrderRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartClient cartClient;
    private final ProductClient productClient;

    public OrderService(OrderRepository orderRepository, CartClient cartClient, ProductClient productClient) {
        this.orderRepository = orderRepository;
        this.cartClient = cartClient;
        this.productClient = productClient;
    }

    /**
     * Places an order from the user's current cart:
     * 1. Fetch cart items from cart-service (Feign).
     * 2. Validate stock and decrement it in product-service for each item.
     * 3. Persist the order, then clear the cart.
     * Wrapped with a circuit breaker so a flaky product-service degrades gracefully
     * instead of cascading failures across the order flow.
     */
    @CircuitBreaker(name = "productService", fallbackMethod = "placeOrderFallback")
    public Order placeOrder(String userEmail) {
        List<CartItemDto> cartItems = cartClient.getCart(userEmail);
        if (cartItems.isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        Order order = new Order(userEmail);
        BigDecimal total = BigDecimal.ZERO;

        for (CartItemDto item : cartItems) {
            ProductDto product = productClient.getProduct(item.productId());
            if (product.stockQuantity() < item.quantity()) {
                throw new IllegalStateException("Insufficient stock for " + product.name());
            }
            productClient.adjustStock(item.productId(), -item.quantity());
            order.addItem(new OrderItem(item.productId(), item.productName(), item.price(), item.quantity()));
            total = total.add(item.price().multiply(BigDecimal.valueOf(item.quantity())));
        }

        order.setTotalAmount(total);
        order.setStatus(OrderStatus.CONFIRMED);
        Order saved = orderRepository.save(order);

        cartClient.clearCart(userEmail);
        return saved;
    }

    private Order placeOrderFallback(String userEmail, Throwable t) {
        Order failed = new Order(userEmail);
        failed.setStatus(OrderStatus.FAILED);
        failed.setTotalAmount(BigDecimal.ZERO);
        return orderRepository.save(failed);
    }

    public List<Order> getOrdersForUser(String userEmail) {
        return orderRepository.findByUserEmail(userEmail);
    }

    public Order getOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new java.util.NoSuchElementException("Order not found: " + id));
    }
}
