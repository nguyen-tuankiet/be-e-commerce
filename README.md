# GlowUp Backend API

Backend RESTful API cho nền tảng dịch vụ sửa chữa & chăm sóc nhà cửa **GlowUp** — kết nối khách hàng với kỹ thuật viên chuyên nghiệp.

---

## Tech Stack

| Thành phần | Công nghệ đề xuất |
|---|---|
| Runtime | Node.js >= 20 |
| Framework | Express.js / NestJS |
| Database | PostgreSQL |
| ORM | Prisma / TypeORM |
| Cache | Redis |
| Auth | JWT (Access Token + Refresh Token) |
| File Upload | AWS S3 / Cloudinary |
| Realtime | Socket.IO |
| Queue | BullMQ (Redis) |
| Email/SMS | Nodemailer + Twilio |
| Docs | Swagger / OpenAPI 3.0 |

---

## Cấu trúc thư mục

```
glowup-be/
├── src/
│   ├── config/                  # Cấu hình app, env, database
│   │   ├── app.config.ts
│   │   ├── database.config.ts
│   │   ├── redis.config.ts
│   │   └── jwt.config.ts
│   │
│   ├── modules/
│   │   ├── auth/                # Đăng nhập, đăng ký, refresh token
│   │   │   ├── auth.controller.ts
│   │   │   ├── auth.service.ts
│   │   │   ├── auth.module.ts
│   │   │   ├── strategies/
│   │   │   │   ├── jwt.strategy.ts
│   │   │   │   └── local.strategy.ts
│   │   │   └── dto/
│   │   │       ├── login.dto.ts
│   │   │       ├── register.dto.ts
│   │   │       └── change-password.dto.ts
│   │   │
│   │   ├── users/               # Quản lý người dùng
│   │   │   ├── users.controller.ts
│   │   │   ├── users.service.ts
│   │   │   ├── users.module.ts
│   │   │   └── dto/
│   │   │
│   │   ├── technicians/         # Hồ sơ & kỹ thuật viên
│   │   │   ├── technicians.controller.ts
│   │   │   ├── technicians.service.ts
│   │   │   ├── technicians.module.ts
│   │   │   └── dto/
│   │   │
│   │   ├── orders/              # Quản lý đơn hàng
│   │   │   ├── orders.controller.ts
│   │   │   ├── orders.service.ts
│   │   │   ├── orders.module.ts
│   │   │   └── dto/
│   │   │
│   │   ├── reviews/             # Đánh giá
│   │   ├── warranty/            # Bảo hành
│   │   ├── reports/             # Khiếu nại
│   │   ├── chat/                # Hội thoại & tin nhắn
│   │   ├── quotes/              # Báo giá
│   │   ├── wallet/              # Ví tiền & giao dịch
│   │   ├── verifications/       # Xác minh danh tính thợ
│   │   ├── categories/          # Danh mục dịch vụ
│   │   ├── notifications/       # Thông báo
│   │   ├── upload/              # Upload file/ảnh
│   │   └── admin/               # Dashboard, finance, settings
│   │       ├── dashboard/
│   │       ├── finance/
│   │       └── settings/
│   │
│   ├── common/
│   │   ├── decorators/          # Custom decorators (Roles, CurrentUser...)
│   │   ├── guards/              # JwtAuthGuard, RolesGuard
│   │   ├── interceptors/        # Response transform, logging
│   │   ├── filters/             # Exception filters
│   │   ├── pipes/               # Validation pipes
│   │   └── utils/               # Helper functions
│   │
│   ├── database/
│   │   ├── migrations/
│   │   └── seeds/
│   │
│   ├── gateway/                 # Socket.IO WebSocket gateway
│   │   └── events.gateway.ts
│   │
│   └── main.ts                  # Entry point
│
├── prisma/
│   └── schema.prisma            # Database schema
│
├── test/
│   ├── unit/
│   └── e2e/
│
├── .env.example
├── .env
├── docker-compose.yml
├── Dockerfile
└── package.json
```

