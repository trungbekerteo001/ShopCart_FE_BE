# Security Testing Report - ShopCart

## 1. Mục đích báo cáo

Báo cáo này ghi nhận kết quả kiểm thử bảo mật cho các luồng Cart, Checkout/Purchase và API liên quan của dự án ShopCart. Mục tiêu là đáp ứng yêu cầu Advanced Testing - Security Testing: thiết kế test cases cho ít nhất 2 nhóm rủi ro, mô phỏng request kiểm thử, ghi nhận kết quả, mô tả tác động và đề xuất khắc phục.

## 2. Phạm vi và công cụ

| Nội dung | Mô tả |
|---|---|
| Hệ thống kiểm thử | ShopCart_FE_BE |
| Backend | Spring Boot REST API |
| Frontend | React + Vite |
| API base URL | `http://localhost:8080` |
| Công cụ mô phỏng | Postman, curl, VS Code REST Client |
| Nhóm rủi ro chính | SQL Injection, XSS, IDOR, Missing Authorization, CSRF, Input Validation |

## 3. Danh sách request mô phỏng

Có thể chạy nhanh các request bằng file:

```text
security/security-requests.http
```

Nếu dùng VS Code, cài extension **REST Client**, mở file trên và bấm **Send Request** từng request.

## 4. Kết quả kiểm thử bảo mật

> Ghi chú: các kết quả dưới đây được xây dựng dựa trên phân tích mã nguồn hiện tại và mô phỏng request. Khi chạy trực tiếp trên máy local, cập nhật lại cột **Actual Result** nếu response thực tế khác.

| ID | Nhóm rủi ro | Request kiểm thử | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|
| TC_SEC_001 | SQL Injection | `POST /api/cart/add` với `productId = P001' OR '1'='1` | Không tạo cart item, không lỗi 500, không lộ DB | Dự kiến trả lỗi sản phẩm không tồn tại do lookup theo ID chính xác | Pass |
| TC_SEC_002 | SQL Injection | `GET /api/orders/ORD-001' OR '1'='1` | Không trả dữ liệu đơn hàng trái phép, không lỗi 500 | Dự kiến trả lỗi không tìm thấy đơn hàng | Pass |
| TC_SEC_003 | XSS | `POST /api/orders` với `shippingAddress = <script>alert('xss')</script>` | Script không được thực thi; input nên được sanitize/encode | Backend hiện chỉ kiểm tra không rỗng, chưa sanitize HTML | Needs Improvement |
| TC_SEC_004 | IDOR | Đổi header `X-USER-ID` để xem/sửa cart | Không được tự đổi định danh người dùng | API hiện tin vào header `X-USER-ID` do client gửi | Fail |
| TC_SEC_005 | Missing Authorization | Tạo order không có token và không có `X-USER-ID` | Trả `401` hoặc `403` | Controller có default `user01`, có nguy cơ xử lý request không xác thực | Fail |
| TC_SEC_006 | CSRF | Gửi request thay đổi dữ liệu với origin lạ | CORS/CSRF/security policy phải chặn request không hợp lệ | CORS có giới hạn localhost, nhưng chưa có cơ chế auth/CSRF hoàn chỉnh | Needs Improvement |
| TC_SEC_007 | Input Validation | `quantity = -1` hoặc `quantity` vượt tồn kho | Từ chối request, không cập nhật dữ liệu | DTO/service đã có validation số lượng và tồn kho | Pass |

## 5. Phân tích chi tiết theo nhóm rủi ro

### 5.1 SQL Injection

Các API nhận `productId` và `orderId` từ client. Payload kiểm thử gồm:

```text
P001' OR '1'='1
ORD-001' OR '1'='1
```

Kết quả dự kiến: hệ thống không bị SQL Injection vì luồng hiện tại sử dụng repository lookup theo khóa/id thay vì nối chuỗi SQL thủ công. Tuy nhiên, vẫn cần giữ nguyên nguyên tắc không viết native query bằng cách nối chuỗi input của người dùng.

**Đề xuất:**

- Tiếp tục dùng Spring Data JPA repository hoặc parameterized query.
- Không dùng raw SQL nối chuỗi từ `productId`, `orderId`, `couponCode`.
- Không trả stack trace hoặc chi tiết exception database ra client.

### 5.2 XSS

Trường `shippingAddress` có thể nhận chuỗi HTML/script. Backend hiện kiểm tra trường này không rỗng, nhưng chưa sanitize các ký tự HTML nguy hiểm.

