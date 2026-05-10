# Test Cases chi tiết - Chức năng Purchase/Checkout
> Tài liệu này trình bày test scenarios và test cases chi tiết cho chức năng Mua hàng/Checkout.
> Mỗi test case có đủ các trường theo template: **Test Case ID, Test Name, Priority, Preconditions, Test Steps, Test Data, Expected Result, Actual Result, Status**.

---
## 1. Dữ liệu kiểm thử tham chiếu
### 1.1 Product
| Product ID | Product Name        | Price       | Stock | Status   | Mục đích sử dụng                          |
|------------|---------------------|-------------|-------|----------|-------------------------------------------|
| P001       | Laptop Dell         | 15.000.000đ | 10    | ACTIVE   | Dùng cho checkout thành công              |
| P002       | Mouse Logitech      | 500.000đ    | 50    | ACTIVE   | Dùng để kiểm tra nhiều sản phẩm trong đơn |
| P003       | Keyboard Mechanical | 2.000.000đ  | 0     | ACTIVE   | Sản phẩm hết hàng                         |
| P004       | Old Monitor         | 3.000.000đ  | 5     | INACTIVE | Sản phẩm không còn bán                    |

### 1.2 Coupon
| Coupon Code | Loại giảm giá | Giá trị  | Mục đích sử dụng       |
|-------------|---------------|----------|------------------------|
| SALE10      | Percent       | 10%      | Mã hợp lệ              | 
| SALE20      | Percent       | 20%      | Mã hợp lệ              |
| FIXED100K   | Fixed amount  | 100.000đ | Mã hợp lệ              |
| INVALID     | Không hợp lệ  | -        | Dùng cho negative test |
| EXPIRED10   | Không hợp lệ  | -        | Dùng cho negative test |

---
## 2. Test scenarios và mức độ ưu tiên
| Scenario ID     | Scenario                                                  | Loại kiểm thử | Priority |
|-----------------|-----------------------------------------------------------|---------------|----------|
| SC_PURCHASE_001 | Đặt hàng thành công và tồn kho được cập nhật              | Happy path    | Critical |
| SC_PURCHASE_002 | Đặt hàng khi sản phẩm hết hàng                            | Negative      | Critical |
| SC_PURCHASE_003 | Kiểm tra tính toán tổng tiền, giảm giá và phí vận chuyển  | Functional    | Critical |
| SC_PURCHASE_004 | Đặt hàng với mã giảm giá không hợp lệ                     | Negative      | High     |
| SC_PURCHASE_005 | Đặt hàng khi số lượng vượt tồn kho tại thời điểm checkout | Negative      | Critical |
| SC_PURCHASE_006 | Đặt hàng với số lượng tối thiểu bằng 1                    | Boundary      | Medium   |
| SC_PURCHASE_007 | Đặt hàng đúng bằng số lượng tồn kho cuối cùng             | Edge case     | High     |
| SC_PURCHASE_008 | Địa chỉ giao hàng rỗng hoặc phương thức thanh toán rỗng   | Negative      | High     |

Giải thích ưu tiên:
- **Critical:** lỗi có thể làm sai đơn hàng, sai tồn kho hoặc sai số tiền thanh toán.
- **High:** lỗi ảnh hưởng trực tiếp tới trải nghiệm checkout và khả năng hoàn tất mua hàng.
- **Medium:** lỗi thuộc trường hợp biên hoặc ít gặp hơn nhưng vẫn cần kiểm thử.
- **Low:** lỗi ít ảnh hưởng tới nghiệp vụ chính.

---
## 3. Test cases chi tiết
### TC_PURCHASE_001 - Đặt hàng thành công và tồn kho được cập nhật
| Trường        | Nội dung                                                                                                                                                      |
|---------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Test Case ID  | TC_PURCHASE_001                                                                                                                                               |
| Test Name     | Đặt hàng thành công và cập nhật tồn kho                                                                                                                       |
| Priority      | Critical                                                                                                                                                      |
| Actual Result | Hệ thống tạo đơn hàng thành công với trạng thái `PENDING`. Tổng tiền được tính đúng theo subtotal, coupon và phí vận chuyển. Tồn kho các sản phẩm trong đơn hàng được cập nhật sau khi đặt hàng                                                                                                                                             |
| Status        | Pass                                                                                                                                                          |