---

## Cài đặt & Chạy

### 1. Clone & cài dependencies

```bash
git clone https://github.com/your-org/glowup-be.git
cd glowup-be
npm install
```

### 2. Cấu hình môi trường

```bash
cp .env.example .env
```

Chỉnh sửa file `.env`:

```env
# App
NODE_ENV=development
PORT=3000
APP_NAME=GlowUp API

# Database
DATABASE_URL=postgresql://postgres:password@localhost:5432/glowup_db

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# JWT
JWT_ACCESS_SECRET=your_access_secret_here
JWT_REFRESH_SECRET=your_refresh_secret_here
JWT_ACCESS_EXPIRES_IN=15m
JWT_REFRESH_EXPIRES_IN=30d

# File Upload (Cloudinary)
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret

# Email (Nodemailer)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USER=your_email@gmail.com
MAIL_PASSWORD=your_app_password
MAIL_FROM=no-reply@glowup.vn

# SMS (Twilio)
TWILIO_ACCOUNT_SID=your_account_sid
TWILIO_AUTH_TOKEN=your_auth_token
TWILIO_PHONE_NUMBER=+1234567890

# CORS
ALLOWED_ORIGINS=http://localhost:5173,https://glowup.vn
```

### 3. Khởi tạo database

```bash
# Tạo database và chạy migrations
npx prisma migrate dev --name init

# Seed dữ liệu mẫu
npx prisma db seed
```

### 4. Chạy development

```bash
npm run start:dev
```

### 5. Chạy với Docker

```bash
docker-compose up -d
```

---

## Database Schema

### Bảng chính

