package com.shopcart.service;

import com.shopcart.dto.CartItemRequest;
import com.shopcart.dto.CartResponse;
import com.shopcart.entity.CartItem;
import com.shopcart.entity.Product;
import com.shopcart.entity.ProductStatus;
import com.shopcart.repository.CartRepository;
import com.shopcart.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Cart Service Unit Tests")
// su dung MockitoExtension de mo phong repository va test logic trong CartService
@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    // mock repository de test logic trong CartService 
    @Mock
    private ProductRepository productRepository;

    // mock repository de test logic trong CartService
    @Mock
    private CartRepository cartRepository;

    // tao object CartService va inject cac mock repository vao de test logic trong CartService
    @InjectMocks
    private CartService cartService;

    @Test
    @DisplayName("TC_CART_SERVICE_001: Them san pham vao gio hang thanh cong")
    void addToCartSuccess() {
        Product product = activeProduct(10);    // tao sp gia co ton kho la 10 
        CartItem savedItem = new CartItem("user01", "P001", "Laptop Dell", 15_000_000L, 2);

        // san pham co ton kho, khi them vao gio hang thi se tao moi 1 cart item
        when(productRepository.findById("P001")).thenReturn(Optional.of(product));

        // san pham chua co trong gio hang, khi them vao gio hang thi se tao moi 1 cart item
        when(cartRepository.findByUserIdAndProductId("user01", "P001")).thenReturn(Optional.empty());
    
        // khi luu cart item thi se tra ve cart item vua duoc luu
        when(cartRepository.save(any(CartItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // sau khi them vao gio hang thi se tra ve danh sach cart item cua user trong gio hang
        when(cartRepository.findByUserId("user01")).thenReturn(List.of(savedItem));

        // goi ham addToCart de them san pham vao gio hang
        CartResponse response = cartService.addToCart("user01", new CartItemRequest("P001", 2));

        // kiem tra ket qua tra ve tu ham addToCart
        assertTrue(response.success());

        // kiem tra thong diep tra ve tu ham addToCart
        assertEquals("Them vao gio hang thanh cong", response.message());

        // kiem tra tong tien trong gio hang sau khi them san pham vao gio hang
        assertEquals(30_000_000L, response.cartTotal());

        // kiem tra ham save duoc goi 1 lan de luu cart item vao gio hang
        verify(cartRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    @DisplayName("TC_CART_SERVICE_002: Them san pham da co trong gio thi cong don so luong")
    void addExistingProductShouldIncreaseQuantity() {
        // tao sp gia co ton kho la 10
        Product product = activeProduct(10);

        // tao cart item gia lap san pham da co san trong gio hang voi so luong la 3
        CartItem existingItem = new CartItem("user01", "P001", "Laptop Dell", 15_000_000L, 3);

        // tao cart item gia lap sau khi cong don so luong: 3 + 2 = 5
        CartItem updatedItem = new CartItem("user01", "P001", "Laptop Dell", 15_000_000L, 5);

        // san pham P001 ton tai trong he thong
        when(productRepository.findById("P001")).thenReturn(Optional.of(product));

        // san pham P001 da co trong gio hang cua user01
        when(cartRepository.findByUserIdAndProductId("user01", "P001")).thenReturn(Optional.of(existingItem));

        // khi luu cart item thi se tra ve chinh cart item vua duoc luu
        when(cartRepository.save(any(CartItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // sau khi cap nhat, gio hang cua user01 co san pham P001 voi so luong moi la 5
        when(cartRepository.findByUserId("user01")).thenReturn(List.of(updatedItem));

        // goi ham addToCart de them tiep 2 san pham P001 vao gio hang
        CartResponse response = cartService.addToCart("user01", new CartItemRequest("P001", 2));

        // kiem tra so luong san pham sau khi cong don co bang 5 khong
        assertEquals(5, response.items().getFirst().quantity());

        // kiem tra tong tien trong gio hang: 15.000.000 * 5 = 75.000.000
        assertEquals(75_000_000L, response.cartTotal());

        // kiem tra ham save duoc goi 1 lan de luu cart item sau khi cap nhat so luong
        verify(cartRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    @DisplayName("TC_CART_SERVICE_003: Them san pham vuot ton kho thi bao loi")
    void addToCartInsufficientStockShouldThrowException() {
        // tao sp gia co ton kho la 10
        Product product = activeProduct(10);

        // san pham P001 ton tai trong he thong
        when(productRepository.findById("P001")).thenReturn(Optional.of(product));

        // san pham P001 chua co trong gio hang cua user01
        when(cartRepository.findByUserIdAndProductId("user01", "P001")).thenReturn(Optional.empty());

        // goi ham addToCart voi so luong 11 lon hon ton kho 10, nen phai nem IllegalArgumentException
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cartService.addToCart("user01", new CartItemRequest("P001", 11))
        );

        // kiem tra thong bao loi co dung voi mong doi khong
        assertEquals("So luong vuot qua ton kho hien tai", exception.getMessage());

        // vi them san pham bi loi nen khong duoc goi ham save
        verify(cartRepository, never()).save(any(CartItem.class));
    }

    @Test
    @DisplayName("TC_CART_SERVICE_004: Them san pham khong ton tai thi bao loi")
    void addToCartProductNotFoundShouldThrowException() {
        // gia lap san pham P999 khong ton tai trong database
        when(productRepository.findById("P999")).thenReturn(Optional.empty());

        // goi ham addToCart voi productId khong ton tai, nen phai nem IllegalArgumentException
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cartService.addToCart("user01", new CartItemRequest("P999", 1))
        );

        // kiem tra thong bao loi co dung voi mong doi khong
        assertEquals("San pham khong ton tai", exception.getMessage());

        // vi san pham khong ton tai nen khong duoc goi ham save
        verify(cartRepository, never()).save(any(CartItem.class));
    }

    @Test
    @DisplayName("TC_CART_SERVICE_005: Cap nhat so luong san pham trong gio thanh cong")
    void updateQuantitySuccess() {
        // tao sp gia co ton kho la 10
        Product product = activeProduct(10);

        // tao cart item gia lap san pham dang co trong gio hang voi so luong la 2
        CartItem existingItem = new CartItem("user01", "P001", "Laptop Dell", 15_000_000L, 2);

        // tao cart item gia lap sau khi cap nhat so luong moi la 4
        CartItem updatedItem = new CartItem("user01", "P001", "Laptop Dell", 15_000_000L, 4);

        // san pham P001 ton tai trong he thong
        when(productRepository.findById("P001")).thenReturn(Optional.of(product));

        // san pham P001 da co trong gio hang cua user01
        when(cartRepository.findByUserIdAndProductId("user01", "P001")).thenReturn(Optional.of(existingItem));

        // khi luu cart item thi se tra ve chinh cart item vua duoc luu
        when(cartRepository.save(any(CartItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // sau khi cap nhat, gio hang cua user01 co san pham P001 voi so luong moi la 4
        when(cartRepository.findByUserId("user01")).thenReturn(List.of(updatedItem));

        // goi ham updateQuantity de cap nhat so luong san pham P001 thanh 4
        CartResponse response = cartService.updateQuantity("user01", new CartItemRequest("P001", 4));

        // kiem tra thong diep tra ve tu ham updateQuantity
        assertEquals("Cap nhat so luong thanh cong", response.message());

        // kiem tra tong tien trong gio hang sau khi cap nhat: 15.000.000 * 4 = 60.000.000
        assertEquals(60_000_000L, response.cartTotal());

        // kiem tra ham save duoc goi 1 lan de luu so luong moi
        verify(cartRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    @DisplayName("TC_CART_SERVICE_006: Xoa san pham khoi gio thanh cong")
    void removeFromCartSuccess() {
        // tao cart item gia lap san pham dang co trong gio hang cua user01
        CartItem existingItem = new CartItem("user01", "P001", "Laptop Dell", 15_000_000L, 2);

        // san pham P001 dang co trong gio hang cua user01
        when(cartRepository.findByUserIdAndProductId("user01", "P001")).thenReturn(Optional.of(existingItem));

        // sau khi xoa san pham, gio hang cua user01 khong con san pham nao
        when(cartRepository.findByUserId("user01")).thenReturn(List.of());

        // goi ham removeFromCart de xoa san pham P001 khoi gio hang
        CartResponse response = cartService.removeFromCart("user01", "P001");

        // kiem tra thong diep tra ve tu ham removeFromCart
        assertEquals("Xoa san pham khoi gio hang thanh cong", response.message());

        // kiem tra tong tien trong gio hang sau khi xoa san pham
        assertEquals(0L, response.cartTotal());

        // kiem tra ham delete duoc goi 1 lan de xoa cart item khoi gio hang
        verify(cartRepository, times(1)).delete(existingItem);
    }

    private Product activeProduct(int stock) {
        // ham ho tro tao nhanh san pham P001 dang duoc ban voi ton kho truyen vao
        return new Product("P001", "Laptop Dell", 15_000_000L, stock, ProductStatus.ACTIVE);
    }
}