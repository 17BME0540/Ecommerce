package com.ecommerce.order.client;

import java.math.BigDecimal;

public record CartItemDto(Long id, String userEmail, Long productId, String productName,
                           BigDecimal price, int quantity) {}
