# Test Cases chi tiết - Chức năng Cart
> Tài liệu này trình bày test scenarios và test cases chi tiết cho chức năng Giỏ hàng.
> Mỗi test case có đủ các trường theo template: **Test Case ID, Test Name, Priority, Preconditions, Test Steps, Test Data, Expected Result, Actual Result, Status**.

---
## 1. Dữ liệu kiểm thử tham chiếu
| Product ID | Product Name        | Price       | Stock | Status   | Mục đích sử dụng                      |
|------------|---------------------|-------------|-------|----------|---------------------------------------|
| P001       | Laptop Dell         | 15.000.000đ | 10    | ACTIVE   | Sản phẩm hợp lệ cho happy path        |
| P002       | Mouse Logitech      | 500.000đ    | 50    | ACTIVE   | Sản phẩm hợp lệ cho cập nhật số lượng |
| P003       | Keyboard Mechanical | 2.000.000đ  | 0     | ACTIVE   | Sản phẩm hết hàng                     |
| P004       | Old Monitor         | 3.000.000đ  | 5     | INACTIVE | Sản phẩm không còn bán                |

---
## 2. Test scenarios và mức độ ưu tiên
| Scenario ID | Scenario                                  | Loại kiểm thử     | Priority |
|-------------|-------------------------------------------|-------------------|----------|
| SC_CART_001 | Thêm sản phẩm hợp lệ vào giỏ hàng         | Happy path        | Critical |
| SC_CART_002 | Thêm sản phẩm với số lượng vượt tồn kho   | Negative          | Critical |
| SC_CART_003 | Thêm sản phẩm hết hàng                    | Negative          | High     |
| SC_CART_004 | Cập nhật số lượng sản phẩm trong giỏ      | Functional        | High     |
| SC_CART_005 | Xóa sản phẩm khỏi giỏ hàng                | Functional        | High     |
| SC_CART_006 | Thêm cùng sản phẩm nhiều lần              | Edge case         | Medium   |
| SC_CART_007 | Nhập số lượng bằng 0 hoặc âm              | Boundary/Negative | Critical |
| SC_CART_008 | Thêm sản phẩm không tồn tại hoặc inactive | Negative          | Medium   |

Giải thích ưu tiên:
- **Critical:** lỗi có thể làm sai nghiệp vụ chính, sai tồn kho hoặc sai tổng tiền.
- **High:** lỗi ảnh hưởng trực tiếp trải nghiệm mua hàng.
- **Medium:** lỗi ít xảy ra hơn nhưng vẫn cần kiểm soát để tránh sai logic.
- **Low:** lỗi ít ảnh hưởng hoặc chỉ liên quan giao diện/phụ trợ.

---
## 3. Test cases chi tiết
### TC_CART_001 - Thêm sản phẩm vào giỏ hàng thành công
| Trường        | Nội dung                                     |
|---------------|----------------------------------------------|
| Test Case ID  | TC_CART_001                                  |
| Test Name     | Thêm sản phẩm hợp lệ vào giỏ hàng thành công |
| Priority      | Critical                                     |
| Actual Result | Để trống - điền sau khi chạy test thực tế    |
| Status        | Not Run                                      |
 
**Preconditions**
- Ứng dụng frontend và backend đang chạy.
- Người dùng đang thao tác với user mặc định `user01`.
- Sản phẩm `P001 - Laptop Dell` tồn tại.
- Trạng thái sản phẩm là `ACTIVE`.
- Tồn kho hiện tại của sản phẩm là `10`.
- Giỏ hàng ban đầu chưa có sản phẩm `P001` hoặc đã được reset trước khi chạy test.

**Test Steps**
1. Truy cập màn hình Cart/Product.
2. Chọn sản phẩm `Laptop Dell`.
3. Nhập số lượng `2`.
4. Nhấn nút `Thêm vào giỏ hàng`.
5. Quan sát thông báo và tổng tiền giỏ hàng.

**Test Data**
| Thuộc tính      | Giá trị     |
|-----------------|-------------|
| Product ID      | P001        |
| Product Name    | Laptop Dell |
| Quantity        | 2           |
| Price           | 15.000.000đ |
| Stock available | 10          |

