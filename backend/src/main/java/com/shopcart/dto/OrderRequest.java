package com.shopcart.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderRequest(
        // danh sach san pham trong don hang
        @NotEmpty(message = "Don hang phai co it nhat 1 san pham")
        List<@Valid OrderItemRequest> items,

        // ma giam gia, co the null hoac rong neu khong ap dung coupon
        String couponCode,

        // phi van chuyen cua don hang
        @NotNull(message = "Phi van chuyen khong duoc rong")
        @Min(value = 0, message = "Phi van chuyen khong duoc am")
        Long shippingFee,

        // dia chi giao hang bat buoc theo yeu cau purchase/checkout
        @NotBlank(message = "Dia chi giao hang khong duoc rong")
        String shippingAddress,

        // phuong thuc thanh toan bat buoc
        @NotBlank(message = "Phuong thuc thanh toan khong duoc rong")
        String paymentMethod
) {
}
