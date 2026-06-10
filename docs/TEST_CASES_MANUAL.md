# Tài liệu Test Case Manual — BE E-Commerce (GlowUp Concierge)

| | |
|---|---|
| **Dự án** | BE E-Commerce — REST API (Spring Boot 3 + Java 21 + PostgreSQL + JWT) |
| **Phạm vi** | Toàn bộ REST API: auth, users, wallet, orders, technicians, verifications, conversations, quotes, reports, reviews, warranty, notifications, categories, admin, upload, payment webhook |
| **Người lập** | QA / Test Engineer |
| **Ngày lập** | 2026-06-02 |
| **Base URL** | `http://localhost:8080` |
| **Phiên bản** | 1.0 |

---

## 1. Quy ước chung

### 1.1. Response wrapper chuẩn

```jsonc
// Thành công
{ "success": true, "data": { ... }, "meta": { ... } }
// Lỗi
{ "success": false, "error": { "code": "MÃ_LỖI", "message": "...", "fields": { ... } } }
```

### 1.2. Mã trạng thái HTTP dùng trong hệ thống

| Code | Ý nghĩa trong hệ thống |
|---|---|
| 200 | Thành công, có/không data |
| 201 | Tạo mới thành công (register, create order, topup, withdraw, upload, create message…) |
| 401 | Chưa xác thực (thiếu/ sai/ hết hạn JWT) |
| 403 | Đã đăng nhập nhưng sai vai trò (RBAC) |
| 404 | Resource không tồn tại |
| 409 | Xung đột (trùng email/SĐT, sai trạng thái chuyển tiếp) |
| 422 | **Validation thất bại** — mã `VALIDATION_ERROR` (đây là code dùng cho lỗi `@Valid`, KHÔNG phải 400) |
| 500 | Lỗi nội bộ |

> ⚠️ **Lưu ý quan trọng cho tester:** mọi lỗi validation `@Valid`/`@Validated` trong hệ thống này trả về **HTTP 422** (qua `GlobalExceptionHandler`), không phải 400.

### 1.3. Vai trò (Role)

`ADMIN` · `TECHNICIAN` · `CUSTOMER`

### 1.4. Chuẩn bị dữ liệu / tài khoản test

| Biến | Mô tả |
|---|---|
| `{{ACCESS_CUSTOMER}}` | JWT access token của CUSTOMER |
| `{{ACCESS_TECH}}` | JWT access token của TECHNICIAN |
| `{{ACCESS_ADMIN}}` | JWT access token của ADMIN |
| `{{REFRESH}}` | Refresh token hợp lệ |

Header chung cho API cần xác thực: `Authorization: Bearer {{ACCESS_*}}` và `x-correlation-id: <uuid>`.

### 1.5. Quy ước cột

- **Pre-condition**: điều kiện cần trước khi chạy.
- **Steps**: thao tác gọi API.
- **Expected**: kết quả mong đợi (status + nội dung chính).
- **P** (Priority): P0 (blocking), P1 (chính), P2 (phụ).

---

## 2. AUTH — `/api/auth` (public)

