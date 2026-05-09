# Security Test Cases - ShopCart

## 1. Mục tiêu

Tài liệu này thiết kế các test cases kiểm thử bảo mật cho các luồng Cart, Checkout/Purchase và API liên quan của dự án ShopCart. Nội dung tập trung vào các nhóm rủi ro chính:

- SQL Injection
- Cross-Site Scripting (XSS)
- IDOR / Broken Access Control
- Missing API Authorization
- CSRF / Cross-site request risk
- Input Validation

## 2. Phạm vi kiểm thử

| Nhóm chức năng | API / Màn hình liên quan | Phạm vi |
|---|---|---|
| Cart | `POST /api/cart/add`, `GET /api/cart`, `PUT /api/cart/update`, `DELETE /api/cart/remove/{productId}` | Thêm, xem, cập nhật, xóa sản phẩm trong giỏ hàng |
| Inventory | `GET /api/inventory/{productId}`, `GET /api/inventory/{productId}/check` | Kiểm tra tồn kho và tình trạng sản phẩm |
| Purchase / Checkout | `POST /api/orders`, `GET /api/orders/{orderId}`, `PUT /api/orders/{orderId}/cancel` | Tạo đơn hàng, xem đơn hàng, hủy đơn hàng |

## 3. Môi trường kiểm thử đề xuất

| Thành phần | Giá trị |
|---|---|
| Backend URL | `http://localhost:8080` |
| Frontend URL | `http://localhost:5173` |
| Tool | Postman, curl, VS Code REST Client hoặc Swagger nếu có |
| Test data | `P001`, `P002`, `P003`, `P004`, `user01`, `user02` |

## 4. Security Test Cases chi tiết

> Cột **Actual Result** và **Status** có thể cập nhật lại sau khi chạy request thực tế trên máy local. Phiên bản hiện tại ghi theo kết quả phân tích mã nguồn và mô phỏng request.

### TC_SEC_001 - SQL Injection khi thêm sản phẩm vào giỏ hàng

| Trường | Nội dung |
|---|---|
| Test Case ID | TC_SEC_001 |
| Test Name | Kiểm tra SQL Injection tại `productId` của Cart API |
| Risk Group | SQL Injection |
| Priority | Critical |
| Preconditions | Backend đang chạy tại `http://localhost:8080`; dữ liệu mẫu đã có sản phẩm `P001` |
| Test Steps | 1. Gửi request `POST /api/cart/add`.<br>2. Truyền `productId` là payload SQL Injection.<br>3. Quan sát status code và body trả về.<br>4. Kiểm tra hệ thống không trả về dữ liệu ngoài ý muốn và không lỗi 500. |
| Test Data | `productId = "P001' OR '1'='1"`, `quantity = 1` |
| Request mẫu | `POST /api/cart/add` với body `{ "productId": "P001' OR '1'='1", "quantity": 1 }` |
| Expected Result | API từ chối payload hoặc trả lỗi validation/business phù hợp; không tạo cart item; không trả lỗi 500; không lộ thông tin database. |
| Actual Result | Dự kiến: API trả lỗi sản phẩm không tồn tại do repository tìm theo ID chính xác, không dùng query nối chuỗi trực tiếp. |
| Status | Pass sau khi xác nhận bằng request thực tế |

### TC_SEC_002 - SQL Injection tại tham số `orderId`

| Trường | Nội dung |
|---|---|
| Test Case ID | TC_SEC_002 |
| Test Name | Kiểm tra SQL Injection khi truy vấn đơn hàng theo `orderId` |
| Risk Group | SQL Injection |
| Priority | High |
| Preconditions | Backend đang chạy; có hoặc không có đơn hàng đều có thể kiểm thử |
| Test Steps | 1. Gửi request `GET /api/orders/{orderId}` với payload SQL Injection.<br>2. Quan sát phản hồi API.<br>3. Đảm bảo không trả về danh sách đơn hàng hoặc dữ liệu không thuộc request. |
| Test Data | `orderId = "ORD-001' OR '1'='1"` |
| Request mẫu | `GET /api/orders/ORD-001%27%20OR%20%271%27=%271` |
| Expected Result | API trả lỗi không tìm thấy đơn hàng hoặc request không hợp lệ; không trả lỗi 500; không lộ stack trace. |
| Actual Result | Dự kiến: API không tìm thấy đơn hàng vì dùng repository lookup theo ID, không nối chuỗi SQL trực tiếp. |
| Status | Pass sau khi xác nhận bằng request thực tế |

