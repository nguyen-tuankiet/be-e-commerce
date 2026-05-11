# Security Review — GlowUp Concierge Backend

> Đối tượng review: code hiện tại trong `tmdt/src/main/java/com/example/becommerce/**`,
> `application.yml`, `.env`, `pom.xml`. Theo template `skill/security.md`.

**Scope**: chỉ review code đã viết, không cover hạ tầng (network, OS, K8s, …).

---

## 1. Security Overview

**Hệ thống**: GlowUp Concierge — marketplace dịch vụ sửa chữa, kết nối customer ↔ technician,
có ví điện tử, KYC, chat realtime.

**Asset cần bảo vệ** (theo thứ tự ưu tiên):

| Asset | Loại | Tác động nếu bị compromise |
|---|---|---|
| Số dư ví & rút tiền | Financial | Mất tiền trực tiếp, fraud |
| KYC documents (CMND, ảnh chân dung) | PII nhạy cảm | Vi phạm pháp luật, lộ danh tính |
| Password hash | Credential | Mất tài khoản, lateral movement |
| JWT access/refresh token | Credential | Mạo danh user |
| DB credentials | Infrastructure | Truy cập toàn bộ data |
| VNPay secret key | API key | Giả mạo giao dịch thanh toán |
| Conversation messages | PII | Lộ thông tin riêng tư |
| Admin dashboard | Privileged | Toàn quyền platform |

---

## 2. Tóm tắt mức độ phát hiện

| Severity | Số lượng | Cần fix trước production |
|---|---|---|
| 🔴 Critical | 3 | ✅ Bắt buộc |
| 🟠 High | 5 | ✅ Bắt buộc |
| 🟡 Medium | 6 | ✅ Khuyến nghị |
| 🟢 Low | 4 | Theo dõi |

---

## 3. Threat Modeling — STRIDE

| STRIDE | Mối đe dọa | Hiện trạng | Severity |
|---|---|---|---|
| **S**poofing | Token bị đánh cắp → impersonation | JWT bearer, không có rotation, không có device binding | 🟠 High |
| **T**ampering | Modify request body bypass authorization | Có Bean Validation; PATCH mass-assignment chưa whitelist | 🟡 Medium |
| **R**epudiation | User phủ nhận giao dịch ví/đơn | Có `audit_logs` trong DBML nhưng **chưa entity** trong code | 🟠 High |
| **I**nformation disclosure | Log chứa email/phone/token | Logs có in `user.getCode()` (OK) nhưng cần audit cụ thể | 🟡 Medium |
| **D**enial of service | Spam `/login`, `/register`, file upload lớn | **Không có rate limiting, không có file size limit** | 🔴 Critical |
| **E**levation of privilege | Customer call admin endpoints | Có `@PreAuthorize('ADMIN')` + service-level check; nhưng WS SUBSCRIBE không auth | 🟠 High |

---

## 4. Critical findings (🔴 P0 — fix trước khi deploy)

### C-01. Secrets committed vào repository

**File**: `tmdt/.env`

```env
DB_PASSWORD=npg_zhXpetL3Fwf8
MAIL_PASSWORD=mcfc rvuq wivx sagm
VNPAY_SECRET_KEY=T6XC8LMOB22JSVCTHD8MFW9P5DW0M5HC
```

`.env` đã được commit. Database production credentials (Neon), Gmail app password,
VNPay sandbox secret đều hiện diện trong git history.

**Tác động**: Bất kỳ ai clone repo có thể:
- Truy cập DB production
- Gửi email giả mạo từ địa chỉ chính thức
- Tạo VNPay transactions giả

**Fix**:
1. **Ngay lập tức**: rotate tất cả secret (đổi DB password, regenerate VNPay key, đổi mail app password)
2. Xoá `.env` khỏi git history: `git filter-repo --path .env --invert-paths`
3. Thêm `.env` vào `.gitignore` (đã có nhưng commit cũ vẫn lưu)
4. Dùng secret manager production (AWS Secrets Manager, HashiCorp Vault, Doppler) thay cho `.env`

### C-02. JWT signing key hardcoded trong application.yml

**File**: `tmdt/src/main/resources/application.yml`

```yaml
jwt:
  secret: 404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
```

Đây là hex-encoded HMAC key cố định trong code. Attacker đọc được repo (hoặc source map)
có thể **tự ký JWT với role admin**.

**Tác động**: Toàn bộ authentication bị bypass.

**Fix**:
```yaml
jwt:
  secret: ${JWT_SECRET}  # đọc từ env, mỗi env một key khác nhau
```