**Preconditions**
- Ứng dụng frontend và backend đang chạy.
- Người dùng đang thao tác với user mặc định `user01`.
- Giỏ hàng có `P001` số lượng `2`.
- Giỏ hàng có `P002` số lượng `1`.
- Sản phẩm `P001` còn tồn kho `10`.
- Sản phẩm `P002` còn tồn kho `50`.
- Coupon `SALE10` hợp lệ.

**Test Steps**
1. Truy cập màn hình Cart.
2. Kiểm tra giỏ hàng có `Laptop Dell x2` và `Mouse Logitech x1`.
3. Nhấn nút chuyển sang Checkout.
4. Nhập địa chỉ giao hàng hợp lệ.
5. Chọn phương thức thanh toán, ví dụ `COD`.
6. Nhập coupon `SALE10` nếu có ô nhập coupon.
7. Kiểm tra tổng tiền hiển thị.
8. Nhấn nút `Đặt hàng`.

**Test Data**
| Thuộc tính       | Giá trị                |
|------------------|------------------------|
| Product 1        | P001 - Laptop Dell     |
| Quantity 1       | 2                      |
| Price 1          | 15.000.000đ            |
| Product 2        | P002 - Mouse Logitech  |
| Quantity 2       | 1                      |
| Price 2          | 500.000đ               |
| Coupon           | SALE10                 |
| Shipping Fee     | 50.000đ                |
| Shipping Address | 12 Nguyen Trai, TP.HCM |
| Payment Method   | COD                    |

**Expected Result**
- Order được tạo thành công với trạng thái `PENDING`.
- Subtotal là `15.000.000 x 2 + 500.000 x 1 = 30.500.000đ`.
- Discount `SALE10` là `3.050.000đ`.
- Shipping fee là `50.000đ`.
- Total là `30.500.000 - 3.050.000 + 50.000 = 27.500.000đ`.
- Tồn kho `P001` giảm từ `10` còn `8`.
- Tồn kho `P002` giảm từ `50` còn `49`.
- Giỏ hàng của `user01` được xóa sau khi checkout thành công.
- API `POST /api/orders` trả HTTP `201 Created`.
- Response có `success = true` hoặc có thông tin order hợp lệ.

---
### TC_PURCHASE_002 - Không cho đặt hàng khi sản phẩm hết hàng
| Trường        | Nội dung                                                                                                                           |
|---------------|------------------------------------------------------------------------------------------------------------------------------------|
| Test Case ID  | TC_PURCHASE_002                                                                                                                    |
| Test Name     | Đặt hàng thất bại khi sản phẩm hết hàng                                                                                            |
| Priority      | Critical                                                                                                                           |
| Actual Result | Khi đặt hàng với sản phẩm hết hàng, hệ thống không tạo order mới. API/UI trả về thông báo lỗi phù hợp và tồn kho không bị thay đổi |
| Status        | Pass                                                                                                                               |

**Preconditions**
- Ứng dụng frontend và backend đang chạy.
- Sản phẩm `P003 - Keyboard Mechanical` tồn tại.
- Trạng thái sản phẩm là `ACTIVE`.
- Tồn kho hiện tại của sản phẩm là `0`.
- Người dùng đang thao tác với user mặc định `user01`.

**Test Steps**
1. Gửi request checkout hoặc thao tác UI đặt hàng với sản phẩm `P003`.
2. Nhập số lượng `1`.
3. Nhập địa chỉ giao hàng hợp lệ.
4. Chọn phương thức thanh toán hợp lệ.
5. Nhấn `Đặt hàng`.

