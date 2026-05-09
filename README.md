# BE E-Commerce — Spring Boot REST API

Backend REST API được xây dựng bằng **Java 21 + Spring Boot 3** theo kiến trúc phân lớp rõ ràng, sử dụng **PostgreSQL**, **Spring Security + JWT** (Access Token + Refresh Token).

---

## 🧱 Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.2 |
| Database | PostgreSQL |
| ORM | Spring Data JPA / Hibernate |
| Security | Spring Security + JWT (JJWT 0.12) |
| Build | Maven |
| Utilities | Lombok, Bean Validation |

---

## 📂 Cấu trúc thư mục

```
src/main/java/com/example/becommerce
├── config/                  # SecurityConfig, CorsConfig
├── constant/                # ApiConstant, ErrorCode, RoleConstant
├── controller/              # AuthController, UserController
├── dto/
│   ├── mapper/              # UserMapper
│   ├── request/             # RegisterRequest, LoginRequest, ...
│   └── response/            # ApiResponse, AuthResponse, UserResponse, PagedResponse
├── entity/
│   ├── enums/               # Role, UserStatus
│   ├── User.java
│   ├── RefreshToken.java
│   ├── PasswordResetToken.java
│   ├── Wallet.java
│   ├── WalletTransaction.java
│   └── BankAccount.java
│   └── PasswordResetToken.java
├── security/                # JwtProvider, JwtAuthenticationFilter, CustomUserDetails(Service), EntryPoint
├── repository/              # UserRepository, RefreshTokenRepository, PasswordResetTokenRepository
│   └── impl/                # AuthServiceImpl, UserServiceImpl, WalletServiceImpl
└── utils/                   # UserCodeGenerator, UserSpecification, TransactionCodeGenerator, MoneyUtils, BankAccountMaskUtils, VietQrGeneratorMock
├── service/                 # AuthService, UserService (interfaces)
│   └── impl/                # AuthServiceImpl, UserServiceImpl
└── utils/                   # UserCodeGenerator, UserSpecification

Sau đó chạy file schema (tuỳ chọn — JPA sẽ tự tạo bảng với `ddl-auto: update`):

```bash
psql -U postgres -d be_ecommerce -f src/main/resources/db/schema.sql
```

### 3. Cấu hình `application.yml`

Chỉnh sửa file `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/be_ecommerce
    username: postgres          # ← đổi thành user của bạn
    password: postgres          # ← đổi thành password của bạn

jwt:
  secret: 404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
  access-token-expiration: 900000      # 15 phút
  refresh-token-expiration: 604800000  # 7 ngày
```

> ⚠️ **Quan trọng**: JWT secret phải là chuỗi Base64 dài ≥ 256-bit. Thay secret mặc định trong môi trường production!

### 4. Build & Run

```bash
# Build
mvn clean package -DskipTests

# Run
mvn spring-boot:run

# Hoặc chạy jar
java -jar target/be-e-commerce-1.0.0.jar
```

Server sẽ khởi động tại: `http://localhost:8080`

---

## 🔐 Authentication Flow

```
┌─────────┐         ┌────────────┐         ┌──────────┐
│  Client │         │  API Server│         │   DB     │
└────┬────┘         └─────┬──────┘         └────┬─────┘
     │                    │                     │
     │  POST /auth/login  │                     │
     │──────────────────► │                     │
     │                    │  findByEmailOrPhone  │
     │                    │────────────────────►│
     │                    │◄────────────────────│
     │                    │  save RefreshToken   │
     │                    │────────────────────►│
     │◄──────────────────-│                     │
     │  accessToken(15m)  │                     │
     │  refreshToken(7d)  │                     │
     │                    │                     │
     │  GET /api/users    │                     │
     │  Bearer: accessToken                     │
     │──────────────────► │                     │
     │◄──────────────────-│                     │
     │   200 OK           │                     │
     │                    │                     │
     │  POST /auth/refresh-token                │
     │  { refreshToken }  │                     │
     │──────────────────► │  validateToken+DB   │
     │                    │────────────────────►│
     │◄──────────────────-│                     │
     │  new accessToken   │                     │
     │                    │                     │
     │  POST /auth/logout │                     │
     │  Bearer: accessToken                     │
     │──────────────────► │  revokeAllTokens    │
     │                    │────────────────────►│
     │◄──────────────────-│                     │
     │  200 OK            │                     │
```

