# API Reference

Tài liệu tham chiếu chính thức cho các API `auth`, `user`, `wallet` và `payment` của backend.

> Nguồn thông tin được viết lại theo code hiện tại trong `controller/`, `dto/`, `service/` và `application.yml`.

---

## 1. Tổng quan

- **Base URL**: `http://localhost:8080`
- **Context path**: `/`
- **API prefix**: `/api`
- **Format dữ liệu**: JSON
- **Xác thực**: JWT Bearer token

### Phân quyền hiện tại

| Nhóm API | Trạng thái |
|---|---|
| `auth` | Public, riêng `/me` và `/logout` cần Bearer token |
| `user` | Cần đăng nhập; `PATCH /api/users/{id}/status` chỉ `ADMIN` |
| `wallet` | Cần đăng nhập |
| `payment` | Public webhook cho VNPay |

---

## 2. Chuẩn response

### 2.1 `ApiResponse<T>`

```json
{
  "success": true,
  "data": {}
}
```

```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Dữ liệu không hợp lệ",
    "fields": {
      "amount": "Số tiền nạp tối thiểu là 10,000"
    }
  }
}
```

### 2.2 `PagedResponse<T>`

```json
{
  "success": true,
  "data": {
    "items": [],
    "pagination": {
      "page": 1,
      "limit": 10,
      "total": 0,
      "totalPages": 0
    }
  }
}
```

---

## 3. Quy ước chung

### 3.1 Header

- `Content-Type: application/json`
- `Authorization: Bearer <access_token>` cho các API bảo vệ

### 3.2 Phân trang

- `page` là **1-based**
- `limit` mặc định là `10`
- `page`/`limit` được backend làm tròn về giá trị an toàn nếu cần

### 3.3 Currency

- Hệ thống dùng **VND**
- Tiền tệ lưu bằng `BigDecimal`
- Không dùng số thập phân cho số tiền

---

## 4. Auth API

**Base path**: `/api/auth`

### 4.1 Đăng ký

`POST /api/auth/register`

**Request**

```json
{
  "fullName": "Nguyễn Văn A",
  "email": "vana@email.com",
  "phone": "0901234567",
  "password": "Abc@1234",
  "role": "customer"
}
```

**Ràng buộc**

- `fullName`: bắt buộc
- `email`: bắt buộc, đúng định dạng
- `phone`: bắt buộc, khớp regex `^(0|\+84)[3-9][0-9]{8}$`
- `password`: tối thiểu 8 ký tự, có chữ hoa, chữ thường, số và ký tự đặc biệt
- `role`: chỉ chấp nhận `CUSTOMER` hoặc `TECHNICIAN` (không cho đăng ký `ADMIN`)

**Response 201**

```json
{
  "success": true,
  "data": {
    "accessToken": "eyJ...",
    "refreshToken": "eyJ...",
    "user": {
      "id": 1,
      "code": "USR-0001",
      "fullName": "Nguyễn Văn A",
      "email": "vana@email.com",
      "phone": "0901234567",
      "role": "CUSTOMER",
      "status": "PENDING",
      "avatar": null,
      "district": null,
      "address": null,
      "bio": null,
      "createdAt": "2026-05-09T10:00:00",
      "updatedAt": "2026-05-09T10:00:00"
    }
  }
}
```

**Lưu ý**

- Tài khoản mới được tạo với trạng thái `PENDING`
- Backend tự tạo ví cho user sau khi đăng ký thành công

---

### 4.2 Đăng nhập

`POST /api/auth/login`

**Request**

```json
{
  "identifier": "vana@email.com",
  "password": "Abc@1234",
  "role": "customer"
}
```

**Ghi chú**

- `identifier` có thể là email hoặc số điện thoại
- `role` là tùy chọn; nếu có thì phải khớp với role thực tế của user

**Response 200**

```json
{
  "success": true,
  "data": {
    "accessToken": "eyJ...",
    "refreshToken": "eyJ...",
    "user": {
      "id": 1,
      "code": "USR-0001",
      "fullName": "Nguyễn Văn A",
      "email": "vana@email.com",
      "phone": "0901234567",
      "role": "CUSTOMER",
      "status": "ACTIVE"
    }
  }
}
```

**Lưu ý**

