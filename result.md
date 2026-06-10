# API Test Result — BE E-Commerce (GlowUp Concierge)

| | |
|---|---|
| **Ngày test** | 2026-06-02 |
| **Người thực hiện** | Test Engineer (QA) |
| **Phạm vi** | Toàn bộ REST API controller layer (18 controller / 75 endpoint) |
| **Tài liệu manual** | [docs/TEST_CASES_MANUAL.md](docs/TEST_CASES_MANUAL.md) — 137 test case |
| **Test tự động** | `src/test/java/com/example/becommerce/controller/GrowupApiControllerTest.java` + `SystemApiControllerTest.java` |

---

## 1. Mục tiêu

Xác minh các REST API đúng **contract ở mức controller**:

- Đúng HTTP method + URL route binding.
- Đúng status code thành công (`200`/`201`) và lỗi validation (`422`).
- Đúng response wrapper `{ success, data }` / `{ success, error }`.
- Hỗ trợ JSON & multipart đúng theo từng API.
- JSR-303 validation (`@Valid`) hoạt động đúng và trả về `VALIDATION_ERROR` (422).

> Đây là **API contract / controller test** (MockMvc standalone + mock service + `GlobalExceptionHandler` thật). Không cần DB hay full Spring context. Tầng nghiệp vụ/persistence/RBAC runtime cần bổ sung integration test (xem mục 7).

---

## 2. Môi trường test

| Thành phần | Giá trị |
|---|---|
| Framework | Spring Boot 3 (Java 21) |
| Test stack | JUnit 5 · Mockito · Spring MockMvc (`standaloneSetup`) |
| Cách chạy | Maven trong Docker container (`maven:3.9.9-eclipse-temurin-21`) |

Lệnh đã chạy:

```bash
docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 \
  -w /workspace maven:3.9.9-eclipse-temurin-21 \
  mvn -q test -Dtest=GrowupApiControllerTest,SystemApiControllerTest
```

Kết quả Surefire:

```text
GrowupApiControllerTest  — Tests run: 4,  Failures: 0, Errors: 0, Skipped: 0
SystemApiControllerTest  — Tests run: 21, Failures: 0, Errors: 0, Skipped: 0
────────────────────────────────────────────────────────────────────────
TỔNG                     — Tests run: 25, Failures: 0, Errors: 0, Skipped: 0   ✅ BUILD SUCCESS
```

---

## 3. Kết quả tổng quan theo module

| Module | Controller | Endpoint | Test method | Kết quả |
|---|---|---:|---:|:---:|
| Auth | AuthController | 8 | 2 | ✅ PASS |
| Users | UserController | 4 | 2 | ✅ PASS |
| Wallet | WalletController | 8 | 2 | ✅ PASS |
| Orders | OrderController | 11 | 2 | ✅ PASS |
| Technicians | TechnicianController | 5 | 2 | ✅ PASS |
| Verifications | VerificationController | 4 (JSON) | 1 | ✅ PASS |
| Conversations | ConversationController | 5 | 2 | ✅ PASS |
| Quotes | QuotationController | 1 | 1 | ✅ PASS |
| Reports | ReportController + OrderReportController | 2 | 2 | ✅ PASS |
| Reviews | OrderReviewController | 1 | 1 | ✅ PASS |
| Warranty | OrderWarrantyController | 2 | 2 | ✅ PASS |
| Notifications | NotificationController | 3 | 1 | ✅ PASS |
| Payment Webhook | PaymentWebhookController | 1 | 1 | ✅ PASS |
| Categories | CategoryController | 5 | 1 | ✅ PASS |
| Admin Dashboard | AdminController | 4 | 1 | ✅ PASS |
| Admin Finance/Settings | AdminController | 7 | 1 | ✅ PASS |
| File Upload | FileUploadController | 2 | 1 | ✅ PASS |
| **Tổng** | **17 controller (+ WsChat STOMP)** | **73** | **25** | **✅ PASS** |

> WsChatController (STOMP `/app/conversations/{id}/typing`) là kênh WebSocket — không test bằng MockMvc HTTP, đã ghi test case manual WS-01..04.

---

## 4. Chi tiết test case tự động — module mới (SystemApiControllerTest)

