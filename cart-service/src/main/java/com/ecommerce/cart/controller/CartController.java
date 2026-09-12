package com.ecommerce.cart.controller;

import com.ecommerce.cart.dto.AddToCartRequest;
import com.ecommerce.cart.model.CartItem;
import com.ecommerce.cart.service.CartService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/{userEmail}")
    public List<CartItem> getCart(@PathVariable String userEmail) {
        return cartService.getCart(userEmail);
    }

    @PostMapping
    public CartItem addItem(@RequestBody AddToCartRequest request) {
        return cartService.addItem(request);
    }

    @DeleteMapping("/{userEmail}/{productId}")
    public void removeItem(@PathVariable String userEmail, @PathVariable Long productId) {
        cartService.removeItem(userEmail, productId);
    }

    @DeleteMapping("/{userEmail}")
    public void clearCart(@PathVariable String userEmail) {
        cartService.clearCart(userEmail);
    }
}
