package com.shopcart.dto;

public record InventoryResponse(
        boolean success,
        String message,
        String productId,
        String productName,
        Integer stock,
        boolean available
) {
}
