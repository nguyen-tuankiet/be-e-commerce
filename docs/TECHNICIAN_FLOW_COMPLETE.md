# Complete Technician Flow - Step by Step

## 📍 Scenario: Khách hàng muốn trở thành thợ sửa điện lạnh

---

## BƯỚC 1: User Login 🔐

```
User truy cập app → Login
```

**API:**
```
POST /api/auth/login
{
  "email": "nguyenvan@example.com",
  "password": "password123"
}

Response:
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "refresh_token_xxx",
    "user": {
      "id": 5,
      "email": "nguyenvan@example.com",
      "fullName": "Nguyễn Văn A",
      "role": "CUSTOMER"
    }
  }
}
```

**Store Token:** Save `accessToken` for subsequent requests

---

## BƯỚC 2: User Đăng Ký Làm Thợ 🔧

```
User nhấp "Trở thành thợ sửa" → Fill form → Submit
```

**API:**
```
POST /api/technicians/register
Headers: Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "experienceYears": 5,
  "bio": "Tôi là thợ sửa điện lạnh có kinh nghiệm 5 năm, chuyên sửa máy lạnh, tủ lạnh",
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

**Database Changes:**
```
INSERT INTO technician_profiles (
  user_id,
  technician_status,
  kyc_status,
  available,
  experience_years,
  bio,
  rating_average,
  review_count,
  completed_job_count,
  base_price,
  hourly_rate,
  created_at,
  updated_at
) VALUES (
  5,
  'PENDING_KYC',
  'PENDING',
  false,
  5,
  'Tôi là thợ sửa...',
  0.00,
  0,
  0,
  150000,
  50000,
  NOW(),
  NOW()
);

-- technician_profiles.id = 1 (auto-generated)
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

**Current State:**
```
✅ TechnicianProfile Created
❌ Cannot receive orders yet
❌ Not visible in public list
⏳ Waiting for KYC verification
```

---

## BƯỚC 3: User Upload KYC Documents 📋

```
User → Upload documents (ID, portrait, certificate, selfie) → Submit
(External flow - handled by KYC system or separate module)
```

**Documents:**
```
- ID front & back
- Portrait photo
- Professional certificate
- Selfie with document
```

**Database State (after upload):**
```
verifications.kyc_status = PENDING
verification_documents:
  - id_front: url1
  - id_back: url2
  - portrait: url3
  - certificate: url4
  - selfie: url5
```

---

## BƯỚC 4: Admin Review & Approve KYC ✅

```
Admin Dashboard → Review technician → Approve
(Either approve or reject)
```

**Admin Action (Backend - TODO endpoint):**
```
PATCH /api/admin/verifications/{verificationId}/approve
{
  "status": "APPROVED"
}
```

**Database Changes:**
```
UPDATE technician_profiles 
SET kyc_status = 'APPROVED'
WHERE user_id = 5;

UPDATE verifications
SET status = 'APPROVED'
WHERE technician_id = 5;
```

**Email Notification (sent to user):**
```
Subject: Xác minh danh tính thành công! ✅

Chúc mừng! Hồ sơ của bạn đã được xác minh.
Bây giờ bạn có thể:
1. Bật trạng thái "Sẵn sàng nhận đơn"
2. Xuất hiện trong danh sách thợ công khai
3. Nhận các đơn hàng từ khách hàng
```

**Current State:**
```
✅ KYC APPROVED
✅ technician_status = ACTIVE
✅ Can now toggle availability
⏳ But not yet in public list (need to enable availability)
```

---

## BƯỚC 5: User Enable Availability 🟢

```
User → Open app → Settings → Toggle "Sẵn sàng nhận đơn" → ON
```

**API:**
```
PATCH /api/technicians/1/availability
Headers: Authorization: Bearer <JWT>
Content-Type: application/json

{
  "available": true
}
```

**Validation Checks:**
```
✓ user is owner (user_id = 5)
✓ kycStatus == APPROVED
✓ technicianStatus == ACTIVE
✓ user.deleted == false
→ All pass → Update allowed
```

**Database Changes:**
```
UPDATE technician_profiles
SET available = true, updated_at = NOW()
WHERE user_id = 5;
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

**Email Notification:**
```
Subject: Bạn đã sẵn sàng nhận đơn! 🎉

Chúc mừng! Bây giờ bạn đã xuất hiện trong danh sách thợ công khai.
Khách hàng có thể tìm thấy và đặt dịch vụ của bạn.
```

**Current State:**
```
✅ available = true
✅ kycStatus = APPROVED
✅ technicianStatus = ACTIVE
✅ NOW VISIBLE IN PUBLIC LIST ⭐
✅ READY TO RECEIVE BOOKINGS ⭐
```

---

## BƯỚC 6: Now Technician Appears in Public List 👥

```
Any Customer → Open app → Search for Technician
```

**API:**
```
GET /api/technicians?available=true&page=0&size=10