```prisma
// prisma/schema.prisma

model User {
  id              String    @id @default(cuid())
  fullName        String
  email           String?   @unique
  phone           String?   @unique
  passwordHash    String
  role            Role      @default(CUSTOMER)
  status          UserStatus @default(PENDING)
  avatar          String?
  address         String?
  district        String?
  createdAt       DateTime  @default(now())
  updatedAt       DateTime  @updatedAt

  technicianProfile   TechnicianProfile?
  ordersAsCustomer    Order[]   @relation("CustomerOrders")
  ordersAsTechnician  Order[]   @relation("TechnicianOrders")
  wallet              Wallet?
  conversations       ConversationParticipant[]
  notifications       Notification[]
  sentMessages        Message[]
}

enum Role {
  CUSTOMER
  TECHNICIAN
  ADMIN
}

enum UserStatus {
  PENDING
  VERIFIED
  LOCKED
}

model TechnicianProfile {
  id                  String    @id @default(cuid())
  userId              String    @unique
  user                User      @relation(fields: [userId], references: [id])
  bio                 String?
  skills              String[]
  areas               String[]
  pricePerHour        Int?
  yearsExperience     Int       @default(0)
  rating              Float     @default(0)
  reviewCount         Int       @default(0)
  completedJobs       Int       @default(0)
  isAvailable         Boolean   @default(false)
  type                TechnicianType @default(NORMAL)
  titleBadge          String?
  verificationStatus  VerificationStatus @default(PENDING)
  createdAt           DateTime  @default(now())
  updatedAt           DateTime  @updatedAt

  verificationRequest VerificationRequest?
}

enum TechnicianType {
  NORMAL
  PREMIUM
}

model Order {
  id              String      @id @default(cuid())
  customerId      String
  customer        User        @relation("CustomerOrders", fields: [customerId], references: [id])
  technicianId    String?
  technician      User?       @relation("TechnicianOrders", fields: [technicianId], references: [id])
  categoryId      String?
  category        Category?   @relation(fields: [categoryId], references: [id])
  serviceName     String?
  subService      String?
  deviceName      String
  description     String
  address         String
  district        String?
  status          OrderStatus @default(NEW)
  estimatedPrice  Int
  finalPrice      Int?
  paymentMethod   String?
  scheduledAt     DateTime?
  startedAt       DateTime?
  completedAt     DateTime?
  cancelledAt     DateTime?
  cancelledBy     String?
  cancelReason    String?
  warrantyMonths  Int         @default(3)
  images          String[]
  createdAt       DateTime    @default(now())
  updatedAt       DateTime    @updatedAt

  priceAdjustment     PriceAdjustment?
  review              Review?
  warranty            Warranty?
  report              Report?
  conversation        Conversation?
}

enum OrderStatus {
  NEW
  SCHEDULED
  IN_PROGRESS
  COMPLETED
  CANCELLED
  WARRANTY
}

model PriceAdjustment {
  id            String              @id @default(cuid())
  orderId       String              @unique
  order         Order               @relation(fields: [orderId], references: [id])
  originalPrice Int
  newPrice      Int
  reason        String
  parts         Json
  evidenceImages String[]
  status        PriceAdjustmentStatus @default(PENDING)
  requestedAt   DateTime            @default(now())
  resolvedAt    DateTime?
}

enum PriceAdjustmentStatus {
  PENDING
  APPROVED
  REJECTED
}

model Review {
  id            String    @id @default(cuid())
  orderId       String    @unique
  order         Order     @relation(fields: [orderId], references: [id])
  authorId      String
  technicianId  String
  rating        Int
  content       String?
  attachedImages String[]
  createdAt     DateTime  @default(now())
}

model Warranty {
  id            String          @id @default(cuid())
  orderId       String          @unique
  order         Order           @relation(fields: [orderId], references: [id])
  status        WarrantyStatus  @default(PENDING)
  description   String
  images        String[]
  scheduledAt   DateTime?
  createdAt     DateTime        @default(now())
  updatedAt     DateTime        @updatedAt
}

enum WarrantyStatus {
  PENDING
  IN_PROGRESS
  RESOLVED
}

model Report {
  id            String        @id @default(cuid())
  orderId       String        @unique
  order         Order         @relation(fields: [orderId], references: [id])
  reporterId    String
  reason        String
  description   String
  evidenceImages String[]
  status        ReportStatus  @default(OPEN)
  resolvedAt    DateTime?
  resolvedBy    String?
  createdAt     DateTime      @default(now())
}

enum ReportStatus {
  OPEN
  IN_REVIEW
  RESOLVED
  CLOSED
}

model Conversation {
  id            String    @id @default(cuid())
  orderId       String?   @unique
  order         Order?    @relation(fields: [orderId], references: [id])
  createdAt     DateTime  @default(now())
  updatedAt     DateTime  @updatedAt

  participants  ConversationParticipant[]
  messages      Message[]
}

model ConversationParticipant {
  id              String        @id @default(cuid())
  conversationId  String
  conversation    Conversation  @relation(fields: [conversationId], references: [id])
  userId          String
  user            User          @relation(fields: [userId], references: [id])
  joinedAt        DateTime      @default(now())

  @@unique([conversationId, userId])
}

model Message {
  id              String        @id @default(cuid())
  conversationId  String
  conversation    Conversation  @relation(fields: [conversationId], references: [id])
  senderId        String
  sender          User          @relation(fields: [senderId], references: [id])
  type            MessageType   @default(TEXT)
  content         String?
  quoteId         String?
  quote           Quote?        @relation(fields: [quoteId], references: [id])
  isRead          Boolean       @default(false)
  sentAt          DateTime      @default(now())
}

enum MessageType {
  TEXT
  QUOTATION
  IMAGE
}

model Quote {
  id              String      @id @default(cuid())
  conversationId  String
  technicianId    String
  serviceName     String
  description     String?
  price           Int
  scheduledAt     DateTime?
  notes           String?
  status          QuoteStatus @default(PENDING)
  createdAt       DateTime    @default(now())
  updatedAt       DateTime    @updatedAt

  messages        Message[]
}

enum QuoteStatus {
  PENDING
  ACCEPTED
  REJECTED
  EXPIRED
}

model Wallet {
  id              String    @id @default(cuid())
  userId          String    @unique
  user            User      @relation(fields: [userId], references: [id])
  balance         Int       @default(0)
  pendingBalance  Int       @default(0)
  totalEarned     Int       @default(0)
  totalWithdrawn  Int       @default(0)
  createdAt       DateTime  @default(now())
  updatedAt       DateTime  @updatedAt

  transactions    Transaction[]
  bankAccounts    BankAccount[]
}

model Transaction {
  id          String              @id @default(cuid())
  walletId    String
  wallet      Wallet              @relation(fields: [walletId], references: [id])
  type        TransactionType
  title       String
  category    String
  amount      Int
  status      TransactionStatus   @default(PENDING)
  metadata    Json?
  createdAt   DateTime            @default(now())
}

enum TransactionType {
  TOPUP
  WITHDRAW
  COMMISSION
  EARNING
  ADJUSTMENT
}

enum TransactionStatus {
  PENDING
  SUCCESS
  FAILED
}

model BankAccount {
  id            String    @id @default(cuid())
  walletId      String
  wallet        Wallet    @relation(fields: [walletId], references: [id])
  bankName      String
  accountNumber String
  accountOwner  String
  isDefault     Boolean   @default(false)
  createdAt     DateTime  @default(now())
}

model VerificationRequest {
  id                  String              @id @default(cuid())
  technicianProfileId String              @unique
  technicianProfile   TechnicianProfile   @relation(fields: [technicianProfileId], references: [id])
  fullName            String
  phone               String
  email               String
  district            String
  serviceCategory     String
  yearsExperience     Int
  status              VerificationStatus  @default(PENDING)
  note                String?
  reviewedBy          String?
  reviewedAt          DateTime?
  submittedAt         DateTime            @default(now())
  documents           Json
}

enum VerificationStatus {
  PENDING
  APPROVED
  REJECTED
  NEEDS_RESUBMIT
}

model Category {
  id          String          @id @default(cuid())
  title       String
  description String?
  iconUrl     String?
  priority    CategoryPriority @default(NORMAL)
  status      CategoryStatus  @default(ACTIVE)
  createdAt   DateTime        @default(now())
  updatedAt   DateTime        @updatedAt

  orders      Order[]
}

enum CategoryPriority {
  HIGH
  NORMAL
  LOW
}

enum CategoryStatus {
  ACTIVE
  INACTIVE
}

model Notification {
  id        String    @id @default(cuid())
  userId    String
  user      User      @relation(fields: [userId], references: [id])
  type      String
  title     String
  body      String
  data      Json?
  isRead    Boolean   @default(false)
  createdAt DateTime  @default(now())
}

model SystemSettings {
  id        String    @id @default("singleton")
  data      Json
  updatedBy String?
  updatedAt DateTime  @updatedAt
}
```

