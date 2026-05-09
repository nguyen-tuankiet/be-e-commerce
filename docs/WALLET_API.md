# Wallet API Documentation

## 📋 Overview

Wallet module cung cấp các API cho:
- Quản lý ví tiền người dùng
- Nạp tiền, rút tiền
- Quản lý tài khoản ngân hàng
- Xem lịch sử giao dịch

**Base URL**: `http://localhost:8080/api/wallet`

**Authentication**: Tất cả endpoint require `Authorization: Bearer <access_token>`

---

## 📊 Entity & Models

### Wallet Entity

```
{
  "id": 1,
  "userId": 1,
  "balance": 1500000,           // Số dư khả dụng
  "pendingBalance": 200000,     // Đang chờ rằng xác nhận (rút tiền)
  "totalEarned": 45000000,      // Tổng cộng được
  "totalWithdrawn": 43500000,   // Tổng rút ra
  "currency": "VND",
  "createdAt": "2025-04-01T10:00:00Z",
  "updatedAt": "2025-05-07T15:30:00Z"
}
```

### WalletTransaction Entity

```
{
  "id": 1,
  "transactionCode": "TX-TOPUP-20250507-001",
  "type": "TOPUP|WITHDRAW|COMMISSION|PAYMENT|REFUND",
  "category": "TÀI CHÍNH/NẠP TIỀN",
  "title": "Nạp tiền vào ví",
  "amount": 500000,
  "fee": 0,
  "netAmount": 500000,           // amount - fee
  "status": "AWAITING_PAYMENT|PENDING_VERIFICATION|PENDING|SUCCESS|FAILED|CANCELLED",
  "paymentMethod": "VIETQR|VNPAY|MOMO|BANK_TRANSFER",
  "bankAccountId": null,          // For withdraw only
  "transferContent": "VQR-GLOWUP-03 - 500000",
  "qrCode": "data:image/png;base64,...",   // For VietQR only
  "gatewayRequestId": "req-123",  // From payment gateway
  "gatewayTransactionId": "txn-456",
  "gatewayPayload": "{...}",
  "expiredAt": "2025-05-07T11:30:00Z",    // For AWAITING_PAYMENT
  "processedAt": "2025-05-07T10:05:00Z",
  "createdAt": "2025-05-07T10:00:00Z"
}
```

### BankAccount Entity

```
{
  "id": 1,
  "code": "BANK-001",
  "userId": 1,
  "bankName": "Vietcombank",
  "accountNumber": "6123 **** 789",      // Masked in response
  "accountOwner": "NGUYEN VAN A",
  "isDefault": true,
  "createdAt": "2025-04-01T10:00:00Z"
}
```

---

## 🔗 API Endpoints

### 1. Get Wallet Summary

**Endpoint**: `GET /api/wallet`

**Headers**:
```
Authorization: Bearer <access_token>
```

**Response**:
```json
{
  "success": true,
  "data": {
    "userId": "USR-001",
    "balance": 1500000,
    "pendingBalance": 200000,
    "totalEarned": 45000000,
    "totalWithdrawn": 43500000,
    "currency": "VND",
    "updatedAt": "2025-05-07T15:30:00.000Z"
  }
}
```

**Special Notes**:
- Auto-create wallet nếu chưa tồn tại
- Nếu user là technician/commission user, `totalEarned` > 0 từ commission
- `pendingBalance` là tiền đang chờ từ rút tiền chưa confirm

**Status Codes**:
- `200`: Success
- `401`: Unauthorized (invalid/expired token)
- `500`: Server error

---

### 2. Get Transactions History

**Endpoint**: `GET /api/wallet/transactions`

**Query Parameters**:
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `type` | string | `all` | `all`, `topup`, `withdraw`, `commission`, `payment`, `refund` |
| `page` | integer | `1` | Page number (1-based) |
| `limit` | integer | `10` | Items per page |

**Example**:
```bash
GET /api/wallet/transactions?type=topup&page=1&limit=20
```

**Response**:
```json
{
  "success": true,
  "data": {
    "items": [
      {
        "id": "TX-240524-01",
        "type": "commission",
        "title": "Phí hoa hồng đơn #GU-99210",
        "category": "DỊCH VỤ/HOA HỒNG",
        "amount": -50000,
        "fee": 0,
        "netAmount": -50000,
        "status": "success",
        "paymentMethod": null,
        "createdAt": "2025-05-07T14:20:00.000Z"
      },
      {
        "id": "TX-240524-02",
        "type": "topup",
        "title": "Nạp tiền vào ví",
        "category": "TÀI CHÍNH/NẠP TIỀN",
        "amount": 500000,
        "fee": 0,
        "netAmount": 500000,
        "status": "success",
        "paymentMethod": "vnpay",
        "createdAt": "2025-05-07T10:00:00.000Z"
      }
    ],
    "pagination": {
      "page": 1,
      "limit": 20,
      "total": 45,
      "totalPages": 3
    }
  }
}
```