Query Validation:
- Filter: available = true
- Sort: by rating (descending)
- Only show: technicianStatus = ACTIVE AND kycStatus = APPROVED
```

**Database Query:**
```sql
SELECT tp.*, u.full_name, u.avatar
FROM technician_profiles tp
JOIN users u ON tp.user_id = u.id
WHERE tp.technician_status = 'ACTIVE'
  AND tp.kyc_status = 'APPROVED'
  AND tp.available = true
  AND u.deleted = false
ORDER BY tp.rating_average DESC
LIMIT 10 OFFSET 0;
```

**Response:**
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
        "bio": "Tôi là thợ sửa điện lạnh có kinh nghiệm 5 năm...",
        "services": ["Sửa máy lạnh", "Bảo dưỡng máy lạnh", "Sửa tủ lạnh"],
        "ratingAverage": 0.0,
        "reviewCount": 0,
        "completedJobCount": 0,
        "basePrice": 150000,
        "hourlyRate": 50000,
        "available": true,
        "workingAreas": ["HN:Hà Đông", "HN:Ba Đình", "HN:Tây Hồ"]
      }
    ],
    "pagination": {
      "page": 0,
      "limit": 10,
      "total": 1,
      "totalPages": 1
    }
  }
}
```

**Current State:**
```
✅ VISIBLE TO ALL CUSTOMERS
✅ READY TO RECEIVE BOOKINGS
✅ NEW TECHNICIAN IN SYSTEM
```

---

## BƯỚC 7: Customer Books This Technician 📞

```
Customer → View technician detail → Click "Book Now" → Select time → Confirm
```

**Customer Views Detail:**
```
GET /api/technicians/1

Response:
{
  "technicianId": 1,
  "userId": 5,
  "fullName": "Nguyễn Văn A",
  "email": "nguyenvan@example.com",
  "phone": "0912345678",
  "avatarUrl": "...",
  "bio": "...",
  "experienceYears": 5,
  "ratingAverage": 0.0,
  "reviewCount": 0,
  "completedJobCount": 0,
  "basePrice": 150000,
  "hourlyRate": 50000,
  "available": true,
  "technicianStatus": "ACTIVE",
  "kycStatus": "APPROVED",
  "createdAt": "2024-02-15T10:30:00Z"
}
```

**Customer Books (existing OrderService):**
```
POST /api/orders
{
  "technicianId": 5,
  "serviceName": "Sửa máy lạnh",
  "description": "Máy lạnh không lạnh, cần sửa chữa",
  "address": "123 Nguyen Hue, Ha Dong, Hanoi",
  "expectedAt": "2024-02-16T14:00:00Z",
  "paymentMethod": "WALLET"
}

Response: New Order Created with status = "NEW"
```

**Notification to Technician:**
```
Title: Bạn có đơn hàng mới! 🔔
Body: Khách hàng Trần Thị B yêu cầu sửa máy lạnh
Action: Accept/Reject booking
```

---

## BƯỚC 8: Technician Completes Job & Gets Review ⭐

```
Technician → Accept booking → Arrive at location → Fix → Mark as completed
Customer → View completed order → Leave review
```

**Order Completed:**
```
Status: "COMPLETED"
```

**Customer Leaves Review:**
```
POST /api/reviews
{
  "orderId": 100,
  "rating": 5,
  "comment": "Thợ rất nhiệt tình, chuyên nghiệp. Máy lạnh chạy tốt lắm!",
  "images": ["url1", "url2"]
}

Response: Review created
```

**Database Update (auto-calculated or via trigger):**
```
UPDATE technician_profiles
SET review_count = 1,
    rating_average = 5.0,
    completed_job_count = 1
WHERE user_id = 5;
```

**Now Technician Profile:**
```
GET /api/technicians/1/reviews
→ Shows 1 review with 5 stars

GET /api/technicians/1
→ ratingAverage: 5.0
→ reviewCount: 1
→ completedJobCount: 1
```

---

## BƯỚC 9: Technician Profile Updates Over Time 📈

```
As more bookings & reviews accumulate...
```

**After 10 bookings with avg 4.8 rating:**
```
GET /api/technicians?sortBy=ratingAverage&page=0
→ Technician moves up in ranking!
```