| ID | API | Pre-condition | Steps / Body | Expected | P |
|---|---|---|---|---|---|
| AUTH-01 | POST `/register` | Email/SĐT chưa tồn tại | `{fullName, email, phone:0901234567, password:Abc@1234, role:customer}` | `201`, `data.accessToken`, `data.refreshToken`, `data.user.role=customer` | P0 |
| AUTH-02 | POST `/register` | — | role = `admin` (ngoài customer/technician) | `422`, `error.code=VALIDATION_ERROR`, field `role` | P1 |
| AUTH-03 | POST `/register` | — | password = `weak` (không đủ độ mạnh) | `422`, field `password` | P1 |
| AUTH-04 | POST `/register` | — | email = `not-an-email`, phone = `123` | `422`, fields `email`, `phone` | P1 |
| AUTH-05 | POST `/register` | Email đã tồn tại | body trùng email đã đăng ký | `409` (duplicate) | P1 |
| AUTH-06 | POST `/login` | Tài khoản tồn tại, active | `{identifier:email|phone, password}` | `200`, `data.accessToken`, `data.user` | P0 |
| AUTH-07 | POST `/login` | — | password sai | `401`, `error.code=INVALID_CREDENTIALS` | P0 |
| AUTH-08 | POST `/login` | — | `{identifier:"", password:""}` | `422` | P1 |
| AUTH-09 | POST `/login` | Tài khoản bị `suspended` | đăng nhập tài khoản đã khóa | `403`/`401` (tùy nghiệp vụ) | P1 |
| AUTH-10 | POST `/refresh-token` | Có refresh token hợp lệ | `{refreshToken:{{REFRESH}}}` | `200`, `data.accessToken` mới | P0 |
| AUTH-11 | POST `/refresh-token` | — | refresh token sai/hết hạn | `401`, `INVALID_TOKEN` | P1 |
| AUTH-12 | POST `/logout` | Đã đăng nhập | header `Authorization: Bearer ...` | `200`, `success=true` | P1 |
| AUTH-13 | GET `/me` | Đã đăng nhập | header Bearer | `200`, `data` = profile user hiện tại | P0 |
| AUTH-14 | GET `/me` | — | không có header Authorization | `401` | P0 |
| AUTH-15 | POST `/forgot-password` | Email tồn tại | `{identifier:email}` | `200` (gửi email reset) | P1 |
| AUTH-16 | POST `/change-password` | Có token reset hợp lệ | `{newPassword:Xyz@5678, confirmPassword:Xyz@5678, token}` | `200` | P1 |
| AUTH-17 | POST `/change-password` | — | newPassword ≠ confirmPassword | `422`/`400` (tùy nghiệp vụ) | P1 |
| AUTH-18 | POST `/verify-email` | Có token verify | `{token}` | `200` | P2 |

---

## 3. USERS — `/api/users` (auth)

| ID | API | Pre-condition | Steps / Body | Expected | P |
|---|---|---|---|---|---|
| USR-01 | GET `/` | Đã đăng nhập | `?role=customer&status=active&page=1&limit=10` | `200`, `data.items[]`, `data.pagination` | P1 |
| USR-02 | GET `/{id}` | User tồn tại | id hợp lệ | `200`, `data.id`, `data.code` | P1 |
| USR-03 | GET `/{id}` | — | id không tồn tại | `404` | P1 |
| USR-04 | PATCH `/{id}` | Chủ sở hữu/đăng nhập | `{fullName, district, bio}` | `200`, profile cập nhật | P1 |
| USR-05 | PATCH `/{id}` | — | `{email:"bad", phone:"123"}` | `422`, fields `email`/`phone` | P1 |
| USR-06 | PATCH `/{id}/status` | **ADMIN** | `{status:"suspended", reason}` | `200` (đổi trạng thái) | P0 |
| USR-07 | PATCH `/{id}/status` | CUSTOMER/TECH | gọi bằng token không phải admin | `403` | P0 (RBAC) |
| USR-08 | PATCH `/{id}/status` | ADMIN | `{status:""}` | `422` | P1 |

---

## 4. WALLET — `/api/wallet` (auth)

