package com.shopcart.dto;

import com.shopcart.entity.OrderStatus;

import java.util.List;

public record OrderResponse(
        boolean success,
        String message,
        String orderId,
        OrderStatus status,
        Long subtotal,
        Long discount,
        Long shippingFee,
        Long totalPrice,
        List<OrderItemResponse> items
) {
}
