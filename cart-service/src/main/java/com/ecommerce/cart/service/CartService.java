package com.ecommerce.cart.service;

import com.ecommerce.cart.dto.AddToCartRequest;
import com.ecommerce.cart.model.CartItem;
import com.ecommerce.cart.repository.CartItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;

    public CartService(CartItemRepository cartItemRepository) {
        this.cartItemRepository = cartItemRepository;
    }

    public List<CartItem> getCart(String userEmail) {
        return cartItemRepository.findByUserEmail(userEmail);
    }

    public CartItem addItem(AddToCartRequest request) {
        return cartItemRepository.findByUserEmailAndProductId(request.userEmail(), request.productId())
                .map(existing -> {
                    existing.setQuantity(existing.getQuantity() + request.quantity());
                    return cartItemRepository.save(existing);
                })
                .orElseGet(() -> cartItemRepository.save(new CartItem(
                        request.userEmail(), request.productId(), request.productName(),
                        request.price(), request.quantity())));
    }

    public void removeItem(String userEmail, Long productId) {
        cartItemRepository.deleteByUserEmailAndProductId(userEmail, productId);
    }

    public void clearCart(String userEmail) {
        cartItemRepository.findByUserEmail(userEmail).forEach(item -> cartItemRepository.deleteById(item.getId()));
    }
}
