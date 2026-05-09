# Notification API Documentation

## Overview

Notification module cung cap hop thu thong bao trong ung dung cho user da dang nhap.

- Base URL: `http://localhost:8080/api/notifications`
- Authentication: `Authorization: Bearer <access_token>`
- Sort: moi nhat truoc (`createdAt DESC`)

## NotificationType

- `order_accepted`
- `price_adjustment`
- `order_completed`
- `payment_success`
- `payment_failed`
- `withdraw_success`
- `withdraw_failed`
- `system`
- `promotion`
- `chat_message`

## 1) Get My Notifications

`GET /api/notifications?page=1&limit=20`

### Response

```json
{
  "success": true,
  "data": {
    "unreadCount": 3,
    "items": [
      {
        "id": "NOTIF-20260507-0001",
        "type": "order_accepted",
        "title": "Don hang duoc nhan",
        "body": "Tho Nguyen Van Minh da nhan don GU-99210",
        "data": {
          "orderId": "GU-99210"
        },
        "isRead": false,
        "createdAt": "2026-05-07T10:00:00"
      }
    ],
    "pagination": {
      "page": 1,
      "limit": 20,
      "total": 15,
      "totalPages": 1
    }
  }
}
```

## 2) Mark Single Notification As Read

`PATCH /api/notifications/{id}/read`

### Response

```json
{
  "success": true,
  "data": {
    "id": "NOTIF-20260507-0001",
    "isRead": true
  }
}
```

Notes:
- Chi owner moi update duoc
- Neu da read roi thi van tra ve thanh cong

## 3) Mark All Unread As Read

`PATCH /api/notifications/read-all`

### Response

```json
{
  "success": true,
  "data": {
    "updatedCount": 3
  }
}
```

## Internal Reusable Service Example

```java
notificationService.createNotification(
    user,
    NotificationType.ORDER_ACCEPTED,
    "Don hang duoc nhan",
    "Tho Nguyen Van Minh da nhan don GU-99210",
    Map.of("orderId", "GU-99210")
);
```