---

## API Endpoints

### Base URL
```
http://localhost:3000/api
```

### Authentication
Tất cả các endpoint (trừ auth) yêu cầu header:
```
Authorization: Bearer <access_token>
```

---

### 🔐 Auth

| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| POST | `/auth/login` | Đăng nhập | ❌ |
| POST | `/auth/register` | Đăng ký | ❌ |
| POST | `/auth/forgot-password` | Quên mật khẩu | ❌ |
| POST | `/auth/change-password` | Đổi mật khẩu | ❌ |
| POST | `/auth/logout` | Đăng xuất | ✅ |
| GET | `/auth/me` | Thông tin user hiện tại | ✅ |

---

### 👤 Users

| Method | Endpoint | Mô tả | Role |
|--------|----------|-------|------|
| GET | `/users` | Danh sách user | ADMIN |
| GET | `/users/:id` | Chi tiết user | ADMIN |
| PATCH | `/users/:id` | Cập nhật thông tin | ADMIN / OWNER |
| PATCH | `/users/:id/status` | Khóa/mở khóa tài khoản | ADMIN |
| DELETE | `/users/:id` | Xóa tài khoản | ADMIN / OWNER |
| GET | `/users/:id/orders` | Lịch sử đơn hàng | ADMIN / OWNER |
| GET | `/users/:id/wallet` | Thông tin ví | ADMIN / OWNER |