---

## 📋 API Endpoints

### Auth — Public (`/api/auth/**`)

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| POST | `/api/auth/register` | Đăng ký tài khoản mới |
| POST | `/api/auth/login` | Đăng nhập |
| POST | `/api/auth/refresh-token` | Làm mới access token |
| POST | `/api/auth/logout` | Đăng xuất |
| POST | `/api/auth/forgot-password` | Quên mật khẩu |
| POST | `/api/auth/change-password` | Đổi mật khẩu |
| GET  | `/api/auth/me` | Lấy thông tin user hiện tại |

### Users — Protected (`/api/users/**`)

| Method | Endpoint | Role | Mô tả |
|--------|----------|------|-------|
| GET | `/api/users` | Any | Danh sách users (pagination + filter) |
| GET | `/api/users/{id}` | Any | Chi tiết user |
| PATCH | `/api/users/{id}` | Any | Cập nhật profile |
| PATCH | `/api/users/{id}/status` | ADMIN | Cập nhật trạng thái |

### Wallet — Protected (`/api/wallet/**`)

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| GET | `/api/wallet` | Lấy ví hiện tại, auto-create nếu chưa có |
| GET | `/api/wallet/transactions?type=all&page=1&limit=10` | Lịch sử giao dịch, phân trang, lọc theo type |
| POST | `/api/wallet/topup` | Nạp tiền — tạo giao dịch + QR (VietQR) hoặc redirect (VNPay, MoMo) |
| POST | `/api/wallet/topup/confirm` | Xác nhận nạp tiền VietQR (trước khi callback) |
| POST | `/api/wallet/withdraw` | Rút tiền về ngân hàng — trừ balance, cộng pending_balance |
| GET | `/api/wallet/bank-accounts` | Danh sách tài khoản ngân hàng của user |
| POST | `/api/wallet/bank-accounts` | Tạo tài khoản ngân hàng mới |
| DELETE | `/api/wallet/bank-accounts/{id}` | Xóa tài khoản ngân hàng |

### Payment Webhook — Public (`/api/payments/**`)

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| GET | `/api/payments/vnpay/ipn` | VNPay IPN callback — verify signature, credit wallet |
| POST | `/api/payments/momo/ipn` | MoMo IPN callback — verify signature, credit wallet |
```

### Danh sách có phân trang

```json
{
  "success": true,
  "data": {
    "items": [],
    "pagination": {
      "page": 1,
      "limit": 10,
      "total": 85,
      "totalPages": 9
    }
  }
}
```

---

## 🔑 Ví dụ sử dụng API

### Đăng ký

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Nguyễn Văn A",
    "email": "vana@email.com",
    "phone": "0901234567",
    "password": "Abc@1234",
    "role": "customer"
  }'
```

### Đăng nhập

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "vana@email.com",
    "password": "Abc@1234"
  }'
```

### Gọi API bảo vệ

```bash
curl -X GET http://localhost:8080/api/users \
  -H "Authorization: Bearer <your-access-token>"
```

### Refresh Token

```bash
curl -X POST http://localhost:8080/api/auth/refresh-token \
  -H "Content-Type: application/json" \
  -d '{ "refreshToken": "<your-refresh-token>" }'