Generate key bằng `openssl rand -base64 64` và inject từ secret manager. Rotate
định kỳ (3-6 tháng) kèm grace period chấp nhận key cũ.

### C-03. Không có rate limiting trên auth endpoints

**Vị trí**: `SecurityConfig` — `/api/auth/**` được `permitAll()` mà không qua filter giới hạn.

**Tác động**:
- Brute force password trên `/login`
- Account enumeration qua `/register` (response khác nhau khi email exists)
- Spam SMS/email qua `/forgot-password` → phí cao + có thể bị nhà cung cấp ban

**Fix**:
- Thêm `bucket4j-spring-boot-starter` (rate limit ngay tại web layer)
- Giới hạn `/login`: 5 attempts / 15 phút / IP, 10 / hour / username
- Giới hạn `/register`: 3 / hour / IP
- Giới hạn `/forgot-password`: 1 / 15 phút / identifier
- Account lockout sau 5 lần sai liên tiếp (đã có cột `status: LOCKED`, cần wire)

```java
// pseudocode
@PostMapping("/login")
@RateLimit(key = "#request.remoteAddr", limit = 5, window = "15m")
public ResponseEntity<...> login(...) { ... }
```

---

## 5. High findings (🟠 P1 — fix trong tuần đầu sau deploy)

### H-01. WebSocket SUBSCRIBE không authorize theo topic

**File**: `WebSocketConfig.java`, `WebSocketAuthInterceptor.java`

Hiện chỉ auth trên CONNECT. Một authenticated user có thể subscribe `/topic/conversations.CONV-001`
mà không cần là participant — **đọc lén toàn bộ chat của người khác**.

**Fix**: Thêm authorization trên SUBSCRIBE frame trong `WebSocketAuthInterceptor`:

```java
if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
    String destination = accessor.getDestination();
    Principal user = accessor.getUser();
    // Verify user is participant of the conversation/order
    if (destination.startsWith("/topic/conversations.") && !isParticipantOf(...)) {
        throw new MessagingException("Forbidden");
    }
}
```

Alternative: dùng Spring Security Messaging với `@MessageMapping + @PreAuthorize`.

### H-02. Refresh token không rotate khi sử dụng

**File**: `AuthServiceImpl` (existing module — không nằm trong Phase 1-5 nhưng vẫn cần xử lý)

Khi gọi `/refresh-token`, hiện trả về access token mới nhưng **refresh token cũ vẫn dùng được**.
Nếu attacker đánh cắp refresh token, họ có thể tiếp tục sinh access tokens mà không phát hiện.

**Fix**: Refresh-token rotation:
1. Khi refresh, invalidate token cũ + cấp cặp mới
2. Detect reuse: nếu user gọi với refresh token đã rotate → buộc đăng xuất tất cả device

### H-03. File upload không validate type & size

**File**: `FileUploadController`, `LocalFileStorageServiceImpl`

Không thấy validation:
- MIME type whitelist (chỉ chấp nhận `image/jpeg`, `image/png`, `image/webp`)
- Magic bytes check (đề phòng MIME spoofing)
- Max size (Spring multipart default 1MB upload, 10MB request; cần explicit)
- Filename sanitization (path traversal qua filename)

**Tác động**:
- DoS qua upload file lớn
- Polyglot file (image + js) phục vụ XSS nếu serve trực tiếp
- Path traversal: filename `../../../etc/passwd`

**Fix**:
```yaml
spring:
  servlet:
    multipart:
      max-file-size: 5MB
      max-request-size: 20MB
```
```java
// In FileStorageService
if (!Set.of("image/jpeg","image/png","image/webp").contains(file.getContentType())) {
    throw new AppException(INVALID_FILE_TYPE, ...);
}
// Generate new filename, NEVER use original:
String filename = UUID.randomUUID() + extensionOf(file.getContentType());
```

### H-04. KYC documents không có access control

**File**: `VerificationServiceImpl`, `LocalFileStorageServiceImpl`

KYC documents (CMND mặt trước/sau, ảnh chân dung) được lưu local FS và serve qua `/uploads/**`
**không authentication** (xem `SecurityConfig`: `.requestMatchers("/uploads/**").permitAll()`).

Nếu attacker biết URL pattern (`/uploads/verifications/abc123.jpg`) → tải về.

**Tác động**: Vi phạm Luật Bảo vệ dữ liệu cá nhân (NĐ 13/2023). KYC documents = "dữ liệu cá nhân nhạy cảm".