**Notes**:
- Negative amount = money out (withdraw, commission, payment)
- Positive amount = money in (topup, refund, commission received)
- Sorted by `createdAt DESC` (newest first)
- Only shows transactions for current user

**Status Codes**:
- `200`: Success
- `400`: Invalid query parameters
- `401`: Unauthorized

---

### 3. Top Up Wallet

**Endpoint**: `POST /api/wallet/topup`

**Headers**:
```
Authorization: Bearer <access_token>
Content-Type: application/json
```

**Request Body**:
```json
{
  "amount": 500000,
  "method": "vnpay"
}
```

**Supported Methods**: `vnpay`

**Validation**:
- `amount` >= 10,000 (config: `app.wallet.topup-min-amount`)
- `method` must be valid

**Response (VNPay)**:
```json
{
  "success": true,
  "data": {
    "transactionId": "TX-TOPUP-20250507-001",
    "amount": 500000,
    "method": "vnpay",
    "checkoutUrl": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?...",
    "deepLink": null,
    "qrCodeUrl": null,
    "paymentInfo": null,
    "expiredAt": "2025-05-07T11:30:00.000Z",
    "status": "pending"
  }
}
```

**Response (VietQR)**:
```json
{
  "success": true,
  "data": {
    "transactionId": "TX-TOPUP-20250507-001",
    "amount": 500000,
    "method": "vietqr",
    "checkoutUrl": null,
    "deepLink": null,
    "qrCodeUrl": null,
    "paymentInfo": {
      "bankName": "Techcombank",
      "accountName": "GLOWUP SERVICE",
      "accountNumber": "123456789",
      "transferContent": "VQR-GLOWUP-03 - 500000",
      "qrCode": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="
    },
    "expiredAt": "2025-05-07T11:30:00.000Z",
    "status": "awaiting_payment"
  }
}
```

**Field Descriptions**:
- `checkoutUrl`: Redirect URL for web payment (VNPay, MoMo)
- `deepLink`: Deep link for mobile app payment (MoMo)
- `qrCodeUrl`: QR code URL (MoMo)
- `paymentInfo`: Manual transfer info (VietQR only)
- `expiredAt`: Transaction expiry time
- `status`: Transaction status

**Status Codes**:
- `200`: Success
- `400`: Validation error (amount too small, invalid method)
- `401`: Unauthorized
- `500`: Payment gateway error

---

### 4. Confirm Top Up (Manual - VietQR only)

**Endpoint**: `POST /api/wallet/topup/confirm`

**Headers**:
```
Authorization: Bearer <access_token>
Content-Type: application/json
```

**Request Body**:
```json
{
  "transactionId": "TX-TOPUP-20250507-001"
}
```

**Response**:
```json
{
  "success": true,
  "data": {
    "transactionId": "TX-TOPUP-20250507-001",
    "status": "pending_verification",
    "message": "Yêu cầu đang được xác minh"
  }
}
```

**Notes**:
- Only for VietQR method
- Changes status from `AWAITING_PAYMENT` → `PENDING_VERIFICATION`
- Wallet chưa được cộng tiền (phải có confirmation từ admin hoặc webhook)
- Có `@Transactional` decorator

**Status Codes**:
- `200`: Success
- `400`: Transaction not found, expired, or already verified
- `401`: Unauthorized

---

### 5. Withdraw to Bank Account

**Endpoint**: `POST /api/wallet/withdraw`

**Headers**:
```
Authorization: Bearer <access_token>
Content-Type: application/json
```

**Request Body**:
```json
{
  "amount": 1000000,
  "bankAccountId": "BANK-001"
}
```

**Validation**:
- `amount` >= 50,000 (config: `app.wallet.withdraw-min-amount`)
- `amount` > withdraw fee (5,000)
- Wallet balance >= amount
- Bank account exists and owned by user

**Response**:
```json
{
  "success": true,
  "data": {
    "transactionId": "TX-240523-01",
    "amount": 1000000,
    "fee": 5000,
    "netAmount": 995000,
    "bankAccount": {
      "bankName": "Vietcombank",
      "accountNumber": "6123 **** 789",
      "owner": "NGUYEN VAN A"
    },
    "status": "pending"
  }
}
```

**Field Descriptions**:
- `amount`: Gross amount user requests to withdraw
- `fee`: Withdraw fee (fixed 5,000)
- `netAmount`: Net amount to transfer (amount - fee)
- `accountNumber`: Masked for security
- `status`: Transaction status (starts as `PENDING`)