**Test Data**
| Thuộc tính       | Giá trị                |
|------------------|------------------------|
| Product ID       | P003                   |
| Product Name     | Keyboard Mechanical    |
| Quantity         | 1                      |
| Stock available  | 0                      |
| Shipping Address | 12 Nguyen Trai, TP.HCM |
| Payment Method   | COD                    |
| Shipping Fee     | 50.000đ                |

**Expected Result**
- Hệ thống không tạo order.
- Hiển thị thông báo lỗi `Ton kho khong du de tao don hang`, `San pham da het hang` hoặc thông báo tương đương.
- Không trừ tồn kho.
- Không xóa giỏ hàng nếu checkout thất bại.

---
### TC_PURCHASE_003 - Tính tổng tiền chính xác với coupon và phí vận chuyển
| Trường        | Nội dung                                                                                                                          |
|---------------|-----------------------------------------------------------------------------------------------------------------------------------|
| Test Case ID  | TC_PURCHASE_003                                                                                                                   |
| Test Name     | Kiểm tra tính toán subtotal, discount, shipping và total                                                                          |
| Priority      | Critical                                                                                                                          |
| Actual Result | Hệ thống tính đúng subtotal, discount, shipping fee và total. Kết quả tính tiền trên giao diện/API khớp với công thức kiểm thử    |
| Status        | Pass                                                                                                                              |

**Preconditions**
- Ứng dụng frontend và backend đang chạy.
- Có dữ liệu sản phẩm `P001` và `P002`.
- Coupon `SALE20` hợp lệ.
- Người dùng đang ở màn hình Checkout.

**Test steps**
1. Thêm `P001` số lượng `1` vào giỏ hàng.
2. Thêm `P002` số lượng `2` vào giỏ hàng.
3. Chuyển sang màn hình Checkout.
4. Nhập coupon `SALE20`.
5. Kiểm tra subtotal, discount, shipping và total hiển thị.
6. Không nhất thiết phải nhấn đặt hàng nếu chỉ kiểm tra tính giá.

**Test Data**
| Thuộc tính   | Giá trị               |
|--------------|-----------------------|
| Product 1    | P001 - Laptop Dell    |
| Quantity 1   | 1                     |
| Price 1      | 15.000.000đ           |
| Product 2    | P002 - Mouse Logitech |
| Quantity 2   | 2                     |
| Price 2      | 500.000đ              |
| Coupon       | SALE20                |
| Shipping Fee | 50.000đ               |

**Expected Result**
- Subtotal là `15.000.000 x 1 + 500.000 x 2 = 16.000.000đ`.
- Discount `SALE20` là `3.200.000đ`.
- Shipping fee là `50.000đ`.
- Total là `16.000.000 - 3.200.000 + 50.000 = 12.850.000đ`.
- Giao diện và API tính giá thống nhất.
- Tổng tiền không phát sinh giá trị âm.

---
### TC_PURCHASE_004 - Không cho đặt hàng với mã giảm giá không hợp lệ
| Trường        | Nội dung                                                                                                                                                      |
|---------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Test Case ID  | TC_PURCHASE_004                                                                                                                                               |
| Test Name     | Checkout thất bại khi coupon không hợp lệ                                                                                                                     |
| Priority      | High                                                                                                                                                          |
| Actual Result | Khi nhập mã giảm giá không hợp lệ hoặc hết hạn, hệ thống không áp dụng giảm giá sai. Thông báo lỗi coupon được hiển thị và người dùng có thể sửa lại mã giảm giá.                                                                                                                                                                            |
| Status        | Pass                                                                                                                                                          |

**Preconditions**
- Ứng dụng frontend và backend đang chạy.
- Giỏ hàng có ít nhất một sản phẩm hợp lệ, ví dụ `P001` số lượng `1`.
- Người dùng đang ở màn hình Checkout.

**Test Steps**
1. Truy cập màn hình Checkout.
2. Nhập coupon `INVALID` hoặc `EXPIRED10`.
3. Nhập địa chỉ giao hàng hợp lệ.
4. Chọn phương thức thanh toán hợp lệ.
5. Nhấn `Đặt hàng`.

