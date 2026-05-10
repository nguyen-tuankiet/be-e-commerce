# Technician API - Comprehensive Guide

## Phần 1: TỔNG QUAN HỆ THỐNG

### 1.1 Kiến trúc User & Technician

**User (users table):**
- Mọi account đều là CUSTOMER mặc định
- Có thể có Role TECHNICIAN, ADMIN
- Soft delete via `deleted` flag

**TechnicianProfile:**
- Optional 1-1 relationship với User
- Tạo khi user đăng ký làm thợ
- Status: PENDING_KYC → ACTIVE (sau KYC) → SUSPENDED/REJECTED
- Không được nhận đơn cho đến khi KYC APPROVED + Status ACTIVE

### 1.2 Flow Đăng Ký Technician Hoàn Chỉnh

```
1. User login
2. POST /api/technicians/register
   - Tạo TechnicianProfile
   - technicianStatus = PENDING_KYC
   - kycStatus = PENDING
   - available = false
3. Upload KYC documents (external flow)
   - Admin review
   - kycStatus → APPROVED/REJECTED
4. Nếu APPROVED:
   - PATCH /api/technicians/{id}/availability
   - available = true
   - Xuất hiện trong GET /api/technicians (danh sách công khai)
5. Ready to receive bookings
```

---

## Phần 2: DATABASE MIGRATION

### 2.1 Entities Reuse từ DBML:
✅ users (cấu trúc đã có)
✅ orders (cấu trúc đã có)
✅ reviews (cấu trúc đã có)
✅ wallets (cấu trúc đã có)
✅ notifications (cấu trúc đã có)
✅ technician_profiles (mới từ DBML)

### 2.2 Liquibase/Flyway Migration (nếu dùng)

```sql
-- Migration: V1__create_technician_status_enum.sql
CREATE TYPE technician_status AS ENUM ('PENDING_KYC', 'ACTIVE', 'SUSPENDED', 'REJECTED');
CREATE TYPE kyc_status AS ENUM ('PENDING', 'APPROVED', 'REJECTED');

-- Migration: V2__create_technician_profiles_table.sql
CREATE TABLE IF NOT EXISTS technician_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    technician_status technician_status NOT NULL DEFAULT 'PENDING_KYC',
    kyc_status kyc_status NOT NULL DEFAULT 'PENDING',
    available BOOLEAN NOT NULL DEFAULT false,
    experience_years INT,
    bio TEXT,
    rating_average NUMERIC(3, 2) NOT NULL DEFAULT 0,
    review_count INT NOT NULL DEFAULT 0,
    completed_job_count INT NOT NULL DEFAULT 0,
    base_price BIGINT,
    hourly_rate BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tech_profile_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_tech_profile_status ON technician_profiles(technician_status);
CREATE INDEX idx_tech_profile_kyc_status ON technician_profiles(kyc_status);
CREATE INDEX idx_tech_profile_available ON technician_profiles(available);
```

### 2.3 Java Entities Đã Tạo:
- TechnicianStatus.java (enum)
- KycStatus.java (enum)
- TechnicianProfile.java (entity)
- User.java (updated - thêm @OneToOne technicianProfile)

---

## Phần 3: API DETAILS & EXAMPLES

### 3.1 POST /api/technicians/register
**Đăng ký làm thợ (User hiện tại)**

**Authorization:** `Bearer <token>` (yêu cầu login)

**Request:**
```json
{
  "experienceYears": 5,
  "bio": "Sửa điện lạnh chuyên nghiệp, kinh nghiệm 5 năm",
  "serviceIds": [1, 2, 3],
  "workingAreas": [
    "HN:Hà Đông",
    "HN:Ba Đình",
    "HN:Tây Hồ"
  ],
  "basePrice": 150000,
  "hourlyRate": 50000
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "data": {
    "technicianProfileId": 1,
    "technicianStatus": "PENDING_KYC",
    "kycStatus": "PENDING",
    "nextStep": "UPLOAD_KYC_DOCUMENTS"
  }
}
```