---

### 🔧 Technicians

| Method | Endpoint | Mô tả | Role |
|--------|----------|-------|------|
| GET | `/technicians` | Danh sách thợ | PUBLIC |
| GET | `/technicians/:id` | Hồ sơ chi tiết thợ | PUBLIC |
| PATCH | `/technicians/:id/profile` | Cập nhật hồ sơ | TECHNICIAN |
| PATCH | `/technicians/:id/availability` | Bật/tắt nhận đơn | TECHNICIAN |
| GET | `/technicians/:id/reviews` | Đánh giá của thợ | PUBLIC |
| GET | `/technicians/:id/schedule` | Lịch làm việc | PUBLIC |

---

### 📋 Orders

| Method | Endpoint | Mô tả | Role |
|--------|----------|-------|------|
| GET | `/orders` | Danh sách đơn | CUSTOMER / TECHNICIAN / ADMIN |
| POST | `/orders` | Tạo đơn mới | CUSTOMER |
| GET | `/orders/:id` | Chi tiết đơn | CUSTOMER / TECHNICIAN / ADMIN |
| PATCH | `/orders/:id/status` | Cập nhật trạng thái | TECHNICIAN / ADMIN |
| POST | `/orders/:id/cancel` | Hủy đơn | CUSTOMER / TECHNICIAN |
| POST | `/orders/:id/accept` | Thợ nhận đơn | TECHNICIAN |
| POST | `/orders/:id/reject` | Thợ từ chối đơn | TECHNICIAN |
| POST | `/orders/:id/start` | Bắt đầu thực hiện | TECHNICIAN |
| POST | `/orders/:id/complete` | Hoàn thành đơn | TECHNICIAN |
| PATCH | `/orders/:id/price` | Điều chỉnh chi phí | TECHNICIAN |
| POST | `/orders/:id/price/approve` | Khách xác nhận giá mới | CUSTOMER |
| POST | `/orders/:id/price/reject` | Khách từ chối giá mới | CUSTOMER |

---

### ⭐ Reviews

| Method | Endpoint | Mô tả | Role |
|--------|----------|-------|------|
| POST | `/orders/:id/reviews` | Gửi đánh giá | CUSTOMER |
| GET | `/reviews` | Danh sách đánh giá | ADMIN |

---

### 🛡️ Warranty

| Method | Endpoint | Mô tả | Role |
|--------|----------|-------|------|
| POST | `/orders/:id/warranty` | Gửi yêu cầu bảo hành | CUSTOMER |
| GET | `/orders/:id/warranty` | Thông tin bảo hành | CUSTOMER / TECHNICIAN |
| PATCH | `/warranty/:id/status` | Cập nhật trạng thái | ADMIN / TECHNICIAN |

---

### 🚨 Reports

| Method | Endpoint | Mô tả | Role |
|--------|----------|-------|------|
| POST | `/orders/:id/reports` | Gửi khiếu nại | CUSTOMER |
| GET | `/reports` | Danh sách khiếu nại | ADMIN |
| PATCH | `/reports/:id/status` | Xử lý khiếu nại | ADMIN |

---

### 💬 Chat

| Method | Endpoint | Mô tả | Role |
|--------|----------|-------|------|
| GET | `/conversations` | Danh sách hội thoại | CUSTOMER / TECHNICIAN |
| POST | `/conversations` | Tạo hội thoại mới | CUSTOMER |
| GET | `/conversations/:id/messages` | Danh sách tin nhắn | CUSTOMER / TECHNICIAN |
| POST | `/conversations/:id/messages` | Gửi tin nhắn | CUSTOMER / TECHNICIAN |
| POST | `/conversations/:id/quotes` | Thợ gửi báo giá | TECHNICIAN |