**Test Data**
| Thuộc tính       | Giá trị                |
|------------------|------------------------|
| Product ID       | P001                   |
| Quantity         | 1                      |
| Coupon           | INVALID                |
| Shipping Fee     | 50.000đ                |
| Shipping Address | 12 Nguyen Trai, TP.HCM |
| Payment Method   | COD                    |

**Expected Result**
- Hệ thống không tạo order với coupon không hợp lệ.
- Hiển thị thông báo lỗi `Ma giam gia khong hop le` hoặc thông báo tương đương.
- Không trừ tồn kho.
- Cart vẫn giữ nguyên để người dùng sửa coupon hoặc đặt lại.

---
### TC_PURCHASE_005 - Không cho đặt hàng khi số lượng vượt tồn kho tại thời điểm checkout
| Trường        | Nội dung                                                                                                                                                   |
|---------------|------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Test Case ID  | TC_PURCHASE_005                                                                                                                                            |
| Test Name     | Checkout thất bại khi số lượng đặt vượt tồn kho                                                                                                            |
| Priority      | Critical                                                                                                                                                   |
| Actual Result | Khi số lượng đặt hàng vượt quá tồn kho tại thời điểm checkout, hệ thống không tạo order. Tồn kho sản phẩm không bị trừ và giỏ hàng vẫn được giữ nguyên để người dùng điều chỉnh.                                                                                                                                                       |
| Status        | Pass                                                                                                                                                       |

**Preconditions**
- Ứng dụng frontend và backend đang chạy.
- Sản phẩm `P001 - Laptop Dell` tồn tại.
- Tồn kho hiện tại của sản phẩm là `10`.
- Người dùng chuẩn bị checkout với số lượng `11`.

**Test Steps**
1. Tạo giỏ hàng hoặc gửi request checkout với `P001` số lượng `11`.
2. Nhập địa chỉ giao hàng hợp lệ.
3. Chọn phương thức thanh toán hợp lệ.
4. Nhấn `Đặt hàng`.
5. Quan sát thông báo lỗi và trạng thái dữ liệu.

**Test Data**
| Thuộc tính       | Giá trị                |
|------------------|------------------------|
| Product ID       | P001                   |
| Product Name     | Laptop Dell            |
| Quantity         | 11                     |
| Stock available  | 10                     |
| Shipping Address | 12 Nguyen Trai, TP.HCM |
| Payment Method   | COD                    |
| Shipping Fee     | 50.000đ                |

**Expected Result**
- Hệ thống không tạo order.
- Hiển thị thông báo `Ton kho khong du de tao don hang` hoặc `So luong vuot qua ton kho hien tai`.
- Tồn kho `P001` vẫn là `10`.
- Không xóa giỏ hàng khi checkout thất bại.

---
## 4. Traceability
| Test Case ID    | Có thể kiểm chứng bằng                                                                                                                                      |
|-----------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------|
| TC_PURCHASE_001 | `frontend/src/tests/priceCalculation.test.js`, `frontend/src/tests/Checkout.integration.test.jsx`, `frontend/e2e/purchase.e2e.spec.ts`, `backend/src/test/java/com/shopcart/service/OrderServiceTest.java`                                                                                                                                |
| TC_PURCHASE_002 | `frontend/src/tests/priceCalculation.test.js`, `backend/src/test/java/com/shopcart/service/OrderServiceTest.java`, manual/API test                          |
| TC_PURCHASE_003 | `frontend/src/tests/priceCalculation.test.js`, `frontend/src/tests/Checkout.integration.test.jsx`, `backend/src/test/java/com/shopcart/service/OrderServiceTest.java`                                                                                                                                                          |
| TC_PURCHASE_004 | `frontend/src/tests/priceCalculation.test.js`, `backend/src/test/java/com/shopcart/service/OrderServiceTest.java`, manual/API test |
| TC_PURCHASE_005 | `frontend/src/tests/priceCalculation.test.js`, `frontend/e2e/purchase.e2e.spec.ts`, `backend/src/test/java/com/shopcart/service/OrderServiceTest.java`      |
