package com.shopcart.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// danh dau class nay la entity, tuong ung voi bang products trong database
@Entity
@Table(name = "products")
public class Product {

    // khoa chinh  
    @Id
    private String id;

    private String name;

    private Long price;

    private Integer stock;

    // db luu 2 enum trong ProductStatus la "ACTIVE" va "INACTIVE"
    @Enumerated(EnumType.STRING)
    private ProductStatus status;

    public Product() {
    }

    public Product(String id, String name, Long price, Integer stock, ProductStatus status) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public void setStatus(ProductStatus status) {
        this.status = status;
    }
}
