package com.shopcart.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopcart.dto.CartItemRequest;
import com.shopcart.dto.CartItemResponse;
import com.shopcart.dto.CartResponse;
import com.shopcart.service.CartService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// su dung WebMvcTest de test API trong CartController, chi load CartController va khong load toan bo application context
@WebMvcTest(CartController.class)
@DisplayName("Cart API Integration Tests")
class CartControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // mock CartService de test API trong CartController ma khong can goi logic trong CartService
    @MockBean
    private CartService cartService;

    @Test
    @DisplayName("POST /api/cart/add - Them san pham vao gio hang")
    void addToCartShouldReturnSuccessResponse() throws Exception {
        CartItemRequest request = new CartItemRequest("P001", 2);
        CartResponse mockResponse = new CartResponse(
                true,
                "Them vao gio hang thanh cong",
                30_000_000L,
                List.of(new CartItemResponse("P001", "Laptop Dell", 15_000_000L, 2, 30_000_000L))
        );

        when(cartService.addToCart(eq("user01"), any(CartItemRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/cart/add")
                        .header("X-USER-ID", "user01")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Them vao gio hang thanh cong"))
                .andExpect(jsonPath("$.cartTotal").value(30_000_000));

        verify(cartService).addToCart(eq("user01"), any(CartItemRequest.class));
    }

    @Test
    @DisplayName("GET /api/cart - Lay thong tin gio hang")
    void getCartShouldReturnCart() throws Exception {
        CartResponse mockResponse = new CartResponse(
                true,
                "Lay gio hang thanh cong",
                30_000_000L,
                List.of(new CartItemResponse("P001", "Laptop Dell", 15_000_000L, 2, 30_000_000L))
        );

        when(cartService.getCart("user01")).thenReturn(mockResponse);

        mockMvc.perform(get("/api/cart")
                        .header("X-USER-ID", "user01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.items[0].productId").value("P001"));
    }

    @Test
    @DisplayName("PUT /api/cart/update - Cap nhat so luong")
    void updateQuantityShouldReturnUpdatedCart() throws Exception {
        CartItemRequest request = new CartItemRequest("P001", 3);
        CartResponse mockResponse = new CartResponse(
                true,
                "Cap nhat so luong thanh cong",
                45_000_000L,
                List.of(new CartItemResponse("P001", "Laptop Dell", 15_000_000L, 3, 45_000_000L))
        );

        when(cartService.updateQuantity(eq("user01"), any(CartItemRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(put("/api/cart/update")
                        .header("X-USER-ID", "user01")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartTotal").value(45_000_000));
    }

    @Test
    @DisplayName("DELETE /api/cart/remove/{productId} - Xoa san pham khoi gio")
    void removeFromCartShouldReturnEmptyCart() throws Exception {
        CartResponse mockResponse = new CartResponse(
                true,
                "Xoa san pham khoi gio hang thanh cong",
                0L,
                List.of()
        );

        when(cartService.removeFromCart("user01", "P001")).thenReturn(mockResponse);

        mockMvc.perform(delete("/api/cart/remove/P001")
                        .header("X-USER-ID", "user01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartTotal").value(0));
    }

    @Test
    @DisplayName("POST /api/cart/add - So luong bang 0 thi tra ve 400")
    void addToCartWithInvalidQuantityShouldReturnBadRequest() throws Exception {
        CartItemRequest request = new CartItemRequest("P001", 0);

        mockMvc.perform(post("/api/cart/add")
                        .header("X-USER-ID", "user01")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
