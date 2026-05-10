# ShopCart_FE_BE

Dự án **ShopCart_FE_BE** là bài tập lớn môn **Kiểm thử phần mềm**.  
Hệ thống mô phỏng một ứng dụng thương mại điện tử đơn giản, tập trung vào các chức năng:

- Giỏ hàng (Cart)
- Kiểm tra tồn kho (Inventory)
- Mua hàng / Checkout / Purchase
- Tính tổng tiền, áp dụng mã giảm giá và phí vận chuyển
- Kiểm thử frontend, backend, E2E, performance, security và CI/CD

---
## 1. Công nghệ sử dụng
### Frontend
- React 19
- Vite
- Vitest
- React Testing Library
- Playwright
- Axios
- CSS

### Backend
- Java 21
- Spring Boot 3.5.0
- Spring Web
- Spring Validation
- Spring Data JPA
- H2 Database
- Maven
- JUnit 5
- Mockito
- JaCoCo

### Testing / DevOps
- Vitest coverage cho frontend
- JUnit 5 + Mockito cho backend
- Playwright E2E test
- k6 performance test
- VS Code REST Client / Postman cho security test
- GitHub Actions CI/CD

---
## 2. Cấu trúc thư mục
ShopCart_FE_BE/
├── .github/
│   └── workflows/
│       └── ci.yml
│
├── backend/
│   ├── src/main/java/com/shopcart/
│   │   ├── config/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── entity/
│   │   ├── exception/
│   │   ├── repository/
│   │   └── service/
│   │
│   ├── src/test/java/com/shopcart/
│   │   ├── config/
│   │   ├── controller/
│   │   ├── entity/
│   │   ├── exception/
│   │   └── service/
│   │
│   └── pom.xml
│
├── frontend/
│   ├── e2e/
│   │   ├── pages/
│   │   │   ├── CartPage.ts
│   │   │   └── CheckoutPage.ts
│   │   ├── cart.e2e.spec.ts
│   │   └── purchase.e2e.spec.ts
│   │
│   ├── src/
│   │   ├── components/
│   │   ├── services/
│   │   ├── test/
│   │   ├── tests/
│   │   └── utils/
│   │
│   ├── package.json
│   ├── playwright.config.ts
│   └── vite.config.js
│
├── docs/
│   ├── test-cases-cart.md
│   ├── test-cases-purchase.md
│   ├── performance-report.md
│   ├── security-test-cases.md
│   └── security-report.md
│
├── performance/
│   ├── cart-load-test.js
│   ├── checkout-load-test.js
│   └── results/
│
├── security/
│   └── security-requests.http
│
├── .gitignore
└── README.md

---
## 3. Chức năng chính
### 3.1. Cart
Các API và UI liên quan đến giỏ hàng hỗ trợ:
- Thêm sản phẩm vào giỏ hàng
- Xem giỏ hàng
- Cập nhật số lượng sản phẩm
- Xóa sản phẩm khỏi giỏ hàng
- Kiểm tra số lượng hợp lệ
- Kiểm tra tồn kho trước khi thêm/cập nhật giỏ hàng
- Tính tổng tiền giỏ hàng

API chính:
POST   /api/cart/add
GET    /api/cart
PUT    /api/cart/update
DELETE /api/cart/remove/{productId}

---
### 3.2. Inventory
Chức năng Inventory dùng để kiểm tra tồn kho sản phẩm.
API chính:
GET /api/inventory/{productId}
GET /api/inventory/{productId}/check?quantity=1

---
### 3.3. Purchase / Checkout / Order
Chức năng Purchase mô phỏng quy trình đặt hàng:
- Kiểm tra tồn kho trước khi tạo đơn hàng
- Tính subtotal, discount, shipping fee và total
- Áp dụng coupon
- Tạo order
- Giảm tồn kho sau khi đặt hàng
- Hủy đơn hàng và hoàn tồn kho

API chính:
POST /api/orders
GET  /api/orders/{orderId}
PUT  /api/orders/{orderId}/cancel

---
## 4. Yêu cầu môi trường
Cần cài đặt:
- Java 21
- Maven
- Node.js 22 hoặc tương thích
- npm
- Git
- k6 nếu muốn chạy performance test
- VS Code REST Client hoặc Postman nếu muốn chạy security request thủ công