- Nếu user bị khóa, backend trả lỗi `ACCOUNT_LOCKED`
- Nếu user bị vô hiệu hóa, backend trả lỗi `ACCOUNT_DISABLED`

---

### 4.3 Refresh access token

`POST /api/auth/refresh-token`

**Request**

```json
{
  "refreshToken": "eyJ..."
}
```

**Response 200**

```json
{
  "success": true,
  "data": {
    "accessToken": "eyJ..."
  }
}
```

**Lưu ý**

- Refresh token phải hợp lệ về chữ ký và chưa hết hạn
- Token trong DB cũng phải chưa bị revoke

---

### 4.4 Đăng xuất

`POST /api/auth/logout`

**Headers**

```http
Authorization: Bearer <access_token>
```

**Response 200**

```json
{
  "success": true,
  "data": null
}
```

**Lưu ý**

- API sẽ revoke toàn bộ refresh token của user hiện tại
- Nếu access token đã không hợp lệ, backend coi như đã logout xong

---

### 4.5 Quên mật khẩu

`POST /api/auth/forgot-password`

**Request**

```json
{
  "identifier": "vana@email.com"
}
```

**Response 200**

```json
{
  "success": true,
  "data": null
}
```

**Lưu ý**

- Backend tạo password reset token mới và vô hiệu hóa token cũ
- Hiện tại token được log ra, phần gửi email/SMS vẫn chưa triển khai

---

### 4.6 Đổi mật khẩu

`POST /api/auth/change-password`

**Request**

```json
{
  "newPassword": "NewPass@123",
  "confirmPassword": "NewPass@123",
  "token": "reset-token-from-email-or-log"
}
```

**Response 200**

```json
{
  "success": true,
  "data": null
}
```

**Lưu ý**

- `newPassword` và `confirmPassword` phải trùng nhau
- Token reset phải còn hiệu lực và chưa sử dụng
- Sau khi đổi mật khẩu, toàn bộ refresh token cũ sẽ bị revoke

---

### 4.7 Lấy thông tin user hiện tại

`GET /api/auth/me`

**Headers**

```http
Authorization: Bearer <access_token>
```

**Response 200**

```json
{
  "success": true,
  "data": {
    "id": 1,
    "code": "USR-0001",
    "fullName": "Nguyễn Văn A",
    "email": "vana@email.com",
    "phone": "0901234567",
    "role": "CUSTOMER",
    "status": "ACTIVE"
  }
}
```

---

## 5. User API

**Base path**: `/api/users`

> Hiện tại các endpoint dưới đây chỉ cần xác thực, chưa có ràng buộc role ngoài `PATCH /{id}/status`.

### 5.1 Danh sách user

`GET /api/users?role=&status=&district=&keyword=&page=1&limit=10`

**Query params**

| Param | Kiểu | Mặc định | Mô tả |
|---|---|---:|---|
| `role` | string | null | Lọc theo role |
| `status` | string | null | Lọc theo status |
| `district` | string | null | Lọc theo quận/huyện |
| `keyword` | string | null | Tìm theo từ khóa |
| `page` | int | 1 | Trang hiện tại |
| `limit` | int | 10 | Số bản ghi/trang |

**Response 200**

```json
{
  "success": true,
  "data": {
    "items": [
      {
        "id": 1,
        "code": "USR-0001",
        "fullName": "Nguyễn Văn A",
        "email": "vana@email.com",
        "phone": "0901234567",
        "role": "CUSTOMER",
        "status": "ACTIVE",
        "district": "Quận 1",
        "address": "123 ABC",
        "bio": null,
        "createdAt": "2026-05-09T10:00:00",
        "updatedAt": "2026-05-09T10:00:00"
      }
    ],
    "pagination": {
      "page": 1,
      "limit": 10,
      "total": 1,
      "totalPages": 1
    }
  }
}
```

**Ghi chú**

- Sắp xếp theo `createdAt DESC`
- Trang API là 1-based

---

### 5.2 Chi tiết user theo id

`GET /api/users/{id}`

**Path params**

| Param | Kiểu | Mô tả |
|---|---|---|
| `id` | long | ID nội bộ của user |

**Response 200**

```json
{
  "success": true,
  "data": {
    "id": 1,
    "code": "USR-0001",
    "fullName": "Nguyễn Văn A",
    "email": "vana@email.com",
    "phone": "0901234567",
    "role": "CUSTOMER",
    "status": "ACTIVE"
  }
}
```

