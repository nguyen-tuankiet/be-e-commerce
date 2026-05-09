# Payment Integration Guide
## Tổng quan
Backend hiện tại đã triển khai **VNPay** cho flow nạp tiền ví.
- Public checkout flow: `POST /api/wallet/topup`
- Public IPN webhook: `GET /api/payments/vnpay/ipn`
> MoMo có một số DTO/enum trong code, nhưng **chưa có endpoint public hoạt động**.
---
## Luồng VNPay hiện tại
1. User gọi `POST /api/wallet/topup` với `method = vnpay`.
2. Backend tạo `WalletTransaction` với trạng thái `awaiting_payment`.
3. Backend sinh `checkoutUrl` từ VNPay.
4. Frontend redirect người dùng sang VNPay.
5. VNPay gọi IPN về `GET /api/payments/vnpay/ipn`.
6. Backend verify chữ ký, kiểm tra số tiền và idempotency.
7. Nếu thành công, ví được cộng tiền và giao dịch chuyển sang `success`.
---
## Cấu hình VNPay
Từ `application.yml`:
```yaml
app:
  payment:
    vnpay:
      tmn-code: ${VNPAY_TMN_CODE:}
      secret-key: ${VNPAY_SECRET_KEY:}
      pay-url: ${VNPAY_PAY_URL:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}
      return-url: ${VNPAY_RETURN_URL:http://localhost:3000/wallet/topup-result}
      ip-address: ${VNPAY_IP_ADDRESS:127.0.0.1}
```
---
## Webhook IPN
`GET /api/payments/vnpay/ipn`
### Các tham số chính
- `vnp_TxnRef`
- `vnp_Amount`
- `vnp_ResponseCode`
- `vnp_TransactionStatus`
- `vnp_TransactionNo`
- `vnp_SecureHash`
### Response thành công
```json
{
  "RspCode": "00",
  "Message": "Confirm Success"
}
```
### Mã trả về phổ biến
| RspCode | Ý nghĩa |
|---|---|
| `00` | Hợp lệ và đã xử lý thành công |
| `01` | Không tìm thấy giao dịch |
| `02` | Callback đã xử lý trước đó |
| `04` | Số tiền không khớp |
| `97` | Chữ ký không hợp lệ |
---
## Bảo mật
- VNPay dùng HMAC-SHA512 cho chữ ký.
- Backend verify chữ ký trước khi credit ví.
- Callback được xử lý idempotent để tránh cộng tiền 2 lần.
- Amount phải khớp đúng với transaction đã tạo.
---
## Lưu ý
- `POST /api/wallet/topup/confirm` chỉ phù hợp với giao dịch `vietqr` nội bộ/tương lai.
- Public flow hiện tại **không** mở MoMo.
---
## Tài liệu đầy đủ
Xem `docs/API_REFERENCE.md` để biết toàn bộ request/response, error code và rule nghiệp vụ của module payment và wallet.
