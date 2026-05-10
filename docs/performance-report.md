# Performance Testing Report – ShopCart
## 1. Mục tiêu kiểm thử
Báo cáo này bổ sung cho **Câu 6.1 – Performance Testing** của bài tập lớn Kiểm thử phần mềm. Mục tiêu là đánh giá hiệu năng các API quan trọng của chức năng **Cart** và **Checkout/Purchase**, bao gồm:

- Thời gian phản hồi API.
- Tỷ lệ lỗi khi có nhiều người dùng đồng thời.
- Throughput/số request xử lý mỗi giây.
- Điểm nghẽn có thể phát sinh khi thêm giỏ hàng và đặt hàng.
- Đề xuất tối ưu sau khi phân tích kết quả.

## 2. Công cụ và môi trường kiểm thử
- Công cụ kiểm thử: k6
- Backend API: Spring Boot chạy tại `http://localhost:8080`
- Cơ sở dữ liệu test: H2 in-memory database
- Máy chạy test: Local Windows development machine
- Kịch bản kiểm thử:
  + Cart Load Test: kiểm thử API giỏ hàng
  + Checkout Load Test: kiểm thử API đặt hàng

## 3. Kịch bản tải
### 3.1. Cart Load Test
- Script: `performance/cart-load-test.js`
- API chính: Cart API
- Số người dùng ảo tối đa: 10 VUs
- Thời lượng chạy: khoảng 2 phút
- Mục tiêu:
  + Kiểm tra khả năng xử lý nhiều request Cart liên tiếp.
  + Đo response time, throughput, error rate.
  + Đảm bảo API không phát sinh lỗi trong quá trình thêm/cập nhật/đọc giỏ hàng.

### 3.2. Checkout Load Test
- Script: `performance/checkout-load-test.js`
- API chính: Checkout/Purchase API
- Số người dùng ảo: 5 VUs
- Số vòng lặp: 25 iterations
- Thời lượng chạy thực tế: khoảng 5.5 giây
- Mục tiêu:
  + Kiểm tra khả năng tạo đơn hàng khi có nhiều user đồng thời.
  + Đo error rate, throughput và thời gian xử lý luồng checkout.
  + Kiểm tra API có xử lý ổn định khi trừ tồn kho và tạo order.

## 4. Kết quả kiểm thử
| Test Script        | VUs | Iterations | HTTP Requests | Throughput  | Avg Duration | p90     | p95      | Error Rate | Result |
|--------------------|-----|------------|---------------|-------------|--------------|---------|----------|------------|--------|
| Cart Load Test     | 10  | 654        | 1962          | 16.29 req/s | 4.61 ms      | 7.47 ms | 10.68 ms | 0.00%      | Pass   |
| Checkout Load Test | 5   | 5          | 51            | 9.20 req/s  | 1.07 s       | 1.22 s  | 1.22 s   | 0.00%      | Pass   |

## 5. Phân tích kết quả
### 5.1. Cart Load Test
Cart API có kết quả tốt với error rate bằng 0%. Response time trung bình khoảng 4.61 ms, p95 khoảng 10.68 ms. Điều này cho thấy các thao tác Cart như thêm sản phẩm, kiểm tra tồn kho và đọc giỏ hàng đang phản hồi nhanh trong môi trường local.

Số request xử lý được là 1962 request với throughput khoảng 16.29 request/giây. Với phạm vi bài tập lớn và dữ liệu test nhỏ, kết quả này đáp ứng yêu cầu kiểm thử hiệu năng cơ bản.

### 5.2. Checkout Load Test
Checkout API chạy với 5 người dùng ảo và hoàn thành 25 iterations, không có iteration nào bị gián đoạn. Error rate bằng 0.00%, chứng tỏ API tạo đơn hàng và xử lý tồn kho ổn định trong kịch bản tải nhỏ.

Thời gian xử lý trung bình của một iteration khoảng 1.07 giây, p95 khoảng 1.22 giây. Checkout chậm hơn Cart là hợp lý vì luồng này phải kiểm tra tồn kho, tính tổng tiền, tạo đơn hàng và cập nhật số lượng tồn kho.

## 6. Điểm nghẽn tiềm năng
Một số điểm có thể trở thành bottleneck khi tải tăng cao:

- Checkout có nhiều bước xử lý nghiệp vụ hơn Cart, gồm kiểm tra tồn kho, tạo order và giảm tồn kho.
- Nếu nhiều user cùng đặt cùng một sản phẩm, hệ thống có thể gặp rủi ro race condition hoặc overselling nếu không khóa dữ liệu tồn kho đúng cách.
- H2 in-memory database phù hợp cho test local, nhưng chưa phản ánh đầy đủ hiệu năng khi dùng database production như PostgreSQL/MySQL.
- Nếu sau này thêm authentication thật, payment gateway hoặc email confirmation, thời gian checkout có thể tăng lên.

## 7. Đề xuất tối ưu
- Bổ sung transaction cho luồng tạo order và giảm tồn kho.
- Kiểm tra tồn kho trong cùng transaction với thao tác trừ tồn kho để tránh bán vượt tồn.
- Tạo index cho các trường thường truy vấn như `productId`, `userId`, `orderId`.
- Tách các tác vụ chậm như gửi email xác nhận sang xử lý bất đồng bộ.
- Với hệ thống thật, nên chạy lại performance test trên database production-like và tăng dần tải từ 10, 50 đến 100 VUs.

## 8. Kết luận
Performance testing đã được thực hiện cho hai nhóm API quan trọng là Cart và Checkout/Purchase. Cả hai kịch bản đều có error rate bằng 0.00%, hoàn thành toàn bộ iterations và không phát sinh lỗi hệ thống trong môi trường local.