| ID | API | Pre-condition | Steps / Body | Expected | P |
|---|---|---|---|---|---|
| WAL-01 | GET `/` | Đã đăng nhập | — | `200`, `data.totalBalance`, `data.currency=VND` | P0 |
| WAL-02 | GET `/transactions` | — | `?type=all&walletType=all&page=1&limit=10` | `200`, `data.items[]`, `data.pagination` | P1 |
| WAL-03 | POST `/topup` | — | `{amount:100000, method:"vnpay"}` | `201`, `data.transactionId`, `data.checkoutUrl` | P0 |
| WAL-04 | POST `/topup` | — | `{amount:5000, method:"vnpay"}` (< 10,000) | `422`, field `amount` | P0 |
| WAL-05 | POST `/topup` | — | `{amount:100000, method:"bitcoin"}` | `422`, field `method` | P1 |
| WAL-06 | POST `/topup/confirm` | Có giao dịch vietqr chờ | `{transactionId:"TOPUP-001"}` | `200`, `data.status` | P1 |
| WAL-07 | POST `/topup/confirm` | — | `{transactionId:""}` | `422` | P2 |
| WAL-08 | POST `/withdraw` | Số dư đủ, có bank account | `{amount:100000, bankAccountId}` | `201`, `data.netAmount` = amount − fee(5000) | P0 |
| WAL-09 | POST `/withdraw` | — | `{amount:1000, bankAccountId}` (< 50,000) | `422`, field `amount` | P0 |
| WAL-10 | POST `/withdraw` | Số dư không đủ | amount > số dư | `422`/`409` (nghiệp vụ) | P0 |
| WAL-11 | GET `/bank-accounts` | — | — | `200`, `data.items[]` | P1 |
| WAL-12 | POST `/bank-accounts` | — | `{bankName, accountNumber:"0123456789", accountOwner:"NGUYEN VAN A"}` | `201`, `data.id` | P1 |
| WAL-13 | POST `/bank-accounts` | — | `accountNumber:"abc"` hoặc owner viết thường | `422`, fields tương ứng | P1 |
| WAL-14 | DELETE `/bank-accounts/{id}` | Bank account tồn tại | id hợp lệ | `200`, `data.message` | P1 |
| WAL-15 | GET `/` | Chưa đăng nhập | không Bearer | `401` | P0 |

---

## 5. ORDERS — `/api/orders` (auth, RBAC trong service)

| ID | API | Pre-condition | Steps / Body | Expected | P |
|---|---|---|---|---|---|
| ORD-01 | GET `/` | Đã đăng nhập | `?status=pending&page=1&limit=10` | `200`, `data.items[]` (lọc theo role) | P0 |
| ORD-02 | GET `/{id}` | Order tồn tại & có quyền | id hợp lệ | `200`, `data` chi tiết order | P0 |
| ORD-03 | GET `/{id}` | Order của người khác | CUSTOMER xem order không phải của mình | `403`/`404` | P0 (RBAC) |
| ORD-04 | POST `/` | CUSTOMER | `{deviceName, description, address, estimatedPrice, serviceName}` | `201`, `data.id`, `status=pending` | P0 |
| ORD-05 | POST `/` | — | thiếu `deviceName/description/address` | `422` | P0 |
| ORD-06 | PATCH `/{id}/status` | Có quyền | `{status:"in_progress"}` | `200`, `data.status` | P1 |
| ORD-07 | PATCH `/{id}/status` | — | chuyển trạng thái không hợp lệ | `422`/`409`, `INVALID_ORDER_STATUS_TRANSITION` | P1 |
| ORD-08 | POST `/{id}/cancel` | CUSTOMER, đơn chưa hoàn thành | `{reason:"Khách bận"}` | `200`, `status=cancelled` | P1 |
| ORD-09 | POST `/{id}/cancel` | — | `{reason:""}` | `422` | P2 |
| ORD-10 | POST `/{id}/accept` | TECHNICIAN | đơn ở trạng thái pending | `200`, `status=accepted` | P0 |
| ORD-11 | POST `/{id}/accept` | CUSTOMER | customer gọi accept | `403` | P0 (RBAC) |
| ORD-12 | POST `/{id}/reject` | TECHNICIAN | `{reason:"Quá xa"}` | `200` | P1 |
| ORD-13 | POST `/{id}/complete` | TECHNICIAN, đơn đang xử lý | `{finalPrice:350000}` | `200`, `finalPrice` | P0 |
| ORD-14 | POST `/{id}/complete` | — | `{finalPrice:-100}` | `422`, field `finalPrice` (Positive) | P1 |
| ORD-15 | PATCH `/{id}/price` | TECHNICIAN | `{newPrice:400000, reason:"Thay linh kiện", parts:[...]}` | `200`, `data.priceAdjustment` | P1 |
| ORD-16 | PATCH `/{id}/price` | — | `{newPrice:-100, reason:""}` | `422` | P1 |
| ORD-17 | POST `/{id}/price/approve` | CUSTOMER | có yêu cầu điều chỉnh giá đang chờ | `200`, `data.priceAdjustment.status=approved` | P1 |
| ORD-18 | POST `/{id}/price/reject` | CUSTOMER | `{reason:"Giá cao"}` | `200`, status rejected | P1 |

