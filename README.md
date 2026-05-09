# BE E-Commerce — Spring Boot REST API
Backend REST API được xây dựng bằng **Java 21 + Spring Boot 3**, sử dụng **PostgreSQL** và **Spring Security + JWT**.
> Tài liệu API chính thức đã được tách ra ở `docs/API_REFERENCE.md`.
---
## Tài liệu chính
- `docs/API_REFERENCE.md`: tài liệu đầy đủ cho `auth`, `user`, `wallet`, `payment`
- `docs/WALLET_API.md`: hướng dẫn nhanh module ví
- `docs/PAYMENT_INTEGRATION.md`: hướng dẫn nhanh tích hợp VNPay
---
## Tính năng chính
- Đăng ký / đăng nhập / refresh token / logout
- Quản lý user và cập nhật trạng thái
- Quản lý ví, nạp tiền, rút tiền, lịch sử giao dịch
- Quản lý tài khoản ngân hàng
- Webhook VNPay IPN cho topup
---
## Cấu hình cần thiết
### Biến môi trường
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `MAIL_USERNAME`
- `MAIL_PASSWORD`
- `VNPAY_TMN_CODE`
- `VNPAY_SECRET_KEY`
- `VNPAY_PAY_URL` (tuỳ chọn)
- `VNPAY_RETURN_URL` (tuỳ chọn)
- `VNPAY_IP_ADDRESS` (tuỳ chọn)
### `application.yml`
```yaml
jwt:
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
      pay-url: https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
      return-url: http://localhost:3000/wallet/topup-result
```
---
## Chạy dự án
```bash
mvn clean package -DskipTests
mvn spring-boot:run
```
Server mặc định chạy tại `http://localhost:8080`.
---
## Lưu ý quan trọng
- Public payment flow hiện tại **chỉ hỗ trợ VNPay**.
- Có một số DTO/enum cho MoMo trong code, nhưng **chưa có endpoint public hoạt động**.
- `POST /api/wallet/topup/confirm` chỉ phù hợp với giao dịch `vietqr` nội bộ/tương lai.
