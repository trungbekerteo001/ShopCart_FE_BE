package com.shopcart.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// danh dau class nay la entity, tuong ung voi bang cart_items trong database
@Entity
@Table(name = "cart_items")
public class CartItem {

    // khoa chinh, tu dong tang
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;

    private String productId;

    private String productName;

    private Long price;

    private Integer quantity;

    public CartItem() {
    }

    public CartItem(String userId, String productId, String productName, Long price, Integer quantity) {
        this.userId = userId;
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    // tinh tong tien cua san pham trong cart = price * quantity
    public Long getLineTotal() {
        if (price == null || quantity == null) {
            return 0L;
        }
        return price * quantity;
    }
}
