# GlowUp Concierge — API Reference

Tài liệu tham chiếu **toàn bộ** REST endpoints + WebSocket events của backend
GlowUp Concierge (Spring Boot 3 + Java 21 + PostgreSQL + JWT).

> Đây là spec đã được implement, **không phải design draft**. Mọi request/response shape
> dưới đây khớp với code trong `controller/`, `service/`, `dto/` và `WsEventPublisher`.

---

## 1. Tổng quan

| Hạng mục | Giá trị |
|---|---|
| Base URL | `http://localhost:8080` |
| API prefix | `/api` |
| WS endpoint | `/ws` (SockJS + STOMP) |
| Auth | JWT Bearer (`Authorization: Bearer <accessToken>`) |
| Content-Type | `application/json` cho phần lớn; `multipart/form-data` cho upload + verification |
| Pagination | `?page=1&limit=10` (1-indexed); response chứa `pagination: { page, limit, total, totalPages }` |
| Date format | ISO-8601 UTC (`2026-05-07T14:00:00.000Z`) |
| Currency | VND, lưu **BIGINT không thập phân** |

### 1.1 Response envelope

```json
// Success
{ "success": true, "data": { ... } }

// Error
{ "success": false, "error": { "code": "...", "message": "...", "fields": { "...": "..." } } }
```

### 1.2 Authentication header

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

Access token expire mặc định 15 phút, refresh 7 ngày — dùng `POST /api/auth/refresh-token`.

### 1.3 Roles

- `customer` — đặt đơn, đánh giá, tạo bảo hành/report, chat
- `technician` — nhận đơn, báo giá, KYC submission
- `admin` — duyệt KYC, xem báo cáo, quản lý hoa hồng

---

## 2. Quick reference (tất cả endpoints)