| Test method | Phủ endpoint | Kịch bản chính | Status |
|---|---|---|:---:|
| `authApisReturnExpectedContracts` | 8 endpoint `/api/auth/*` | register(201) · login · refresh · logout · forgot · change-pw · me · verify-email | ✅ |
| `authValidationErrors` | register, login | email/password/role sai, login rỗng → 422 `VALIDATION_ERROR` | ✅ |
| `userApisReturnExpectedContracts` | list · detail · update · update-status | paged list, profile, đổi status | ✅ |
| `userValidationErrors` | update, update-status | email sai, status rỗng → 422 | ✅ |
| `walletApisReturnExpectedContracts` | 8 endpoint `/api/wallet/*` | wallet · transactions · topup(201) · confirm · withdraw(201, netAmount) · bank-accounts CRUD | ✅ |
| `walletValidationErrors` | topup, withdraw, bank-account | amount dưới min, method/STK sai → 422 | ✅ |
| `orderApisReturnExpectedContracts` | 11 endpoint `/api/orders/*` | list · detail · create(201) · status · cancel · accept · reject · complete · price request/approve/reject | ✅ |
| `orderValidationErrors` | create, price | thiếu field bắt buộc, giá âm → 422 | ✅ |
| `technicianApisReturnExpectedContracts` | list · detail · profile · availability · reviews | marketplace + cập nhật hồ sơ TV | ✅ |
| `technicianValidationErrors` | availability | thiếu `isAvailable` → 422 | ✅ |
| `verificationApisReturnExpectedContracts` | list · detail · review | duyệt hồ sơ xác minh (approved) | ✅ |
| `conversationApisReturnExpectedContracts` | list · create · messages · send · quote | luồng chat + gửi báo giá | ✅ |
| `conversationValidationErrors` | message, quote | nội dung rỗng / quote thiếu giá → 422 | ✅ |
| `quotationApisReturnExpectedContracts` | accept quote | accept → tạo orderId | ✅ |
| `reportApisReturnExpectedContracts` | admin list · create report | report đơn (201) + list | ✅ |
| `reportValidationErrors` | create report | thiếu reason/description → 422 | ✅ |
| `reviewApisReturnExpectedContracts` | create review | rating 5 (201); rating 6 → 422 | ✅ |
| `warrantyApisReturnExpectedContracts` | create · get | tạo & lấy claim bảo hành | ✅ |
| `warrantyValidationErrors` | create warranty | thiếu description → 422 | ✅ |
| `notificationApisReturnExpectedContracts` | list · mark-read · mark-all | inbox + đánh dấu đã đọc | ✅ |
| `vnpayIpnReturnsAck` | VNPay IPN | webhook trả ACK rspCode=00 (xem F-01) | ✅ |

Các module đã có sẵn (Categories/Admin/Upload — 4 test method, 18 endpoint) nằm trong `GrowupApiControllerTest` — tất cả PASS.

---

## 5. Negative / validation test đã phủ

| # | Endpoint | Input lỗi | Expected | Kết quả |
|---|---|---|---|:---:|
| 1 | POST `/api/auth/register` | email sai, password yếu, role=admin | 422, `VALIDATION_ERROR` | ✅ |
| 2 | POST `/api/auth/login` | identifier/password rỗng | 422 | ✅ |
| 3 | PATCH `/api/users/{id}` | email sai, phone sai pattern | 422 | ✅ |
| 4 | PATCH `/api/users/{id}/status` | status rỗng | 422 | ✅ |
| 5 | POST `/api/wallet/topup` | amount < 10,000, method=bitcoin | 422 | ✅ |
| 6 | POST `/api/wallet/withdraw` | amount < 50,000, bankAccountId rỗng | 422 | ✅ |
| 7 | POST `/api/wallet/bank-accounts` | bankName rỗng, STK chữ, owner thường | 422 | ✅ |
| 8 | POST `/api/orders` | thiếu deviceName/description/address | 422 | ✅ |
| 9 | PATCH `/api/orders/{id}/price` | newPrice âm, reason rỗng | 422 | ✅ |
| 10 | PATCH `/api/technicians/{id}/availability` | thiếu isAvailable | 422 | ✅ |
| 11 | POST `/api/conversations/{id}/messages` | content rỗng | 422 | ✅ |
| 12 | POST `/api/conversations/{id}/quotes` | serviceName rỗng / thiếu price | 422 | ✅ |
| 13 | POST `/api/orders/{id}/reports` | reason/description rỗng | 422 | ✅ |
| 14 | POST `/api/orders/{id}/reviews` | rating = 6 (>5) | 422 | ✅ |
| 15 | POST `/api/orders/{id}/warranty` | description rỗng | 422 | ✅ |