---

### 5.3 Cập nhật profile user

`PATCH /api/users/{id}`

**Request**

```json
{
  "fullName": "Nguyễn Văn A",
  "email": "vana.new@email.com",
  "phone": "0901234567",
  "address": "123 ABC",
  "district": "Quận 1",
  "bio": "Khách hàng thân thiết",
  "avatar": "https://example.com/avatar.png"
}
```

**Lưu ý**

- Tất cả field đều là optional
- Nếu đổi `email` hoặc `phone`, backend sẽ kiểm tra trùng lặp
- Giá trị rỗng sẽ bị bỏ qua

**Response 200**

```json
{
  "success": true,
  "data": {
    "id": 1,
    "code": "USR-0001",
    "fullName": "Nguyễn Văn A",
    "email": "vana.new@email.com",
    "phone": "0901234567",
    "role": "CUSTOMER",
    "status": "ACTIVE"
  }
}
```

---

### 5.4 Cập nhật trạng thái user

`PATCH /api/users/{id}/status`

**Quyền**: `ADMIN`

**Request**

```json
{
  "status": "LOCKED",
  "reason": "Vi phạm chính sách"
}
```

**Lưu ý**

- Trạng thái hợp lệ: `PENDING`, `ACTIVE`, `LOCKED`, `INACTIVE`
- `reason` chỉ phục vụ log nghiệp vụ, không bắt buộc

**Response 200**

```json
{
  "success": true,
  "data": {
    "id": 1,
    "code": "USR-0001",
    "status": "LOCKED"
  }
}
```

---

## 6. Wallet API

**Base path**: `/api/wallet`

**Yêu cầu xác thực**: Bearer token

### 6.1 Lấy ví hiện tại

`GET /api/wallet`

**Response 200**

```json
{
  "success": true,
  "data": {
    "userId": "1",
    "balance": 1500000,
    "pendingBalance": 0,
    "totalEarned": 1500000,
    "totalWithdrawn": 0,
    "currency": "VND",
    "updatedAt": "2026-05-09T10:30:00"
  }
}
```

**Lưu ý**

- Ví sẽ được tự tạo nếu chưa tồn tại

---

### 6.2 Lịch sử giao dịch

`GET /api/wallet/transactions?type=all&page=1&limit=10`

**Query params**

| Param | Kiểu | Mặc định | Mô tả |
|---|---|---:|---|
| `type` | string | `all` | `all`, `topup`, `withdraw`, `commission`, `payment`, `refund` |
| `page` | int | 1 | Trang |
| `limit` | int | 10 | Số bản ghi/trang |

**Response 200**

```json
{
  "success": true,
  "data": {
    "items": [
      {
        "id": "TX-TOPUP-20260509-001",
        "type": "topup",
        "title": "Nạp tiền vào ví",
        "category": "TÀI CHÍNH/NẠP TIỀN",
        "amount": 500000,
        "status": "success",
        "createdAt": "2026-05-09T10:25:00"
      }
    ],
    "pagination": {
      "page": 1,
      "limit": 10,
      "total": 1,
      "totalPages": 1
    }
  }
}
```

**Lưu ý**

- `type` không hợp lệ sẽ trả lỗi `INVALID_TRANSACTION_TYPE`
- Danh sách chỉ bao gồm giao dịch của user hiện tại

---

### 6.3 Tạo giao dịch nạp tiền

`POST /api/wallet/topup`

**Request**

```json
{
  "amount": 500000,
  "method": "vnpay"
}
```

**Validation**

- `amount` >= `10000`
- `amount` là số nguyên
- `method` hiện tại chỉ chấp nhận `vnpay`

**Response 201**

```json
{
  "success": true,
  "data": {
    "transactionId": "TX-TOPUP-20260509-001",
    "amount": 500000,
    "method": "vnpay",
    "checkoutUrl": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?...",
    "deepLink": null,
    "qrCodeUrl": null,
    "paymentInfo": null,
    "expiredAt": "2026-05-09T10:55:00",
    "status": "awaiting_payment"
  }
}
```

**Lưu ý quan trọng**

- Code hiện tại chỉ triển khai public flow qua **VNPay**
- `WalletTopUpResponse` có các field `deepLink`/`qrCodeUrl`, nhưng backend hiện không trả về dữ liệu MoMo
- Giao dịch top-up được tạo với trạng thái `awaiting_payment`

