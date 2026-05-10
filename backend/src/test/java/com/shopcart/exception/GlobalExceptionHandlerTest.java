package com.shopcart.exception;

import com.shopcart.dto.CartResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Global Exception Handler Tests")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("Xu ly IllegalArgumentException thanh BAD_REQUEST")
    void shouldHandleIllegalArgumentException() {
        ResponseEntity<CartResponse> response = handler.handleIllegalArgument(
                new IllegalArgumentException("So luong vuot qua ton kho hien tai")
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().success());
        assertEquals("So luong vuot qua ton kho hien tai", response.getBody().message());
        assertEquals(0L, response.getBody().cartTotal());
        assertEquals(0, response.getBody().items().size());
    }

    @Test
    @DisplayName("Xu ly MethodArgumentNotValidException voi field error dau tien")
    void shouldHandleValidationExceptionWithFirstFieldError() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("cartItemRequest", "productId", "Product ID khong duoc rong");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<CartResponse> response = handler.handleValidation(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().success());
        assertEquals("Product ID khong duoc rong", response.getBody().message());
    }

    @Test
    @DisplayName("Xu ly MethodArgumentNotValidException khong co field error")
    void shouldHandleValidationExceptionWithoutFieldError() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());

        ResponseEntity<CartResponse> response = handler.handleValidation(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().success());
        assertEquals("Du lieu khong hop le", response.getBody().message());
    }
}