**Error Cases:**
```json
{
  "success": false,
  "error": {
    "code": "TECHNICIAN_ALREADY_EXISTS",
    "message": "User already has a technician profile"
  }
}
```

```json
{
  "success": false,
  "error": {
    "code": "INVALID_PRICE",
    "message": "Base price cannot be negative"
  }
}
```

**Validation Rules:**
- basePrice >= 0
- hourlyRate >= 0
- experienceYears >= 0
- User phải login
- User chưa có TechnicianProfile

---

### 3.2 GET /api/technicians
**Lấy danh sách thợ công khai (ACTIVE + APPROVED)**

**Authorization:** Không yêu cầu (public)

**Query Parameters:**
```
GET /api/technicians?keyword=&serviceId=&province=&district=&minRating=4.0&available=true&page=0&size=10&sortBy=ratingAverage&sortDir=DESC
```

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| keyword | string | null | Tìm kiếm theo tên |
| serviceId | long | null | Lọc theo dịch vụ |
| province | string | null | Lọc theo tỉnh |
| district | string | null | Lọc theo huyện |
| minRating | double | null | Lọc theo đánh giá tối thiểu |
| available | boolean | null | Chỉ lấy thợ sẵn sàng nhận đơn |
| page | int | 0 | Trang (0-indexed) |
| size | int | 10 | Số item/trang |
| sortBy | string | ratingAverage | Sắp xếp theo (ratingAverage, completedJobs, basePrice) |
| sortDir | string | DESC | Hướng sắp xếp (ASC, DESC) |

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "items": [
      {
        "technicianId": 1,
        "userId": 5,
        "fullName": "Nguyễn Văn A",
        "avatarUrl": "https://...",
        "bio": "Sửa điện lạnh chuyên nghiệp",
        "services": ["Sửa máy lạnh", "Bảo dưỡng máy lạnh"],
        "ratingAverage": 4.8,
        "reviewCount": 25,
        "completedJobCount": 50,
        "basePrice": 150000,
        "hourlyRate": 50000,
        "available": true,
        "workingAreas": ["HN:Hà Đông", "HN:Ba Đình"]
      },
      ...
    ],
    "pagination": {
      "page": 0,
      "limit": 10,
      "total": 127,
      "totalPages": 13
    }
  }
}
```

**Filter Logic:**
- Chỉ hiển thị: `technicianStatus = ACTIVE AND kycStatus = APPROVED`
- Nếu `available=true`: thêm `available = true`
- Chỉ lấy user không bị xóa (`user.deleted = false`)

---

### 3.3 GET /api/technicians/{id}
**Xem chi tiết hồ sơ thợ**

**Authorization:** Không yêu cầu (public)

**Request:**
```
GET /api/technicians/1
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "technicianId": 1,
    "userId": 5,
    "fullName": "Nguyễn Văn A",
    "email": "tech@example.com",
    "phone": "0912345678",
    "avatarUrl": "https://...",
    "bio": "Sửa điện lạnh chuyên nghiệp, 5 năm kinh nghiệm",
    "experienceYears": 5,
    "services": ["Sửa máy lạnh", "Bảo dưỡng máy lạnh"],
    "workingAreas": ["HN:Hà Đông", "HN:Ba Đình", "HN:Tây Hồ"],
    "ratingAverage": 4.8,
    "reviewCount": 25,
    "completedJobCount": 50,
    "basePrice": 150000,
    "hourlyRate": 50000,
    "available": true,
    "technicianStatus": "ACTIVE",
    "kycStatus": "APPROVED",
    "createdAt": "2024-01-15T10:30:00Z"
  }
}
```

**Error Cases:**
```json
{
  "success": false,
  "error": {
    "code": "TECHNICIAN_NOT_FOUND",
    "message": "Technician not found"
  }
}
```

```json
{
  "success": false,
  "error": {
    "code": "UNAUTHORIZED",
    "message": "Cannot view this technician profile"
  }
}
```

**Authorization Logic:**
- Công khai nếu: `technicianStatus = ACTIVE AND kycStatus = APPROVED`
- Chỉ owner/admin xem được profile chưa xác minh

---

### 3.4 PATCH /api/technicians/{id}/profile
**Cập nhật hồ sơ thợ (owner only)**

**Authorization:** `Bearer <token>` (yêu cầu login + owner)

**Request:**
```json
PATCH /api/technicians/1/profile