```

### Đăng xuất

```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer <your-access-token>"
```

---

## 👥 Roles

| Role | Mô tả |
|------|-------|
| `CUSTOMER` | Khách hàng — có thể đăng ký, cập nhật profile |
| `TECHNICIAN` | Kỹ thuật viên — có thể đăng ký, cập nhật profile |
| `ADMIN` | Quản trị viên — có thể quản lý tất cả users |

> Admin phải được tạo trực tiếp trong DB. Không cho phép đăng ký với role ADMIN qua API.

---

## 🛡️ Password Policy

Mật khẩu phải thỏa mãn:
- Ít nhất **8 ký tự**
- Có **chữ hoa** (A-Z)
- Có **chữ thường** (a-z)
- Có **chữ số** (0-9)
- Có **ký tự đặc biệt** (`@`, `$`, `!`, `%`, `*`, `?`, `&`)

Ví dụ hợp lệ: `Abc@1234`, `Pass@word1`

---

## 🗄️ Database Schema

```
users
├── id (PK, BIGSERIAL)
├── code (UNIQUE, e.g. USR-001)
├── full_name
├── email (UNIQUE)
├── phone (UNIQUE)
├── password (BCrypt)
├── avatar
├── role (CUSTOMER | TECHNICIAN | ADMIN)
├── status (PENDING | ACTIVE | LOCKED | INACTIVE)
├── district, address, bio
├── deleted (soft delete)
└── created_at, updated_at

refresh_tokens
├── id (PK)
├── token (TEXT, UNIQUE)
├── expired_at
├── revoked (BOOLEAN)
└── user_id (FK → users.id)

password_reset_tokens
├── id (PK)
├── token (TEXT, UNIQUE)
├── expired_at
├── used (BOOLEAN)
└── user_id (FK → users.id)

wallets
├── id (PK)
├── user_id (FK → users.id, UNIQUE)
├── balance
├── pending_balance
├── total_earned
├── total_withdrawn
├── currency
└── created_at, updated_at

bank_accounts
├── id (PK)
├── code (UNIQUE)
├── user_id (FK → users.id)
├── bank_name
├── account_number
├── account_owner
├── is_default
└── created_at

wallet_transactions
├── id (PK)
├── transaction_code (UNIQUE)
├── wallet_id (FK → wallets.id)
├── type
├── category
├── title
├── amount, fee, net_amount
├── status
├── payment_method
├── bank_account_id (FK → bank_accounts.id)
├── transfer_content
├── qr_code
└── expired_at, processed_at, created_at
```

---

## ⚙️ Cấu hình JWT

| Property | Default | Mô tả |
|----------|---------|-------|
| `jwt.secret` | 64-char hex | HMAC-SHA256 signing key |
| `jwt.access-token-expiration` | 900000 (15m) | Access token TTL (ms) |
| `jwt.refresh-token-expiration` | 604800000 (7d) | Refresh token TTL (ms) |
| `jwt.password-reset-expiration` | 3600000 (1h) | Reset token TTL (ms) |

---

## 💳 Payment Gateway Integration

### 📍 Status: Finalized (VNPay Only)

Payment module hiện chỉ hỗ trợ **VNPay** (giải pháp thanh toán tiêu chuẩn cho các nền tảng e-commerce VN).

### VNPay Flow

```
1. User POST /api/wallet/topup
   ↓
2. WalletService creates transaction (status: AWAITING_PAYMENT)
   ↓
3. PaymentGatewayService generates checkout URL + signature
   ↓
4. FE redirects user to VNPay → Payment page
   ↓
5. User completes payment
   ↓
6. VNPay callback → GET /api/payments/vnpay/ipn
   ↓
7. Signature verification + idempotency check
   ↓
8. If success: confirmTopupAndCreditWallet() → Balance += amount
   ↓
9. FE polls transaction status / redirect to result page
```

### Configuration

```yaml
app:
  payment:
    vnpay:
      tmn-code: "YOUR_TMN_CODE"           # Merchant code from VNPay
      secret-key: "YOUR_SECRET_KEY"       # Secret for HMAC-SHA512
      pay-url: "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html"  # Sandbox
      # For production:
      # pay-url: "https://vnpayment.vn/paymentv2/vpcpay.html"
      return-url: "http://localhost:3000/wallet/topup-result"
      ip-address: "127.0.0.1"             # Server IP for VNPay logging
```

### Security on Webhooks

- ✅ **HMAC-SHA512 signature** verification mandatory
- ✅ **Idempotent processing** — duplicate IPN callbacks ignored
- ✅ **Amount validation** — IPN amount must match transaction
- ✅ **Transaction code match** — Prevents tampering
- ✅ **Status tracking** — Prevents double-crediting

---