**Expected Result**
- Hệ thống hiển thị thông báo `Them vao gio hang thanh cong`.
- Giỏ hàng có sản phẩm `Laptop Dell` với số lượng `2`.
- Tổng tiền dòng sản phẩm là `15.000.000 x 2 = 30.000.000đ`.
- Cart total là `30.000.000đ` nếu giỏ hàng trước đó rỗng.
- API `POST /api/cart/add` trả HTTP `200 OK`.
- Response có `success = true`.

---
### TC_CART_002 - Không cho thêm sản phẩm vượt quá tồn kho
| Trường        | Nội dung                                  |
|---------------|-------------------------------------------|
| Test Case ID  | TC_CART_002                               |
| Test Name     | Thêm sản phẩm với số lượng vượt tồn kho   |
| Priority      | Critical                                  |
| Actual Result | Để trống - điền sau khi chạy test thực tế |
| Status        | Not Run                                   |

**Preconditions**
- Ứng dụng frontend và backend đang chạy.
- Sản phẩm `P001 - Laptop Dell` tồn tại.
- Trạng thái sản phẩm là `ACTIVE`.
- Tồn kho hiện tại của sản phẩm là `10`.
- Người dùng đang thao tác với user mặc định `user01`.

**Test Steps**
1. Truy cập màn hình Cart/Product.
2. Chọn sản phẩm `Laptop Dell`.
3. Nhập số lượng `11`.
4. Nhấn nút `Thêm vào giỏ hàng`.
5. Quan sát thông báo lỗi và trạng thái giỏ hàng.

**Test Data**
| Thuộc tính      | Giá trị     |
|-----------------|-------------|
| Product ID      | P001        |
| Product Name    | Laptop Dell |
| Quantity        | 11          |
| Stock available | 10          |

**Expected Result**
- Hệ thống không thêm sản phẩm vào giỏ.
- Hiển thị thông báo lỗi `So luong vuot qua ton kho hien tai` hoặc thông báo tương đương.
- Cart total không thay đổi.
- Không tạo hoặc cập nhật `CartItem` vượt tồn kho.

---
### TC_CART_003 - Không cho thêm sản phẩm hết hàng
| Trường        | Nội dung                                  |
|---------------|-------------------------------------------|
| Test Case ID  | TC_CART_003                               |
| Test Name     | Thêm sản phẩm hết hàng vào giỏ            |
| Priority      | High                                      |
| Actual Result | Để trống - điền sau khi chạy test thực tế |
| Status        | Not Run                                   |

**Preconditions**
- Ứng dụng frontend và backend đang chạy.
- Sản phẩm `P003 - Keyboard Mechanical` tồn tại.
- Trạng thái sản phẩm là `ACTIVE`.
- Tồn kho hiện tại của sản phẩm là `0`.
- Người dùng đang thao tác với user mặc định `user01`.

**Test Steps**
1. Truy cập màn hình Cart/Product.
2. Chọn sản phẩm `Keyboard Mechanical` hoặc gửi request thêm sản phẩm `P003`.
3. Nhập số lượng `1`.
4. Nhấn nút `Thêm vào giỏ hàng`.
5. Kiểm tra thông báo lỗi.

**Test Data**
| Thuộc tính      | Giá trị             |
|-----------------|---------------------|
| Product ID      | P003                |
| Product Name    | Keyboard Mechanical |
| Quantity        | 1                   |
| Stock available | 0                   |

**Expected Result**
- Hệ thống không thêm sản phẩm vào giỏ hàng.
- Hiển thị thông báo lỗi `San pham da het hang` hoặc thông báo tương đương.
- Cart total không thay đổi.
- Danh sách sản phẩm trong giỏ không thay đổi.

---
### TC_CART_004 - Xóa sản phẩm khỏi giỏ hàng thành công
| Trường        | Nội dung                                  |
|---------------|-------------------------------------------|
| Test Case ID  | TC_CART_004                               |
| Test Name     | Xóa sản phẩm đang có trong giỏ hàng       |
| Priority      | High                                      |
| Actual Result | Để trống - điền sau khi chạy test thực tế |
| Status        | Not Run                                   |