---

### 6.4 Xác nhận nạp tiền thủ công

`POST /api/wallet/topup/confirm`

**Request**

```json
{
  "transactionId": "TX-TOPUP-20260509-001"
}
```

**Response 200**

```json
{
  "success": true,
  "data": {
    "transactionId": "TX-TOPUP-20260509-001",
    "status": "pending_verification",
    "message": "Yêu cầu đang được xác minh"
  }
}
```

**Lưu ý**

- Endpoint này chỉ hợp lệ với giao dịch có `paymentMethod = vietqr`
- Với code hiện tại, API public top-up không tạo VietQR transaction, nên endpoint này chủ yếu dành cho luồng nội bộ/tương lai
- Nếu transaction không phải VietQR, backend trả lỗi

---

### 6.5 Rút tiền

`POST /api/wallet/withdraw`

**Request**

```json
{
  "amount": 1000000,
  "bankAccountId": "BANK-001"
}
```

**Validation**

- `amount` >= `50000`
- `amount` phải lớn hơn phí rút
- Số dư ví phải đủ
- `bankAccountId` phải thuộc sở hữu của user hiện tại

**Response 201**

```json
{
  "success": true,
  "data": {
    "transactionId": "TX-WITHDRAW-20260509-001",
    "amount": 1000000,
    "fee": 5000,
    "netAmount": 995000,
    "bankAccount": {
      "bankName": "MB Bank",
      "accountNumber": "190332 **** 123",
      "owner": "NGUYEN VAN A"
    },
    "status": "pending"
  }
}
```

**Lưu ý**

- Backend khóa ví bằng `PESSIMISTIC_WRITE` khi rút tiền
- `balance` bị trừ ngay, `pendingBalance` tăng theo `netAmount`
- Trạng thái ban đầu của giao dịch là `pending`

---

### 6.6 Danh sách tài khoản ngân hàng

`GET /api/wallet/bank-accounts`

**Response 200**

```json
{
  "success": true,
  "data": {
    "items": [
      {
        "id": "BANK-001",
        "bankName": "MB Bank",
        "accountNumber": "190332 **** 123",
        "accountOwner": "NGUYEN VAN A",
        "isDefault": true,
        "createdAt": "2026-05-09T09:00:00"
      }
    ]
  }
}
```

**Lưu ý**

- Số tài khoản được mask trong response
- Danh sách chỉ gồm tài khoản của user hiện tại

---

### 6.7 Tạo tài khoản ngân hàng

`POST /api/wallet/bank-accounts`

**Request**

```json
{
  "bankName": "MB Bank",
  "accountNumber": "190332884123",
  "accountOwner": "NGUYEN VAN A"
}
```

**Validation**

- `bankName`: bắt buộc
- `accountNumber`: 6–20 chữ số
- `accountOwner`: bắt buộc, phải viết hoa theo regex hiện tại

**Response 201**

```json
{
  "success": true,
  "data": {
    "id": "BANK-002",
    "bankName": "MB Bank",
    "accountNumber": "190332 **** 123",
    "accountOwner": "NGUYEN VAN A",
    "isDefault": false,
    "createdAt": "2026-05-09T09:10:00"
  }
}
```

**Lưu ý**

- Nếu là tài khoản đầu tiên thì `isDefault = true`
- Trùng số tài khoản của cùng user sẽ trả lỗi `BANK_ACCOUNT_ALREADY_EXISTS`

---

### 6.8 Xóa tài khoản ngân hàng

`DELETE /api/wallet/bank-accounts/{id}`

**Path params**

| Param | Kiểu | Mô tả |
|---|---|---|
| `id` | string | Mã tài khoản ngân hàng, ví dụ `BANK-001` |

**Response 200**

```json
{
  "success": true,
  "data": {
    "message": "Xóa tài khoản ngân hàng thành công"
  }
}
```

**Lưu ý**

- `id` ở đây là **code** của bank account, không phải numeric id nội bộ
- Không thể xóa tài khoản mặc định nếu user còn tài khoản khác

---

## 7. Payment API

**Base path**: `/api/payments`

### 7.1 VNPay IPN

`GET /api/payments/vnpay/ipn`

