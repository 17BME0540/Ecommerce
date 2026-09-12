package com.ecommerce.cart.dto;

import java.math.BigDecimal;

public record AddToCartRequest(String userEmail, Long productId, String productName, BigDecimal price, int quantity) {}