**Preconditions**
- Ứng dụng frontend và backend đang chạy.
- Giỏ hàng của `user01` đã có sản phẩm `P001 - Laptop Dell`.
- Số lượng sản phẩm `P001` trong giỏ là `2`.
- Cart total hiện tại có bao gồm dòng tiền của `P001`.

**Test Steps**
1. Truy cập màn hình Cart.
2. Kiểm tra sản phẩm `Laptop Dell` đang hiển thị trong giỏ.
3. Nhấn nút `Xóa` tại dòng sản phẩm `Laptop Dell`.
4. Xác nhận thao tác nếu hệ thống có hộp thoại xác nhận.
5. Quan sát danh sách giỏ hàng và tổng tiền.

**Test Data**
| Thuộc tính             | Giá trị     |
|------------------------|-------------|
| User ID                | user01      |
| Product ID             | P001        |
| Quantity before remove | 2           |
| Removed amount         | 30.000.000đ |

**Expected Result**
- Hệ thống hiển thị thông báo `Xoa san pham khoi gio hang thanh cong` hoặc thông báo tương đương.
- Dòng sản phẩm `Laptop Dell` không còn hiển thị trong giỏ hàng.
- Cart total giảm đúng bằng `30.000.000đ` nếu trước đó có `2` sản phẩm `P001`.
- API `DELETE /api/cart/remove/P001` trả HTTP `200 OK`.
- Response có `success = true`.

---
### TC_CART_005 - Cập nhật số lượng sản phẩm trong giỏ hàng thành công
| Trường        | Nội dung                                  |
|---------------|-------------------------------------------|
| Test Case ID  | TC_CART_005                               |
| Test Name     | Cập nhật số lượng sản phẩm trong giỏ hàng |
| Priority      | High                                      |
| Actual Result | Để trống - điền sau khi chạy test thực tế |
| Status        | Not Run                                   |

**Preconditions**
- Ứng dụng frontend và backend đang chạy.
- Giỏ hàng của `user01` đã có sản phẩm `P002 - Mouse Logitech`.
- Số lượng ban đầu là `1`.
- Sản phẩm `P002` có tồn kho là `50`.
- Trạng thái sản phẩm là `ACTIVE`.

**Test Steps**
1. Truy cập màn hình Cart.
2. Tại dòng sản phẩm `Mouse Logitech`, đổi số lượng từ `1` thành `5`.
3. Nhấn nút `Cập nhật` hoặc kích hoạt chức năng update quantity.
4. Quan sát thông báo và tổng tiền giỏ hàng.

**Test Data**
| Thuộc tính      | Giá trị  |
|-----------------|----------|
| User ID         | user01   |
| Product ID      | P002     |
| Old Quantity    | 1        |
| New Quantity    | 5        |
| Price           | 500.000đ |
| Stock available | 50       |

**Expected Result**
- Hệ thống hiển thị thông báo `Cap nhat so luong thanh cong` hoặc thông báo tương đương.
- Số lượng sản phẩm `Mouse Logitech` trong giỏ được cập nhật thành `5`.
- Line total là `500.000 x 5 = 2.500.000đ`.
- API `PUT /api/cart/update` trả HTTP `200 OK`.
- Response có `success = true`.

---
## 4. Traceability
| Test Case ID | Có thể kiểm chứng bằng                                                                                                                                         |
|--------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| TC_CART_001  | `frontend/src/tests/cartValidation.test.js`, `frontend/src/tests/Cart.integration.test.jsx`, `frontend/e2e/cart.e2e.spec.ts`, `backend/src/test/java/com/shopcart/service/CartServiceTest.java`                                                                                                                                          |
| TC_CART_002  | `frontend/src/tests/cartValidation.test.js`, `frontend/src/tests/Cart.integration.test.jsx`, `backend/src/test/java/com/shopcart/service/CartServiceTest.java` |
| TC_CART_003  | `backend/src/test/java/com/shopcart/service/CartServiceTest.java`, manual/API test                                                                             |
| TC_CART_004  | `backend/src/test/java/com/shopcart/service/CartServiceTest.java`, manual/API test                                                                             |
| TC_CART_005  | `backend/src/test/java/com/shopcart/service/CartServiceTest.java`, manual/API test                                                                             |