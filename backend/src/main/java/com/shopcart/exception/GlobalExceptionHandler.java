package com.shopcart.exception;

import com.shopcart.dto.CartResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

// GlobalExceptionHandler dung de xu ly ngoai le chung cho toan bo REST controller.
// Thay vi viet @ExceptionHandler truc tiep trong CartController, tach ra file rieng de controller gon hon.

// @RestControllerAdvice se tu dong bat cac ngoai le xay ra trong controller va goi phuong thuc tuong ung de xu ly
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Xu ly cac loi do service nem ra 
    // Vi du:
    // - San pham khong ton tai
    // - San pham khong dang duoc ban
    // - So luong vuot qua ton kho hien tai
    // - San pham khong co trong gio hang
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CartResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new CartResponse(false, ex.getMessage(), 0L, List.of()));
    }

    // Xi ly cac loi validate trong DTO
    // Vi du:
    // - productId bi rong
    // - quantity nho hon 1
    // - quantity bi null
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CartResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()              // lay thong bao loi tu field bi loi validate
                .getFieldErrors()                           // lay danh sach cac field bi loi validate
                .stream()                                   // chuyen sang stream de xu ly    
                .findFirst()                                // lay field dau tien bi loi validate (neu co)
                .map(error -> error.getDefaultMessage())    // lay thong bao loi mac dinh tu annotation validate (vi du: "Product ID khong duoc rong")
                .orElse("Du lieu khong hop le");     // neu khong co field nao bi loi validate thi tra ve thong bao mac dinh "Du lieu khong hop le"

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new CartResponse(false, message, 0L, List.of()));
    }
}