**Technician Can Update Profile Anytime:**
```
PATCH /api/technicians/1/profile
{
  "bio": "5 năm kinh nghiệm, 10 công việc thành công, đánh giá 4.8/5",
  "basePrice": 180000,
  "experienceYears": 6
}
```

---

## BƯỚC 10: Technician Management 🎛️

### Can Disable Availability (e.g., on vacation):
```
PATCH /api/technicians/1/availability
{ "available": false }

Result:
- Not in public list anymore (for available = true filter)
- Can still be viewed by customers
- Cannot receive new bookings
- Previous bookings still active
```

### View Own Reviews:
```
GET /api/technicians/1/reviews?page=0&size=10

Shows all customer reviews with ratings & comments
```

### View Own Schedule:
```
GET /api/technicians/1/schedule?fromDate=2024-03-01&toDate=2024-03-31

Shows booked time slots + free time (for owner)
Public users see only busy/free status
```

---

## COMPLETE STATE MACHINE

```
┌─────────────────────────────────────────────────────────┐
│ 1. REGISTRATION                                         │
│    POST /api/technicians/register                       │
│    → TechnicianProfile created                          │
│    Status: PENDING_KYC, KYC: PENDING, Available: false  │
└─────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│ 2. KYC UPLOAD (external)                                │
│    User uploads identity documents                      │
│    → verifications.status = PENDING                     │
└─────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│ 3. KYC APPROVAL (Admin)                                 │
│    Admin approves KYC                                   │
│    → technician_profiles.kyc_status = APPROVED          │
│    → technician_profiles.status = ACTIVE                │
│    Notification: "KYC approved"                         │
└─────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│ 4. ENABLE AVAILABILITY                                  │
│    PATCH /api/technicians/1/availability                │
│    → available = true                                   │
│    Notification: "You're now in public list"            │
└─────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│ 5. PUBLIC (Ready for bookings)                          │
│    GET /api/technicians → Shows in list                 │
│    Customers can book → Order created                   │
│    → order.status = NEW                                 │
└─────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│ 6. WORKING (Booking accepted)                           │
│    Technician accepts order                             │
│    → order.status = ACCEPTED/IN_PROGRESS                │
│    → order.technician_id = 5                            │
└─────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│ 7. COMPLETED                                            │
│    Job finished                                         │
│    → order.status = COMPLETED                           │
│    → payment processed                                  │
└─────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│ 8. REVIEWED                                             │
│    Customer leaves review                               │
│    → reviews created                                    │
│    → rating_average updated                             │
│    → review_count updated                               │
│    → completed_job_count updated                        │
└─────────────────────────────────────────────────────────┘
```

---

## KEY STATISTICS AFTER 1 MONTH

| Metric | Initial | After 1 Month |
|--------|---------|---------------|
| technicianStatus | PENDING_KYC | ACTIVE |
| kycStatus | PENDING | APPROVED |
| available | false | true |
| ratingAverage | 0.0 | 4.7 |
| reviewCount | 0 | 12 |
| completedJobCount | 0 | 15 |
| visible_in_list | ❌ | ✅ |
| can_receive_bookings | ❌ | ✅ |

---

## ERROR SCENARIOS

### Scenario: Technician tries to enable availability before KYC approved
```
PATCH /api/technicians/1/availability
{ "available": true }

Response (400 Bad Request):
{
  "success": false,
  "error": {
    "code": "KYC_NOT_APPROVED",
    "message": "Technician account is not verified yet"
  }
}
```

### Scenario: Technician tries to view another's private profile
```
GET /api/technicians/2

But technician 2 is PENDING_KYC (not yet approved)

Response (403 Forbidden):
{
  "success": false,
  "error": {
    "code": "UNAUTHORIZED",
    "message": "Cannot view this technician profile"
  }
}
```

### Scenario: Non-owner tries to update someone's profile
```
PATCH /api/technicians/1/profile
Authorization: Bearer <user_2_token>
{ "bio": "Hacked!" }

Response (403 Forbidden):
{
  "success": false,
  "error": {
    "code": "UNAUTHORIZED",
    "message": "You can only update your own profile"
  }
}
```

---

## NEXT STEPS FOR ADMIN/SYSTEM

- [ ] Implement KYC approval endpoint
- [ ] Add technician suspension endpoint
- [ ] Add technician rejection endpoint
- [ ] Implement rating calculation trigger
- [ ] Implement review count sync
- [ ] Implement completed job count sync
- [ ] Link service_skills table to show services
- [ ] Link service_areas table to show working areas
- [ ] Setup KYC document storage
- [ ] Setup email notifications

---

**End of Complete Flow Walkthrough** ✅

For full API documentation, see **TECHNICIAN_API_GUIDE.md**
