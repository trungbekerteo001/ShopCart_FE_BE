# Security Test Cases - ShopCart
## 1. Mục tiêu
Tài liệu này mô tả các test cases kiểm thử bảo mật cho các luồng chính của dự án **ShopCart**, gồm:
- Cart
- Inventory
- Purchase / Checkout
- Order API

Các nhóm rủi ro được kiểm thử:
- SQL Injection
- Cross-Site Scripting (XSS)
- IDOR / Broken Access Control
- Missing Authorization
- CSRF / Cross-site request risk
- Input Validation

## 2. Phạm vi kiểm thử
| Nhóm chức năng      | API / Màn hình liên quan                                                                             | Phạm vi kiểm thử                                 |
|---------------------|------------------------------------------------------------------------------------------------------|--------------------------------------------------|
| Cart                | `POST /api/cart/add`, `GET /api/cart`, `PUT /api/cart/update`, `DELETE /api/cart/remove/{productId}` | Thêm, xem, cập nhật, xóa sản phẩm trong giỏ hàng |
| Inventory           | `GET /api/inventory/{productId}`, `GET /api/inventory/{productId}/check`                             | Kiểm tra tồn kho và trạng thái sản phẩm          |
| Purchase / Checkout | `POST /api/orders`, `GET /api/orders/{orderId}`, `PUT /api/orders/{orderId}/cancel`                  | Tạo đơn hàng, xem đơn hàng, hủy đơn hàng         |

## 3. Môi trường kiểm thử
| Thành phần       | Giá trị                                            |
|------------------|----------------------------------------------------|
| Backend URL      | `http://localhost:8080`                            |
| Frontend URL     | `http://localhost:5173`                            |
| Công cụ          | Postman, curl, VS Code REST Client hoặc Swagger    |
| File request mẫu | `security/security-requests.http`                  |
| Test data        | `P001`, `P002`, `P003`, `P004`, `user01`, `user02` |

## 4. Bảng tổng hợp test cases
| Test Case ID | Nhóm rủi ro                  | Tên test case                                           | Priority | Status dự kiến          |
|--------------|------------------------------|---------------------------------------------------------|----------|-------------------------|
| TC_SEC_001   | SQL Injection                | Kiểm tra SQL Injection tại `productId` của Cart API     | Critical | Pass sau khi xác nhận   |
| TC_SEC_002   | SQL Injection                | Kiểm tra SQL Injection tại `orderId`                    | High     | Pass sau khi xác nhận   |
| TC_SEC_003   | XSS                          | Kiểm tra XSS tại trường `shippingAddress`               | High     | Needs Improvement       |
| TC_SEC_004   | IDOR / Broken Access Control | Đổi header `X-USER-ID` để xem/sửa dữ liệu người khác    | Critical | Fail / Security Finding |
| TC_SEC_005   | Missing Authorization        | Tạo đơn hàng khi không có token hoặc định danh xác thực | Critical | Fail / Security Finding |
| TC_SEC_006   | CSRF                         | Mô phỏng request thay đổi dữ liệu từ origin lạ          | Medium   | Needs Improvement       |
| TC_SEC_007   | Input Validation             | Kiểm tra `quantity` âm hoặc vượt tồn kho                | High     | Pass sau khi xác nhận   |

---
## 5. Test cases chi tiết
## TC_SEC_001 - SQL Injection tại `productId` của Cart API
| Trường       | Nội dung                                            |
|--------------|-----------------------------------------------------|
| Test Case ID | TC_SEC_001                                          |
| Test Name    | Kiểm tra SQL Injection tại `productId` của Cart API |
| Risk Group   | SQL Injection                                       |
| Priority     | Critical                                            |
| Status       | Pass sau khi xác nhận bằng request thực tế          |

### Preconditions
- Backend đang chạy tại `http://localhost:8080`.
- Dữ liệu mẫu đã có sản phẩm `P001`.

### Test Steps
1. Gửi request `POST /api/cart/add`.
2. Truyền `productId` là payload SQL Injection.
3. Quan sát status code và body trả về.
4. Kiểm tra hệ thống không trả về dữ liệu ngoài ý muốn.
5. Kiểm tra hệ thống không bị lỗi `500 Internal Server Error`.

