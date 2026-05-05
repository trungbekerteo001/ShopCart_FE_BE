package com.shopcart.service;

import com.shopcart.dto.InventoryResponse;
import com.shopcart.entity.Product;
import com.shopcart.entity.ProductStatus;
import com.shopcart.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// service xu ly cac nghiep vu lien quan den ton kho san pham
@Service
public class InventoryService {

    private final ProductRepository productRepository;

    public InventoryService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // lay thong tin ton kho cua san pham
    @Transactional(readOnly = true)
    public InventoryResponse getInventory(String productId) {
        Product product = findProduct(productId);
        boolean available = product.getStatus() == ProductStatus.ACTIVE
                && product.getStock() != null
                && product.getStock() > 0;

        return new InventoryResponse(
                true,
                "Lay thong tin ton kho thanh cong",
                product.getId(),
                product.getName(),
                product.getStock(),
                available
        );
    }

    // kiem tra so luong yeu cau co con du trong kho hay khong
    @Transactional(readOnly = true)
    public InventoryResponse checkAvailability(String productId, Integer quantity) {
        Product product = findProduct(productId);
        int requestedQuantity = quantity == null ? 1 : quantity;
        boolean available = product.getStatus() == ProductStatus.ACTIVE
                && product.getStock() != null
                && requestedQuantity >= 1
                && product.getStock() >= requestedQuantity;

        return new InventoryResponse(
                true,
                available ? "San pham con du hang" : "San pham khong du hang",
                product.getId(),
                product.getName(),
                product.getStock(),
                available
        );
    }

    private Product findProduct(String productId) {
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("Product ID khong duoc rong");
        }

        return productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("San pham khong ton tai"));
    }
}