---

## 6. TECHNICIANS — `/api/technicians`

| ID | API | Pre-condition | Steps / Body | Expected | P |
|---|---|---|---|---|---|
| TEC-01 | GET `/` | Public | `?service=&district=Quận 1&isAvailable=true&minRating=4.0&page=1&limit=10` | `200`, `data.items[]` (lọc đúng filter) | P0 |
| TEC-02 | GET `/{id}` | Public, tech tồn tại | id hợp lệ | `200`, `data` chi tiết kỹ thuật viên | P0 |
| TEC-03 | GET `/{id}` | — | id không tồn tại | `404` | P1 |
| TEC-04 | PATCH `/{id}/profile` | TECHNICIAN (chính chủ) | `{bio, pricePerHour, skills[]}` | `200`, profile cập nhật | P1 |
| TEC-05 | PATCH `/{id}/profile` | Người khác | tech khác sửa hồ sơ không phải mình | `403` | P1 (RBAC) |
| TEC-06 | PATCH `/{id}/availability` | TECHNICIAN | `{isAvailable:false}` | `200`, `data.isAvailable=false` | P1 |
| TEC-07 | PATCH `/{id}/availability` | — | `{}` (thiếu isAvailable) | `422` | P1 |
| TEC-08 | GET `/{id}/reviews` | Public | `?page=1&limit=5` | `200`, `data.averageRating`, `data.items[]` | P2 |

---

## 7. VERIFICATIONS — `/api/verifications`

| ID | API | Pre-condition | Steps / Body | Expected | P |
|---|---|---|---|---|---|
| VRF-01 | GET `/` | **ADMIN** | `?status=pending&page=1&limit=10` | `200`, `data.items[]` | P1 |
| VRF-02 | GET `/` | Non-admin | token CUSTOMER/TECH | `403` | P1 (RBAC) |
| VRF-03 | POST `/` (multipart) | TECHNICIAN | form-data: `district, serviceCategory, yearsExperience, idFront, idBack, portrait, certificate` (file) | `201`, `data.id`, `status=pending` | P0 |
| VRF-04 | POST `/` (multipart) | — | thiếu `district`/`serviceCategory` | `422` | P1 |
| VRF-05 | GET `/{id}` | Chủ sở hữu hoặc ADMIN | id hợp lệ | `200`, `data.documents` | P1 |
| VRF-06 | PATCH `/{id}` | **ADMIN** | `{status:"approved", note}` | `200`, `data.status=approved`, `data.technicianStatus` | P0 |
| VRF-07 | PATCH `/{id}` | Non-admin | token không phải admin | `403` | P0 (RBAC) |

---

## 8. CONVERSATIONS & QUOTES — `/api/conversations`, `/api/quotes`

| ID | API | Pre-condition | Steps / Body | Expected | P |
|---|---|---|---|---|---|
| CHT-01 | GET `/conversations` | Đã đăng nhập | `?page=1&limit=20` | `200`, `data.items[]`, `unreadCount` | P1 |
| CHT-02 | POST `/conversations` | CUSTOMER | `{technicianId:"TECH-001", orderId}` | `201`, `data.id`, `participants[]` | P1 |
| CHT-03 | POST `/conversations` | — | `{technicianId:""}` | `422` | P2 |
| CHT-04 | GET `/conversations/{id}/messages` | Là thành viên hội thoại | `?page=1&limit=20` | `200`, `data.items[]` | P1 |
| CHT-05 | GET `/conversations/{id}/messages` | Không phải thành viên | user ngoài hội thoại | `403` | P1 (RBAC) |
| CHT-06 | POST `/conversations/{id}/messages` | Thành viên | `{type:"text", content:"Xin chào"}` | `201`, `data.id`, `data.content` | P0 |
| CHT-07 | POST `/conversations/{id}/messages` | — | `{content:""}` | `422` | P1 |
| CHT-08 | POST `/conversations/{id}/quotes` | TECHNICIAN | `{serviceName, price:300000, scheduledAt}` | `201`, `data.id`, `status=pending` | P1 |
| CHT-09 | POST `/conversations/{id}/quotes` | — | `{serviceName:"", price:null}` | `422` | P1 |
| QUO-01 | PATCH `/quotes/{id}/accept` | CUSTOMER (người nhận quote) | id quote hợp lệ | `200`, `data.status=accepted`, `data.orderId` | P0 |
| QUO-02 | PATCH `/quotes/{id}/accept` | Người không có quyền | user khác accept | `403` | P1 (RBAC) |