### Test Data
{
  "productId": "P001' OR '1'='1",
  "quantity": 1
}


### Request mẫu
POST http://localhost:8080/api/cart/add
Content-Type: application/json
X-USER-ID: user01

{
  "productId": "P001' OR '1'='1",
  "quantity": 1
}


### Expected Result
- API từ chối payload hoặc trả lỗi validation/business phù hợp.
- Không tạo cart item.
- Không trả lỗi `500`.
- Không lộ thông tin database hoặc stack trace.

### Actual Result
Dự kiến API trả lỗi sản phẩm không tồn tại do repository tìm theo ID chính xác, không dùng query nối chuỗi trực tiếp.

---
## TC_SEC_002 - SQL Injection tại `orderId`
| Trường       | Nội dung                                                    |
|--------------|-------------------------------------------------------------|
| Test Case ID | TC_SEC_002                                                  |
| Test Name    | Kiểm tra SQL Injection khi truy vấn đơn hàng theo `orderId` |
| Risk Group   | SQL Injection                                               |
| Priority     | High                                                        |
| Status       | Pass sau khi xác nhận bằng request thực tế                  |

### Preconditions
- Backend đang chạy.
- Có hoặc không có đơn hàng đều có thể kiểm thử.

### Test Steps
1. Gửi request `GET /api/orders/{orderId}` với payload SQL Injection.
2. Quan sát phản hồi API.
3. Đảm bảo API không trả về danh sách đơn hàng hoặc dữ liệu không thuộc request.
4. Đảm bảo API không lỗi `500` và không lộ stack trace.

### Test Data
orderId = ORD-001' OR '1'='1


### Request mẫu
GET http://localhost:8080/api/orders/ORD-001%27%20OR%20%271%27=%271
X-USER-ID: user01

### Expected Result
- API trả lỗi không tìm thấy đơn hàng hoặc request không hợp lệ.
- Không trả dữ liệu đơn hàng trái phép.
- Không lộ thông tin database.

### Actual Result
Dự kiến API không tìm thấy đơn hàng vì dùng repository lookup theo ID, không nối chuỗi SQL trực tiếp.

---
## TC_SEC_003 - XSS tại trường `shippingAddress`
| Trường       | Nội dung                                               |
|--------------|--------------------------------------------------------|
| Test Case ID | TC_SEC_003                                             |
| Test Name    | Kiểm tra XSS tại trường `shippingAddress` khi Checkout |
| Risk Group   | XSS                                                    |
| Priority     | High                                                   |
| Status       | Needs Improvement                                      |

### Preconditions
- Backend đang chạy.
- Sản phẩm `P001` còn tồn kho.
- Có thể tạo đơn hàng bằng API `POST /api/orders`.

### Test Steps
1. Gửi request `POST /api/orders`.
2. Truyền `shippingAddress` chứa thẻ script HTML.
3. Kiểm tra API có chấp nhận chuỗi nguy hiểm không.
4. Nếu đơn được tạo, mở màn hình/response hiển thị địa chỉ.
5. Kiểm tra script có được thực thi trên UI không.
6. Ghi nhận nguy cơ stored XSS nếu dữ liệu được lưu và hiển thị lại không encode.

### Test Data
shippingAddress = <script>alert('xss')</script>

### Request mẫu
POST http://localhost:8080/api/orders
Content-Type: application/json
X-USER-ID: user01

{
  "items": [
    {
      "productId": "P001",
      "quantity": 1,
      "unitPrice": 15000000
    }
  ],
  "shippingAddress": "<script>alert('xss')</script>",
  "paymentMethod": "COD",
  "shippingFee": 50000
}

### Expected Result
- Hệ thống nên validate hoặc sanitize input.
- Nếu lưu dữ liệu, frontend phải encode output khi hiển thị.
- Script không được thực thi trên UI.