---

### 📄 Quotes

| Method | Endpoint | Mô tả | Role |
|--------|----------|-------|------|
| PATCH | `/quotes/:id/accept` | Chấp nhận báo giá | CUSTOMER |
| PATCH | `/quotes/:id/reject` | Từ chối báo giá | CUSTOMER |

---

### 💰 Wallet

| Method | Endpoint | Mô tả | Role |
|--------|----------|-------|------|
| GET | `/wallet` | Số dư ví hiện tại | TECHNICIAN |
| GET | `/wallet/transactions` | Lịch sử giao dịch | TECHNICIAN |
| POST | `/wallet/topup` | Tạo yêu cầu nạp tiền | TECHNICIAN |
| POST | `/wallet/topup/confirm` | Xác nhận đã chuyển khoản | TECHNICIAN |
| POST | `/wallet/withdraw` | Yêu cầu rút tiền | TECHNICIAN |
| GET | `/wallet/bank-accounts` | Danh sách ngân hàng | TECHNICIAN |
| POST | `/wallet/bank-accounts` | Thêm ngân hàng | TECHNICIAN |
| DELETE | `/wallet/bank-accounts/:id` | Xóa ngân hàng | TECHNICIAN |

---

### ✅ Verifications

| Method | Endpoint | Mô tả | Role |
|--------|----------|-------|------|
| GET | `/verifications` | Danh sách hồ sơ xác minh | ADMIN |
| POST | `/verifications` | Nộp hồ sơ xác minh | TECHNICIAN |
| GET | `/verifications/:id` | Chi tiết hồ sơ | ADMIN |
| PATCH | `/verifications/:id` | Cập nhật trạng thái | ADMIN |
| GET | `/verifications/technician/:technicianId` | Trạng thái xác minh | ADMIN / OWNER |

---

### 🗂️ Categories

| Method | Endpoint | Mô tả | Role |
|--------|----------|-------|------|
| GET | `/categories` | Danh sách danh mục | PUBLIC |
| POST | `/categories` | Thêm danh mục | ADMIN |
| PUT | `/categories/:id` | Cập nhật danh mục | ADMIN |
| DELETE | `/categories/:id` | Xóa danh mục | ADMIN |
| PATCH | `/categories/:id/status` | Bật/tắt danh mục | ADMIN |

---

### 📊 Admin Dashboard

| Method | Endpoint | Mô tả | Role |
|--------|----------|-------|------|
| GET | `/admin/stats` | Thống kê tổng quan | ADMIN |
| GET | `/admin/stats/revenue` | Biểu đồ doanh thu | ADMIN |
| GET | `/admin/stats/service-distribution` | Tỷ trọng dịch vụ | ADMIN |
| GET | `/admin/orders/recent` | Đơn hàng gần đây | ADMIN |

---

### 💵 Admin Finance

| Method | Endpoint | Mô tả | Role |
|--------|----------|-------|------|
| GET | `/admin/transactions` | Lịch sử giao dịch hệ thống | ADMIN |
| GET | `/admin/withdraw-requests` | Yêu cầu rút tiền chờ duyệt | ADMIN |
| POST | `/admin/withdraw-requests/:id/approve` | Xác nhận chuyển khoản | ADMIN |
| PATCH | `/admin/commission` | Cập nhật tỷ lệ hoa hồng | ADMIN |
| POST | `/admin/wallet/adjust` | Điều chỉnh số dư thủ công | ADMIN |

---

### ⚙️ Admin Settings

| Method | Endpoint | Mô tả | Role |
|--------|----------|-------|------|
| GET | `/admin/settings` | Lấy cấu hình hệ thống | ADMIN |
| PUT | `/admin/settings` | Cập nhật cấu hình | ADMIN |

---

### 📁 Upload

