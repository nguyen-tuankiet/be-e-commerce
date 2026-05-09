# Wallet API Documentation
## Tổng quan
Module ví cung cấp các API để:
- Xem số dư ví hiện tại
- Xem lịch sử giao dịch
- Nạp tiền qua VNPay
- Xác nhận giao dịch VietQR nội bộ/tương lai
- Rút tiền về tài khoản ngân hàng
- Quản lý tài khoản ngân hàng của user
**Base URL**: `http://localhost:8080/api/wallet`
**Authentication**: tất cả endpoint đều cần `Authorization: Bearer <access_token>`.
---
## Endpoint hiện có
| Method | Endpoint | Mô tả |
|---|---|---|
| GET | `/api/wallet` | Lấy ví hiện tại |
| GET | `/api/wallet/transactions` | Lịch sử giao dịch, hỗ trợ phân trang |
| POST | `/api/wallet/topup` | Tạo giao dịch nạp tiền |
| POST | `/api/wallet/topup/confirm` | Xác nhận topup VietQR |
| POST | `/api/wallet/withdraw` | Tạo yêu cầu rút tiền |
| GET | `/api/wallet/bank-accounts` | Danh sách tài khoản ngân hàng |
| POST | `/api/wallet/bank-accounts` | Tạo tài khoản ngân hàng mới |
| DELETE | `/api/wallet/bank-accounts/{id}` | Xóa tài khoản ngân hàng |
---
## Quy tắc nghiệp vụ quan trọng
- Ví sẽ được tự tạo nếu chưa tồn tại.
- `topup` hiện tại chỉ chấp nhận `method = vnpay`.
- `topup/confirm` chỉ dùng cho giao dịch có `paymentMethod = vietqr`.
- `withdraw` yêu cầu số dư đủ và khóa ví bằng pessimistic lock để tránh race condition.
- `bankAccountId` ở API rút tiền và `id` ở API xóa tài khoản ngân hàng là **mã code** như `BANK-001`, không phải numeric id.
---
## Cấu hình liên quan
Từ `application.yml`:
- `app.wallet.default-currency: VND`
- `app.wallet.topup-min-amount: 10000`
- `app.wallet.withdraw-min-amount: 50000`
- `app.wallet.withdraw-fee: 5000`
- `app.wallet.topup-expiry-minutes: 30`
---
## Ví dụ nhanh
### Lấy ví hiện tại
```bash
curl -X GET http://localhost:8080/api/wallet \
  -H "Authorization: Bearer <access_token>"
```
### Nạp tiền qua VNPay
```bash
curl -X POST http://localhost:8080/api/wallet/topup \
  -H "Authorization: Bearer <access_token>" \
  -H "Content-Type: application/json" \
  -d '{"amount":500000,"method":"vnpay"}'
```
### Rút tiền
```bash
curl -X POST http://localhost:8080/api/wallet/withdraw \
  -H "Authorization: Bearer <access_token>" \
  -H "Content-Type: application/json" \
  -d '{"amount":1000000,"bankAccountId":"BANK-001"}'
```
---
## Tài liệu đầy đủ
Xem `docs/API_REFERENCE.md` để biết request/response chi tiết, error code và ví dụ đầy đủ cho toàn bộ API `auth`, `user`, `wallet`, `payment`.