{
  "fullName": "Nguyễn Văn A",
  "phone": "0912345678",
  "avatarUrl": "https://new-avatar.jpg",
  "bio": "Sửa điện lạnh 5 năm kinh nghiệm, giỏi tay",
  "experienceYears": 6,
  "serviceIds": [1, 2, 3, 4],
  "workingAreas": ["HN:Hà Đông", "HN:Ba Đình"],
  "basePrice": 180000,
  "hourlyRate": 60000
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "technicianId": 1,
    "userId": 5,
    "fullName": "Nguyễn Văn A",
    "email": "tech@example.com",
    "phone": "0912345678",
    "avatarUrl": "https://new-avatar.jpg",
    "bio": "Sửa điện lạnh 5 năm kinh nghiệm, giỏi tay",
    "experienceYears": 6,
    "services": ["Sửa máy lạnh", "Bảo dưỡng máy lạnh", "Sửa tủ lạnh", "Bảo dưỡng tủ lạnh"],
    "workingAreas": ["HN:Hà Đông", "HN:Ba Đình"],
    "ratingAverage": 4.8,
    "reviewCount": 25,
    "completedJobCount": 50,
    "basePrice": 180000,
    "hourlyRate": 60000,
    "available": true,
    "technicianStatus": "ACTIVE",
    "kycStatus": "APPROVED",
    "createdAt": "2024-01-15T10:30:00Z"
  }
}
```

**Validation Rules:**
- basePrice >= 0
- hourlyRate >= 0
- experienceYears >= 0
- Chỉ owner mới sửa được
- Các field optional (sửa cái nào gửi cái đó)

---

### 3.5 PATCH /api/technicians/{id}/availability
**Bật/tắt trạng thái sẵn sàng nhận đơn**

**Authorization:** `Bearer <token>` (owner only)

**Request:**
```json
PATCH /api/technicians/1/availability

{
  "available": true
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "technicianId": 1,
    "available": true,
    "kycStatus": "APPROVED",
    "technicianStatus": "ACTIVE",
    "message": "Availability updated successfully"
  }
}
```

**Error Cases:**
```json
{
  "success": false,
  "error": {
    "code": "KYC_NOT_APPROVED",
    "message": "Technician account is not verified yet"
  }
}
```

```json
{
  "success": false,
  "error": {
    "code": "TECHNICIAN_NOT_ACTIVE",
    "message": "Technician account is not active"
  }
}
```

```json
{
  "success": false,
  "error": {
    "code": "ACCOUNT_DELETED",
    "message": "Your account is deleted or locked"
  }
}
```

**Validation Rules:**
- Chỉ owner mới cập nhật được
- Chỉ cho `available = true` nếu:
  - `kycStatus = APPROVED`
  - `technicianStatus = ACTIVE`
  - Tài khoản không bị xóa
- Luôn cho phép `available = false`

---

### 3.6 GET /api/technicians/{id}/reviews
**Lấy danh sách review của thợ**

**Authorization:** Không yêu cầu (public)

**Request:**
```
GET /api/technicians/1/reviews?page=0&size=10
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "items": [
      {
        "reviewId": 1,
        "customerName": "Trần Thị B",
        "customerAvatar": "https://...",
        "rating": 5,
        "comment": "Thợ rất nhiệt tình, sửa xong máy lạnh chạy rất tốt",
        "images": [
          "https://...",
          "https://..."
        ],
        "bookingId": 100,
        "createdAt": "2024-02-20T14:30:00Z"
      },
      {
        "reviewId": 2,
        "customerName": "Lê Văn C",
        "customerAvatar": "https://...",
        "rating": 4,
        "comment": "Tốc độ làm việc nhanh, giá hợp lý",
        "images": [],
        "bookingId": 98,
        "createdAt": "2024-02-18T10:00:00Z"
      },
      ...
    ],
    "pagination": {
      "page": 0,
      "limit": 10,
      "total": 25,
      "totalPages": 3
    }
  }
}
```

**Filter Logic:**
- Chỉ lấy review từ booking đã completed
- Sắp xếp theo createdAt DESC (mới nhất trước)

---

### 3.7 GET /api/technicians/{id}/schedule
**Lấy lịch làm việc của thợ**

**Authorization:** 
- Owner: xem full schedule
- Public: chỉ xem slot busy/free
- Admin: full schedule

**Request:**
```
GET /api/technicians/1/schedule?fromDate=2024-03-01&toDate=2024-03-31
```

**Response (200 OK - Placeholder):**
```json
{
  "success": true,
  "data": {
    "technicianId": 1,
    "message": "Schedule endpoint - TODO: Implement full schedule logic"
  }
}
```

> **TODO:** Cần implement logic fetch lịch từ technician_schedules, bookings

---

## Phần 4: POSTMAN TESTING

### 4.1 Environment Setup

```json
{
  "id": "technician-api-env",
  "name": "Technician API Env",
  "values": [
    {
      "key": "BASE_URL",
      "value": "http://localhost:8080",
      "enabled": true
    },
    {
      "key": "BEARER_TOKEN",
      "value": "your-jwt-token-here",
      "enabled": true
    },
    {
      "key": "TECH_ID",
      "value": "1",
      "enabled": true
    }
  ]
}
```

### 4.2 Test Collection

**1. Register Technician**
```
POST {{BASE_URL}}/api/technicians/register
Authorization: Bearer {{BEARER_TOKEN}}
Content-Type: application/json

