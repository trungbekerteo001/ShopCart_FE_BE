# Security Testing Report - ShopCart

## 1. Mục đích báo cáo

Báo cáo này ghi nhận kết quả kiểm thử bảo mật cho các luồng:

- Cart
- Checkout / Purchase
- Order API
- Inventory API

Mục tiêu là đáp ứng phần **Advanced Testing - Security Testing**, gồm:

- Thiết kế test cases cho ít nhất 2 nhóm rủi ro bảo mật.
- Mô phỏng request kiểm thử.
- Ghi nhận kết quả.
- Mô tả tác động bảo mật.
- Đề xuất biện pháp khắc phục.

## 2. Phạm vi và công cụ

| Nội dung          | Mô tả                                                                   | 
|-------------------|-------------------------------------------------------------------------|
| Hệ thống kiểm thử | ShopCart_FE_BE                                                          |
| Backend           | Spring Boot REST API                                                    |
| Frontend          | React + Vite                                                            |
| API base URL      | `http://localhost:8080`                                                 |
| Công cụ mô phỏng  | Postman, curl, VS Code REST Client                                      |
| File request mẫu  | `security/security-requests.http`                                       |
| Nhóm rủi ro chính | SQL Injection, XSS, IDOR, Missing Authorization, CSRF, Input Validation |

## 3. Cách chạy request mô phỏng

Có thể chạy nhanh bằng file:
security/security-requests.http

Nếu dùng VS Code:
1. Cài extension REST Client.
2. Mở file `security/security-requests.http`.
3. Bấm Send Request ở từng request.
4. Chụp lại response để đưa vào báo cáo cuối.

## 4. Bảng tổng hợp kết quả kiểm thử

| ID         | Nhóm rủi ro           | Request kiểm thử                                                         | Status            |
|------------|-----------------------|--------------------------------------------------------------------------|-------------------|
| TC_SEC_001 | SQL Injection         | `POST /api/cart/add` với `productId = P001' OR '1'='1`                   | Pass              |
| TC_SEC_002 | SQL Injection         | `GET /api/orders/ORD-001' OR '1'='1`                                     | Pass              |
| TC_SEC_003 | XSS                   | `POST /api/orders` với `shippingAddress = <script>alert('xss')</script>` | Needs Improvement |
| TC_SEC_004 | IDOR                  | Đổi header `X-USER-ID` để xem/sửa cart                                   | Fail              |
| TC_SEC_005 | Missing Authorization | Tạo order không có token và không có `X-USER-ID`                         | Fail              |
| TC_SEC_006 | CSRF                  | Gửi request thay đổi dữ liệu với origin lạ                               | Needs Improvement |
| TC_SEC_007 | Input Validation      | `quantity = -1` hoặc `quantity` vượt tồn kho                             | Pass              |

> Ghi chú: kết quả hiện tại được ghi theo phân tích mã nguồn và mô phỏng request. Sau khi chạy trực tiếp trên máy local, cập nhật lại nếu response thực tế khác.

---

## 5. Kết quả chi tiết
## 5.1 SQL Injection
### Request kiểm thử
Các payload đã dùng:

P001' OR '1'='1
ORD-001' OR '1'='1

API liên quan:
- `POST /api/cart/add`
- `GET /api/orders/{orderId}`

### Expected Result
- Hệ thống không tạo cart item sai.
- Hệ thống không trả dữ liệu đơn hàng trái phép.
- Hệ thống không lỗi `500`.
- Hệ thống không lộ stack trace hoặc thông tin database.

### Actual Result dự kiến
Hệ thống dự kiến không bị SQL Injection vì luồng hiện tại sử dụng repository lookup theo khóa/id, không nối chuỗi SQL thủ công.

### Đánh giá
| Tiêu chí                | Kết quả                                                     |
|-------------------------|-------------------------------------------------------------|
| Mức độ rủi ro           | Critical                                                    |
| Tình trạng              | Pass sau khi xác nhận bằng request thực tế                  |
| Tác động nếu lỗi xảy ra | Có thể đọc/sửa dữ liệu trái phép, bỏ qua điều kiện truy vấn |

### Đề xuất
- Tiếp tục dùng Spring Data JPA repository hoặc parameterized query.
- Không dùng raw SQL nối chuỗi từ `productId`, `orderId`, `couponCode`.
- Không trả stack trace hoặc chi tiết exception database ra client.

---
## 5.2 Cross-Site Scripting - XSS
### Request kiểm thử
Payload dùng ở trường `shippingAddress`:

<script>alert('xss')</script>

API liên quan:
- `POST /api/orders`
- Màn hình hiển thị lịch sử đơn hàng hoặc chi tiết đơn hàng nếu có