---

## 9. REPORTS — `/api/reports`, `/api/orders/{id}/reports`

| ID | API | Pre-condition | Steps / Body | Expected | P |
|---|---|---|---|---|---|
| RPT-01 | GET `/reports` | **ADMIN** | `?status=open&page=1&limit=10` | `200`, `data.items[]` | P1 |
| RPT-02 | GET `/reports` | Non-admin | token CUSTOMER/TECH | `403` | P1 (RBAC) |
| RPT-03 | POST `/orders/{id}/reports` | Thành viên đơn | `{reason:"Thợ trễ hẹn", description, evidenceImages[]}` | `201`, `data.id`, `status=open` | P1 |
| RPT-04 | POST `/orders/{id}/reports` | — | `{reason:"", description:""}` | `422` | P1 |

---

## 10. REVIEWS — `/api/orders/{id}/reviews`

| ID | API | Pre-condition | Steps / Body | Expected | P |
|---|---|---|---|---|---|
| REV-01 | POST `/orders/{id}/reviews` | CUSTOMER, đơn đã hoàn thành | `{rating:5, content:"Tuyệt vời", attachedImages[]}` | `201`, `data.rating=5` | P0 |
| REV-02 | POST `/orders/{id}/reviews` | — | `{rating:6}` (>5) | `422`, field `rating` | P1 |
| REV-03 | POST `/orders/{id}/reviews` | — | `{rating:0}` (<1) | `422`, field `rating` | P1 |
| REV-04 | POST `/orders/{id}/reviews` | Đơn chưa hoàn thành | review đơn đang xử lý | `422`/`409` (nghiệp vụ) | P1 |
| REV-05 | POST `/orders/{id}/reviews` | TECHNICIAN | tech tự review | `403` | P1 (RBAC) |

---

## 11. WARRANTY — `/api/orders/{id}/warranty`

| ID | API | Pre-condition | Steps / Body | Expected | P |
|---|---|---|---|---|---|
| WAR-01 | POST `/orders/{id}/warranty` | Đơn đã hoàn thành, còn hạn BH | `{description:"Máy lại hỏng", scheduledAt}` | `201`, `data.id`, `status=active` | P1 |
| WAR-02 | POST `/orders/{id}/warranty` | — | `{description:""}` | `422` | P1 |
| WAR-03 | GET `/orders/{id}/warranty` | Có claim | id đơn hợp lệ | `200`, `data.remainingDays` | P1 |
| WAR-04 | POST `/orders/{id}/warranty` | Đơn hết hạn bảo hành | tạo claim quá hạn | `422`/`409` (nghiệp vụ) | P2 |

---

## 12. NOTIFICATIONS — `/api/notifications` (auth)

| ID | API | Pre-condition | Steps / Body | Expected | P |
|---|---|---|---|---|---|
| NOT-01 | GET `/` | Đã đăng nhập | `?page=1&limit=20` | `200`, `data.unreadCount`, `data.items[]` | P1 |
| NOT-02 | PATCH `/{id}/read` | Notification của user | id hợp lệ | `200`, `data.isRead=true` | P1 |
| NOT-03 | PATCH `/read-all` | — | — | `200`, `data.updatedCount` | P1 |
| NOT-04 | GET `/` | Chưa đăng nhập | không Bearer | `401` | P1 |

---

## 13. CATEGORIES — `/api/categories`

