package com.shopcart.repository;

import com.shopcart.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, String> {
    // lay danh sach don hang theo userId
    List<Order> findByUserId(String userId);
}