Đây là webhook public từ VNPay.

**Query params chính**

| Param | Mô tả |
|---|---|
| `vnp_TxnRef` | Mã giao dịch nội bộ |
| `vnp_Amount` | Số tiền VNPay gửi về, tính theo đơn vị nhỏ hơn 100 lần |
| `vnp_ResponseCode` | Mã phản hồi |
| `vnp_TransactionStatus` | Trạng thái giao dịch |
| `vnp_TransactionNo` | Mã giao dịch từ VNPay |
| `vnp_SecureHash` | Chữ ký HMAC-SHA512 |

**Response thành công**

```json
{
  "RspCode": "00",
  "Message": "Confirm Success"
}
```

**Các mã phản hồi phổ biến**

| RspCode | Ý nghĩa |
|---|---|
| `00` | Hợp lệ và đã xử lý thành công |
| `01` | Không tìm thấy transaction |
| `02` | Callback đã được xử lý trước đó |
| `04` | Số tiền không khớp |
| `97` | Sai chữ ký |

**Lưu ý**

- Backend tự verify chữ ký trước khi ghi nhận
- Callback xử lý idempotent để tránh cộng ví 2 lần
- Nếu thanh toán thành công, ví sẽ được cộng tiền và transaction chuyển sang `success`
- Nếu thất bại, transaction chuyển sang `failed`

---

## 8. Danh sách giá trị enum

### 8.1 `Role`

- `CUSTOMER`
- `TECHNICIAN`
- `ADMIN`

### 8.2 `UserStatus`

- `PENDING`
- `ACTIVE`
- `LOCKED`
- `INACTIVE`

### 8.3 `PaymentMethod`

- `vietqr`
- `vnpay`
- `momo`
- `bank_transfer`

> Chú ý: enum có đủ giá trị, nhưng public flow hiện tại chỉ dùng `vnpay`.

### 8.4 `TransactionType`

- `topup`
- `withdraw`
- `commission`
- `payment`
- `refund`

### 8.5 `TransactionStatus`

- `pending`
- `success`
- `failed`
- `cancelled`
- `awaiting_payment`
- `pending_verification`

---

## 9. Cấu hình môi trường

Từ `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

jwt:
  secret: <base64-secret>
  access-token-expiration: 900000
  refresh-token-expiration: 604800000
  password-reset-expiration: 3600000

app:
  wallet:
    default-currency: VND
    topup-min-amount: 10000
    withdraw-min-amount: 50000
    withdraw-fee: 5000
    topup-expiry-minutes: 30
  payment:
    vnpay:
      tmn-code: ${VNPAY_TMN_CODE:}
      secret-key: ${VNPAY_SECRET_KEY:}
      pay-url: ${VNPAY_PAY_URL:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}
      return-url: ${VNPAY_RETURN_URL:http://localhost:3000/wallet/topup-result}
      ip-address: ${VNPAY_IP_ADDRESS:127.0.0.1}
```

### Biến môi trường cần thiết

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `MAIL_USERNAME`
- `MAIL_PASSWORD`
- `VNPAY_TMN_CODE`
- `VNPAY_SECRET_KEY`
- `VNPAY_PAY_URL` nếu muốn dùng production endpoint
- `VNPAY_RETURN_URL`
- `VNPAY_IP_ADDRESS`

---

## 10. Lỗi thường gặp

### Validation error

```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Dữ liệu không hợp lệ",
    "fields": {
      "amount": "Số tiền nạp tối thiểu là 10,000"
    }
  }
}
```

### Not found

```json
{
  "success": false,
  "error": {
    "code": "NOT_FOUND",
    "message": "Không tìm thấy người dùng với id: 1"
  }
}
```

### Conflict

```json
{
  "success": false,
  "error": {
    "code": "BANK_ACCOUNT_ALREADY_EXISTS",
    "message": "Tài khoản ngân hàng đã tồn tại"
  }
}
```

---

## 11. Kết luận nhanh

- Auth, user, wallet đều đã có tài liệu hóa theo code hiện tại.
- Public payment flow hiện tại là **VNPay**.
- MoMo có DTO/enums trong code, nhưng chưa có endpoint public hoạt động.

---

## 12. Tham chiếu liên quan

- `README.md`
- `docs/WALLET_API.md`
- `docs/PAYMENT_INTEGRATION.md`