| ID | API | Pre-condition | Steps / Body | Expected | P |
|---|---|---|---|---|---|
| CAT-01 | GET `/` | Public | `?status=active` | `200`, `data.items[]` | P0 |
| CAT-02 | POST `/` (multipart) | **ADMIN** | form-data: `title, description, priority, status, icon(file)` | `201`, `data.id`, `data.title` | P0 |
| CAT-03 | POST `/` | Non-admin | token CUSTOMER | `403` | P0 (RBAC) |
| CAT-04 | PUT `/{id}` (multipart) | ADMIN | cập nhật danh mục | `200`, `data` đã cập nhật | P1 |
| CAT-05 | DELETE `/{id}` | ADMIN | id hợp lệ | `200`, `data.message` | P1 |
| CAT-06 | PATCH `/{id}/status` | ADMIN | `{status:"inactive"}` | `200`, `data.status` | P1 |
| CAT-07 | POST `/` | ADMIN | `priority` không hợp lệ | `422`, `INVALID_CATEGORY_PRIORITY` | P2 |

---

## 14. ADMIN — `/api/admin` (chỉ ADMIN)

| ID | API | Pre-condition | Steps / Body | Expected | P |
|---|---|---|---|---|---|
| ADM-01 | GET `/dashboard/stats` | ADMIN | `?mode=month&year=2026` | `200`, revenue/profit/technicians/orders | P1 |
| ADM-02 | GET `/dashboard/revenue` | ADMIN | `?mode=month` | `200`, `data.range`, `data.items[]` | P1 |
| ADM-03 | GET `/stats/service-distribution` | ADMIN | `?mode=month` | `200`, `data.items[].name/percentage/color` | P2 |
| ADM-04 | GET `/dashboard/recent-orders` | ADMIN | `?limit=5` | `200`, `data.items[]` | P2 |
| ADM-05 | GET `/transactions` | ADMIN | `?type=all&date=2026-05-07&page=1&limit=10` | `200`, `data.totalBalance`, `data.items[]`, `pagination` | P1 |
| ADM-06 | GET `/withdraw-requests` | ADMIN | — | `200`, `data.pendingCount`, `data.items[]` | P1 |
| ADM-07 | POST `/withdraw-requests/{id}/approve` | ADMIN | id hợp lệ | `200`, `data.status=approved` | P0 |
| ADM-08 | PATCH `/commission` | ADMIN | `{fixedCommissionFee, minimumCommissionBalance}` | `200`, giá trị cập nhật | P1 |
| ADM-09 | GET `/commission-settings` | ADMIN | — | `200` | P2 |
| ADM-10 | GET `/commission-wallets` | ADMIN | `?status=all&page=1&size=10` | `200`, danh sách ví hoa hồng | P2 |
| ADM-11 | POST `/wallet/adjust` | ADMIN | `{technicianId, amount:-50000, type, reason}` | `200`, `data.transactionId`, `data.newBalance` | P0 |
| ADM-12 | GET `/settings` | ADMIN | — | `200`, `general/billing/notifications/operations` | P1 |
| ADM-13 | PUT `/settings` | ADMIN | body settings hợp lệ | `200`, `data.message` | P1 |
| ADM-14 | bất kỳ `/admin/*` | Non-admin | token CUSTOMER/TECH | `403` | P0 (RBAC) |
| ADM-15 | bất kỳ `/admin/*` | Chưa login | không Bearer | `401` | P0 |

---

## 15. FILE UPLOAD — `/api/upload` (auth)

| ID | API | Pre-condition | Steps / Body | Expected | P |
|---|---|---|---|---|---|
| UPL-01 | POST `/image` (multipart) | Đã đăng nhập | `file(image)`, `folder=avatars` | `201`, `data.url/filename/size/mimeType` | P1 |
| UPL-02 | POST `/images` (multipart) | Đã đăng nhập | nhiều `files`, `folder=orders` | `201`, `data.urls[]` | P1 |
| UPL-03 | POST `/image` | — | file không phải ảnh (.exe) | `422`, `INVALID_FILE_TYPE` | P1 |
| UPL-04 | POST `/image` | — | folder không hợp lệ | `422`, `INVALID_FILE_FOLDER` | P2 |
| UPL-05 | POST `/image` | Chưa login | không Bearer | `401` | P1 |

---

## 16. PAYMENT WEBHOOK — `/api/payments/vnpay/ipn` (public)