| Method | Endpoint | Mô tả | Role |
|--------|----------|-------|------|
| POST | `/upload/image` | Upload 1 ảnh | ✅ |
| POST | `/upload/images` | Upload nhiều ảnh | ✅ |

---

### 🔔 Notifications

| Method | Endpoint | Mô tả | Role |
|--------|----------|-------|------|
| GET | `/notifications` | Danh sách thông báo | ✅ |
| PATCH | `/notifications/:id/read` | Đánh dấu đã đọc | ✅ |
| PATCH | `/notifications/read-all` | Đánh dấu tất cả đã đọc | ✅ |

---

## Request & Response Format

### Request chung

```http
POST /api/auth/login
Content-Type: application/json
Authorization: Bearer <token>   (với các route cần auth)

{
  "identifier": "0901234567",
  "password": "Abc@1234",
  "role": "customer"
}
```

### Response thành công

```json
{
  "success": true,
  "data": { ... }
}
```

### Response có phân trang

```json
{
  "success": true,
  "data": {
    "items": [ ... ],
    "pagination": {
      "page": 1,
      "limit": 10,
      "total": 85,
      "totalPages": 9
    }
  }
}
```

### Response lỗi

```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Dữ liệu không hợp lệ",
    "fields": {
      "email": "Email đã được sử dụng"
    }
  }
}
```

### Bảng Error Codes

| Code | HTTP Status | Ý nghĩa |
|------|-------------|---------|
| `UNAUTHORIZED` | 401 | Token không hợp lệ hoặc hết hạn |
| `FORBIDDEN` | 403 | Không có quyền truy cập |
| `NOT_FOUND` | 404 | Không tìm thấy tài nguyên |
| `VALIDATION_ERROR` | 422 | Dữ liệu đầu vào không hợp lệ |
| `INVALID_CREDENTIALS` | 401 | Sai email/password |
| `DUPLICATE_ENTRY` | 409 | Email/SĐT đã tồn tại |
| `INSUFFICIENT_BALANCE` | 400 | Số dư không đủ |
| `ORDER_ALREADY_TAKEN` | 409 | Đơn đã có thợ nhận |
| `ORDER_CANNOT_CANCEL` | 400 | Đơn không thể hủy ở trạng thái này |
| `INTERNAL_SERVER_ERROR` | 500 | Lỗi server |

---

## Query Parameters

### Phân trang (tất cả API danh sách)

```
?page=1&limit=10
```

### Filter đặc thù theo module

**Users:**
```
?role=customer|technician
&status=verified|pending|locked
&district=Quận 1
&keyword=Nguyễn
```

**Technicians:**
```
?service=Máy lạnh
&district=Quận 1
&minRating=4
&isAvailable=true
```

**Orders:**
```
?status=new|scheduled|in-progress|completed|warranty|cancelled
```

**Transactions:**
```
?type=all|income|expense
&date=2026-05-07
```

**Verifications:**
```
?status=pending|approved|rejected|needs_resubmit
&keyword=Nguyễn
```

**Admin revenue chart:**
```
?range=7days|30days
```

---

## WebSocket Events

### Kết nối

```js
const socket = io("http://localhost:3000", {
  auth: { token: "Bearer <access_token>" }
});
```

### Client gửi lên server

```js
// Tham gia hội thoại để nhận tin nhắn realtime
socket.emit("join_conversation", { conversationId: "CONV-001" });

// Báo đang gõ
socket.emit("typing", { conversationId: "CONV-001" });

// Dừng gõ
socket.emit("stop_typing", { conversationId: "CONV-001" });
```

### Server gửi xuống client