**Tác động:** nếu dữ liệu địa chỉ được lưu và hiển thị lại ở trang lịch sử đơn hàng, trang quản trị hoặc email mà không encode, hệ thống có thể bị stored XSS.

**Đề xuất:**

- Encode output khi hiển thị dữ liệu người dùng nhập.
- Không dùng `dangerouslySetInnerHTML` ở React cho dữ liệu đến từ API.
- Có thể bổ sung validation/sanitization cho các trường text như `shippingAddress`, `couponCode` nếu cần.
- Thêm security header như `Content-Security-Policy` khi triển khai thật.

### 5.3 IDOR / Broken Access Control

Các API Cart đang lấy user bằng header:

```text
X-USER-ID: user01
```

và nhiều controller có `defaultValue = "user01"`. Điều này phù hợp cho demo/test đơn giản, nhưng không an toàn nếu dùng như hệ thống thật.

**Tác động:** người dùng có thể tự đổi `X-USER-ID` để truy cập giỏ hàng hoặc đơn hàng của người khác nếu biết định danh.

**Đề xuất:**

- Bỏ `defaultValue = "user01"` ở các API nghiệp vụ.
- Tích hợp Spring Security + JWT/session.
- Lấy `userId` từ authentication principal phía server, không lấy trực tiếp từ header do client tự truyền.
- Với `GET /api/orders/{orderId}`, cần kiểm tra order thuộc về user hiện tại trước khi trả dữ liệu.

### 5.4 Missing Authorization

Các API quan trọng như tạo order, cập nhật cart, hủy order hiện chưa yêu cầu token xác thực thật.

**Tác động:** request không xác thực vẫn có thể tác động dữ liệu nếu endpoint public.

**Đề xuất:**

- Bảo vệ API bằng Spring Security.
- Trả `401 Unauthorized` nếu chưa đăng nhập.
- Trả `403 Forbidden` nếu user không có quyền thao tác tài nguyên.
- Bổ sung integration/security tests cho status code 401/403.

### 5.5 CSRF

Project hiện có `@CrossOrigin(origins = "http://localhost:5173")`, giúp trình duyệt chặn một số request cross-origin không hợp lệ. Tuy nhiên, nếu sau này dùng cookie/session, cần cấu hình CSRF rõ ràng.

**Đề xuất:**

- Nếu dùng JWT Bearer token trong header `Authorization`, không lưu token trong cookie không bảo vệ.
- Nếu dùng session/cookie, bật CSRF token.
- Cấu hình CORS chỉ cho phép origin frontend thật.
- Không dùng wildcard `*` cho production.

### 5.6 Input Validation

Các DTO đã có validation như `@NotBlank`, `@NotNull`, `@Min(1)`. Service cũng kiểm tra tồn kho, trạng thái sản phẩm và số lượng vượt tồn.

**Đề xuất:**

- Giữ validation ở cả DTO và service.
- Thêm test cho quantity cực lớn, productId rỗng, order rỗng, shippingFee âm.
- Chuẩn hóa error response để frontend dễ hiển thị.

## 6. Danh sách ảnh minh chứng cần chụp sau khi chạy

Để đưa vào báo cáo cuối, nên chụp một lần sau khi hoàn tất toàn bộ phần còn thiếu:

1. Ảnh chạy request SQL Injection và response không lỗi 500.
2. Ảnh request XSS ở `shippingAddress` và response thực tế.
3. Ảnh request đổi `X-USER-ID` chứng minh rủi ro IDOR hoặc kết quả kiểm tra access control.
4. Ảnh request tạo order không token/không `X-USER-ID`.
5. Ảnh file `docs/security-test-cases.md` và `docs/security-report.md` trong repository.
6. Ảnh GitHub commit/push nếu cần minh chứng đã bổ sung tài liệu security testing.

## 7. Kết luận

Phần kiểm thử bảo mật đã bao phủ nhiều nhóm rủi ro theo yêu cầu gồm SQL Injection, XSS, IDOR, Missing Authorization, CSRF và Input Validation. Kết quả phân tích cho thấy các validation nghiệp vụ cơ bản đã có, nhưng hệ thống còn điểm yếu lớn ở xác thực và phân quyền do đang dùng `X-USER-ID` từ client và có giá trị mặc định `user01`.

Ưu tiên khắc phục cao nhất là bổ sung Spring Security/JWT hoặc cơ chế authentication phù hợp, loại bỏ userId mặc định trong API nghiệp vụ, và kiểm tra quyền sở hữu tài nguyên trước khi trả về hoặc thay đổi Cart/Order.

