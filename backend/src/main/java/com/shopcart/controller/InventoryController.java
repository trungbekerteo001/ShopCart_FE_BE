package com.shopcart.controller;

import com.shopcart.dto.InventoryResponse;
import com.shopcart.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// controller dung de test endpoint ton kho trong nhom Order/Inventory
@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = "http://localhost:5173")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    // lay thong tin ton kho cua san pham
    @GetMapping("/{productId}")
    public ResponseEntity<InventoryResponse> getInventory(@PathVariable String productId) {
        return ResponseEntity.ok(inventoryService.getInventory(productId));
    }

    // kiem tra san pham co du so luong yeu cau hay khong
    @GetMapping("/{productId}/check")
    public ResponseEntity<InventoryResponse> checkAvailability(
            @PathVariable String productId,
            @RequestParam(defaultValue = "1") Integer quantity
    ) {
        return ResponseEntity.ok(inventoryService.checkAvailability(productId, quantity));
    }
}
