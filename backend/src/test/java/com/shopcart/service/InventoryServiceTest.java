package com.shopcart.service;

import com.shopcart.dto.InventoryResponse;
import com.shopcart.entity.Product;
import com.shopcart.entity.ProductStatus;
import com.shopcart.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Inventory Service Unit Tests")
@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    @DisplayName("TC_INVENTORY_SERVICE_001: Lay ton kho san pham active va con hang")
    void getInventoryShouldReturnAvailableWhenProductIsActiveAndInStock() {
        Product product = product("P001", "Laptop Dell", 15_000_000L, 10, ProductStatus.ACTIVE);
        when(productRepository.findById("P001")).thenReturn(Optional.of(product));

        InventoryResponse response = inventoryService.getInventory("P001");

        assertTrue(response.success());
        assertEquals("Lay thong tin ton kho thanh cong", response.message());
        assertEquals("P001", response.productId());
        assertEquals("Laptop Dell", response.productName());
        assertEquals(10, response.stock());
        assertTrue(response.available());
        verify(productRepository).findById("P001");
    }

    @Test
    @DisplayName("TC_INVENTORY_SERVICE_002: Lay ton kho san pham inactive thi available false")
    void getInventoryShouldReturnUnavailableWhenProductIsInactive() {
        Product product = product("P004", "Old Monitor", 3_000_000L, 5, ProductStatus.INACTIVE);
        when(productRepository.findById("P004")).thenReturn(Optional.of(product));

        InventoryResponse response = inventoryService.getInventory("P004");

        assertTrue(response.success());
        assertEquals("P004", response.productId());
        assertEquals(5, response.stock());
        assertFalse(response.available());
    }

    @Test
    @DisplayName("TC_INVENTORY_SERVICE_003: Lay ton kho san pham co stock bang 0 thi available false")
    void getInventoryShouldReturnUnavailableWhenStockIsZero() {
        Product product = product("P003", "Keyboard Mechanical", 2_000_000L, 0, ProductStatus.ACTIVE);
        when(productRepository.findById("P003")).thenReturn(Optional.of(product));

        InventoryResponse response = inventoryService.getInventory("P003");

        assertTrue(response.success());
        assertEquals(0, response.stock());
        assertFalse(response.available());
    }

    @Test
    @DisplayName("TC_INVENTORY_SERVICE_004: Lay ton kho san pham co stock null thi available false")
    void getInventoryShouldReturnUnavailableWhenStockIsNull() {
        Product product = product("P005", "Unknown Stock Product", 1_000_000L, null, ProductStatus.ACTIVE);
        when(productRepository.findById("P005")).thenReturn(Optional.of(product));

        InventoryResponse response = inventoryService.getInventory("P005");

        assertTrue(response.success());
        assertEquals("P005", response.productId());
        assertFalse(response.available());
    }

    @Test
    @DisplayName("TC_INVENTORY_SERVICE_005: Kiem tra ton kho du so luong yeu cau")
    void checkAvailabilityShouldReturnAvailableWhenStockIsEnough() {
        Product product = product("P001", "Laptop Dell", 15_000_000L, 10, ProductStatus.ACTIVE);
        when(productRepository.findById("P001")).thenReturn(Optional.of(product));

        InventoryResponse response = inventoryService.checkAvailability("P001", 2);

        assertTrue(response.success());
        assertEquals("San pham con du hang", response.message());
        assertEquals("P001", response.productId());
        assertEquals(10, response.stock());
        assertTrue(response.available());
    }

    @Test
    @DisplayName("TC_INVENTORY_SERVICE_006: Quantity null duoc xem nhu so luong 1")
    void checkAvailabilityShouldUseOneWhenQuantityIsNull() {
        Product product = product("P001", "Laptop Dell", 15_000_000L, 10, ProductStatus.ACTIVE);
        when(productRepository.findById("P001")).thenReturn(Optional.of(product));

        InventoryResponse response = inventoryService.checkAvailability("P001", null);

        assertTrue(response.success());
        assertEquals("San pham con du hang", response.message());
        assertTrue(response.available());
    }

    @Test
    @DisplayName("TC_INVENTORY_SERVICE_007: Quantity nho hon 1 thi khong hop le")
    void checkAvailabilityShouldReturnUnavailableWhenQuantityIsLessThanOne() {
        Product product = product("P001", "Laptop Dell", 15_000_000L, 10, ProductStatus.ACTIVE);
        when(productRepository.findById("P001")).thenReturn(Optional.of(product));

        InventoryResponse response = inventoryService.checkAvailability("P001", 0);

        assertTrue(response.success());
        assertEquals("San pham khong du hang", response.message());
        assertFalse(response.available());
    }

    @Test
    @DisplayName("TC_INVENTORY_SERVICE_008: Ton kho khong du thi available false")
    void checkAvailabilityShouldReturnUnavailableWhenStockIsInsufficient() {
        Product product = product("P001", "Laptop Dell", 15_000_000L, 10, ProductStatus.ACTIVE);
        when(productRepository.findById("P001")).thenReturn(Optional.of(product));

        InventoryResponse response = inventoryService.checkAvailability("P001", 11);

        assertTrue(response.success());
        assertEquals("San pham khong du hang", response.message());
        assertFalse(response.available());
    }

    @Test
    @DisplayName("TC_INVENTORY_SERVICE_009: San pham inactive thi khong cho dat hang")
    void checkAvailabilityShouldReturnUnavailableWhenProductIsInactive() {
        Product product = product("P004", "Old Monitor", 3_000_000L, 5, ProductStatus.INACTIVE);
        when(productRepository.findById("P004")).thenReturn(Optional.of(product));

        InventoryResponse response = inventoryService.checkAvailability("P004", 1);

        assertTrue(response.success());
        assertEquals("San pham khong du hang", response.message());
        assertFalse(response.available());
    }

    @Test
    @DisplayName("TC_INVENTORY_SERVICE_010: Product ID rong thi bao loi")
    void shouldThrowExceptionWhenProductIdIsBlank() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> inventoryService.getInventory("   ")
        );

        assertEquals("Product ID khong duoc rong", exception.getMessage());
    }

    @Test
    @DisplayName("TC_INVENTORY_SERVICE_011: Product ID null thi bao loi")
    void shouldThrowExceptionWhenProductIdIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> inventoryService.checkAvailability(null, 1)
        );

        assertEquals("Product ID khong duoc rong", exception.getMessage());
    }

    @Test
    @DisplayName("TC_INVENTORY_SERVICE_012: San pham khong ton tai thi bao loi")
    void shouldThrowExceptionWhenProductDoesNotExist() {
        when(productRepository.findById("P999")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> inventoryService.getInventory("P999")
        );

        assertEquals("San pham khong ton tai", exception.getMessage());
    }

    private Product product(String id, String name, Long price, Integer stock, ProductStatus status) {
        return new Product(id, name, price, stock, status);
    }
}