```js
// Tin nhắn mới
socket.on("message:new", {
  conversationId: "CONV-001",
  message: {
    id: "MSG-010",
    senderId: "TECH-001",
    type: "text",
    content: "Tôi đang trên đường đến",
    sentAt: "2026-05-07T13:55:00.000Z"
  }
});

// Trạng thái đơn thay đổi
socket.on("order:status_changed", {
  orderId: "GU-99210",
  oldStatus: "scheduled",
  newStatus: "in-progress",
  updatedAt: "2026-05-07T14:05:00.000Z"
});

// Thợ điều chỉnh giá
socket.on("price:adjustment_requested", {
  orderId: "GU-99210",
  originalPrice: 450000,
  newPrice: 600000,
  reason: "Phát sinh thay tụ điện"
});

// Thông báo mới
socket.on("notification:new", {
  id: "NOTIF-003",
  type: "order_completed",
  title: "Đơn hoàn thành",
  body: "Đơn GU-99210 đã hoàn thành",
  data: { orderId: "GU-99210" }
});

// Người kia đang gõ
socket.on("typing", { conversationId: "CONV-001", userId: "TECH-001" });
socket.on("stop_typing", { conversationId: "CONV-001", userId: "TECH-001" });
```

---

## Business Logic quan trọng

### Luồng đặt dịch vụ

```
Customer tạo đơn (NEW)
    → Thợ nhận đơn (SCHEDULED)
    → Thợ di chuyển đến nơi
    → Thợ bắt đầu sửa (IN_PROGRESS)
        ↓ [Nếu có phát sinh chi phí]
        Thợ gửi điều chỉnh giá → Khách approve/reject
    → Thợ hoàn thành đơn (COMPLETED)
    → Khách đánh giá
    ↓ [Nếu có sự cố trong thời hạn bảo hành]
    Khách gửi yêu cầu bảo hành (WARRANTY)
```

### Tính hoa hồng

```
Hoa hồng = finalPrice * (platformFeePercent / 100)
Thợ nhận = finalPrice - hoa hồng
```

### Phí rút tiền

```
Phí giao dịch = amount * 0.5%
Thực nhận = amount - phí
```

### Quy tắc hủy đơn

- Trạng thái `NEW` → Cả khách và thợ đều có thể hủy tự do
- Trạng thái `SCHEDULED` → Cần nhập lý do, có thể bị phạt nếu hủy gần giờ
- Trạng thái `IN_PROGRESS` → Chỉ admin mới được hủy

---

## Middleware & Guards

```
JwtAuthGuard       → Kiểm tra JWT token hợp lệ
RolesGuard         → Kiểm tra role (ADMIN, TECHNICIAN, CUSTOMER)
OwnerGuard         → Kiểm tra chính chủ resource
ThrottleGuard      → Rate limiting (100 req/min mặc định)
```

---

## Scripts

```bash
# Development
npm run start:dev

# Production build
npm run build
npm run start:prod

# Database
npx prisma migrate dev       # Tạo migration mới
npx prisma migrate deploy    # Apply migration lên production
npx prisma studio            # GUI quản lý database
npx prisma db seed           # Seed dữ liệu mẫu
npx prisma generate          # Generate Prisma Client

# Testing
npm run test                 # Unit tests
npm run test:e2e             # End-to-end tests
npm run test:cov             # Coverage report

# Linting
npm run lint
npm run format
```

---

## Docker

```yaml
# docker-compose.yml
version: '3.8'

services:
  api:
    build: .
    ports:
      - "3000:3000"
    environment:
      - DATABASE_URL=postgresql://postgres:password@db:5432/glowup_db
      - REDIS_HOST=redis
    depends_on:
      - db
      - redis

  db:
    image: postgres:16
    environment:
      POSTGRES_DB: glowup_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: password
    volumes:
      - postgres_data:/var/lib/postgresql/data
    ports:
      - "5432:5432"

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

volumes:
  postgres_data:
```

---

## API Documentation

Sau khi chạy server, truy cập Swagger UI tại:

```
http://localhost:3000/api/docs
```

---

## Liên quan

- **Frontend Repo:** [glowup-fe](https://github.com/your-org/glowup-fe)
- **Figma Design:** _(link figma)_
- **Postman Collection:** _(link postman)_
