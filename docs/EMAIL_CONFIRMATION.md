# Email Confirmation Feature

## Tổng quan

Khi người dùng đăng ký tài khoản mới, hệ thống sẽ:
1. Tạo tài khoản với trạng thái `PENDING`
2. Tạo token xác nhận email
3. Gửi email chứa link xác nhận
4. Người dùng phải nhấp vào link để xác nhận email
5. Sau khi xác nhận, tài khoản chuyển thành trạng thái `ACTIVE`

## Quy trình đăng ký và xác nhận email

### 1. Bước 1: Người dùng đăng ký (Register)

**Endpoint:**
```
POST /api/auth/register
```

**Request:**
```json
{
  "fullName": "Nguyễn Văn A",
  "email": "user@example.com",
  "phone": "0912345678",
  "password": "Password@123",
  "role": "CUSTOMER"
}
```

**Response (HTTP 201 Created):**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "code": "USR-001",
      "fullName": "Nguyễn Văn A",
      "email": "user@example.com",
      "phone": "0912345678",
      "role": "CUSTOMER",
      "status": "PENDING",
      "createdAt": "2026-05-09T10:30:00"
    }
  }
}
```

**Hành động tự động:**
- Email xác nhận sẽ được gửi đến `user@example.com` với:
  - Link xác nhận: `http://localhost:3000/verify-email?token=<token>`
  - Token hết hạn trong 24 giờ
  - Giao diện email với branding công ty

### 2. Bước 2: Người dùng xác nhận email

Người dùng nhấp vào link trong email hoặc sử dụng token để gọi API:

**Endpoint:**
```
POST /api/auth/verify-email
```

**Request:**
```json
{
  "token": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Response (HTTP 200 OK):**
```json
{
  "success": true,
  "data": null
}
```

**Kết quả:**
- Tài khoản người dùng chuyển từ trạng thái `PENDING` → `ACTIVE`
- Tất cả token xác nhận khác bị vô hiệu hóa
- Người dùng có thể đăng nhập bình thường

## Cặu trúc Cơ sở dữ liệu

### 1. Bảng `users`
```sql
-- Trạng thái: PENDING (chờ xác nhận email), ACTIVE, LOCKED, INACTIVE
ALTER TABLE users ADD COLUMN status VARCHAR(20) DEFAULT 'PENDING';
```

### 2. Bảng `email_confirmation_tokens` (mới)
```sql
CREATE TABLE email_confirmation_tokens (
  id BIGSERIAL PRIMARY KEY,
  token TEXT NOT NULL UNIQUE,
  user_id BIGINT NOT NULL REFERENCES users(id),
  expired_at TIMESTAMP NOT NULL,
  used BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_email_token ON email_confirmation_tokens(token);
CREATE INDEX idx_email_user ON email_confirmation_tokens(user_id);
```

## Cấu hình Email

### 1. Biến môi trường (.env)

```bash
# Gmail SMTP
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# Frontend URL (để tạo link xác nhận)
FRONTEND_URL=http://localhost:3000
```

### 2. Cấu hình trong `application.yml`

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true

app:
  frontend-url: ${FRONTEND_URL:http://localhost:3000}
```

### 3. Thiết lập Gmail

1. Bật "Đăng nhập ứng dụng" trong tài khoản Google
2. Tạo "Mật khẩu ứng dụng" (App Password)
3. Sử dụng mật khẩu ứng dụng trong `MAIL_PASSWORD`

## Xử lý lỗi

### Lỗi khi xác nhận email

| Lỗi | Mã lỗi | HTTP | Mô tả |
|---|---|---|---|
| Token không hợp lệ | `INVALID_TOKEN` | 400 | Token không tồn tại hoặc đã được sử dụng |
| Token hết hạn | `TOKEN_EXPIRED` | 400 | Token đã hết hạn (24h) |
| Email đã xác nhận | `INVALID_TOKEN` | 400 | Token đã được sử dụng |

**Ví dụ khi token không hợp lệ:**
```json
{
  "success": false,
  "error": {
    "code": "INVALID_TOKEN",
    "message": "Token không hợp lệ hoặc đã được sử dụng"
  }
}
```

## Luồng xác nhận (Frontend Integration)

### 1. Trang xác nhận email (Frontend)

```javascript
// component: VerifyEmail.vue / VerifyEmail.tsx

useEffect(() => {
  const token = new URLSearchParams(window.location.search).get('token');
  
  if (token) {
    axios.post('/api/auth/verify-email', { token })
      .then(() => {
        message.success('Email xác nhận thành công!');
        navigate('/login');
      })
      .catch((error) => {
        message.error('Không thể xác nhận email: ' + error.response.data.error.message);
      });
  }
}, []);
```

### 2. URL xác nhận trong email

```
http://localhost:3000/verify-email?token=550e8400-e29b-41d4-a716-446655440000
```

## Tính năng bổ sung (Future)

- [ ] Gửi lại email xác nhận
- [ ] Resend endpoint: `POST /api/auth/resend-verification-email`
- [ ] Kiểm tra trạng xác nhận email: `GET /api/auth/email-status`
- [ ] Xác nhận email khi đăng nhập nếu chưa xác nhận
- [ ] SMS verification (tuỳ chọn)

## Các file được thêm/sửa đổi

### Thêm mới:
- `src/main/java/com/example/becommerce/entity/EmailConfirmationToken.java`
- `src/main/java/com/example/becommerce/repository/EmailConfirmationTokenRepository.java`
- `src/main/java/com/example/becommerce/service/EmailService.java`
- `src/main/java/com/example/becommerce/service/impl/EmailServiceImpl.java`
- `src/main/java/com/example/becommerce/dto/request/VerifyEmailRequest.java`

### Sửa đổi:
- `src/main/java/com/example/becommerce/service/AuthService.java` - Thêm method `verifyEmail()`
- `src/main/java/com/example/becommerce/service/impl/AuthServiceImpl.java` - Implements `verifyEmail()`, gửi email khi đăng ký
- `src/main/java/com/example/becommerce/controller/AuthController.java` - Thêm endpoint `/verify-email`
- `src/main/java/com/example/becommerce/constant/ApiConstant.java` - Thêm hằng số `AUTH_VERIFY_EMAIL`
- `src/main/java/com/example/becommerce/constant/ErrorCode.java` - Thêm mã lỗi email
- `src/main/java/com/example/becommerce/entity/User.java` - Thêm relation với EmailConfirmationToken
- `src/main/resources/application.yml` - Thêm cấu hình `app.frontend-url`

## Kiểm tra khả năng hoạt động

```bash
# Build project
mvn clean compile

# Chạy ứng dụng
mvn spring-boot:run

# Test endpoint đăng ký
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Test User",
    "email": "test@example.com",
    "phone": "0912345678",
    "password": "Password@123",
    "role": "CUSTOMER"
  }'
```

