package com.shopcart.config;

import com.shopcart.entity.Product;
import com.shopcart.entity.ProductStatus;
import com.shopcart.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


// DataSeeder dung de tao du lieu mau khi ung dung khoi dong.

// Khong dat bean seed data trong ShopCartApplication
// de tranh loi khi chay @WebMvcTest cho controller

// @Component de Spring tu dong phat hien va chay bean DataSeeder khi ung dung khoi dong
@Component
public class DataSeeder implements CommandLineRunner {

    private final ProductRepository productRepository;

    public DataSeeder(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) {
        // neu da co du lieu san pham thi khong seed lai
        if (productRepository.count() > 0) {
            return;
        }

        // tao san pham mau dung de test API cart
        productRepository.save(new Product("P001", "Laptop Dell", 15_000_000L, 10, ProductStatus.ACTIVE));
        productRepository.save(new Product("P002", "Mouse Logitech", 500_000L, 50, ProductStatus.ACTIVE));
        productRepository.save(new Product("P003", "Keyboard Mechanical", 2_000_000L, 0, ProductStatus.ACTIVE));
        productRepository.save(new Product("P004", "Old Monitor", 3_000_000L, 5, ProductStatus.INACTIVE));
    }
}