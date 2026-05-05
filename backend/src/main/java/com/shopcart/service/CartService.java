package com.shopcart.service;

import com.shopcart.dto.CartItemRequest;
import com.shopcart.dto.CartItemResponse;
import com.shopcart.dto.CartResponse;
import com.shopcart.entity.CartItem;
import com.shopcart.entity.Product;
import com.shopcart.entity.ProductStatus;
import com.shopcart.repository.CartRepository;
import com.shopcart.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// dang dau class nay la service, chua logic nghiep vu lien quan den cart
@Service

public class CartService {
    // inject repository de truy cap du lieu san pham va cart
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;

    // constructor
    public CartService(ProductRepository productRepository, CartRepository cartRepository) {
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
    }

    // transaction 
    @Transactional
    public CartResponse addToCart(String userId, CartItemRequest request) {

        // validate so luong san pham duoc yeu cau them vao cart
        validateRequestedQuantity(request.quantity());
        Product product = findActiveProduct(request.productId());

        // tim xem san pham da co trong cart cua user chua
        // neu chua co thi tao moi, neu co roi thi cap nhat so luong
        CartItem cartItem = cartRepository
                .findByUserIdAndProductId(userId, request.productId())
                .orElseGet(() -> new CartItem(
                        userId,
                        product.getId(),
                        product.getName(),
                        product.getPrice(),
                        0
                ));

        // tinh so luong moi sau khi them vao cart
        int newQuantity = cartItem.getQuantity() + request.quantity();
        validateQuantity(newQuantity, product.getStock());

        // cap nhat thong tin san pham trong cart
        cartItem.setProductName(product.getName());
        cartItem.setPrice(product.getPrice());
        cartItem.setQuantity(newQuantity);
        // goi repository de luu thong tin san pham trong cart vao database
        cartRepository.save(cartItem);

        return buildCartResponse(userId, "Them vao gio hang thanh cong");
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(String userId) {
        return buildCartResponse(userId, "Lay gio hang thanh cong");
    }

    @Transactional
    public CartResponse updateQuantity(String userId, CartItemRequest request) {
        // validate so luong san pham duoc yeu cau cap nhat trong cart
        Product product = findActiveProduct(request.productId());
        validateQuantity(request.quantity(), product.getStock());

        // tim san pham trong cart cua user, neu khong co thi tra ve loi
        CartItem cartItem = cartRepository
                .findByUserIdAndProductId(userId, request.productId())
                .orElseThrow(() -> new IllegalArgumentException("San pham khong co trong gio hang"));

        // cap nhat so luong san pham trong cart bang repository
        cartItem.setQuantity(request.quantity());
        cartRepository.save(cartItem);

        return buildCartResponse(userId, "Cap nhat so luong thanh cong");
    }

    @Transactional
    public CartResponse removeFromCart(String userId, String productId) {
        // tim san pham trong cart cua user, neu khong co thi tra ve loi
        CartItem cartItem = cartRepository
                .findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new IllegalArgumentException("San pham khong co trong gio hang"));

        cartRepository.delete(cartItem);
        return buildCartResponse(userId, "Xoa san pham khoi gio hang thanh cong");
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

    private void validateRequestedQuantity(Integer quantity) {
        if (quantity == null || quantity < 1) {
            // global exception handler se bat va tra ve response loi cho client
            throw new IllegalArgumentException("So luong phai lon hon hoac bang 1");
        }
    }

    private void validateQuantity(Integer quantity, Integer stock) {
        validateRequestedQuantity(quantity);

        if (stock == null || stock <= 0) {
            throw new IllegalArgumentException("San pham da het hang");
        }

        if (quantity > stock) {
            throw new IllegalArgumentException("So luong vuot qua ton kho hien tai");
        }
    }

    private CartResponse buildCartResponse(String userId, String message) {
        // lay tat ca san pham trong cart cua user
        List<CartItem> cartItems = cartRepository.findByUserId(userId);

        // 1 dong chi tiet gio hang = 1 san pham 
        List<CartItemResponse> itemResponses = cartItems.stream()   
                // chuyen tu CartItem -> CartItemResponse de tra ve cho client
                .map(item -> new CartItemResponse(
                        item.getProductId(),        // id san pham
                        item.getProductName(),      // ten san pham
                        item.getPrice(),            // gia san pham
                        item.getQuantity(),         // so luong san pham trong cart
                        item.getLineTotal()         // tong tien cua san pham trong cart = price * quantity
                ))
                .toList();

        // tinh tong tien cua gio hang = tong tien cua tung san pham trong cart
        long cartTotal = itemResponses.stream()
                .mapToLong(CartItemResponse::lineTotal)
                .sum();

        return new CartResponse(true, message, cartTotal, itemResponses);
    }
}
