package com.shopcart.dto;

import java.util.List;

public record CartResponse(
        boolean success,
        String message,
        Long cartTotal,
        List<CartItemResponse> items    // danh sach san pham trong cart
) {
}
