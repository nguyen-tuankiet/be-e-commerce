# 🚀 Hướng dẫn cài đặt tính năng xác nhận email

## Bước 1: Cấu hình Gmail

### 1.1. Bật "Đăng nhập ứng dụng" (App Passwords)
- Truy cập: [myaccount.google.com/security](https://myaccount.google.com/security)
- Tìm mục "Ứng dụng & trình duyệt"
- Chọn "Mật khẩu ứng dụng"
- Chọn app: **Mail** và device: **Window/Mac/Linux**
- Google sẽ hiển thị mật khẩu 16 ký tự, copy mật khẩu này

### 1.2. Tạo file `.env`
Tạo file `.env` ở thư mục gốc của project:

```bash
# ========== DATABASE ==========
DB_URL=jdbc:postgresql://localhost:5432/be_ecommerce
DB_USERNAME=postgres
DB_PASSWORD=password

# ========== EMAIL (Gmail) ==========
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=xxxx xxxx xxxx xxxx (16 ký tự từ bước 1.1)

# ========== APPLICATION ==========
FRONTEND_URL=http://localhost:3000

# ========== PAYMENT (VNPay) ==========
VNPAY_TMN_CODE=your_tmn_code
VNPAY_SECRET_KEY=your_secret_key
```

## Bước 2: Database Migration

Chạy SQL câu lệnh để tạo bảng `email_confirmation_tokens`:

```sql
CREATE TABLE email_confirmation_tokens (
  id BIGSERIAL PRIMARY KEY,
  token TEXT NOT NULL UNIQUE,
  user_id BIGINT NOT NULL,
  expired_at TIMESTAMP NOT NULL,
  used BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_email_token_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_email_token ON email_confirmation_tokens(token);
CREATE INDEX idx_email_user ON email_confirmation_tokens(user_id);
```

## Bước 3: Build & Run

### 3.1. Build
```bash
mvn clean package -DskipTests
```

### 3.2. Run local
```bash
mvn spring-boot:run
```

### 3.3. Run JAR
```bash
java -jar target/be-e-commerce-1.0.0.jar
```

## Bước 4: Test

### Test 1: Đăng ký (Register)
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Test User",
    "email": "your-email@gmail.com",
    "phone": "0912345678",
    "password": "Password@123",
    "role": "CUSTOMER"
  }'
```

**Kỳ vọng:**
- Email xác nhận được gửi
- Response trả về token

### Test 2: Kiểm tra email
- Mở hộp thư của `your-email@gmail.com`
- Tìm email từ "BE E-Commerce"
- Nhấp link xác nhận hoặc copy token

### Test 3: Xác nhận email (Verify)
```bash
curl -X POST http://localhost:8080/api/auth/verify-email \
  -H "Content-Type: application/json" \
  -d '{
    "token": "<token-từ-email>"
  }'
```

**Kỳ vọng:**
- Response: `{"success": true}`
- Trạng thái tài khoản: `PENDING` → `ACTIVE`

## Bước 5: Frontend Integration

### 5.1. Tạo trang xác nhận email

**File:** `src/pages/VerifyEmail.jsx`

```javascript
import { useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import axios from 'axios';

export default function VerifyEmail() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const token = searchParams.get('token');

  useEffect(() => {
    if (!token) {
      alert('Token không hợp lệ');
      navigate('/register');
      return;
    }

    // Gọi API xác nhận email
    axios.post('http://localhost:8080/api/auth/verify-email', { token })
      .then(() => {
        alert('✅ Email xác nhận thành công! Vui lòng đăng nhập.');
        navigate('/login');
      })
      .catch((error) => {
        alert('❌ Lỗi: ' + error.response?.data?.error?.message);
        navigate('/register');
      });
  }, [token, navigate]);

  return (
    <div style={{ textAlign: 'center', padding: '50px' }}>
      <h2>⏳ Đang xác nhận email...</h2>
      <p>Vui lòng chờ...</p>
    </div>
  );
}
```

### 5.2. Cấu hình route
```javascript
<Route path="/verify-email" element={<VerifyEmail />} />
```

## 🐛 Troubleshooting

### Problem 1: Email không gửi được

**Giải pháp:**
- ✅ Kiểm tra username/password Gmail đúng
- ✅ Bật "Cho phép ứng dụng kém an toàn" (nếu cần)
- ✅ Kiểm tra firewall/proxy
- ✅ Xem logs: `tail -f application.log | grep -i email`

### Problem 2: Link xác nhận không đúng

**Giải pháp:**
- ✅ Kiểm tra biến `FRONTEND_URL` đúng
- ✅ Url phải là frontend domain của bạn
- ✅ Ví dụ: `FRONTEND_URL=https://yourdomain.com`

### Problem 3: Token hết hạn

**Giải pháp:**
- ✅ Token có thể hết hạn sau 24 giờ
- ✅ Yêu cầu người dùng "Gửi lại email" (chưa implement)
- ✅ Hoặc admin có thể reset token

### Problem 4: Database error

**Giải pháp:**
- ✅ Chạy SQL migration script ở Bước 2
- ✅ Kiểm tra user có quyền CREATE TABLE
- ✅ PostgreSQL version >= 9.6

## 📚 Tài liệu thêm

- [EMAIL_CONFIRMATION.md](./docs/EMAIL_CONFIRMATION.md) - Tài liệu chi tiết API
- [API_REFERENCE.md](./docs/API_REFERENCE.md) - Tổng quan tất cả API
- [IMPLEMENTATION_SUMMARY.md](./IMPLEMENTATION_SUMMARY.md) - Tóm tắt implementation

## ✅ Checklist cài đặt

- [ ] Gmail app password được tạo
- [ ] File `.env` được cấu hình
- [ ] Database migration được chạy
- [ ] Build project thành công
- [ ] Application chạy không lỗi
- [ ] Test đăng ký thành công
- [ ] Email xác nhận nhận được
- [ ] Verify email thành công
- [ ] Frontend route `/verify-email` được tạo
- [ ] End-to-end test hoàn tất

---

**Status:** ✅ Ready to use  
**Support Email:** support@yourdomain.com  
**Last Updated:** 2026-05-09