| # | Method | Path | Module | Role |
|---|---|---|---|---|
| **AUTH** ||||| 
| 1 | POST | `/api/auth/register` | Auth | Public |
| 2 | POST | `/api/auth/login` | Auth | Public |
| 3 | POST | `/api/auth/refresh-token` | Auth | Public |
| 4 | POST | `/api/auth/logout` | Auth | Authenticated |
| 5 | POST | `/api/auth/forgot-password` | Auth | Public |
| 6 | POST | `/api/auth/change-password` | Auth | Authenticated |
| 7 | POST | `/api/auth/verify-email` | Auth | Public (token from email) |
| 8 | GET | `/api/auth/me` | Auth | Authenticated |
| **USER** ||||| 
| 9 | GET | `/api/users` | User | Authenticated |
| 10 | GET | `/api/users/:id` | User | Authenticated |
| 11 | PATCH | `/api/users/:id` | User | Self or Admin |
| 12 | PATCH | `/api/users/:id/status` | User | Admin |
| **TECHNICIAN** ||||| 
| 13 | GET | `/api/technicians` | Technician | Public |
| 14 | GET | `/api/technicians/:id` | Technician | Public |
| 15 | PATCH | `/api/technicians/:id/profile` | Technician | Self or Admin |
| 16 | PATCH | `/api/technicians/:id/availability` | Technician | Self or Admin |
| 17 | GET | `/api/technicians/:id/reviews` | Technician | Public |
| **ORDER** ||||| 
| 18 | GET | `/api/orders` | Order | Authenticated |
| 19 | POST | `/api/orders` | Order | Customer |
| 20 | GET | `/api/orders/:id` | Order | Participant or Admin |
| 21 | PATCH | `/api/orders/:id/status` | Order | Technician/Admin |
| 22 | POST | `/api/orders/:id/cancel` | Order | Participant or Admin |
| 23 | POST | `/api/orders/:id/accept` | Order | Technician |
| 24 | POST | `/api/orders/:id/reject` | Order | Technician |
| 25 | POST | `/api/orders/:id/complete` | Order | Technician |
| 26 | PATCH | `/api/orders/:id/price` | Order | Technician |
| 27 | POST | `/api/orders/:id/price/approve` | Order | Customer |
| 28 | POST | `/api/orders/:id/price/reject` | Order | Customer |
| **REVIEW** ||||| 
| 29 | POST | `/api/orders/:id/reviews` | Review | Customer |
| **WARRANTY** ||||| 
| 30 | POST | `/api/orders/:id/warranty` | Warranty | Customer |
| 31 | GET | `/api/orders/:id/warranty` | Warranty | Participant or Admin |
| **REPORT** ||||| 
| 32 | POST | `/api/orders/:id/reports` | Report | Participant |
| 33 | GET | `/api/reports` | Report | Admin |
| **CHAT** ||||| 
| 34 | GET | `/api/conversations` | Chat | Authenticated |
| 35 | POST | `/api/conversations` | Chat | Customer |
| 36 | GET | `/api/conversations/:id/messages` | Chat | Participant |
| 37 | POST | `/api/conversations/:id/messages` | Chat | Participant |
| **QUOTATION** ||||| 
| 38 | POST | `/api/conversations/:id/quotes` | Quotation | Technician |
| 39 | PATCH | `/api/quotes/:id/accept` | Quotation | Customer |
| **WALLET** ||||| 
| 40 | GET | `/api/wallet` | Wallet | Authenticated |
| 41 | GET | `/api/wallet/transactions` | Wallet | Authenticated |
| 42 | POST | `/api/wallet/topup` | Wallet | Authenticated |
| 43 | POST | `/api/wallet/topup/confirm` | Wallet | Authenticated |
| 44 | POST | `/api/wallet/withdraw` | Wallet | Authenticated |
| 45 | GET | `/api/wallet/bank-accounts` | Wallet | Authenticated |
| 46 | POST | `/api/wallet/bank-accounts` | Wallet | Authenticated |
| 47 | DELETE | `/api/wallet/bank-accounts/:id` | Wallet | Authenticated |
| **VERIFICATION** ||||| 
| 48 | GET | `/api/verifications` | Verification | Admin |
| 49 | POST | `/api/verifications` | Verification | Technician |
| 50 | GET | `/api/verifications/:id` | Verification | Owner or Admin |
| 51 | PATCH | `/api/verifications/:id` | Verification | Admin |
| **CATEGORY** ||||| 
| 52 | GET | `/api/categories` | Category | Public |
| 53 | POST | `/api/categories` | Category | Admin |
| 54 | PUT | `/api/categories/:id` | Category | Admin |
| 55 | DELETE | `/api/categories/:id` | Category | Admin |
| 56 | PATCH | `/api/categories/:id/status` | Category | Admin |
| **ADMIN** ||||| 
| 57 | GET | `/api/admin/stats` | Admin Dashboard | Admin |
| 58 | GET | `/api/admin/stats/revenue` | Admin Dashboard | Admin |
| 59 | GET | `/api/admin/stats/service-distribution` | Admin Dashboard | Admin |
| 60 | GET | `/api/admin/orders/recent` | Admin Dashboard | Admin |
| 61 | GET | `/api/admin/transactions` | Admin Finance | Admin |
| 62 | GET | `/api/admin/withdraw-requests` | Admin Finance | Admin |
| 63 | POST | `/api/admin/withdraw-requests/:id/approve` | Admin Finance | Admin |
| 64 | PATCH | `/api/admin/commission` | Admin Finance | Admin |
| 65 | POST | `/api/admin/wallet/adjust` | Admin Finance | Admin |
| 66 | GET | `/api/admin/settings` | Admin Settings | Admin |
| 67 | PUT | `/api/admin/settings` | Admin Settings | Admin |
| **FILE UPLOAD** ||||| 
| 68 | POST | `/api/upload/image` | Upload | Authenticated |
| 69 | POST | `/api/upload/images` | Upload | Authenticated |
| **NOTIFICATION** ||||| 
| 70 | GET | `/api/notifications` | Notification | Authenticated |
| 71 | PATCH | `/api/notifications/:id/read` | Notification | Authenticated |
| 72 | PATCH | `/api/notifications/read-all` | Notification | Authenticated |

**Tổng: 72 REST endpoints + 7 WebSocket events.**

