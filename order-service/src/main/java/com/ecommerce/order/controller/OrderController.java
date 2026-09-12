package com.ecommerce.order.controller;

import com.ecommerce.order.dto.PlaceOrderRequest;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<Order> placeOrder(@RequestBody PlaceOrderRequest request) {
        return ResponseEntity.ok(orderService.placeOrder(request.userEmail()));
    }

    @GetMapping("/user/{userEmail}")
    public List<Order> getOrdersForUser(@PathVariable String userEmail) {
        return orderService.getOrdersForUser(userEmail);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrder(id));
    }
}
