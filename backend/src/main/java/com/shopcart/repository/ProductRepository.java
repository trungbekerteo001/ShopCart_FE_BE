package com.shopcart.repository;

import com.shopcart.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, String> {
}

// JpaRepository co san cac phuong thuc: 
// findById()
// findAll()
// save()
// delete()
// existsById()