---

## 3. AUTH

### 3.1 POST `/api/auth/login`

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "0901234567",
    "password": "Abc@1234",
    "role": "customer"
  }'
```

**200 OK**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "user": {
      "id": "USR-001",
      "fullName": "Trần Thị Lan",
      "email": "lan@email.com",
      "phone": "0901234567",
      "role": "customer",
      "avatar": "https://...",
      "status": "verified"
    }
  }
}
```

**401** `INVALID_CREDENTIALS` — email/phone hoặc password sai.

### 3.2 POST `/api/auth/register`

```json
{
  "fullName": "Nguyễn Văn A",
  "email": "vana@email.com",
  "phone": "0901234567",
  "password": "Abc@1234",
  "role": "customer"
}
```

Trả `201` với cùng shape login, status mặc định `pending` (sẽ chuyển `verified` sau khi xác nhận email).

### 3.3 POST `/api/auth/forgot-password`

```json
{ "identifier": "0901234567" }
```

### 3.4 POST `/api/auth/change-password`

```json
{
  "newPassword": "NewAbc@1234",
  "confirmPassword": "NewAbc@1234",
  "token": "reset-token-from-sms-or-email"
}
```

### 3.5 GET `/api/auth/me`

```bash
curl http://localhost:8080/api/auth/me -H "Authorization: Bearer $TOKEN"
```

### 3.6 Other auth endpoints

`POST /api/auth/refresh-token`, `POST /api/auth/logout`, `POST /api/auth/verify-email` — standard JWT refresh, logout revokes refresh token, verify-email kích hoạt account.

---

## 4. USER

### 4.1 GET `/api/users`

```
?role=technician&status=pending&district=Quận 1&keyword=Nguyễn&page=1&limit=10
```

### 4.2 PATCH `/api/users/:id`

Update profile (fullName, phone, email, address). Self-only trừ admin.

### 4.3 PATCH `/api/users/:id/status` (Admin)

```json
{ "status": "locked", "reason": "Vi phạm điều khoản sử dụng" }
```

---

## 5. TECHNICIAN

### 5.1 GET `/api/technicians`

Public marketplace listing với filter:
```
?service=Máy lạnh&district=Quận 1&minRating=4&isAvailable=true&page=1&limit=10
```

`rating`, `reviewCount`, `completedJobs` được tính realtime từ Review/Order.

### 5.2 GET `/api/technicians/:id`

Full profile gồm `bio`, `skills[]`, `coverImage`, `verificationStatus`, `schedule: { monday, tuesday, ... }`.

### 5.3 PATCH `/api/technicians/:id/profile`

Self hoặc admin. PATCH semantics — chỉ field non-null mới apply.

```json
{
  "fullName": "Nguyễn Văn Minh",
  "phone": "098 765 4321",
  "bio": "Kỹ thuật viên hơn 10 năm kinh nghiệm...",
  "skills": ["Máy lạnh", "Máy giặt", "Tủ lạnh"],
  "areas": ["Quận Bình Thạnh", "Quận 1", "Quận 3"]
}
```

### 5.4 PATCH `/api/technicians/:id/availability`

```json
{ "isAvailable": true }
```

### 5.5 GET `/api/technicians/:id/reviews`

```
?page=1&limit=5
```
Trả `averageRating`, `totalReviews`, paged `items[]`.

---

## 6. ORDER (module core)

### 6.1 POST `/api/orders` (Customer)

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{
    "deviceName": "Máy giặt cửa ngang LG",
    "description": "Máy giặt nhà tôi dạo này chạy không vắt được...",
    "address": "Quận 7, HCMC",
    "estimatedPrice": 450000,
    "expectedTime": "2026-05-08T14:00:00.000Z",
    "serviceCategory": "Máy giặt",
    "images": ["https://...image1.jpg"]
  }'
