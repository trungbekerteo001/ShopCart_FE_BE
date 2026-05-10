package com.shopcart.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Entity Coverage Tests")
class EntityCoverageTest {

    @Test
    @DisplayName("CartItem constructors, getters, setters va line total")
    void cartItemShouldExposeAllFieldsAndCalculateLineTotal() {
        CartItem emptyItem = new CartItem();
        emptyItem.setId(1L);
        emptyItem.setUserId("user01");
        emptyItem.setProductId("P001");
        emptyItem.setProductName("Laptop Dell");
        emptyItem.setPrice(15_000_000L);
        emptyItem.setQuantity(2);

        assertEquals(1L, emptyItem.getId());
        assertEquals("user01", emptyItem.getUserId());
        assertEquals("P001", emptyItem.getProductId());
        assertEquals("Laptop Dell", emptyItem.getProductName());
        assertEquals(15_000_000L, emptyItem.getPrice());
        assertEquals(2, emptyItem.getQuantity());
        assertEquals(30_000_000L, emptyItem.getLineTotal());

        CartItem constructorItem = new CartItem("user02", "P002", "Mouse Logitech", 500_000L, 3);
        assertEquals("user02", constructorItem.getUserId());
        assertEquals("P002", constructorItem.getProductId());
        assertEquals("Mouse Logitech", constructorItem.getProductName());
        assertEquals(1_500_000L, constructorItem.getLineTotal());

        constructorItem.setPrice(null);
        assertEquals(0L, constructorItem.getLineTotal());

        constructorItem.setPrice(500_000L);
        constructorItem.setQuantity(null);
        assertEquals(0L, constructorItem.getLineTotal());
    }

    @Test
    @DisplayName("Product constructors, getters va setters")
    void productShouldExposeAllFields() {
        Product emptyProduct = new Product();
        emptyProduct.setId("P001");
        emptyProduct.setName("Laptop Dell");
        emptyProduct.setPrice(15_000_000L);
        emptyProduct.setStock(10);
        emptyProduct.setStatus(ProductStatus.ACTIVE);

        assertEquals("P001", emptyProduct.getId());
        assertEquals("Laptop Dell", emptyProduct.getName());
        assertEquals(15_000_000L, emptyProduct.getPrice());
        assertEquals(10, emptyProduct.getStock());
        assertEquals(ProductStatus.ACTIVE, emptyProduct.getStatus());

        Product constructorProduct = new Product("P004", "Old Monitor", 3_000_000L, 5, ProductStatus.INACTIVE);
        assertEquals("P004", constructorProduct.getId());
        assertEquals("Old Monitor", constructorProduct.getName());
        assertEquals(3_000_000L, constructorProduct.getPrice());
        assertEquals(5, constructorProduct.getStock());
        assertEquals(ProductStatus.INACTIVE, constructorProduct.getStatus());
    }

    @Test
    @DisplayName("OrderItem constructors, getters va setters")
    void orderItemShouldExposeAllFields() {
        Order order = new Order();
        OrderItem emptyItem = new OrderItem();
        emptyItem.setId(1L);
        emptyItem.setProductId("P001");
        emptyItem.setProductName("Laptop Dell");
        emptyItem.setUnitPrice(15_000_000L);
        emptyItem.setQuantity(2);
        emptyItem.setLineTotal(30_000_000L);
        emptyItem.setOrder(order);

        assertEquals(1L, emptyItem.getId());
        assertEquals("P001", emptyItem.getProductId());
        assertEquals("Laptop Dell", emptyItem.getProductName());
        assertEquals(15_000_000L, emptyItem.getUnitPrice());
        assertEquals(2, emptyItem.getQuantity());
        assertEquals(30_000_000L, emptyItem.getLineTotal());
        assertSame(order, emptyItem.getOrder());

        OrderItem constructorItem = new OrderItem("P002", "Mouse Logitech", 500_000L, 4);
        assertEquals("P002", constructorItem.getProductId());
        assertEquals("Mouse Logitech", constructorItem.getProductName());
        assertEquals(500_000L, constructorItem.getUnitPrice());
        assertEquals(4, constructorItem.getQuantity());
        assertEquals(2_000_000L, constructorItem.getLineTotal());
    }

    @Test
    @DisplayName("Order constructors, getters, setters va addItem")
    void orderShouldExposeAllFieldsAndLinkOrderItem() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 5, 10, 8, 30);
        Order emptyOrder = new Order();
        emptyOrder.setId("ORD-001");
        emptyOrder.setUserId("user01");
        emptyOrder.setShippingAddress("123 Nguyen Trai, TP.HCM");
        emptyOrder.setPaymentMethod("COD");
        emptyOrder.setCouponCode("SALE10");
        emptyOrder.setSubtotal(30_500_000L);
        emptyOrder.setDiscount(3_050_000L);
        emptyOrder.setShippingFee(50_000L);
        emptyOrder.setTotalPrice(27_500_000L);
        emptyOrder.setStatus(OrderStatus.PENDING);
        emptyOrder.setCreatedAt(createdAt);

        assertEquals("ORD-001", emptyOrder.getId());
        assertEquals("user01", emptyOrder.getUserId());
        assertEquals("123 Nguyen Trai, TP.HCM", emptyOrder.getShippingAddress());
        assertEquals("COD", emptyOrder.getPaymentMethod());
        assertEquals("SALE10", emptyOrder.getCouponCode());
        assertEquals(30_500_000L, emptyOrder.getSubtotal());
        assertEquals(3_050_000L, emptyOrder.getDiscount());
        assertEquals(50_000L, emptyOrder.getShippingFee());
        assertEquals(27_500_000L, emptyOrder.getTotalPrice());
        assertEquals(OrderStatus.PENDING, emptyOrder.getStatus());
        assertEquals(createdAt, emptyOrder.getCreatedAt());

        List<OrderItem> items = new ArrayList<>();
        emptyOrder.setItems(items);
        assertSame(items, emptyOrder.getItems());

        OrderItem item = new OrderItem("P001", "Laptop Dell", 15_000_000L, 2);
        emptyOrder.addItem(item);
        assertEquals(1, emptyOrder.getItems().size());
        assertSame(emptyOrder, item.getOrder());

        Order constructorOrder = new Order(
                "ORD-002",
                "user02",
                "456 Le Loi, TP.HCM",
                "BANK_TRANSFER",
                null,
                500_000L,
                0L,
                50_000L,
                550_000L,
                OrderStatus.CANCELED,
                createdAt
        );

        assertEquals("ORD-002", constructorOrder.getId());
        assertEquals("user02", constructorOrder.getUserId());
        assertEquals("456 Le Loi, TP.HCM", constructorOrder.getShippingAddress());
        assertEquals("BANK_TRANSFER", constructorOrder.getPaymentMethod());
        assertEquals(OrderStatus.CANCELED, constructorOrder.getStatus());
        assertTrue(constructorOrder.getItems().isEmpty());
    }
}