Kiểm tra phiên bản:
```bash
java -version
mvn -version
node -v
npm -v
git --version
```

---
## 5. Cách chạy backend
Từ thư mục gốc project:
```bash
cd backend
mvn spring-boot:run
```

Backend mặc định chạy tại:
```bash
http://localhost:8080
```

Ví dụ kiểm tra API tồn kho:

```bash
curl http://localhost:8080/api/inventory/P002
```

---

## 6. Cách chạy frontend
Mở terminal khác, từ thư mục gốc project:
```bash
cd frontend
npm ci
npm run dev
```

Frontend mặc định chạy tại:
```bash
http://localhost:5173
```

Nếu dùng PowerShell trên Windows và bị lỗi Execution Policy, có thể dùng:
```cmd
npm.cmd ci
npm.cmd run dev
```

---
## 7. Chạy backend tests và JaCoCo report
Từ thư mục gốc project:
```bash
cd backend
mvn clean test jacoco:report
```

Sau khi chạy xong, mở report tại:
backend/target/site/jacoco/index.html

Kết quả hiện tại sau khi bổ sung test:
Backend JaCoCo:
- Instruction coverage: khoảng 92%
- Line coverage: khoảng 94.5%
- Kết quả: đạt yêu cầu coverage >= 80%

Các nhóm test backend gồm:
- CartServiceTest
- OrderServiceTest
- InventoryServiceTest
- CartControllerIntegrationTest
- OrderControllerIntegrationTest
- InventoryControllerIntegrationTest
- EntityCoverageTest
- DataSeederTest
- GlobalExceptionHandlerTest

---
## 8. Chạy frontend unit/integration tests và coverage
Từ thư mục gốc project:
```bash
cd frontend
npm run test:coverage
```

Kết quả hiện tại sau khi bổ sung test:
Frontend Vitest:
- Test files: 9 passed
- Tests: 52 passed
- Line coverage: khoảng 90.49%
- Statement coverage: khoảng 90.74%
- Kết quả: đạt yêu cầu coverage >= 80%

Coverage report được tạo tại:
frontend/coverage/
frontend/coverage/lcov-report/index.html

Các nhóm test frontend gồm:
- cartValidation.test.js
- priceCalculation.test.js
- Cart.integration.test.jsx
- Checkout.integration.test.jsx
- cartService.test.js
- inventoryService.test.js
- orderService.test.js
- Cart.extra.test.jsx
- Checkout.extra.test.jsx

---
## 9. Chạy Playwright E2E tests
Từ thư mục gốc project:
```bash
cd frontend
npx playwright install
npm run test:e2e
```

Playwright được cấu hình chạy trên 3 browser:
- Chromium
- Firefox
- WebKit

Kết quả hiện tại:
9 passed

Playwright HTML report được tạo tại:
frontend/playwright-report/

Có thể mở report bằng:
```bash
cd frontend
npx playwright show-report
```

Các E2E test chính:
frontend/e2e/cart.e2e.spec.ts
frontend/e2e/purchase.e2e.spec.ts

Page Object Model:
frontend/e2e/pages/CartPage.ts
frontend/e2e/pages/CheckoutPage.ts

---
## 10. Chạy performance tests bằng k6
Performance tests nằm trong:
performance/
├── cart-load-test.js
├── checkout-load-test.js
└── results/

Trước khi chạy k6, cần chạy backend trước:
```bash
cd backend
mvn spring-boot:run
```
Mở terminal khác ở thư mục gốc project.

### 10.1. Cart Load Test
```bash
k6 run --summary-export performance/results/cart-summary.json performance/cart-load-test.js
```

Kết quả thực tế đã ghi nhận:
Cart Load Test:
- VUs max: 10
- Iterations: 654
- HTTP requests: 1962
- Throughput: khoảng 16.29 req/s
- Average response time: khoảng 4.61 ms
- p95: khoảng 10.68 ms
- Error rate: 0.00%
- Result: Pass

### 10.2. Checkout Load Test
```bash
k6 run --summary-export performance/results/checkout-summary.json performance/checkout-load-test.js
```

Kết quả thực tế đã ghi nhận:
Checkout Load Test:
- VUs: 5
- Iterations: 25
- HTTP requests: 51
- Throughput: khoảng 9.20 req/s
- Average iteration duration: khoảng 1.07 s
- p95: khoảng 1.22 s
- Error rate: 0.00%
- Result: Pass

