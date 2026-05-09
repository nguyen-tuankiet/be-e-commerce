# Payment Gateway Integration Guide

## 📋 Overview

Backend đã tích hợp ** cổng thanh toán chính**:
- **VNPay**: Thanh toán qua ngân hàng/thẻ

Ngoài ra, có **VietQR** (mock) cho phát triển/test.

---

## 🔄 Payment Flow

### 1️⃣ VietQR (Mock - For Development)

```
User → POST /api/wallet/topup (method: vietqr)
  ↓
Backend sinh QR code (mock)
  ↓
Response: QR code + transfer content
  ↓
User chuyển khoản (ngoài BE)
  ↓
User → POST /api/wallet/topup/confirm
  ↓
Backend → Status: PENDING_VERIFICATION
  ↓
Admin xác minh → Credit wallet (phải làm thủ công hoặc via admin API — chưa implement)
```

**Ưu điểm**: Đơn giản, không cần API key từ bên thứ ba

---

### 2️⃣ VNPay (Production)

```
User → POST /api/wallet/topup (method: vnpay)
  ↓
Backend create WalletTransaction
  ↓
Backend gửi request sang VNPay → nhận checkoutUrl
  ↓
Response: checkoutUrl → Frontend redirect
  ↓
User thanh toán trên VNPay → VNPay redirect về return-url
  ↓
(Đồng thời) VNPay callback → GET /api/payments/vnpay/ipn?vnp_TxnRef=...&vnp_SecureHash=...
  ↓
Backend verify signature + amount
  ↓
If success: Credit wallet, Status: SUCCESS
  ↓
User thấy balance tăng ngay lập tức
```

**VNPay IPN Security**:
- VNPay ký request dùng `vnp_SecureHash` (HMAC-SHA512)
- Backend phải verify signature trước credit wallet
- Nếu signature không valid → trả về `RspCode: 97`
- Nếu đã process (idempotent) → trả về `RspCode: 02`

**Endpoints**:
- Sandbox: `https://sandbox.vnpayment.vn/paymentv2/vpcpay.html`
- Production: `https://paymentv2.vnpayment.vn/vpcpay.html`

---


## 🛠️ Setup Payment Gateway Credentials

### VNPay Setup

1. Đăng ký tài khoản: https://sandbox.vnpayment.vn/
2. Nhận credentials:
   - Terminal Code (TMN Code)
   - Secret Key
3. Thêm vào `.env`:
   ```
   VNPAY_TMN_CODE=<your-tmn-code>
   VNPAY_SECRET_KEY=<your-secret-key>
   VNPAY_PAY_URL=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
   VNPAY_RETURN_URL=http://your-domain/wallet/topup-result
   VNPAY_IP_ADDRESS=<your-server-ip>
   ```

---

## 🔌 API Endpoints

### 1. Nạp tiền

**Request**:
```bash
curl -X POST http://localhost:8080/api/wallet/topup \
  -H "Authorization: Bearer <access-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 500000,
    "method": "vnpay"
  }'
```

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
    "transactionId": "TX-TOPUP-20250507-003",
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
      "qrCode": "data:image/png;base64,..."
    },
    "expiredAt": "2025-05-07T11:30:00.000Z",
    "status": "awaiting_payment"
  }
}
```

---

### 2. VNPay IPN Callback

**From VNPay** (GET request):
```
/api/payments/vnpay/ipn?vnp_TxnRef=TX-TOPUP-001&vnp_Amount=50000000&vnp_ResponseCode=00&vnp_TransactionStatus=00&vnp_TransactionNo=12345&vnp_SecureHash=<signature>
```

**Sample simulation** (Postman):
```bash
curl -X GET "http://localhost:8080/api/payments/vnpay/ipn?vnp_TxnRef=TX-TOPUP-001&vnp_Amount=50000000&vnp_ResponseCode=00&vnp_TransactionStatus=00&vnp_TransactionNo=12345&vnp_SecureHash=<your-hash>"
```

**Backend Response**:
```json
{
  "RspCode": "00",
  "Message": "Confirm Success"
}
```

---


## 🧪 Testing

### Test VietQR (Mock)

1. **Topup**:
   ```bash
   curl -X POST http://localhost:8080/api/wallet/topup \
     -H "Authorization: Bearer <token>" \
     -H "Content-Type: application/json" \
     -d '{"amount": 100000, "method": "vietqr"}'
   ```

2. **Confirm** (nếu cần):
   ```bash
   curl -X POST http://localhost:8080/api/wallet/topup/confirm \
     -H "Authorization: Bearer <token>" \
     -H "Content-Type: application/json" \
     -d '{"transactionId": "TX-TOPUP-..."}'
   ```

3. **Check wallet**:
   ```bash
   curl -X GET http://localhost:8080/api/wallet \
     -H "Authorization: Bearer <token>"
   ```

### Test VNPay (Sandbox)

1. Lấy API credentials từ VNPay sandbox
2. Cấu hình `.env`
3. Topup via API → redirect đến VNPay sandbox
4. Thanh toán thử (VNPay cung cấp test card)
5. Verify IPN callback được gọi → wallet credit


## 🔐 Security Checklist

- ✅ VNPay signature verification (HMAC-SHA512)
- ✅ MoMo signature verification (HMAC-SHA256)
- ✅ Idempotent IPN processing (check `processedAt`)
- ✅ Amount validation
- ✅ Transaction code validation
- ✅ Wallet pessimistic locking trên withdraw
- ✅ BigDecimal cho tất cả tiền tệ

---

## 📱 Integration with Frontend

### Frontend flow (VNPay):
```javascript
// 1. Call backend topup endpoint
const res = await fetch('http://api/wallet/topup', {
  method: 'POST',
  body: JSON.stringify({ amount: 500000, method: 'vnpay' })
});

const { data } = await res.json();

// 2. Redirect to VNPay
window.location.href = data.checkoutUrl;

// 3. After payment, VNPay redirects back to return-url
// Frontend can check wallet balance or transaction status
```

### Frontend flow (MoMo):
```javascript
// 1. Call backend topup endpoint
const res = await fetch('http://api/wallet/topup', {
  method: 'POST',
  body: JSON.stringify({ amount: 500000, method: 'momo' })
});

const { data } = await res.json();

// 2. Options:
//   - Web: redirect to data.checkoutUrl
//   - App: deep link (data.deepLink)
//   - QR: show QR image (data.qrCodeUrl)

// 3. After payment, open return-url
```

---

## 🛠️ Troubleshooting

### Signature mismatch

**Nguyên nhân**: Secret key không đúng hoặc order của raw string sai

**Giải pháp**:
- Double-check secret key từ dashboard payment provider
- VNPay: TreeMap (sorted order)
- MoMo: Specific field order (alfabetical)

### Transaction not found

**Nguyên nhân**: `orderId` / `vnp_TxnRef` không match với transaction code

**Giải pháp**:
- Đảm bảo transaction code được sinh đúng format
- Check database xem transaction có được tạo không

### Wallet không credit sau thanh toán

**Nguyên nhân**: IPN callback không được gọi hoặc xử lý thất bại

**Giải pháp**:
- Kiểm tra logs backend
- Verify webhook URL từ dashboard payment provider
- Test IPN manually từ Postman

---

## 📝 Notes

- Tất cả tiền tệ dùng **BigDecimal** (không `double`)
- Default currency: **VND**
- Topup expiry: **30 phút** (config ở `application.yml`)
- Withdraw fee: **5,000 VND** (fixed)
- VietQR mặc định sandbox, không thể payment thực tế

