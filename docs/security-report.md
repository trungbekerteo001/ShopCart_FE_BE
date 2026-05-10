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
| Nhóm kiểm thử         | Test Case              | Kết quả                 | Nhận xét                                                                                 |
|-----------------------|------------------------|-------------------------|------------------------------------------------------------------------------------------|
| SQL Injection         | TC_SEC_001, TC_SEC_002 | Pass                    | Chưa ghi nhận dấu hiệu payload SQL Injection làm lộ dữ liệu hoặc thay đổi logic truy vấn |
| XSS                   | TC_SEC_003             | Needs Improvement       | Cần bổ sung sanitize/escape dữ liệu người dùng nhập trước khi hiển thị ra giao diện      |
| IDOR / Access Control | TC_SEC_004             | Fail / Security Finding | Có rủi ro nếu hệ thống tin vào `X-USER-ID` hoặc userId do client truyền lên              |
| Missing Authorization | TC_SEC_005             | Fail / Security Finding | Cần bổ sung xác thực và phân quyền rõ ràng cho API nghiệp vụ                             |
| CSRF / CORS           | TC_SEC_006             | Needs Improvement       | Cần cấu hình CORS chặt chẽ theo frontend origin hợp lệ                                   |
| Input Validation      | TC_SEC_007             | Pass                    | Quantity không hợp lệ được kiểm soát đúng                                                |

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

### Actual Result
Khi kiểm tra với payload SQL Injection tại `productId` và `orderId`, hệ thống không xử lý payload như một câu truy vấn SQL hợp lệ. Request không làm lộ dữ liệu ngoài phạm vi, không truy xuất được sản phẩm/đơn hàng không hợp lệ và không phát sinh lỗi hệ thống nghiêm trọng.

### Đánh giá
| Tiêu chí                | Kết quả                                                     |
|-------------------------|-------------------------------------------------------------|
| Mức độ rủi ro           | Critical                                                    |
| Tình trạng              | Pass                                                        |
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

### Actual Result
Khi kiểm tra với payload XSS trong trường `shippingAddress`, hệ thống chưa thể hiện đầy đủ cơ chế sanitize dữ liệu đầu vào ở phía backend. React thường escape text khi render, tuy nhiên nếu dữ liệu này được hiển thị bằng `dangerouslySetInnerHTML` hoặc render HTML trực tiếp, payload script có nguy cơ gây stored XSS.

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

### Actual Result
Khi mô phỏng thay đổi `X-USER-ID`, hệ thống có nguy cơ phụ thuộc vào định danh người dùng do client truyền lên. Một số API còn có thể dùng giá trị mặc định `user01`, phù hợp cho demo nhưng không an toàn nếu triển khai thật. Đây là rủi ro IDOR/Broken Access Control vì người dùng có thể sửa định danh và truy cập hoặc thay đổi tài nguyên không thuộc quyền sở hữu của mình.

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

### Actual Result
Khi mô phỏng request thiếu `Authorization header` hoặc thiếu thông tin định danh người dùng, một số API nghiệp vụ vẫn có nguy cơ xử lý theo user mặc định hoặc user truyền từ client. Điều này cho thấy hệ thống cần bổ sung xác thực và phân quyền rõ ràng hơn cho các API Cart, Checkout và Order.

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

### Actual Result
Khi mô phỏng request từ origin lạ, hệ thống cần kiểm tra và giới hạn CORS theo danh sách frontend origin hợp lệ. Project có định hướng giới hạn frontend origin local, tuy nhiên chưa có security configuration hoàn chỉnh cho authentication, authorization và CSRF token. Vì vậy nhóm rủi ro này được ghi nhận là cần cải thiện.

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

### Actual Result
Khi kiểm tra các giá trị quantity âm, bằng 0, quá lớn hoặc `productId` không hợp lệ, hệ thống từ chối request không hợp lệ. DTO có validation như `@NotBlank`, `@NotNull`, `@Min(1)`, đồng thời service kiểm tra tồn kho, trạng thái sản phẩm và số lượng vượt tồn. Hệ thống không tạo cart item/order sai và không trừ tồn kho sai.

### Đánh giá
| Tiêu chí                | Kết quả                                    |
|-------------------------|--------------------------------------------|
| Mức độ rủi ro           | High                                       |
| Tình trạng              | Pass                                       |
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

## 7. Kết luận
Phần kiểm thử bảo mật đã bao phủ các nhóm rủi ro quan trọng gồm SQL Injection, XSS, IDOR, Missing Authorization, CSRF và Input Validation.

Kết quả phân tích cho thấy các validation nghiệp vụ cơ bản đã có. Tuy nhiên, hệ thống còn điểm yếu lớn ở xác thực và phân quyền do đang dùng `X-USER-ID` từ client và có giá trị mặc định `user01`.

Ưu tiên khắc phục cao nhất:
1. Bổ sung Spring Security/JWT hoặc cơ chế authentication phù hợp.
2. Loại bỏ userId mặc định trong API nghiệp vụ.
3. Kiểm tra quyền sở hữu tài nguyên trước khi trả về hoặc thay đổi Cart/Order.
4. Duy trì encode/sanitize dữ liệu người dùng nhập để giảm rủi ro XSS.
