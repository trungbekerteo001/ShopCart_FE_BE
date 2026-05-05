package com.shopcart.dto;

public record OrderItemResponse(
        String productId,
        String productName,
        Long unitPrice,
        Integer quantity,
        Long lineTotal
) {
}