### TC_SEC_003 - Stored/Reflected XSS tại địa chỉ giao hàng

| Trường | Nội dung |
|---|---|
| Test Case ID | TC_SEC_003 |
| Test Name | Kiểm tra XSS tại trường `shippingAddress` khi Checkout |
| Risk Group | XSS |
| Priority | High |
| Preconditions | Backend đang chạy; sản phẩm `P001` còn tồn kho |
| Test Steps | 1. Gửi request `POST /api/orders` với `shippingAddress` chứa script HTML.<br>2. Kiểm tra API có chấp nhận chuỗi nguy hiểm không.<br>3. Nếu đơn được tạo, mở màn hình/response hiển thị địa chỉ để kiểm tra script có chạy không.<br>4. Ghi nhận khả năng stored XSS nếu dữ liệu được lưu và hiển thị lại không encode. |
| Test Data | `shippingAddress = "<script>alert('xss')</script>"` |
| Request mẫu | `POST /api/orders` với body chứa địa chỉ trên |
| Expected Result | Hệ thống nên validate/sanitize input hoặc encode output; script không được thực thi trên UI. |
| Actual Result | Dự kiến: Backend hiện chỉ kiểm tra địa chỉ không rỗng, chưa sanitize nội dung HTML. React thường escape text khi render, nhưng vẫn nên ghi nhận rủi ro nếu sau này dùng `dangerouslySetInnerHTML` hoặc render HTML. |
| Status | Needs Improvement |

### TC_SEC_004 - IDOR khi xem giỏ hàng bằng cách đổi `X-USER-ID`

| Trường | Nội dung |
|---|---|
| Test Case ID | TC_SEC_004 |
| Test Name | Kiểm tra IDOR/Broken Access Control khi đổi header `X-USER-ID` |
| Risk Group | IDOR / Broken Access Control |
| Priority | Critical |
| Preconditions | Backend đang chạy; tạo dữ liệu cart cho `user01`; thử truy cập bằng `user02` hoặc đổi header thủ công |
| Test Steps | 1. Gửi `POST /api/cart/add` với `X-USER-ID: user01` để tạo cart.<br>2. Gửi `GET /api/cart` với `X-USER-ID: user01` và ghi nhận dữ liệu.<br>3. Gửi lại `GET /api/cart` với `X-USER-ID: user02` hoặc tự đổi thành user khác.<br>4. Đánh giá API có kiểm tra token/identity thật hay chỉ tin vào header client gửi lên. |
| Test Data | `X-USER-ID: user01`, `X-USER-ID: user02` |
| Request mẫu | `GET /api/cart` kèm header `X-USER-ID` khác nhau |
| Expected Result | Người dùng không được tự ý đổi định danh để xem/sửa dữ liệu của user khác; hệ thống phải xác thực bằng token/session và lấy userId từ server-side identity. |
| Actual Result | Dự kiến: API hiện tin vào header `X-USER-ID` và còn có default `user01`, nên đây là điểm yếu bảo mật trong bản demo. |
| Status | Fail / Security Finding |

### TC_SEC_005 - Missing Authorization khi tạo đơn hàng không có định danh xác thực

| Trường | Nội dung |
|---|---|
| Test Case ID | TC_SEC_005 |
| Test Name | Kiểm tra tạo đơn hàng khi không có token hoặc thông tin xác thực |
| Risk Group | Missing Authorization |
| Priority | Critical |
| Preconditions | Backend đang chạy; sản phẩm `P001` còn hàng |
| Test Steps | 1. Gửi request `POST /api/orders` không kèm token xác thực.<br>2. Không truyền header `X-USER-ID`.<br>3. Quan sát API có cho tạo đơn hàng không.<br>4. Kiểm tra response có mặc định user là `user01` không. |
| Test Data | Không có `Authorization`; không có `X-USER-ID` |
| Request mẫu | `POST /api/orders` với body hợp lệ nhưng bỏ header định danh |
| Expected Result | API nên trả `401 Unauthorized` hoặc `403 Forbidden`; không được tự gán mặc định user khi là endpoint nghiệp vụ quan trọng. |
| Actual Result | Dự kiến: Controller đang dùng `@RequestHeader(defaultValue = "user01")`, nên request không xác thực vẫn có thể được xử lý như `user01`. |
| Status | Fail / Security Finding |