### Actual Result
Dự kiến backend hiện chỉ kiểm tra địa chỉ không rỗng, chưa sanitize nội dung HTML. React thường escape text khi render, nhưng vẫn cần ghi nhận rủi ro nếu sau này dùng `dangerouslySetInnerHTML` hoặc render HTML trực tiếp.

---
## TC_SEC_004 - IDOR khi đổi header `X-USER-ID`
| Trường       | Nội dung                                                       |
|--------------|----------------------------------------------------------------|
| Test Case ID | TC_SEC_004                                                     |
| Test Name    | Kiểm tra IDOR/Broken Access Control khi đổi header `X-USER-ID` |
| Risk Group   | IDOR / Broken Access Control                                   |
| Priority     | Critical                                                       |
| Status       | Fail / Security Finding                                        |

### Preconditions
- Backend đang chạy.
- Có dữ liệu cart cho `user01`.
- Có thể gửi request thủ công bằng REST Client, Postman hoặc curl.

### Test Steps
1. Gửi `POST /api/cart/add` với `X-USER-ID: user01` để tạo cart.
2. Gửi `GET /api/cart` với `X-USER-ID: user01` và ghi nhận dữ liệu.
3. Gửi lại `GET /api/cart` với `X-USER-ID: user02` hoặc một user khác.
4. Đánh giá API có kiểm tra token/identity thật hay chỉ tin vào header client gửi lên.

### Test Data
X-USER-ID: user01
X-USER-ID: user02

### Request mẫu
GET http://localhost:8080/api/cart
X-USER-ID: user02

### Expected Result
- Người dùng không được tự ý đổi định danh để xem/sửa dữ liệu của user khác.
- Hệ thống phải xác thực bằng token/session.
- `userId` nên được lấy từ server-side identity, không lấy trực tiếp từ header do client tự truyền.

### Actual Result
Dự kiến API hiện tin vào header `X-USER-ID` và còn có default `user01`, nên đây là điểm yếu bảo mật trong bản demo.

---
## TC_SEC_005 - Missing Authorization khi tạo đơn hàng
| Trường       | Nội dung                                                         |
|--------------|------------------------------------------------------------------|
| Test Case ID | TC_SEC_005                                                       |
| Test Name    | Kiểm tra tạo đơn hàng khi không có token hoặc thông tin xác thực |
| Risk Group   | Missing Authorization                                            |
| Priority     | Critical                                                         |
| Status       | Fail / Security Finding                                          |

### Preconditions
- Backend đang chạy.
- Sản phẩm `P001` còn hàng.
- Endpoint tạo đơn hàng có thể gọi trực tiếp bằng REST Client/Postman.

### Test Steps
1. Gửi request `POST /api/orders` không kèm token xác thực.
2. Không truyền header `X-USER-ID`.
3. Quan sát API có cho tạo đơn hàng không.
4. Kiểm tra response có mặc định user là `user01` không.

### Test Data
Không có Authorization header
Không có X-USER-ID header

### Request mẫu
POST http://localhost:8080/api/orders
Content-Type: application/json

{
  "items": [
    {
      "productId": "P001",
      "quantity": 1,
      "unitPrice": 15000000
    }
  ],
  "shippingAddress": "12 Nguyen Trai, TP.HCM",
  "paymentMethod": "COD",
  "shippingFee": 50000
}

### Expected Result
- API nên trả `401 Unauthorized` hoặc `403 Forbidden`.
- Không được tự gán mặc định user khi là endpoint nghiệp vụ quan trọng.
- Không tạo order nếu chưa xác thực.

### Actual Result
Dự kiến controller đang dùng `@RequestHeader(defaultValue = "user01")`, nên request không xác thực vẫn có thể được xử lý như `user01`.

---
## TC_SEC_006 - CSRF / Cross-site request risk
| Trường       | Nội dung                                                 |
|--------------|----------------------------------------------------------|
| Test Case ID | TC_SEC_006                                               |
| Test Name    | Kiểm tra rủi ro CSRF với API thay đổi dữ liệu Cart/Order |
| Risk Group   | CSRF                                                     |
| Priority     | Medium                                                   |
| Status       | Needs Improvement                                        |