### Expected Result
- Script không được thực thi trên giao diện.
- Dữ liệu người dùng nhập phải được encode khi hiển thị.
- Hệ thống nên validate hoặc sanitize các trường text có nguy cơ chứa HTML.

### Actual Result dự kiến
Backend hiện chủ yếu kiểm tra địa chỉ không rỗng, chưa sanitize HTML. React thường escape text khi render, nhưng nếu sau này dùng `dangerouslySetInnerHTML` hoặc render HTML trực tiếp thì có thể phát sinh stored XSS.

### Đánh giá
| Tiêu chí                | Kết quả                                                                              |
|-------------------------|--------------------------------------------------------------------------------------|
| Mức độ rủi ro           | High                                                                                 |
| Tình trạng              | Needs Improvement                                                                    |
| Tác động nếu lỗi xảy ra | Chèn script vào trình duyệt người dùng, đánh cắp token/session, thay đổi nội dung UI |

### Đề xuất
- Encode output khi hiển thị dữ liệu người dùng nhập.
- Không dùng `dangerouslySetInnerHTML` với dữ liệu đến từ API.
- Bổ sung validation/sanitization cho `shippingAddress`, `couponCode`, ghi chú đơn hàng nếu có.
- Thêm security header như `Content-Security-Policy` khi triển khai thật.

---
## 5.3 IDOR / Broken Access Control
### Request kiểm thử
Mô phỏng đổi user bằng header:

X-USER-ID: user01
X-USER-ID: user02

API liên quan:
- `GET /api/cart`
- `POST /api/cart/add`
- `PUT /api/cart/update`
- `DELETE /api/cart/remove/{productId}`
- `GET /api/orders/{orderId}`

### Expected Result
- Người dùng không được tự đổi định danh để xem hoặc sửa dữ liệu người khác.
- API phải xác thực user bằng token/session.
- `userId` phải lấy từ authentication principal phía server.

### Actual Result dự kiến
API hiện có nguy cơ tin vào `X-USER-ID` do client gửi lên. Một số controller còn dùng giá trị mặc định `user01`, phù hợp demo nhưng không an toàn cho hệ thống thật.

### Đánh giá
| Tiêu chí                | Kết quả                                                                   |
|-------------------------|---------------------------------------------------------------------------|
| Mức độ rủi ro           | Critical                                                                  |
| Tình trạng              | Fail / Security Finding                                                   |
| Tác động nếu lỗi xảy ra | Người dùng có thể truy cập hoặc thay đổi giỏ hàng/đơn hàng của người khác |

### Đề xuất
- Bỏ `defaultValue = "user01"` ở các API nghiệp vụ.
- Tích hợp Spring Security + JWT/session.
- Lấy `userId` từ authentication principal phía server.
- Với `GET /api/orders/{orderId}`, kiểm tra order thuộc về user hiện tại trước khi trả dữ liệu.

---
## 5.4 Missing Authorization
### Request kiểm thử
Gửi request tạo đơn hàng không có:

Authorization header
X-USER-ID header

API liên quan:
- `POST /api/orders`
- `POST /api/cart/add`
- `PUT /api/cart/update`
- `DELETE /api/cart/remove/{productId}`

### Expected Result
- API trả `401 Unauthorized` nếu chưa đăng nhập.
- API trả `403 Forbidden` nếu không có quyền thao tác tài nguyên.
- Không tạo order hoặc cập nhật cart khi request chưa xác thực.

### Actual Result dự kiến
Controller có nguy cơ xử lý request như `user01` nếu đang dùng `@RequestHeader(defaultValue = "user01")`.

### Đánh giá
| Tiêu chí                | Kết quả                                                      |
|-------------------------|--------------------------------------------------------------|
| Mức độ rủi ro           | Critical                                                     |
| Tình trạng              | Fail / Security Finding                                      |
| Tác động nếu lỗi xảy ra | Request không xác thực vẫn có thể thay đổi dữ liệu nghiệp vụ |

### Đề xuất
- Bảo vệ API bằng Spring Security.
- Bắt buộc token/session cho các endpoint nghiệp vụ.
- Bổ sung integration/security tests cho status code `401` và `403`.
- Không dùng user mặc định cho các endpoint tạo/cập nhật/xóa dữ liệu.

---
## 5.5 CSRF / Cross-site request risk
### Request kiểm thử
Mô phỏng request với origin lạ:

Origin: http://evil.localhost

API liên quan:
- `POST /api/cart/add`
- `POST /api/orders`
- Các API thay đổi dữ liệu khác

### Expected Result
- Origin lạ không được phép thao tác dữ liệu qua trình duyệt.
- Nếu dùng cookie/session, phải có CSRF token.
- Nếu dùng Bearer token, cần kiểm soát CORS chặt và không lưu token trong cookie không bảo vệ.