**Business Logic**:
1. Withdraw balance trừ ngay khỏi user wallet
2. Cộng vào `pendingBalance` (đang chờ confirm)
3. Tạo WalletTransaction với status `PENDING`
4. Ghi lại bank account + transfer content

**Pessimistic Locking**:
- Wallet được lock (`PESSIMISTIC_WRITE`) khi rút để tránh race condition
- Ensure không double-withdraw

**Status Codes**:
- `200`: Success
- `400`: Validation errors (amount too small, insufficient balance, bank account not found)
- `401`: Unauthorized
- `409`: Bank account owner mismatch

---

### 6. List Bank Accounts

**Endpoint**: `GET /api/wallet/bank-accounts`

**Headers**:
```
Authorization: Bearer <access_token>
```

**Response**:
```json
{
  "success": true,
  "data": {
    "items": [
      {
        "id": "BANK-001",
        "bankName": "Vietcombank",
        "accountNumber": "6123 **** 789",
        "accountOwner": "NGUYEN VAN A",
        "isDefault": true,
        "createdAt": "2025-04-01T10:00:00.000Z"
      },
      {
        "id": "BANK-002",
        "bankName": "MB Bank",
        "accountNumber": "190332 **** 123",
        "accountOwner": "NGUYEN VAN A",
        "isDefault": false,
        "createdAt": "2025-04-15T14:30:00.000Z"
      }
    ]
  }
}
```

**Notes**:
- Sorted by `createdAt ASC` (oldest first)
- Account number masked (first 6 digits + *, last 3 digits)
- Only returns accounts for current user

**Status Codes**:
- `200`: Success
- `401`: Unauthorized

---

### 7. Create Bank Account

**Endpoint**: `POST /api/wallet/bank-accounts`

**Headers**:
```
Authorization: Bearer <access_token>
Content-Type: application/json
```

**Request Body**:
```json
{
  "bankName": "MB Bank",
  "accountNumber": "190332884123",
  "accountOwner": "NGUYEN VAN A"
}
```

**Validation**:
- `bankName`: non-empty, max 120 chars
- `accountNumber`: regex match, no duplicates for user
- `accountOwner`: non-empty, max 120 chars

**Response**:
```json
{
  "success": true,
  "data": {
    "id": "BANK-002",
    "bankName": "MB Bank",
    "accountNumber": "190332 **** 123",
    "accountOwner": "NGUYEN VAN A",
    "isDefault": false,
    "createdAt": "2025-05-07T10:00:00.000Z"
  }
}
```

**Special Notes**:
- If first bank account for user → `isDefault = true`
- If duplicate account number for user → error 409
- Account number masked in response
- Generated unique code (BANK-XXX)

**Status Codes**:
- `200`: Success
- `400`: Validation errors
- `401`: Unauthorized
- `409`: Bank account already exists

---

### 8. Delete Bank Account

**Endpoint**: `DELETE /api/wallet/bank-accounts/{id}`

**Headers**:
```
Authorization: Bearer <access_token>
```

**Path Parameters**:
| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | string | Bank account code (e.g., BANK-001) |

**Response**:
```json
{
  "success": true,
  "data": {
    "message": "Xóa tài khoản ngân hàng thành công"
  }
}
```

**Validation**:
- Bank account must exist and owned by user
- If `isDefault = true` AND count > 1 → error (cannot delete default if other accounts exist)
- If count = 1 → allowed to delete (last account)

**Status Codes**:
- `200`: Success
- `400`: Bank account is default and other accounts exist
- `401`: Unauthorized
- `404`: Bank account not found
- `409`: Owner mismatch

---

## 🧮 Transaction Type & Status

### Transaction Types

| Type | Category | Description |
|------|----------|-------------|
| TOPUP | TÀI CHÍNH/NẠP TIỀN | User nạp tiền vào ví |
| WITHDRAW | TÀI CHÍNH/RÚT TIỀN | User rút tiền từ ví |
| COMMISSION | DỊCH VỤ/HOA HỒNG | Commission từ sales/referral |
| PAYMENT | DỊCH VỤ/THANH TOÁN | Payment for services |
| REFUND | DỊCH VỤ/HOÀN TRỊ | Refund to customer |

### Transaction Status

| Status | Meaning |
|--------|---------|
| PENDING | Chờ xử lý (withdraw) |
| SUCCESS | Thành công, money đã được credited |
| FAILED | Thất bại (payment gateway reject) |
| CANCELLED | Bị hủy |
| AWAITING_PAYMENT | Chờ thanh toán (VietQR topup) |
| PENDING_VERIFICATION | Chờ xác minh (VietQR topup sau confirm) |

---

## 💵 Money Handling

### Important Notes

1. **BigDecimal for All Currency**:
   ```java
   BigDecimal amount = request.getAmount(); // NOT double
   ```

2. **VND is Whole Number**:
   - No fractional cents
   - 1 VND = 1 đồng
   - Stored without decimal places in DB: `NUMERIC(19,0)`