```
→ `201` với order code `GU-99300`, status `new`.

### 6.2 GET `/api/orders`

```
?status=scheduled&page=1&limit=10
```

- Customer thấy chỉ đơn của mình
- Technician thấy đơn được assign
- Admin thấy tất cả

### 6.3 GET `/api/orders/:id`

Detail có thêm `priceAdjustment` (nếu có), `images[]`, `review` (sau khi có).

### 6.4 PATCH `/api/orders/:id/status` (Technician)

Chỉ chấp nhận `SCHEDULED → IN_PROGRESS → COMPLETED`.
```json
{ "status": "in-progress" }
```

### 6.5 POST `/api/orders/:id/cancel`

```json
{ "reason": "Tôi tìm được người quen sửa giúp rồi" }
```

### 6.6 POST `/api/orders/:id/accept` (Technician)

Không body. Yêu cầu order ở status `new`. Conflict 409 `ORDER_ALREADY_TAKEN` nếu đã có người nhận.

### 6.7 POST `/api/orders/:id/reject` (Technician)

```json
{ "reason": "Tôi đang kẹt xe ở quận khác không về kịp" }
```
→ Đơn trả về pool, status `new`.

### 6.8 POST `/api/orders/:id/complete` (Technician)

```json
{
  "finalPrice": 600000,
  "images": ["https://...completion1.jpg"]
}
```

### 6.9 PATCH `/api/orders/:id/price` (Technician)

```json
{
  "newPrice": 600000,
  "reason": "Phát sinh thay tụ điện do cháy nổ linh kiện cũ",
  "parts": [
    { "name": "Thay tụ quạt dàn lạnh", "price": 150000, "partCode": "CAP-D822" }
  ],
  "evidenceImages": ["https://...evidence1.jpg"]
}
```

### 6.10 POST `/api/orders/:id/price/approve` (Customer)

Không body. Update `order.finalPrice` = `newPrice` của adjustment đang pending.

### 6.11 POST `/api/orders/:id/price/reject` (Customer)

```json
{ "reason": "Chi phí phát sinh quá cao so với ban đầu" }
```

---

## 7. REVIEW

### 7.1 POST `/api/orders/:id/reviews` (Customer, after COMPLETED)

```json
{
  "rating": 5,
  "content": "Anh Hùng làm việc rất kỹ, giải thích rõ ràng",
  "attachedImages": ["https://...review1.jpg"]
}
```
→ `201`, code `REV-001`. Constraint: 1 review/order.

---

## 8. WARRANTY

### 8.1 POST `/api/orders/:id/warranty` (Customer, after COMPLETED)

```json
{
  "description": "Máy lạnh rỉ nước lại chỗ cũ sau 2 tuần sửa",
  "images": ["https://...warranty1.jpg"],
  "scheduledAt": "2026-05-10T09:00:00.000Z"
}
```

### 8.2 GET `/api/orders/:id/warranty`

Trả về claim mới nhất, kèm `warrantyExpiresAt = completedAt + warrantyMonths (default 3)` và `remainingDays`.

---

## 9. REPORT

### 9.1 POST `/api/orders/:id/reports` (Participant)

`reason` enum: `extra_fee | bad_attitude | no_show | poor_quality | fraud | other`.

```json
{
  "reason": "extra_fee",
  "description": "Thợ yêu cầu thu thêm phụ phí ngoài hệ thống 200k",
  "evidenceImages": ["https://...evidence1.jpg"]
}
```

### 9.2 GET `/api/reports` (Admin)

```
?status=open&keyword=GU-9921&page=1&limit=10
```

---

## 10. CHAT

### 10.1 GET `/api/conversations`

Trả về conversations của user hiện tại với `partner` (đối tác), `lastMessage`, `unreadCount`.

### 10.2 POST `/api/conversations` (Customer)

```json
{ "technicianId": "TECH-001", "orderId": "GU-99300" }
```
→ Tạo `CONV-002` (hoặc reuse nếu đã có conv giữa cùng cặp customer-technician).

### 10.3 GET `/api/conversations/:id/messages`

`?page=1&limit=20`. **Side effect:** tự động update `lastReadAt` của caller — mark-as-read on view.

### 10.4 POST `/api/conversations/:id/messages`

```json
{ "type": "text", "content": "Ok anh." }
```
Type chấp nhận: `text`, `image`. Quote messages **không** tạo qua endpoint này — dùng `/quotes`.

---

## 11. QUOTATION

### 11.1 POST `/api/conversations/:id/quotes` (Technician)

```json
{
  "serviceName": "Sửa máy lạnh",
  "description": "Vệ sinh + nạp gas R32",
  "price": 450000,
  "scheduledAt": "2026-05-08T14:00:00.000Z",
  "notes": "Bao gồm vật tư"
}
```
→ Tạo Quotation + tự động đăng message type=`quotation` vào thread.

### 11.2 PATCH `/api/quotes/:id/accept` (Customer)

Không body. Side effects:
- Quote → `accepted`
- **Tạo Order mới với status `scheduled`** linking customer↔technician + service info từ quote
- Response chứa `orderId`

---

## 12. WALLET

### 12.1 GET `/api/wallet`

Số dư hiện tại + tổng đã kiếm/đã rút.

### 12.2 GET `/api/wallet/transactions`

`?type=all&page=1&limit=10` — `type` ∈ `topup | withdraw | commission | earning | refund | adjustment`.

### 12.3 POST `/api/wallet/topup`

```json
{ "amount": 500000, "method": "vietqr" }
```
→ Trả về QR code + thông tin chuyển khoản. Method khác: `vnpay` (redirect URL).

### 12.4 POST `/api/wallet/topup/confirm`

Sau khi user chuyển tiền:
```json
{ "transactionId": "TX-TOPUP-001" }
```

### 12.5 POST `/api/wallet/withdraw`

```json
{ "amount": 1000000, "bankAccountId": "BANK-001" }
```
Trả về `transactionId`, `fee`, `netAmount`, `status: pending`.

### 12.6 Bank accounts

| Method | Path |
|---|---|
| GET | `/api/wallet/bank-accounts` |
| POST | `/api/wallet/bank-accounts` — body: `{ bankName, accountNumber, accountOwner }` |
| DELETE | `/api/wallet/bank-accounts/:id` |

---

## 13. VERIFICATION (KYC)

### 13.1 POST `/api/verifications` (Technician, multipart)

```bash
curl -X POST http://localhost:8080/api/verifications \
  -H "Authorization: Bearer $TOKEN" \
  -F "district=Quận 7, HCMC" \
  -F "serviceCategory=Điện lạnh" \
  -F "yearsExperience=5" \
  -F "idFront=@/path/id-front.jpg" \
  -F "idBack=@/path/id-back.jpg" \
  -F "portrait=@/path/portrait.jpg" \
  -F "certificate=@/path/cert.pdf"