**Fix**:
- Chuyển sang object storage (S3) với signed URLs ngắn hạn (5-15 phút)
- Hoặc bắt request `/uploads/verifications/**` qua Spring Security filter chỉ owner + admin
- Encrypt-at-rest cho KYC bucket

### H-05. Audit log không được implement

DBML schema có `audit_logs` table nhưng **không có entity/repository** trong code.

Các action cần audit:
- Admin approve/reject KYC → ai? lúc nào? note?
- Admin adjust wallet → đổi số dư của thợ
- Admin update commission %
- Order cancel với reason
- Withdraw approve

Hiện chỉ có `log.info(...)` — log sẽ rotate đi mất, không truy vết được.

**Fix**: Tạo `AuditLogService.record(actorCode, action, targetType, targetId, payload)` và
gọi trong các transition critical.

---

## 6. Medium findings (🟡 P2)

### M-01. PATCH endpoints — mass assignment risk

**File**: `UpdateTechnicianProfileRequest`, `UpdateUserRequest`

Các DTO PATCH có nhiều field. Nếu thêm field nhạy cảm vào entity (vd: `isVerified`, `tier`)
và để vào DTO mà không check role, customer/technician có thể tự nâng cấp.

**Hiện trạng**: Không có field nhạy cảm trong DTO hiện tại nhưng risk còn đó cho tương lai.

**Fix**:
- Đặt convention: DTO PATCH chỉ chứa **field user-editable**
- Reviewer phải block PR nào thêm `verificationStatus`, `tier`, `rating` vào public DTOs
- Hoặc dùng JSON Patch explicit (RFC 6902) cho admin-only endpoints

### M-02. Logging có thể leak PII

**File**: nhiều `log.info("...{}", user.getEmail())`

Vd `AuthServiceImpl`, `UserServiceImpl` log email khi tạo/update.

**Fix**:
- Mask email: `lan***@email.com`
- KHÔNG log token (full or partial)
- Document: log levels production = INFO (không DEBUG vì query verbose có thể log password trong query string nếu lỡ dùng JDBC raw)
- Tắt `spring.jpa.show-sql: true` trong production (hiện đang `true`)

### M-03. CORS cho phép wildcard origin pattern trên WS

**File**: `WebSocketConfig`

```java
registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
```

Cho phép mọi origin connect. OK trong dev nhưng production phải pin domain.

**Fix**:
```java
registry.addEndpoint("/ws")
        .setAllowedOrigins("https://glowup.vn", "https://admin.glowup.vn")
        .withSockJS();
```

### M-04. Soft delete không cleanup foreign key references

**Hiện trạng**: User có `deleted = true` nhưng vẫn còn:
- Orders với customer/technician trỏ tới
- Reviews dưới tên user đã xoá
- Conversations vẫn show "USR-XXX" trong list

**Tác động**: Vi phạm GDPR/NĐ13 "quyền được xoá" — user yêu cầu xoá nhưng PII vẫn xuất hiện ở nơi khác.

**Fix**:
- Scheduled job xoá hoặc anonymize PII trong orders/reviews sau N ngày
- Hoặc tách PII (fullName, phone) sang bảng riêng, JOIN khi cần, drop bảng đó khi delete

### M-05. Không có CSRF cho cookie-based session (nếu có)

Hiện stateless JWT trong header — không cần CSRF. Nhưng nếu **đổi sang HttpOnly cookie**
cho refresh token (best practice theo `security.md`), phải bật CSRF token cho mutation endpoints.

### M-06. WebSocket không rate limit `typing` events

Một client có thể spam `/app/conversations/{id}/typing` 1000 lần/giây → fan-out gây tải.

**Fix**: Throttle 1 message / 500ms / user / conversation tại `WsChatController` hoặc qua
broker-level rate limiting.

---

## 7. Low findings (🟢 P3)

### L-01. Stack trace có thể leak qua error response

`GlobalExceptionHandler` catch generic `Exception` → trả "Đã xảy ra lỗi nội bộ".
Nhưng log `log.error("Unhandled exception on {}: ", ..., ex)` — nếu log endpoint công khai
(actuator) hoặc log aggregator có UI cho user thì stack trace leak.

**Fix**: Đảm bảo log không cho phép truy cập từ client.

### L-02. Bcrypt cost factor mặc định

Spring Security default cost = 10. Năm 2026 khuyến nghị 12-14 cho mật khẩu user-facing.

**Fix**: `new BCryptPasswordEncoder(12)` trong `SecurityConfig`.

### L-03. Không có Content-Security-Policy / security headers

