package com.shopcart.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopcart.dto.OrderItemRequest;
import com.shopcart.dto.OrderItemResponse;
import com.shopcart.dto.OrderRequest;
import com.shopcart.dto.OrderResponse;
import com.shopcart.entity.OrderStatus;
import com.shopcart.service.OrderService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// su dung WebMvcTest de test API trong OrderController, chi load OrderController va khong load toan bo application context
@WebMvcTest(OrderController.class)
@DisplayName("Order API Integration Tests")
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // mock OrderService de test API trong OrderController ma khong can goi logic trong OrderService
    @MockBean
    private OrderService orderService;

    @Test
    @DisplayName("POST /api/orders - Tao don hang thanh cong")
    void createOrderShouldReturnCreatedResponse() throws Exception {
        // tao request dat hang gui len API
        OrderRequest request = orderRequest();

        // tao response gia lap ma service se tra ve
        OrderResponse mockResponse = orderResponse("ORD-001", OrderStatus.PENDING, "Tao don hang thanh cong");

        // gia lap service tao don hang thanh cong
        when(orderService.createOrder(eq("user01"), any(OrderRequest.class))).thenReturn(mockResponse);

        // goi API POST /api/orders va kiem tra JSON tra ve
        mockMvc.perform(post("/api/orders")
                        .header("X-USER-ID", "user01")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.orderId").value("ORD-001"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalPrice").value(27_500_000));

        // kiem tra service createOrder duoc goi dung 1 lan voi user01
        verify(orderService).createOrder(eq("user01"), any(OrderRequest.class));
    }

    @Test
    @DisplayName("GET /api/orders/{orderId} - Lay thong tin don hang")
    void getOrderByIdShouldReturnOrder() throws Exception {
        // tao response gia lap cho order ORD-001
        OrderResponse mockResponse = orderResponse("ORD-001", OrderStatus.PENDING, "Lay thong tin don hang thanh cong");

        // gia lap service tim thay order
        when(orderService.getOrderById("ORD-001")).thenReturn(mockResponse);

        // goi API GET /api/orders/ORD-001 va kiem tra ket qua
        mockMvc.perform(get("/api/orders/ORD-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.orderId").value("ORD-001"))
                .andExpect(jsonPath("$.items[0].productId").value("P001"));
    }

    @Test
    @DisplayName("PUT /api/orders/{orderId}/cancel - Huy don hang")
    void cancelOrderShouldReturnCanceledOrder() throws Exception {
        // tao response gia lap cho don hang da huy
        OrderResponse mockResponse = orderResponse("ORD-001", OrderStatus.CANCELED, "Huy don hang thanh cong");

        // gia lap service huy don hang thanh cong
        when(orderService.cancelOrder("ORD-001")).thenReturn(mockResponse);

        // goi API huy don hang va kiem tra status CANCELED
        mockMvc.perform(put("/api/orders/ORD-001/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"))
                .andExpect(jsonPath("$.message").value("Huy don hang thanh cong"));
    }

    @Test
    @DisplayName("POST /api/orders - Dia chi rong thi tra ve 400")
    void createOrderWithBlankAddressShouldReturnBadRequest() throws Exception {
        // tao request co dia chi giao hang rong de test validate DTO
        OrderRequest request = new OrderRequest(
                List.of(new OrderItemRequest("P001", 2)),
                "SALE10",
                50_000L,
                "",
                "COD"
        );

        // goi API va mong doi HTTP 400
        mockMvc.perform(post("/api/orders")
                        .header("X-USER-ID", "user01")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    private OrderRequest orderRequest() {
        // ham ho tro tao request dat hang mau
        return new OrderRequest(
                List.of(
                        new OrderItemRequest("P001", 2),
                        new OrderItemRequest("P002", 1)
                ),
                "SALE10",
                50_000L,
                "123 Nguyen Trai, TP.HCM",
                "COD"
        );
    }

    private OrderResponse orderResponse(String orderId, OrderStatus status, String message) {
        // ham ho tro tao response don hang mau
        return new OrderResponse(
                true,
                message,
                orderId,
                status,
                30_500_000L,
                3_050_000L,
                50_000L,
                27_500_000L,
                List.of(
                        new OrderItemResponse("P001", "Laptop Dell", 15_000_000L, 2, 30_000_000L),
                        new OrderItemResponse("P002", "Mouse Logitech", 500_000L, 1, 500_000L)
                )
        );
    }
}