```

Chặn nếu đang có submission pending cho cùng technician (409 `VERIFICATION_PENDING_EXISTS`).

### 13.2 GET `/api/verifications` (Admin)

`?status=pending&keyword=Nguyễn&page=1&limit=10`

### 13.3 GET `/api/verifications/:id`

Owner hoặc admin.

### 13.4 PATCH `/api/verifications/:id` (Admin)

```json
{
  "status": "approved",
  "note": "Hồ sơ đầy đủ, thông tin trùng khớp.",
  "reviewedBy": "Admin AD-9902",
  "notifyTechnician": true
}
```

Side effect: cập nhật `technicianProfile.verificationStatus`.

---

## 14. CATEGORY

GET public, mọi endpoint khác Admin-only. Body multipart cho POST/PUT (vì có icon upload).

```bash
curl -X POST http://localhost:8080/api/categories \
  -H "Authorization: Bearer $TOKEN" \
  -F "title=Máy lạnh" \
  -F "description=Vệ sinh, bơm gas, sửa chữa board mạch" \
  -F "priority=high" \
  -F "status=active" \
  -F "icon=@/path/icon.svg"
```

---

## 15. ADMIN

### 15.1 Dashboard

| Path | Description |
|---|---|
| `GET /api/admin/stats` | Total revenue / profit / active technicians / orders today |
| `GET /api/admin/stats/revenue?range=7days` | Revenue chart data |
| `GET /api/admin/stats/service-distribution` | Pie chart data |
| `GET /api/admin/orders/recent?limit=5` | Latest orders |

### 15.2 Finance

```bash
# Approve withdraw
curl -X POST http://localhost:8080/api/admin/withdraw-requests/WR-001/approve \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Update commission/VAT
curl -X PATCH http://localhost:8080/api/admin/commission \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H "Content-Type: application/json" \
  -d '{ "platformFeePercent": 15, "vatPercent": 10 }'