### TC_SEC_006 - CSRF/Cross-site request risk đối với API thay đổi dữ liệu

| Trường | Nội dung |
|---|---|
| Test Case ID | TC_SEC_006 |
| Test Name | Kiểm tra rủi ro CSRF với API thay đổi dữ liệu Cart/Order |
| Risk Group | CSRF |
| Priority | Medium |
| Preconditions | Backend đang chạy; frontend chạy tại `http://localhost:5173`; API có CORS giới hạn origin |
| Test Steps | 1. Gửi request `POST /api/cart/add` từ origin không hợp lệ hoặc mô phỏng bằng Postman/curl.<br>2. Kiểm tra backend có yêu cầu CSRF token hay cơ chế xác thực chống request giả mạo không.<br>3. Kiểm tra CORS có chặn trình duyệt từ origin lạ không. |
| Test Data | `Origin: http://evil.localhost`, body thêm sản phẩm hợp lệ |
| Request mẫu | `POST /api/cart/add` với header `Origin: http://evil.localhost` |
| Expected Result | Nếu dùng cookie/session, API cần CSRF token. Nếu dùng Bearer token, cần không lưu token trong cookie không bảo vệ và cần kiểm soát CORS chặt. |
| Actual Result | Dự kiến: Project hiện dùng CORS giới hạn `http://localhost:5173`, chưa có Spring Security/CSRF token. Rủi ro ở mức trung bình trong demo, cần cải thiện nếu triển khai thật. |
| Status | Needs Improvement |

### TC_SEC_007 - Input Validation với quantity âm hoặc quá lớn

| Trường | Nội dung |
|---|---|
| Test Case ID | TC_SEC_007 |
| Test Name | Kiểm tra validate quantity bất thường ở Cart API |
| Risk Group | Input Validation |
| Priority | High |
| Preconditions | Backend đang chạy; sản phẩm `P001` tồn tại |
| Test Steps | 1. Gửi `POST /api/cart/add` với `quantity = -1`.<br>2. Gửi `POST /api/cart/add` với `quantity = 999999999`.<br>3. Quan sát lỗi validation/business. |
| Test Data | `quantity = -1`, `quantity = 999999999` |
| Request mẫu | `POST /api/cart/add` với body `{ "productId": "P001", "quantity": -1 }` hoặc `{ "productId": "P001", "quantity": 999999999 }` |
| Expected Result | API từ chối số lượng âm, số lượng bằng 0 và số lượng vượt tồn kho; không cập nhật cart; không lỗi 500. |
| Actual Result | Dự kiến: DTO có `@Min(1)` và service kiểm tra tồn kho, nên case này phải bị chặn. |
| Status | Pass sau khi xác nhận bằng request thực tế |

## 5. Ma trận rủi ro tổng hợp

| Risk Group | Mức độ | API liên quan | Tình trạng dự kiến |
|---|---:|---|---|
| SQL Injection | Critical | Cart, Order | Pass nếu dùng repository lookup an toàn và không lộ lỗi DB |
| XSS | High | Checkout / Order | Needs Improvement vì backend chưa sanitize input HTML |
| IDOR / Broken Access Control | Critical | Cart, Order | Fail vì tin vào `X-USER-ID` do client gửi |
| Missing Authorization | Critical | Cart, Order | Fail vì chưa có xác thực thật cho API nghiệp vụ |
| CSRF | Medium | Cart, Order | Needs Improvement; CORS có giới hạn nhưng chưa có security config hoàn chỉnh |
| Input Validation | High | Cart, Order | Pass nếu validation và service business rules hoạt động đúng |

