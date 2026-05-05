package com.shopcart.service;

import com.shopcart.dto.OrderItemRequest;
import com.shopcart.dto.OrderRequest;
import com.shopcart.dto.OrderResponse;
import com.shopcart.entity.CartItem;
import com.shopcart.entity.Order;
import com.shopcart.entity.OrderItem;
import com.shopcart.entity.OrderStatus;
import com.shopcart.entity.Product;
import com.shopcart.entity.ProductStatus;
import com.shopcart.repository.CartRepository;
import com.shopcart.repository.OrderRepository;
import com.shopcart.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Order Service Unit Tests")
// su dung MockitoExtension de mo phong repository va test logic trong OrderService
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    // mock repository de test logic tao/huy don hang ma khong can DB that
    @Mock
    private OrderRepository orderRepository;

    // mock repository de test logic san pham va ton kho
    @Mock
    private ProductRepository productRepository;

    // mock repository de test viec xoa cart sau khi checkout thanh cong
    @Mock
    private CartRepository cartRepository;

    // tao object OrderService va inject cac mock repository vao de test logic
    @InjectMocks
    private OrderService orderService;

    @Test
    @DisplayName("TC_ORDER_SERVICE_001: Tao don hang thanh cong va tru ton kho")
    void createOrderSuccessShouldDecreaseStock() {
        // tao 2 san pham gia lap con du ton kho
        Product laptop = product("P001", "Laptop Dell", 15_000_000L, 10);
        Product mouse = product("P002", "Mouse Logitech", 500_000L, 50);

        // tao request gom 2 laptop va 1 mouse, co ma giam gia SALE10 va phi ship 50.000
        OrderRequest request = orderRequest(
                List.of(
                        new OrderItemRequest("P001", 2),
                        new OrderItemRequest("P002", 1)
                ),
                "SALE10",
                50_000L
        );

        // gia lap repository tim thay san pham P001 va P002
        when(productRepository.findById("P001")).thenReturn(Optional.of(laptop));
        when(productRepository.findById("P002")).thenReturn(Optional.of(mouse));

        // gia lap gio hang cua user01 co 2 san pham, de service xoa sau khi checkout thanh cong
        when(cartRepository.findByUserId("user01")).thenReturn(List.of(
                new CartItem("user01", "P001", "Laptop Dell", 15_000_000L, 2),
                new CartItem("user01", "P002", "Mouse Logitech", 500_000L, 1)
        ));

        // khi luu order thi tra ve chinh order vua duoc luu
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // goi ham createOrder de tao don hang
        OrderResponse response = orderService.createOrder("user01", request);

        // kiem tra tao don hang thanh cong
        assertTrue(response.success());
        assertNotNull(response.orderId());
        assertEquals(OrderStatus.PENDING, response.status());

        // subtotal = 15.000.000 * 2 + 500.000 * 1 = 30.500.000
        assertEquals(30_500_000L, response.subtotal());

        // discount SALE10 = 10% * 30.500.000 = 3.050.000
        assertEquals(3_050_000L, response.discount());

        // total = subtotal - discount + shippingFee = 30.500.000 - 3.050.000 + 50.000 = 27.500.000
        assertEquals(27_500_000L, response.totalPrice());

        // kiem tra ton kho da bi tru sau khi tao order
        assertEquals(8, laptop.getStock());
        assertEquals(49, mouse.getStock());

        // kiem tra order duoc save 1 lan va product duoc save 2 lan de cap nhat ton kho
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(productRepository, times(2)).save(any(Product.class));
    }

    @Test
    @DisplayName("TC_ORDER_SERVICE_002: Lay don hang theo id thanh cong")
    void getOrderByIdSuccess() {
        // tao order gia lap da ton tai trong database
        Order order = sampleOrder("ORD-001", OrderStatus.PENDING);
        order.addItem(new OrderItem("P001", "Laptop Dell", 15_000_000L, 2));

        // gia lap repository tim thay order ORD-001
        when(orderRepository.findById("ORD-001")).thenReturn(Optional.of(order));

        // goi ham getOrderById de lay thong tin don hang
        OrderResponse response = orderService.getOrderById("ORD-001");

        // kiem tra thong tin tra ve dung voi order gia lap
        assertTrue(response.success());
        assertEquals("ORD-001", response.orderId());
        assertEquals(OrderStatus.PENDING, response.status());
        assertEquals(1, response.items().size());
    }

    @Test
    @DisplayName("TC_ORDER_SERVICE_003: Huy don hang thanh cong va hoan ton kho")
    void cancelOrderSuccessShouldRestoreStock() {
        // tao order gia lap dang o trang thai PENDING
        Order order = sampleOrder("ORD-001", OrderStatus.PENDING);
        order.addItem(new OrderItem("P001", "Laptop Dell", 15_000_000L, 2));

        // tao san pham gia lap sau khi da bi tru ton kho con 8
        Product product = product("P001", "Laptop Dell", 15_000_000L, 8);

        // gia lap repository tim thay order va product
        when(orderRepository.findById("ORD-001")).thenReturn(Optional.of(order));
        when(productRepository.findById("P001")).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // goi ham cancelOrder de huy don hang
        OrderResponse response = orderService.cancelOrder("ORD-001");

        // kiem tra don hang da chuyen sang CANCELED
        assertEquals(OrderStatus.CANCELED, response.status());

        // kiem tra ton kho da duoc hoan lai: 8 + 2 = 10
        assertEquals(10, product.getStock());

        // kiem tra product va order deu duoc save sau khi huy
        verify(productRepository, times(1)).save(product);
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    @DisplayName("TC_ORDER_SERVICE_004: Tinh tong tien don hang dung")
    void calculateOrderTotalSuccess() {
        // tao san pham gia lap con du ton kho
        Product laptop = product("P001", "Laptop Dell", 15_000_000L, 10);
        Product mouse = product("P002", "Mouse Logitech", 500_000L, 50);

        // tao request co SALE10 va phi ship 50.000
        OrderRequest request = orderRequest(
                List.of(
                        new OrderItemRequest("P001", 2),
                        new OrderItemRequest("P002", 1)
                ),
                "SALE10",
                50_000L
        );

        // gia lap repository tim thay san pham
        when(productRepository.findById("P001")).thenReturn(Optional.of(laptop));
        when(productRepository.findById("P002")).thenReturn(Optional.of(mouse));

        // goi ham calculateOrderTotal de tinh tong tien
        Long total = orderService.calculateOrderTotal(request);

        // total = 30.500.000 - 3.050.000 + 50.000 = 27.500.000
        assertEquals(27_500_000L, total);
    }

    @Test
    @DisplayName("TC_ORDER_SERVICE_005: Kiem tra ton kho khong du thi tra ve false")
    void checkStockBeforeOrderShouldReturnFalseWhenInsufficientStock() {
        // tao san pham gia lap chi con ton kho 1
        Product product = product("P001", "Laptop Dell", 15_000_000L, 1);

        // request dat so luong 2, lon hon ton kho 1
        OrderRequest request = orderRequest(
                List.of(new OrderItemRequest("P001", 2)),
                null,
                50_000L
        );

        // gia lap repository tim thay san pham P001
        when(productRepository.findById("P001")).thenReturn(Optional.of(product));

        // goi ham checkStockBeforeOrder
        boolean result = orderService.checkStockBeforeOrder(request);

        // kiem tra ket qua la false vi ton kho khong du
        assertFalse(result);
    }

    @Test
    @DisplayName("TC_ORDER_SERVICE_006: Tao don hang voi coupon khong hop le thi bao loi")
    void createOrderWithInvalidCouponShouldThrowException() {
        // tao san pham gia lap con du ton kho
        Product product = product("P001", "Laptop Dell", 15_000_000L, 10);

        // request dung ma giam gia khong hop le
        OrderRequest request = orderRequest(
                List.of(new OrderItemRequest("P001", 1)),
                "INVALID",
                50_000L
        );

        // gia lap repository tim thay san pham P001
        when(productRepository.findById("P001")).thenReturn(Optional.of(product));

        // goi createOrder va mong doi nem IllegalArgumentException
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.createOrder("user01", request)
        );

        // kiem tra message loi
        assertEquals("Ma giam gia khong hop le", exception.getMessage());
    }

    private Product product(String id, String name, Long price, Integer stock) {
        // ham ho tro tao san pham dang ban voi du lieu truyen vao
        return new Product(id, name, price, stock, ProductStatus.ACTIVE);
    }

    private OrderRequest orderRequest(List<OrderItemRequest> items, String couponCode, Long shippingFee) {
        // ham ho tro tao request dat hang dung chung cho cac test case
        return new OrderRequest(
                items,
                couponCode,
                shippingFee,
                "123 Nguyen Trai, TP.HCM",
                "COD"
        );
    }

    private Order sampleOrder(String orderId, OrderStatus status) {
        // ham ho tro tao nhanh mot order gia lap
        return new Order(
                orderId,
                "user01",
                "123 Nguyen Trai, TP.HCM",
                "COD",
                "SALE10",
                30_500_000L,
                3_050_000L,
                50_000L,
                27_500_000L,
                status,
                LocalDateTime.now()
        );
    }
}