# Adjust technician wallet
curl -X POST http://localhost:8080/api/admin/wallet/adjust \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H "Content-Type: application/json" \
  -d '{
    "technicianId": "TECH-001",
    "amount": -50000,
    "type": "commission-minus",
    "reason": "Điều chỉnh hoa hồng đơn GU-99210"
  }'
```

### 15.3 Settings

`GET` / `PUT /api/admin/settings` quản lý `general`, `billing`, `notifications`, `operations` blocks.

---

## 16. FILE UPLOAD

```bash
curl -X POST http://localhost:8080/api/upload/image \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@/path/photo.jpg" \
  -F "folder=avatars"
```

`folder` ∈ `avatars | orders | verifications | categories`. Multi-upload tương tự `/api/upload/images` (key `files`).

---

## 17. NOTIFICATIONS

| Method | Path | Description |
|---|---|---|
| GET | `/api/notifications` | Inbox + `unreadCount` |
| PATCH | `/api/notifications/:id/read` | Mark single as read |
| PATCH | `/api/notifications/read-all` | Mark all as read |

Notifications được **tự động tạo** khi:
- Technician nhận đơn → notify customer (`order_accepted`)
- Technician hoàn thành → notify customer (`order_completed`)
- Technician đề nghị tăng giá → notify customer (`price_adjustment`)
- Customer accept quote → notify technician (`order_accepted`)

Mỗi notification persistence DB **đồng thời** phát WebSocket event (mục 18).

---

## 18. WebSocket events

### 18.1 Kết nối

```js
import SockJS from 'sockjs-client'
import { Client } from '@stomp/stompjs'

const client = new Client({
  webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
  connectHeaders: { Authorization: `Bearer ${accessToken}` },
  onConnect: () => { /* subscribe... */ }
})
client.activate()
```

JWT phải gửi qua STOMP `Authorization` header trên CONNECT frame.

### 18.2 Subscribe destinations

| Destination | Events nhận |
|---|---|
| `/topic/conversations.{convCode}` | `message:new`, `typing`, `stop_typing` |
| `/topic/orders.{orderCode}` | `order:status_changed`, `price:adjustment_requested` |
| `/topic/notifications.{userCode}` | `notification:new` |

### 18.3 Payload shapes

```json
// message:new — push to /topic/conversations.CONV-001
{
  "event": "message:new",
  "conversationId": "CONV-001",
  "message": {
    "id": "MSG-010",
    "senderId": "TECH-001",
    "type": "text",
    "content": "Tôi đang trên đường đến",
    "sentAt": "2026-05-07T13:55:00.000Z",
    "isRead": false
  }
}

// order:status_changed
{
  "event": "order:status_changed",
  "orderId": "GU-99210",
  "oldStatus": "scheduled",
  "newStatus": "in-progress",
  "updatedAt": "2026-05-07T14:05:00.000Z"
}

// price:adjustment_requested
{
  "event": "price:adjustment_requested",
  "orderId": "GU-99210",
  "originalPrice": 450000,
  "newPrice": 600000,
  "reason": "Phát sinh thay tụ điện"
}