Backend trả JSON, ít risk XSS trực tiếp, nhưng response error có thể chứa user input.
Spring Security mặc định set một số header, nên thêm:
- `X-Content-Type-Options: nosniff`
- `Referrer-Policy: strict-origin-when-cross-origin`
- `Permissions-Policy: ...`

### L-04. Không invalidate WS sessions khi user bị lock

Khi admin lock một user (`PATCH /users/:id/status`), các WS sessions hiện tại vẫn duy trì.
User vẫn nhận message realtime cho đến khi disconnect tự nhiên.

**Fix**: Maintain `SimpUserRegistry`, force-close sessions khi user status đổi sang LOCKED.

---

## 8. Authentication design — đánh giá

| Hạng mục | Hiện trạng | Đánh giá |
|---|---|---|
| Password hashing | BCrypt | ✅ OK (consider cost 12) |
| Access token lifetime | 15 phút | ✅ OK |
| Refresh token lifetime | 7 ngày | ✅ OK |
| Refresh rotation | ❌ Không rotate | 🟠 Cần fix |
| MFA | ❌ Không có | 🟡 Khuyến nghị cho admin |
| Account lockout | DB có cột nhưng chưa wire | 🟠 Cần fix |
| Logout / revoke | Có | ✅ |
| Device/session list | ❌ Không | 🟡 Nice-to-have |

---

## 9. Authorization design — đánh giá

| Tầng | Hiện trạng | Đánh giá |
|---|---|---|
| URL-level (`SecurityConfig`) | Phân quyền theo path prefix + HTTP method | ✅ OK |
| Method-level (`@PreAuthorize`) | Dùng cho admin endpoints | ✅ OK |
| Object-level (ownership) | Check trong service layer (customer chỉ thấy đơn mình) | ✅ Pattern tốt |
| WebSocket SUBSCRIBE | ❌ Không có (xem H-01) | 🟠 Cần fix |
| Audit log | ❌ Không có (xem H-05) | 🟠 Cần fix |

---

## 10. Recommendations — Action plan

### Tuần 1 (trước deploy production)
- [ ] **C-01**: Rotate ALL secrets, remove `.env` khỏi git history
- [ ] **C-02**: Move JWT secret to env var
- [ ] **C-03**: Implement rate limiting cho `/login`, `/register`, `/forgot-password`
- [ ] **H-01**: Add SUBSCRIBE authorization trên WebSocket interceptor
- [ ] **H-03**: Multipart size limits + MIME validation
- [ ] **H-04**: Move KYC docs sang signed URLs hoặc Spring Security ownership check

### Tuần 2-4
- [ ] **H-02**: Refresh token rotation + reuse detection
- [ ] **H-05**: Implement `AuditLogService` + audit critical actions
- [ ] **M-02**: PII masking trong logs
- [ ] **M-03**: CORS pin domain
- [ ] **M-06**: WS typing rate limit

### Backlog
- [ ] L-01..L-04
- [ ] MFA cho admin
- [ ] Device/session management UI
- [ ] Compliance review NĐ 13/2023

---

## 11. Test plan

Sau khi fix các issue trên, test:
1. **Brute force login** — `hydra` hoặc script Python, đảm bảo bị block sau 5 lần
2. **JWT forgery** — thử ký token với secret rỗng / secret cũ
3. **WS subscribe to other's conversation** — login user A, subscribe `/topic/conversations.CONV-OF-USER-B`, kỳ vọng disconnect/error
4. **Upload exploits**:
   - File `.php` đặt `.jpg` extension
   - Polyglot file (HTML + JPEG)
   - 1GB file
   - Filename `../../../etc/passwd`
5. **Mass assignment** — gửi `{ "rating": 5, "verificationStatus": "approved" }` qua PATCH technician profile, verify không apply
6. **KYC URL guessing** — không có session, GET `/uploads/verifications/*` → kỳ vọng 401

---

## 12. Compliance notes

- **NĐ 13/2023 về Bảo vệ dữ liệu cá nhân**: KYC documents là dữ liệu nhạy cảm — bắt buộc
  encrypt-at-rest, access log, có quy trình xử lý yêu cầu xoá.
- **PCI-DSS**: hiện tại không lưu PAN (số thẻ thật) vì payment qua VNPay redirect — OK.
  Đảm bảo VNPay secret không leak (xem C-01).
- **Logging retention**: tối thiểu 12 tháng cho audit log tài chính theo Luật Kế toán 2015.

---

**Reviewer**: Senior Security Engineer (theo `skill/security.md`)
**Ngày**: 2026-05-11
**Lần review tiếp**: sau khi tất cả P0/P1 đã fix; recommend re-review sau 6 tháng.