### Actual Result dự kiến
Project có CORS giới hạn `http://localhost:5173`, nhưng chưa có security configuration hoàn chỉnh như authentication/authorization/CSRF token.

### Đánh giá
| Tiêu chí                | Kết quả                                                        |
|-------------------------|----------------------------------------------------------------|
| Mức độ rủi ro           | Medium                                                         |
| Tình trạng              | Needs Improvement                                              |
| Tác động nếu lỗi xảy ra | Có thể bị request giả mạo nếu dùng cookie/session không bảo vệ |

### Đề xuất
- Nếu dùng JWT Bearer token, gửi token qua `Authorization` header.
- Nếu dùng session/cookie, bật CSRF token.
- Cấu hình CORS chỉ cho phép frontend thật.
- Không dùng wildcard `*` cho production.

---
## 5.6 Input Validation
### Request kiểm thử
Các giá trị bất thường:

quantity = -1
quantity = 999999999
productId = empty/null

API liên quan:
- `POST /api/cart/add`
- `PUT /api/cart/update`
- `POST /api/orders`

### Expected Result
- API từ chối số lượng âm, số lượng bằng 0 và số lượng vượt tồn kho.
- Không cập nhật cart/order sai.
- Không lỗi `500`.
- Response có thông báo lỗi rõ ràng.

### Actual Result dự kiến
DTO đã có validation như `@NotBlank`, `@NotNull`, `@Min(1)`. Service cũng kiểm tra tồn kho, trạng thái sản phẩm và số lượng vượt tồn.

### Đánh giá
| Tiêu chí                | Kết quả                                    |
|-------------------------|--------------------------------------------|
| Mức độ rủi ro           | High                                       |
| Tình trạng              | Pass sau khi xác nhận bằng request thực tế |
| Tác động nếu lỗi xảy ra | Dữ liệu tồn kho/cart/order có thể sai lệch |

### Đề xuất
- Giữ validation ở cả DTO và service.
- Thêm test cho quantity cực lớn, productId rỗng, order rỗng, shippingFee âm.
- Chuẩn hóa error response để frontend dễ hiển thị.

---
## 6. Tổng hợp phát hiện bảo mật
| Mức ưu tiên | Vấn đề                                             | Hướng xử lý                                                               |
|-------------|----------------------------------------------------|---------------------------------------------------------------------------|
| Critical    | API tin vào `X-USER-ID` do client gửi              | Dùng Spring Security/JWT và lấy user từ server-side principal             |
| Critical    | API nghiệp vụ có thể thiếu xác thực                | Bắt buộc token/session, trả `401/403` khi không hợp lệ                    |
| High        | Trường text có nguy cơ XSS nếu render sai cách     | Encode output, tránh `dangerouslySetInnerHTML`, thêm sanitization nếu cần |
| Medium      | CSRF chưa được thiết kế rõ nếu dùng cookie/session | Bật CSRF token hoặc dùng Bearer token đúng cách                           |
| High        | Cần duy trì input validation ở DTO và service      | Thêm test cho boundary/negative cases                                     |

## 7. Danh sách ảnh minh chứng cần chụp sau khi chạy
Để đưa vào báo cáo cuối, nên chụp một lần sau khi hoàn tất toàn bộ phần còn thiếu:

1. Ảnh chạy request SQL Injection và response không lỗi `500`.
2. Ảnh request XSS ở `shippingAddress` và response thực tế.
3. Ảnh request đổi `X-USER-ID` để minh chứng rủi ro IDOR hoặc kết quả kiểm tra access control.
4. Ảnh request tạo order không token/không `X-USER-ID`.
5. Ảnh file `docs/security-test-cases.md` và `docs/security-report.md` trong repository.
6. Ảnh GitHub commit/push nếu cần minh chứng đã bổ sung tài liệu security testing.

## 8. Kết luận
Phần kiểm thử bảo mật đã bao phủ các nhóm rủi ro quan trọng gồm SQL Injection, XSS, IDOR, Missing Authorization, CSRF và Input Validation.

Kết quả phân tích cho thấy các validation nghiệp vụ cơ bản đã có. Tuy nhiên, hệ thống còn điểm yếu lớn ở xác thực và phân quyền do đang dùng `X-USER-ID` từ client và có giá trị mặc định `user01`.

Ưu tiên khắc phục cao nhất:
1. Bổ sung Spring Security/JWT hoặc cơ chế authentication phù hợp.
2. Loại bỏ userId mặc định trong API nghiệp vụ.
3. Kiểm tra quyền sở hữu tài nguyên trước khi trả về hoặc thay đổi Cart/Order.
4. Duy trì encode/sanitize dữ liệu người dùng nhập để giảm rủi ro XSS.