### Preconditions
- Backend đang chạy.
- Frontend chạy tại `http://localhost:5173`.
- API có CORS giới hạn origin.

### Test Steps
1. Gửi request `POST /api/cart/add` từ origin không hợp lệ hoặc mô phỏng bằng Postman/curl.
2. Kiểm tra backend có yêu cầu CSRF token không.
3. Kiểm tra backend có cơ chế xác thực chống request giả mạo không.
4. Kiểm tra CORS có chặn trình duyệt từ origin lạ không.

### Test Data
Origin: http://evil.localhost
productId: P001
quantity: 1

### Request mẫu
POST http://localhost:8080/api/cart/add
Content-Type: application/json
Origin: http://evil.localhost
X-USER-ID: user01

{
  "productId": "P001",
  "quantity": 1
}

### Expected Result
- Nếu dùng cookie/session, API cần CSRF token.
- Nếu dùng Bearer token, cần kiểm soát CORS chặt.
- Origin lạ không được phép thao tác dữ liệu qua trình duyệt.

### Actual Result
Dự kiến project hiện dùng CORS giới hạn `http://localhost:5173`, nhưng chưa có Spring Security/CSRF token. Rủi ro ở mức trung bình trong demo, cần cải thiện nếu triển khai thật.

---
## TC_SEC_007 - Input Validation với `quantity` âm hoặc quá lớn

| Trường       | Nội dung                                         |
|--------------|--------------------------------------------------|
| Test Case ID | TC_SEC_007                                       |
| Test Name    | Kiểm tra validate quantity bất thường ở Cart API |
| Risk Group   | Input Validation                                 |
| Priority     | High                                             |
| Status       | Pass sau khi xác nhận bằng request thực tế       |

### Preconditions
- Backend đang chạy.
- Sản phẩm `P001` tồn tại và còn hàng.

### Test Steps
1. Gửi `POST /api/cart/add` với `quantity = -1`.
2. Gửi `POST /api/cart/add` với `quantity = 999999999`.
3. Quan sát lỗi validation/business.
4. Kiểm tra hệ thống không cập nhật cart sai.
5. Kiểm tra hệ thống không lỗi `500`.

### Test Data
{
  "productId": "P001",
  "quantity": -1
}

hoặc 
{
  "productId": "P001",
  "quantity": 999999999
}


### Request mẫu
POST http://localhost:8080/api/cart/add
Content-Type: application/json
X-USER-ID: user01

{
  "productId": "P001",
  "quantity": -1
}

### Expected Result
- API từ chối số lượng âm, số lượng bằng 0 và số lượng vượt tồn kho.
- Không cập nhật cart.
- Không lỗi `500`.
- Response có thông báo lỗi rõ ràng để frontend hiển thị.

### Actual Result
Dự kiến DTO có `@Min(1)` và service kiểm tra tồn kho, nên case này phải bị chặn.

---

## 6. Ma trận rủi ro tổng hợp
| Risk Group                   | Mức độ   | API liên quan    | Tình trạng dự kiến                                                           |
|------------------------------|----------|------------------|------------------------------------------------------------------------------|
| SQL Injection                | Critical | Cart, Order      | Pass nếu dùng repository lookup an toàn và không lộ lỗi DB                   |
| XSS                          | High     | Checkout / Order | Needs Improvement vì backend chưa sanitize input HTML                        |
| IDOR / Broken Access Control | Critical | Cart, Order      | Fail vì tin vào `X-USER-ID` do client gửi                                    |
| Missing Authorization        | Critical | Cart, Order      | Fail vì chưa có xác thực thật cho API nghiệp vụ                              |
| CSRF                         | Medium   | Cart, Order      | Needs Improvement; CORS có giới hạn nhưng chưa có security config hoàn chỉnh |
| Input Validation             | High     | Cart, Order      | Pass nếu validation và service business rules hoạt động đúng                 |

## 7. Ghi chú cập nhật sau khi chạy thực tế
Sau khi chạy file `security/security-requests.http`, cập nhật lại các mục:
- Actual Result
- Status
- Ảnh minh chứng response thực tế
- Nhận xét nếu response khác với dự kiến