// notification:new
{
  "event": "notification:new",
  "id": "NOTIF-003",
  "type": "order_completed",
  "title": "Đơn hoàn thành",
  "body": "Đơn GU-99210 đã hoàn thành",
  "data": { "orderId": "GU-99210" }
}
```

### 18.4 Send (client → server)

| Destination | Body | Effect |
|---|---|---|
| `/app/conversations/{id}/typing` | empty | Fan out `typing` event |
| `/app/conversations/{id}/stop-typing` | empty | Fan out `stop_typing` |

`join_conversation` (theo PDF spec) thực hiện ngầm qua `SUBSCRIBE /topic/conversations.{id}`.

### 18.5 Spec ↔ STOMP mapping

| PDF Socket.IO syntax | STOMP equivalent |
|---|---|
| `socket.on("message:new", cb)` | `client.subscribe('/topic/conversations.{id}', cb)` rồi switch `payload.event` |
| `socket.on("order:status_changed", cb)` | `client.subscribe('/topic/orders.{id}', cb)` |
| `socket.emit("typing", {...})` | `client.publish({ destination: '/app/conversations/{id}/typing' })` |
| `socket.emit("join_conversation", {...})` | Không cần — subscribe ngầm join |

---

## 19. Error codes

| Code | HTTP | Ý nghĩa |
|---|---|---|
| `UNAUTHORIZED` | 401 | Chưa đăng nhập hoặc token hết hạn |
| `FORBIDDEN` | 403 | Không đủ quyền |
| `NOT_FOUND` | 404 | Tài nguyên không tồn tại |
| `VALIDATION_ERROR` | 422 | Lỗi field validation, kèm `fields` map |
| `INVALID_CREDENTIALS` | 401 | Sai email/phone hoặc password |
| `INVALID_TOKEN` | 401 | Token signature sai hoặc revoked |
| `TOKEN_EXPIRED` | 401 | Access/refresh token expired |
| `EMAIL_ALREADY_EXISTS` | 409 | Email trùng |
| `PHONE_ALREADY_EXISTS` | 409 | SĐT trùng |
| `INSUFFICIENT_BALANCE` | 400 | Số dư không đủ để rút |
| `ORDER_ALREADY_TAKEN` | 409 | Đơn đã có thợ nhận |
| `INVALID_ORDER_STATUS_TRANSITION` | 400 | Chuyển trạng thái không hợp lệ |
| `REVIEW_ALREADY_EXISTS` | 409 | Đơn đã có review |
| `WARRANTY_EXPIRED` | 400 | Đã hết thời hạn bảo hành |
| `VERIFICATION_PENDING_EXISTS` | 409 | Đang có submission pending |
| `QUOTATION_NOT_PENDING` | 400 | Báo giá không còn pending để accept |
| `INTERNAL_SERVER_ERROR` | 500 | Lỗi server |

---

## 20. Setup & run

```bash
# Prerequisites: Java 21, Maven 3.9+, PostgreSQL (hoặc dùng docker-compose.yml)

# 1. Environment variables (.env hoặc shell)
export DB_URL="jdbc:postgresql://localhost:5432/glowup"
export DB_USERNAME="glowup"
export DB_PASSWORD="..."
export MAIL_USERNAME="..."   # Gmail app password
export MAIL_PASSWORD="..."

# 2. Build + run
cd tmdt
mvn clean package -DskipTests
mvn spring-boot:run
# Server listening on :8080
# WebSocket endpoint: ws://localhost:8080/ws
```

Schema được Hibernate auto-create (`spring.jpa.hibernate.ddl-auto: update`) khi khởi động.
Seed data demo nằm ở `src/main/resources/data.sql`.

---

## 21. Demo seed data

File `src/main/resources/data.sql` seed sẵn:
- 1 admin: `admin@glowup.vn` / `Admin@1234`
- 2 customers: `lan@email.com`, `hoang@email.com` / `Customer@1234`
- 2 technicians: `tuan@glowup.pro`, `minh@glowup.pro` / `Tech@1234`
- 5 service categories
- 1 sample completed order với review để test detail view

Yêu cầu `spring.jpa.defer-datasource-initialization: true` trong `application.yml`.

---

## 22. Tham khảo thêm

- **DB schema (DBML)**: `glowup_schema.dbml` (root repo)
- **Postman collection**: `docs/postman_collection.json`
- **Security review**: `docs/SECURITY_REVIEW.md`
- **Email confirmation flow**: `docs/EMAIL_CONFIRMATION.md`
- **VNPay integration**: `docs/PAYMENT_INTEGRATION.md`