Báo cáo chi tiết:
docs/performance-report.md

---
## 11. Chạy security tests
Security tests được mô phỏng bằng file:
security/security-requests.http

Có thể chạy theo 2 cách.

### 11.1. Cách 1: Chạy thủ công bằng VS Code REST Client
Cài extension:
REST Client

Sau đó mở file:
security/security-requests.http

Bấm **Send Request** ở từng request để kiểm tra và chụp ảnh response.

Các nhóm request chính:
- SQL Injection
- XSS
- IDOR / Broken Access Control
- Missing Authorization
- CSRF / CORS
- Input Validation

### 11.2. Cách 2: Chạy tự động bằng httpyac
Từ thư mục gốc project, chạy:
npx -y httpyac send security/security-requests.http --all

Lưu kết quả ra file:
```bash
npx -y httpyac send security/security-requests.http --all > security/security-run-output.txt
```

Trên Windows nếu dùng Command Prompt:
```bash
npx.cmd -y httpyac send security/security-requests.http --all
```

---
## 12. Tài liệu kiểm thử
Các tài liệu kiểm thử chính nằm trong thư mục `docs/`:

docs/test-cases-cart.md
Mô tả test cases chi tiết cho chức năng Cart theo template:
- Test Case ID
- Test Name
- Priority
- Preconditions
- Test Steps
- Test Data
- Expected Result
- Actual Result
- Status

docs/test-cases-purchase.md
Mô tả test cases chi tiết cho chức năng Purchase/Checkout.

docs/performance-report.md
Báo cáo performance testing bằng k6.

docs/security-test-cases.md
Danh sách security test cases.

docs/security-report.md
Báo cáo kết quả security testing, tác động và đề xuất khắc phục.

---
## 13. CI/CD GitHub Actions
Workflow CI/CD nằm tại:
.github/workflows/ci.yml

Pipeline gồm 3 job chính:
1. Backend tests and JaCoCo report
2. Frontend unit and integration tests
3. Playwright E2E tests

Pipeline tự động chạy khi:
- push lên main, develop, backend-cart, backend-order, frontend-cart, frontend-checkout
- pull_request vào main hoặc develop

Artifacts được upload sau mỗi lần chạy:
- backend-surefire-reports
- backend-jacoco-report
- frontend-coverage-report
- playwright-html-report

Mục tiêu:
- Backend tests pass
- Frontend tests pass
- Playwright E2E tests pass
- Có report để minh chứng kết quả kiểm thử

---
## 14. Kết quả kiểm thử tổng hợp
| Nhóm kiểm thử                   | Công cụ                       | Kết quả hiện tại                                              |
|---------------------------------|-------------------------------|---------------------------------------------------------------|
| Backend Unit/Integration Tests  | JUnit 5, Mockito, MockMvc     | Pass                                                          |
| Backend Coverage                | JaCoCo                        | Instruction coverage khoảng 92%, line coverage khoảng 94.5%   |
| Frontend Unit/Integration Tests | Vitest, React Testing Library | 52 tests passed                                               |
| Frontend Coverage               | Vitest Coverage V8            | Line coverage khoảng 90.49%                                   |
| E2E Testing                     | Playwright                    | 9 passed trên Chromium, Firefox, WebKit                       |
| Performance Testing             | k6                            | Cart và Checkout đều error rate 0.00%                         |
| Security Testing                | REST Client / httpyac         | Có test cases, mô phỏng request, finding và đề xuất khắc phục |
| CI/CD                           | GitHub Actions                | Pipeline có backend, frontend và E2E jobs                     |

---
## 15. Lưu ý khi commit source code
Không commit các thư mục sinh tự động:
frontend/node_modules/
frontend/coverage/
frontend/playwright-report/
frontend/test-results/
backend/target/

---
## 16. Hướng dẫn mở các report quan trọng
### JaCoCo backend report
backend/target/site/jacoco/index.html

### Vitest frontend coverage repor
frontend/coverage/lcov-report/index.html

### Playwright HTML report
```bash
cd frontend
npx playwright show-report
```

### k6 result files
performance/results/cart-summary.json
performance/results/checkout-summary.json

---