{
  "experienceYears": 5,
  "bio": "Sửa điện lạnh chuyên nghiệp",
  "serviceIds": [1, 2],
  "workingAreas": ["HN:Hà Đông", "HN:Ba Đình"],
  "basePrice": 150000,
  "hourlyRate": 50000
}
```

**2. Get Technicians List**
```
GET {{BASE_URL}}/api/technicians?available=true&minRating=4.0&page=0&size=10
```

**3. Get Technician Detail**
```
GET {{BASE_URL}}/api/technicians/{{TECH_ID}}
```

**4. Update Profile**
```
PATCH {{BASE_URL}}/api/technicians/{{TECH_ID}}/profile
Authorization: Bearer {{BEARER_TOKEN}}
Content-Type: application/json

{
  "bio": "Sửa điện lạnh 5 năm kinh nghiệm",
  "basePrice": 180000,
  "experienceYears": 6
}
```

**5. Update Availability**
```
PATCH {{BASE_URL}}/api/technicians/{{TECH_ID}}/availability
Authorization: Bearer {{BEARER_TOKEN}}
Content-Type: application/json

{
  "available": true
}
```

**6. Get Reviews**
```
GET {{BASE_URL}}/api/technicians/{{TECH_ID}}/reviews?page=0&size=10
```

**7. Get Schedule**
```
GET {{BASE_URL}}/api/technicians/{{TECH_ID}}/schedule?fromDate=2024-03-01&toDate=2024-03-31
Authorization: Bearer {{BEARER_TOKEN}}
```

---

## Phần 5: FLOW HOÀN CHỈNH: Customer → Technician

### 5.1 Scenario: Người dùng muốn trở thành thợ sửa điện lạnh

**Step 1: User login**
```bash
POST /api/auth/login
{
  "email": "tech@example.com",
  "password": "password123"
}
→ Response: JWT token
```

**Step 2: Register as Technician**
```bash
POST /api/technicians/register
Headers: Authorization: Bearer {JWT}
Body: {
  "experienceYears": 5,
  "bio": "Sửa điện lạnh 5 năm",
  "serviceIds": [1, 2],
  "workingAreas": ["HN:Hà Đông"],
  "basePrice": 150000,
  "hourlyRate": 50000
}
→ Response: 
{
  "technicianProfileId": 1,
  "technicianStatus": "PENDING_KYC",
  "kycStatus": "PENDING",
  "nextStep": "UPLOAD_KYC_DOCUMENTS"
}
```

**Step 3: Upload KYC Documents (external flow)**
- User uploads ID, portrait, certificate, selfie
- Admin reviews
- Status changes to `kycStatus = APPROVED` / `REJECTED`

**Step 4: Admin approves KYC**
- Admin dashboard updates status
- `kycStatus` → `APPROVED`
- `technicianStatus` → `ACTIVE` (hoặc vẫn pending)

**Step 5: Enable Availability**
```bash
PATCH /api/technicians/1/availability
Headers: Authorization: Bearer {JWT}
Body: {
  "available": true
}
→ Response:
{
  "technicianId": 1,
  "available": true,
  "kycStatus": "APPROVED",
  "technicianStatus": "ACTIVE",
  "message": "Availability updated successfully"
}
```

**Step 6: Now Technician appears in public list**
```bash
GET /api/technicians?available=true
→ Response: List includes this technician
```

**Step 7: Receive bookings**
- Customers can book this technician
- System assigns orders to available technicians

---

## Phần 6: SCHEMA & INDEXES

### 6.1 Queries được optimize:

```sql
-- Public list (most critical)
SELECT tp.*, u.full_name, u.avatar
FROM technician_profiles tp
JOIN users u ON tp.user_id = u.id
WHERE tp.technician_status = 'ACTIVE'
  AND tp.kyc_status = 'APPROVED'
  AND u.deleted = false