---

## 6. Phát hiện (Findings)

| ID | Mức độ | Mô tả | Khuyến nghị |
|---|---|---|---|
| F-01 | ⚠️ **Medium** | **VNPay IPN response sai key.** `VnpayIpnResponse` khai báo field `RspCode`/`Message` nhưng không có `@JsonProperty`, nên Jackson serialize thành `rspCode`/`message` (lower-camel). VNPay yêu cầu **đúng** `RspCode`/`Message` — sai key có thể khiến VNPay không nhận được ACK và **retry IPN nhiều lần**. | Thêm `@JsonProperty("RspCode")` / `@JsonProperty("Message")` vào DTO, hoặc đổi tên field. Cần test integration thật với sandbox VNPay. |
| F-02 | ℹ️ Info | `MarkReadResponse.isRead` (primitive boolean) serialize thành key JSON `read` (mất tiền tố `is`). FE cần đọc đúng key `read`. Tương tự cần soát các DTO boolean primitive khác để tránh lệch contract với FE. | Thống nhất convention; nếu FE dùng `isRead` thì thêm `@JsonProperty("isRead")`. |
| F-03 | ℹ️ Info | Lỗi validation trả **HTTP 422** (không phải 400). Đúng theo `GlobalExceptionHandler` nhưng khác mặc định REST phổ biến — cần ghi rõ trong API docs để FE/đối tác xử lý đúng. | Ghi chú vào `docs/API_REFERENCE.md`. |

---

## 7. Hạn chế & việc cần bổ sung (chưa nằm trong phạm vi test này)

Test hiện tại là **contract/controller test** (mock service, không full security/DB). Cần bổ sung trên staging:

- **RBAC runtime test** (OWASP A01): xác nhận `/api/admin/**`, `GET /api/reports`, `GET/PATCH /api/verifications`, `PATCH /api/users/{id}/status`, `POST/PUT/DELETE /api/categories` trả **403** khi sai role và **401** khi chưa đăng nhập. → Đã liệt kê đầy đủ trong ma trận RBAC mục 18 của tài liệu manual.
- **Persistence/integration test** (`@SpringBootTest` + Testcontainers PostgreSQL): tạo/đọc/cập nhật thật, kiểm tra transaction (vd trừ số dư ví, snapshot giá đơn).
- **Order state machine**: kiểm tra mọi chuyển trạng thái hợp lệ/không hợp lệ (`INVALID_ORDER_STATUS_TRANSITION`).
- **Wallet nghiệp vụ**: rút vượt số dư, fee 5,000, idempotency topup.
- **VNPay IPN**: chữ ký HMAC, idempotency (gọi IPN 2 lần không cộng kép) — liên quan F-01.
- **File upload thật**: loại file, dung lượng, file lưu vào `uploads/`.
- **Multipart `POST /api/verifications`**: test upload 4 file (idFront/idBack/portrait/certificate).
- **WebSocket STOMP**: kết nối JWT, broadcast typing.
- **NFR**: performance (k6), security scan (OWASP ZAP) trước go-live.

---

## 8. Kết luận

- ✅ **25/25 test tự động PASS** — phủ **73/75 endpoint HTTP** ở mức contract (2 endpoint còn lại là kênh WebSocket STOMP, test thủ công).
- ✅ Route, HTTP method, status code (200/201/422), response wrapper và body validation hoạt động đúng contract.
- ⚠️ **1 finding Medium (F-01 — VNPay IPN response key)** cần xử lý trước khi tích hợp thanh toán production.
- 📋 Đã bàn giao **137 test case manual** trong [docs/TEST_CASES_MANUAL.md](docs/TEST_CASES_MANUAL.md), bao gồm ma trận RBAC và các case nghiệp vụ/bảo mật cần chạy ở giai đoạn integration/UAT.
