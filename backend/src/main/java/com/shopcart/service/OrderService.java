package com.shopcart.service;

import com.shopcart.dto.OrderItemRequest;
import com.shopcart.dto.OrderItemResponse;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// danh dau class nay la service, chua logic nghiep vu lien quan den purchase/checkout/order
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;

    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        CartRepository cartRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
    }

    // tao don hang moi, kiem tra ton kho, tinh tong tien, tru ton kho va luu order
    @Transactional
    public OrderResponse createOrder(String userId, OrderRequest request) {
        validateOrderRequest(request);

        // kiem tra ton kho truoc khi tao don hang
        if (!checkStockBeforeOrder(request)) {
            throw new IllegalArgumentException("Ton kho khong du de tao don hang");
        }

        List<OrderItem> orderItems = new ArrayList<>();
        List<Product> orderedProducts = new ArrayList<>();

        long subtotal = 0L;

        // duyet tung san pham trong request de tinh tien va tao order item
        for (OrderItemRequest itemRequest : request.items()) {
            Product product = findActiveProduct(itemRequest.productId());
            validateQuantity(itemRequest.quantity(), product.getStock());

            OrderItem orderItem = new OrderItem(
                    product.getId(),
                    product.getName(),
                    product.getPrice(),
                    itemRequest.quantity()
            );

            subtotal += orderItem.getLineTotal();
            orderItems.add(orderItem);
            orderedProducts.add(product);
        }

        long discount = calculateDiscount(subtotal, request.couponCode());
        long shippingFee = request.shippingFee();
        long totalPrice = subtotal - discount + shippingFee;

        if (totalPrice <= 0) {
            throw new IllegalArgumentException("Tong tien don hang phai lon hon 0");
        }

        // tao order voi trang thai PENDING
        Order order = new Order(
                generateOrderId(),
                userId,
                request.shippingAddress(),
                request.paymentMethod(),
                request.couponCode(),
                subtotal,
                discount,
                shippingFee,
                totalPrice,
                OrderStatus.PENDING,
                LocalDateTime.now()
        );

        // gan cac order item vao order
        for (OrderItem orderItem : orderItems) {
            order.addItem(orderItem);
        }

        // tru ton kho cua tung san pham sau khi tao don hang thanh cong
        for (int i = 0; i < orderedProducts.size(); i++) {
            Product product = orderedProducts.get(i);
            OrderItemRequest itemRequest = request.items().get(i);
            product.setStock(product.getStock() - itemRequest.quantity());
            productRepository.save(product);
        }

        // xoa gio hang cua user sau khi checkout thanh cong
        List<CartItem> cartItems = cartRepository.findByUserId(userId);
        cartRepository.deleteAll(cartItems);

        Order savedOrder = orderRepository.save(order);
        return buildOrderResponse(savedOrder, "Tao don hang thanh cong");
    }

    // lay thong tin don hang theo orderId
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(String orderId) {
        Order order = findOrder(orderId);
        return buildOrderResponse(order, "Lay thong tin don hang thanh cong");
    }

    // huy don hang va hoan lai ton kho
    @Transactional
    public OrderResponse cancelOrder(String orderId) {
        Order order = findOrder(orderId);

        if (order.getStatus() == OrderStatus.CANCELED) {
            throw new IllegalArgumentException("Don hang da duoc huy truoc do");
        }

        // hoan ton kho cho tung san pham trong don hang
        for (OrderItem item : order.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("San pham khong ton tai"));
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        }

        order.setStatus(OrderStatus.CANCELED);
        Order savedOrder = orderRepository.save(order);
        return buildOrderResponse(savedOrder, "Huy don hang thanh cong");
    }

    // tinh tong tien cuoi cung cua don hang, dung cho unit test va nghiep vu tinh gia
    @Transactional(readOnly = true)
    public Long calculateOrderTotal(OrderRequest request) {
        validateOrderRequest(request);

        long subtotal = 0L;
        for (OrderItemRequest itemRequest : request.items()) {
            Product product = findActiveProduct(itemRequest.productId());
            subtotal += product.getPrice() * itemRequest.quantity();
        }

        long discount = calculateDiscount(subtotal, request.couponCode());
        return subtotal - discount + request.shippingFee();
    }

    // kiem tra tat ca san pham trong request co con du ton kho hay khong
    @Transactional(readOnly = true)
    public boolean checkStockBeforeOrder(OrderRequest request) {
        validateOrderRequest(request);

        for (OrderItemRequest itemRequest : request.items()) {
            Product product = findActiveProduct(itemRequest.productId());
            if (itemRequest.quantity() > product.getStock()) {
                return false;
            }
        }

        return true;
    }

    private void validateOrderRequest(OrderRequest request) {
        if (request == null || request.items() == null || request.items().isEmpty()) {
            throw new IllegalArgumentException("Don hang phai co it nhat 1 san pham");
        }

        if (request.shippingFee() == null || request.shippingFee() < 0) {
            throw new IllegalArgumentException("Phi van chuyen khong hop le");
        }

        if (request.shippingAddress() == null || request.shippingAddress().isBlank()) {
            throw new IllegalArgumentException("Dia chi giao hang khong duoc rong");
        }

        if (request.paymentMethod() == null || request.paymentMethod().isBlank()) {
            throw new IllegalArgumentException("Phuong thuc thanh toan khong duoc rong");
        }
    }

    private Product findActiveProduct(String productId) {
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("Product ID khong duoc rong");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("San pham khong ton tai"));

        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new IllegalArgumentException("San pham khong o trang thai dang ban");
        }

        return product;
    }

    private void validateQuantity(Integer quantity, Integer stock) {
        if (quantity == null || quantity < 1) {
            throw new IllegalArgumentException("So luong phai lon hon hoac bang 1");
        }

        if (stock == null || stock <= 0) {
            throw new IllegalArgumentException("San pham da het hang");
        }

        if (quantity > stock) {
            throw new IllegalArgumentException("So luong vuot qua ton kho hien tai");
        }
    }

    private long calculateDiscount(long subtotal, String couponCode) {
        if (couponCode == null || couponCode.isBlank()) {
            return 0L;
        }

        String normalizedCoupon = couponCode.trim().toUpperCase();
        long discount = switch (normalizedCoupon) {
            case "SALE10" -> subtotal * 10 / 100;
            case "SALE20" -> subtotal * 20 / 100;
            case "FIXED100K" -> 100_000L;
            default -> throw new IllegalArgumentException("Ma giam gia khong hop le");
        };

        return Math.min(discount, subtotal);
    }

    private Order findOrder(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("Order ID khong duoc rong");
        }

        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Don hang khong ton tai"));
    }

    private String generateOrderId() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private OrderResponse buildOrderResponse(Order order, String message) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getProductId(),
                        item.getProductName(),
                        item.getUnitPrice(),
                        item.getQuantity(),
                        item.getLineTotal()
                ))
                .toList();

        return new OrderResponse(
                true,
                message,
                order.getId(),
                order.getStatus(),
                order.getSubtotal(),
                order.getDiscount(),
                order.getShippingFee(),
                order.getTotalPrice(),
                itemResponses
        );
    }
}
