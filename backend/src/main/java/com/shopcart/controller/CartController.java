package com.shopcart.controller;

import com.shopcart.dto.CartItemRequest;
import com.shopcart.dto.CartResponse;
import com.shopcart.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

 // danh dau class nay la controller, se nhan request tu client gui den
@RestController                                    

// routing API cho cart
@RequestMapping("/api/cart")                        

// cho phep frontend o dia chi nay truy cap API
@CrossOrigin(origins = "http://localhost:5173")

public class CartController {

    // inject CartService de xu ly logic lien quan den cart
    private final CartService cartService;

    // constructor
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // endponit them san pham vao cart
    @PostMapping("/add")
    public ResponseEntity<CartResponse> addToCart(
            // lay userId tu header X-USER-ID, neu khong co thi mac dinh la "user01"       
            @RequestHeader(value = "X-USER-ID", defaultValue = "user01") String userId,
            // lay du lieu them san pham vao cart tu body cua request, va validate du lieu do o DTO 
            @Valid @RequestBody CartItemRequest request
    ) {
        // 
        return ResponseEntity.ok(cartService.addToCart(userId, request));
    }

    // endpoint lay thong tin cart cua user
    @GetMapping
    public ResponseEntity<CartResponse> getCart(
            // lay userId tu header X-USER-ID, neu khong co thi mac dinh la "user01"
            @RequestHeader(value = "X-USER-ID", defaultValue = "user01") String userId
    ) {
        // goi service tra ve thong tin cart cua user do
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    // endpoint cap nhat so luong san pham trong cart
    @PutMapping("/update")
    public ResponseEntity<CartResponse> updateQuantity(
            // lay userId tu header X-USER-ID, neu khong co thi mac dinh la "user01"
            @RequestHeader(value = "X-USER-ID", defaultValue = "user01") String userId,
            // lay du lieu cap nhat so luong san pham tu body cua request, va validate du lieu do o DTO
            @Valid @RequestBody CartItemRequest request
    ) {
        // goi service de cap nhat so luong san pham trong cart cua user do
        return ResponseEntity.ok(cartService.updateQuantity(userId, request));
    }

    // endpoint xoa san pham khoi cart
    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<CartResponse> removeFromCart(
            // lay userId tu header X-USER-ID, neu khong co thi mac dinh la "user01"
            @RequestHeader(value = "X-USER-ID", defaultValue = "user01") String userId,
            // gan {productId} vao productId
            @PathVariable String productId
    ) {
        // goi service de xoa san pham có productId khoi cart cua user do
        return ResponseEntity.ok(cartService.removeFromCart(userId, productId));
    }

    
}