| ID | API | Pre-condition | Steps / Body | Expected | P |
|---|---|---|---|---|---|
| PAY-01 | GET `/vnpay/ipn` | Giao dịch topup tồn tại, hash đúng | query VNPay: `vnp_TxnRef, vnp_ResponseCode=00, vnp_SecureHash` | `200`, `{RspCode:"00", Message:"Confirm Success"}`, ví được cộng tiền | P0 |
| PAY-02 | GET `/vnpay/ipn` | Sai chữ ký | `vnp_SecureHash` sai | `200`, `RspCode≠00` (vd `97` - Invalid signature) | P0 |
| PAY-03 | GET `/vnpay/ipn` | TxnRef không tồn tại | `vnp_TxnRef` lạ | `200`, `RspCode="01"` (Order not found) | P1 |
| PAY-04 | GET `/vnpay/ipn` | Giao dịch đã xử lý | gọi IPN lần 2 (idempotency) | `200`, `RspCode="02"` (đã confirm), ví KHÔNG cộng kép | P0 |

> Lưu ý: VNPay IPN luôn trả HTTP 200 với `RspCode` khác nhau theo nghiệp vụ — đây là yêu cầu của cổng thanh toán (không trả 4xx/5xx cho gateway).

---

## 17. WEBSOCKET CHAT (STOMP) — `/ws`

| ID | Kênh | Pre-condition | Steps | Expected | P |
|---|---|---|---|---|---|
| WS-01 | Handshake `/ws` | JWT trong STOMP header | kết nối với token hợp lệ | Kết nối thành công | P1 |
| WS-02 | Handshake `/ws` | — | token sai/thiếu | Bị từ chối kết nối | P1 |
| WS-03 | SEND `/app/conversations/{id}/typing` | Đã kết nối | gửi sự kiện typing | Broadcast tới `/topic/conversations/{id}/typing` | P2 |
| WS-04 | SEND `/app/conversations/{id}/stop-typing` | Đã kết nối | gửi stop typing | Broadcast stop-typing | P2 |

---

## 18. Ma trận kiểm thử bảo mật / RBAC (gợi ý chạy riêng)

| Nhóm endpoint | ADMIN | TECHNICIAN | CUSTOMER | Chưa login |
|---|:---:|:---:|:---:|:---:|
| `GET /api/categories`, `GET /api/technicians`, `/api/auth/*`, `/api/payments/*` | ✅ | ✅ | ✅ | ✅ |
| `/api/admin/**` | ✅ | 403 | 403 | 401 |
| `POST/PUT/DELETE /api/categories` | ✅ | 403 | 403 | 401 |
| `GET /api/reports`, `GET /api/verifications`, `PATCH /api/verifications/{id}` | ✅ | 403 | 403 | 401 |
| `PATCH /api/users/{id}/status` | ✅ | 403 | 403 | 401 |
| `/api/wallet/**`, `/api/notifications/**`, `/api/orders/**` | ✅ | ✅ | ✅ | 401 |
| `POST /api/orders/{id}/accept|reject` | (tùy) | ✅ | 403 | 401 |
| `POST /api/orders` (tạo đơn) | (tùy) | 403 | ✅ | 401 |

> Mỗi ô khác ✅ là một test case bắt buộc cho phần Security (OWASP A01 — Broken Access Control).

---

## 19. Tổng hợp số lượng test case manual

| Module | Số endpoint | Số test case |
|---|---:|---:|
| Auth | 8 | 18 |
| Users | 4 | 8 |
| Wallet | 8 | 15 |
| Orders | 11 | 18 |
| Technicians | 5 | 8 |
| Verifications | 4 | 7 |
| Conversations & Quotes | 6 | 11 |
| Reports | 2 | 4 |
| Reviews | 1 | 5 |
| Warranty | 2 | 4 |
| Notifications | 3 | 4 |
| Categories | 5 | 7 |
| Admin | 13 | 15 |
| Upload | 2 | 5 |
| Payment Webhook | 1 | 4 |
| WebSocket | — | 4 |
| **Tổng** | **75** | **137** |
