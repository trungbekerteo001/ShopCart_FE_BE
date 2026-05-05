package com.shopcart.controller;

import com.shopcart.dto.InventoryResponse;
import com.shopcart.service.InventoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// test endpoint inventory bang MockMvc va mocked InventoryService
@WebMvcTest(InventoryController.class)
@DisplayName("Inventory API Integration Tests")
class InventoryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // mock service de test rieng tang controller
    @MockBean
    private InventoryService inventoryService;

    @Test
    @DisplayName("GET /api/inventory/{productId} - Lay thong tin ton kho")
    void getInventoryShouldReturnInventory() throws Exception {
        // tao response ton kho gia lap
        InventoryResponse mockResponse = new InventoryResponse(
                true,
                "Lay thong tin ton kho thanh cong",
                "P001",
                "Laptop Dell",
                10,
                true
        );

        // gia lap service tra ve ton kho cua P001
        when(inventoryService.getInventory("P001")).thenReturn(mockResponse);

        // goi API va kiem tra ket qua
        mockMvc.perform(get("/api/inventory/P001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value("P001"))
                .andExpect(jsonPath("$.stock").value(10))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    @DisplayName("GET /api/inventory/{productId}/check - Kiem tra ton kho theo so luong")
    void checkAvailabilityShouldReturnAvailable() throws Exception {
        // tao response kiem tra ton kho gia lap
        InventoryResponse mockResponse = new InventoryResponse(
                true,
                "San pham con du hang",
                "P001",
                "Laptop Dell",
                10,
                true
        );

        // gia lap service check ton kho thanh cong
        when(inventoryService.checkAvailability("P001", 2)).thenReturn(mockResponse);

        // goi API check ton kho voi quantity = 2
        mockMvc.perform(get("/api/inventory/P001/check")
                        .param("quantity", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));
    }
}
