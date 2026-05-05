package com.shopcart.dto;

public record CartItemResponse(
        String productId,
        String productName,
        Long price,
        Integer quantity,
        Long lineTotal
) {
}
