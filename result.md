# API Test Result

Ngày test: 2026-05-11  
Người thực hiện: Senior Backend/API Tester  
Phạm vi: CATEGORIES, ADMIN DASHBOARD, ADMIN FINANCE, ADMIN SYSTEM SETTINGS, FILE UPLOAD

## 1. Mục tiêu test

Xác nhận các API mới được implement đúng contract trong file `API Growup.pdf` ở mức controller/API contract:

- Đúng HTTP method và URL.
- Đúng status code thành công: `200 OK` hoặc `201 Created`.
- Đúng response wrapper chuẩn: `success`, `data`.
- Đúng các field response quan trọng theo tài liệu.
- Hỗ trợ JSON request và multipart/form-data request theo từng API.

## 2. Môi trường test

- Framework: Spring Boot 3.2.5
- Java: 21
- Test framework: JUnit 5, Mockito, Spring MockMvc
- Cách chạy: Maven trong Docker container
- Test file: `src/test/java/com/example/becommerce/controller/GrowupApiControllerTest.java`

Lệnh đã chạy:

```bash
docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9.9-eclipse-temurin-21 mvn -q test
```

Kết quả Maven Surefire:

```text
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
```

## 3. Kết quả tổng quan

| Nhóm API | Số endpoint | Kết quả |
|---|---:|---|
| Categories | 5 | PASS |
| Admin Dashboard | 4 | PASS |
| Admin Finance | 5 | PASS |
| Admin System Settings | 2 | PASS |
| File Upload | 2 | PASS |
| Tổng | 18 | PASS |

## 4. Chi tiết test case

| ID | API | Kịch bản kiểm thử | Expected | Actual | Status |
|---|---|---|---|---|---|
| CAT-01 | `GET /api/categories?status=active` | Lấy danh sách danh mục theo status | `200`, có `data.items[]` | Đúng | PASS |
| CAT-02 | `POST /api/categories` | Tạo danh mục bằng multipart form có icon | `201`, trả `id`, `title`, `status` | Đúng | PASS |
| CAT-03 | `PUT /api/categories/{id}` | Cập nhật danh mục bằng multipart form | `200`, trả danh mục đã cập nhật | Đúng | PASS |
| CAT-04 | `DELETE /api/categories/{id}` | Xóa danh mục | `200`, trả message xóa thành công | Đúng | PASS |
| CAT-05 | `PATCH /api/categories/{id}/status` | Bật/tắt hiển thị danh mục | `200`, trả `id`, `status` | Đúng | PASS |
| DASH-01 | `GET /api/admin/stats` | Lấy số liệu tổng quan | `200`, có revenue/profit/technicians/orders | Đúng | PASS |
| DASH-02 | `GET /api/admin/stats/revenue?range=7days` | Lấy biểu đồ doanh thu | `200`, có `range`, `items[]` | Đúng | PASS |
| DASH-03 | `GET /api/admin/stats/service-distribution` | Lấy tỷ trọng dịch vụ | `200`, có `name`, `percentage`, `color` | Đúng | PASS |
| DASH-04 | `GET /api/admin/orders/recent?limit=5` | Lấy đơn gần đây | `200`, có `items[]` | Đúng | PASS |
| FIN-01 | `GET /api/admin/transactions` | Lấy lịch sử giao dịch có phân trang | `200`, có `totalBalance`, `items`, `pagination` | Đúng | PASS |
| FIN-02 | `GET /api/admin/withdraw-requests` | Lấy danh sách yêu cầu rút | `200`, có `pendingCount`, `items[]` | Đúng | PASS |
| FIN-03 | `POST /api/admin/withdraw-requests/{id}/approve` | Duyệt yêu cầu rút tiền | `200`, trả `status=approved` | Đúng | PASS |
| FIN-04 | `PATCH /api/admin/commission` | Cập nhật hoa hồng | `200`, trả `platformFeePercent`, `vatPercent` | Đúng | PASS |
| FIN-05 | `POST /api/admin/wallet/adjust` | Điều chỉnh số dư ví | `200`, trả `transactionId`, `newBalance` | Đúng | PASS |
| SET-01 | `GET /api/admin/settings` | Lấy cấu hình hệ thống | `200`, có `general`, `billing`, `notifications`, `operations` | Đúng | PASS |
| SET-02 | `PUT /api/admin/settings` | Cập nhật cấu hình hệ thống | `200`, trả message lưu thành công | Đúng | PASS |
| UP-01 | `POST /api/upload/image` | Upload một ảnh | `201`, trả `url`, `filename`, `size`, `mimeType` | Đúng | PASS |
| UP-02 | `POST /api/upload/images` | Upload nhiều ảnh | `201`, trả `urls[]` | Đúng | PASS |

## 5. Ghi chú QA

Test hiện tại là API contract/controller test bằng MockMvc và mock service, phù hợp để bắt lỗi sai route, HTTP method, status code, JSON shape, multipart binding và response wrapper.

Chưa thực hiện live integration test với database thật vì test contract không khởi động full Spring context và không ghi dữ liệu thật. Với môi trường staging, nên bổ sung thêm các test sau:

- Auth/security test: xác nhận API admin trả `401` khi chưa đăng nhập và `403` khi role không phải admin.
- Persistence test: tạo/cập nhật/xóa category thật trong database test.
- File storage test: upload file thật và xác nhận file tồn tại trong thư mục `uploads`.
- Finance workflow test: tạo withdraw transaction, approve, kiểm tra `pendingBalance`, `status`, `processedAt`.
- Negative test: invalid status, invalid priority, invalid file type, missing required fields.

## 6. Kết luận

Tất cả 18 endpoint trong phạm vi yêu cầu đã PASS ở mức API contract test. Không phát hiện lỗi blocking trong route, status code hoặc response format của các API mới.
