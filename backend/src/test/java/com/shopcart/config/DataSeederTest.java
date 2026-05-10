package com.shopcart.config;

import com.shopcart.entity.Product;
import com.shopcart.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Data Seeder Tests")
@ExtendWith(MockitoExtension.class)
class DataSeederTest {

    @Mock
    private ProductRepository productRepository;

    @Test
    @DisplayName("Khong seed lai du lieu khi da co san pham")
    void shouldNotSeedProductsWhenDatabaseAlreadyHasData() throws Exception {
        DataSeeder dataSeeder = new DataSeeder(productRepository);
        when(productRepository.count()).thenReturn(4L);

        dataSeeder.run();

        verify(productRepository, never()).save(org.mockito.ArgumentMatchers.any(Product.class));
    }

    @Test
    @DisplayName("Seed du lieu san pham mau khi database rong")
    void shouldSeedDefaultProductsWhenDatabaseIsEmpty() throws Exception {
        DataSeeder dataSeeder = new DataSeeder(productRepository);
        when(productRepository.count()).thenReturn(0L);

        dataSeeder.run();

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository, times(4)).save(captor.capture());
        List<Product> products = captor.getAllValues();

        assertEquals("P001", products.get(0).getId());
        assertEquals("Laptop Dell", products.get(0).getName());
        assertEquals(10, products.get(0).getStock());
        assertEquals("P004", products.get(3).getId());
        assertTrue(products.stream().anyMatch(product -> "P003".equals(product.getId()) && product.getStock() == 0));
    }
}
