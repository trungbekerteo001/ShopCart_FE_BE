package com.shopcart.repository;

import com.shopcart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<CartItem, Long> {
    // tim kiem san pham trong cart theo userId va productId
    // Optional de tranh truong hop khong tim thay san pham, tra ve Optional.empty() thay vi null
    Optional<CartItem> findByUserIdAndProductId(String userId, String productId);

    // lay tat ca san pham trong cart theo userId
    List<CartItem> findByUserId(String userId);

    // xoa san pham khoi cart theo userId va productId
    void deleteByUserIdAndProductId(String userId, String productId);
}

// JpaRepository co san cac phuong thuc: 
// findById()
// findAll()
// save()
// delete()
// existsById()