3. **Calculation Safe**:
   ```java
   // Normalize to remove trailing zeros
   amount = moneyUtils.normalize(amount);
   
   // Calculate net amount
   netAmount = amount.subtract(fee);
   ```

---

## 🔄 Workflow Examples

### Workflow 1: Nạp tiền qua VNPay

```
1. User: POST /api/wallet/topup { "amount": 500000, "method": "vnpay" }
2. Backend:
   - Validate amount >= 10,000
   - Create WalletTransaction (status: PENDING)
   - Call PaymentGatewayService.createCheckout()
   - Get checkoutUrl from VNPay
   - Return checkoutUrl to user
3. Frontend: Redirect to checkoutUrl
4. User: Pay on VNPay
5. VNPay: Callback GET /api/payments/vnpay/ipn?vnp_TxnRef=...&vnp_SecureHash=...
6. Backend:
   - Verify signature
   - Verify amount matches
   - Check transaction exists
   - Credit wallet (balance += 500000)
   - Set status: SUCCESS
   - Return { RspCode: "00", Message: "Confirm Success" }
7. User: Sees balance increased
```

### Workflow 2: Rút tiền

```
1. User: POST /api/wallet/withdraw { "amount": 1000000, "bankAccountId": "BANK-001" }
2. Backend:
   - Validate amount >= 50,000
   - Lock wallet (PESSIMISTIC_WRITE)
   - Check balance >= 1000000
   - Fee = 5000
   - NetAmount = 995000
   - Subtract balance: 1000000
   - Add pendingBalance: 995000
   - Create WalletTransaction (status: PENDING)
   - Return transaction data
3. Frontend: Show "Yêu cầu rút tiền đang chờ xử lý"
4. Admin: Confirm withdrawal (manual or via API)
5. Transaction: Status → SUCCESS
6. Backend: Transfer money (actual transfer system)
7. User: Money received
```

---

## ❌ Error Responses

### 400 — Validation Error

```json
{
  "success": false,
  "errorCode": "VALIDATION_ERROR",
  "message": "Dữ liệu không hợp lệ",
  "fields": {
    "amount": "Số tiền nạp tối thiểu là 10,000"
  }
}
```

### 401 — Unauthorized

```json
{
  "success": false,
  "errorCode": "UNAUTHORIZED",
  "message": "Người dùng chưa đăng nhập"
}
```

### 404 — Not Found

```json
{
  "success": false,
  "errorCode": "NOT_FOUND",
  "message": "Không tìm thấy giao dịch nạp tiền"
}
```

### 409 — Conflict

```json
{
  "success": false,
  "errorCode": "BANK_ACCOUNT_ALREADY_EXISTS",
  "message": "Tài khoản ngân hàng đã tồn tại"
}
```

---

## 📝 Useful Utilities

### TransactionCodeGenerator

Generates unique transaction codes:

```
TX-TOPUP-20250507-001
TX-WITHDRAW-20250507-001
TX-COMMISSION-20250507-001
```

### BankAccountMaskUtils

Masks bank account number:

```
6123456789 → 6123 **** 789
```

### MoneyUtils

Normalizes BigDecimal (remove trailing zeros):

```
BigDecimal 100000.00 → 100000
```

---

## 🧪 Testing with cURL

### 1. Get Wallet

```bash
curl -X GET http://localhost:8080/api/wallet \
  -H "Authorization: Bearer <access_token>"
```

### 2. Top Up

```bash
curl -X POST http://localhost:8080/api/wallet/topup \
  -H "Authorization: Bearer <access_token>" \
  -H "Content-Type: application/json" \
  -d '{"amount": 500000, "method": "vietqr"}'
```

### 3. Confirm Top Up

```bash
curl -X POST http://localhost:8080/api/wallet/topup/confirm \
  -H "Authorization: Bearer <access_token>" \
  -H "Content-Type: application/json" \
  -d '{"transactionId": "TX-TOPUP-20250507-001"}'
```

### 4. Withdraw

```bash
curl -X POST http://localhost:8080/api/wallet/withdraw \
  -H "Authorization: Bearer <access_token>" \
  -H "Content-Type: application/json" \
  -d '{"amount": 1000000, "bankAccountId": "BANK-001"}'
```

### 5. Create Bank Account

```bash
curl -X POST http://localhost:8080/api/wallet/bank-accounts \
  -H "Authorization: Bearer <access_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "bankName": "MB Bank",
    "accountNumber": "190332884123",
    "accountOwner": "NGUYEN VAN A"
  }'
```

### 6. Delete Bank Account

```bash
curl -X DELETE http://localhost:8080/api/wallet/bank-accounts/BANK-001 \
  -H "Authorization: Bearer <access_token>"
```