ORDER BY tp.rating_average DESC
LIMIT 10 OFFSET 0;

-- Indexes:
CREATE INDEX idx_tech_profile_status_kyc 
  ON technician_profiles(technician_status, kyc_status);
CREATE INDEX idx_tech_profile_available 
  ON technician_profiles(available);
CREATE INDEX idx_tech_profile_rating 
  ON technician_profiles(rating_average DESC);
```

### 6.2 Recommended Indexes:

```sql
CREATE INDEX idx_technician_profiles_tech_status 
  ON technician_profiles(technician_status);
CREATE INDEX idx_technician_profiles_kyc_status 
  ON technician_profiles(kyc_status);
CREATE INDEX idx_technician_profiles_available 
  ON technician_profiles(available);
CREATE INDEX idx_technician_profiles_rating 
  ON technician_profiles(rating_average DESC);
CREATE INDEX idx_technician_profiles_status_kyc_available 
  ON technician_profiles(technician_status, kyc_status, available);
CREATE INDEX idx_review_technician_id 
  ON reviews(technician_id, created_at DESC);
```

---

## Phần 7: ERROR HANDLING & STATUS CODES

| HTTP Code | Condition |
|-----------|-----------|
| 200 OK | Successful GET/PATCH |
| 201 Created | Successful POST |
| 400 Bad Request | Validation failed |
| 401 Unauthorized | Not authenticated |
| 403 Forbidden | Not authorized (e.g., not owner) |
| 404 Not Found | Resource not found |
| 409 Conflict | Already exists |
| 500 Internal Server Error | Server error |

### Error Response Format:

```json
{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "message": "Human-readable error message",
    "fields": {
      "fieldName": "Validation error details"
    }
  }
}
```

---

## Phần 8: FILES CREATED/MODIFIED

### Created:
- ✅ [src/main/java/com/example/becommerce/entity/enums/TechnicianStatus.java](src/main/java/com/example/becommerce/entity/enums/TechnicianStatus.java)
- ✅ [src/main/java/com/example/becommerce/entity/enums/KycStatus.java](src/main/java/com/example/becommerce/entity/enums/KycStatus.java)
- ✅ [src/main/java/com/example/becommerce/entity/TechnicianProfile.java](src/main/java/com/example/becommerce/entity/TechnicianProfile.java)
- ✅ [src/main/java/com/example/becommerce/repository/TechnicianProfileRepository.java](src/main/java/com/example/becommerce/repository/TechnicianProfileRepository.java)
- ✅ [src/main/java/com/example/becommerce/service/TechnicianService.java](src/main/java/com/example/becommerce/service/TechnicianService.java)
- ✅ [src/main/java/com/example/becommerce/controller/TechnicianController.java](src/main/java/com/example/becommerce/controller/TechnicianController.java)
- ✅ [src/main/java/com/example/becommerce/dto/request/TechnicianRegisterRequest.java](src/main/java/com/example/becommerce/dto/request/TechnicianRegisterRequest.java)
- ✅ [src/main/java/com/example/becommerce/dto/request/TechnicianUpdateProfileRequest.java](src/main/java/com/example/becommerce/dto/request/TechnicianUpdateProfileRequest.java)
- ✅ [src/main/java/com/example/becommerce/dto/request/TechnicianAvailabilityRequest.java](src/main/java/com/example/becommerce/dto/request/TechnicianAvailabilityRequest.java)
- ✅ [src/main/java/com/example/becommerce/dto/response/TechnicianRegisterResponse.java](src/main/java/com/example/becommerce/dto/response/TechnicianRegisterResponse.java)
- ✅ [src/main/java/com/example/becommerce/dto/response/TechnicianListItemResponse.java](src/main/java/com/example/becommerce/dto/response/TechnicianListItemResponse.java)
- ✅ [src/main/java/com/example/becommerce/dto/response/TechnicianDetailResponse.java](src/main/java/com/example/becommerce/dto/response/TechnicianDetailResponse.java)
- ✅ [src/main/java/com/example/becommerce/dto/response/TechnicianReviewResponse.java](src/main/java/com/example/becommerce/dto/response/TechnicianReviewResponse.java)
- ✅ [src/main/java/com/example/becommerce/dto/response/TechnicianAvailabilityResponse.java](src/main/java/com/example/becommerce/dto/response/TechnicianAvailabilityResponse.java)

### Modified:
- ✅ [src/main/java/com/example/becommerce/entity/User.java](src/main/java/com/example/becommerce/entity/User.java) - Added `@OneToOne technicianProfile`
- ✅ [src/main/java/com/example/becommerce/repository/ReviewRepository.java](src/main/java/com/example/becommerce/repository/ReviewRepository.java) - Added `findByTechnicianId` method

---

## Phần 9: NEXT STEPS

### Entities reuse từ DBML (cần verify):
- [ ] Kiểm tra Review entity có `customerId`, `technicianId` không
- [ ] Kiểm tra Order entity có `status = COMPLETED` check
- [ ] Kiểm tra Wallet integration nếu cần

### TODO - Chưa implement:
- [ ] Fetch technician services từ `technician_skills` table (join query)
- [ ] Fetch technician service areas từ `technician_service_areas` table
- [ ] Fetch review images từ `review_images` table
- [ ] Fetch schedule từ `technician_schedules` table
- [ ] Filter logic optimization (keyword search, location filter)
- [ ] KYC flow integration (verification table)
- [ ] Rating average calculation trigger
- [ ] Admin endpoints để approve/reject KYC
- [ ] Notification triggers khi KYC approved
- [ ] Integration với payment/wallet system

### Database Migrations (if using Liquibase/Flyway):
- [ ] Create enums: technician_status, kyc_status
- [ ] Create table: technician_profiles
- [ ] Create indexes
- [ ] Update Review table constraints if needed

---

## Phần 10: TESTING CHECKLIST

- [ ] Test register technician
  - ✅ Success case
  - ✅ User already technician
  - ✅ Invalid price
  - ✅ Not authenticated
  
- [ ] Test get technicians list
  - ✅ Public list
  - ✅ Filter available=true
  - ✅ Pagination
  - ✅ Sorting
  
- [ ] Test get detail
  - ✅ Public profile (ACTIVE + APPROVED)
  - ✅ Owner view (PENDING_KYC)
  - ✅ Unauthorized access
  
- [ ] Test update profile
  - ✅ Owner can update
  - ✅ Non-owner cannot update
  - ✅ Validation (negative price)
  
- [ ] Test availability
  - ✅ Can enable if KYC approved + ACTIVE
  - ✅ Cannot enable if KYC pending
  - ✅ Can disable anytime
  
- [ ] Test reviews
  - ✅ Pagination works
  - ✅ Only shows completed bookings
  
- [ ] Integration
  - [ ] With KYC system
  - [ ] With Booking system
  - [ ] With Wallet system
  - [ ] With Rating calculation

