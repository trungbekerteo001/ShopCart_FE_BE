package com.shopcart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CartItemRequest(
        // id cua san pham, duoc gui tu client khi them san pham vao cart
        @NotBlank(message = "Product ID khong duoc rong")
        String productId,

        // so luong san pham, duoc gui tu client khi them san pham vao cart
        @NotNull(message = "So luong khong duoc rong")
        @Min(value = 1, message = "So luong phai lon hon hoac bang 1")
        Integer quantity
) {
}
