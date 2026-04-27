# 03. API Specification

## 🌐 API Overview

- **Base URL**: `https://api.angi.vn/api/v1` (production) | `http://localhost:8080/api/v1` (dev)
- **Format**: REST + JSON
- **Auth**: Bearer JWT token trong header `Authorization: Bearer {token}`
- **Documentation**: Swagger UI tại `/swagger-ui.html`

## 📋 Conventions

### Response format

**Success**:
```json
{
  "success": true,
  "data": { ... },
  "meta": { "timestamp": "2026-04-23T10:00:00Z" }
}
```

**Error**:
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Email không hợp lệ",
    "details": { "field": "email" }
  }
}
```

### Error codes
- `400 BAD_REQUEST`: Validation error
- `401 UNAUTHORIZED`: Missing/invalid token
- `403 FORBIDDEN`: Insufficient permissions
- `404 NOT_FOUND`: Resource not found
- `409 CONFLICT`: Duplicate resource
- `429 TOO_MANY_REQUESTS`: Rate limit exceeded
- `500 INTERNAL_ERROR`: Server error
- `503 SERVICE_UNAVAILABLE`: External service down

### Pagination
```
GET /endpoint?page=0&size=20&sort=createdAt,desc
```

Response:
```json
{
  "data": [...],
  "pagination": {
    "page": 0,
    "size": 20,
    "totalElements": 150,
    "totalPages": 8
  }
}
```

---

## 🔐 Authentication Endpoints

### POST /auth/register
Tạo tài khoản mới.

**Request**:
```json
{
  "email": "minh@example.com",
  "password": "SecurePass123!",
  "name": "Nguyễn Văn Minh"
}
```

**Response 201**:
```json
{
  "success": true,
  "data": {
    "user": {
      "id": "uuid",
      "email": "minh@example.com",
      "name": "Nguyễn Văn Minh"
    },
    "tokens": {
      "accessToken": "eyJhbGc...",
      "refreshToken": "eyJhbGc...",
      "expiresIn": 900
    }
  }
}
```

### POST /auth/login
Đăng nhập.

**Request**:
```json
{
  "email": "minh@example.com",
  "password": "SecurePass123!"
}
```

**Response 200**: Same as register.

### POST /auth/google
Đăng nhập qua Google OAuth.

**Request**:
```json
{
  "idToken": "google-id-token-from-client"
}
```

**Response 200**: Same as login.

### POST /auth/refresh
Refresh access token.

**Request**:
```json
{
  "refreshToken": "eyJhbGc..."
}
```

**Response 200**:
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGc...",
    "expiresIn": 900
  }
}
```

### POST /auth/logout
Đăng xuất (revoke refresh token).

**Auth**: Required

**Response 200**: `{ "success": true }`

### POST /auth/forgot-password
Gửi email reset password.

**Request**: `{ "email": "minh@example.com" }`

### POST /auth/reset-password
Reset password với token từ email.

**Request**:
```json
{
  "token": "reset-token",
  "newPassword": "NewPass123!"
}
```

---

## 👤 User Endpoints

### GET /users/me
Lấy thông tin user hiện tại.

**Auth**: Required

**Response 200**:
```json
{
  "data": {
    "id": "uuid",
    "email": "minh@example.com",
    "name": "Nguyễn Văn Minh",
    "avatarUrl": "https://...",
    "preferences": {
      "region": "south",
      "dietType": "normal",
      "excludedFoods": ["seafood", "spicy"],
      "budgetMin": 30000,
      "budgetMax": 80000,
      "officeLocation": {
        "lat": 10.7769,
        "lng": 106.7009,
        "address": "Quận 1, TP.HCM"
      },
      "searchRadiusMeters": 1000
    }
  }
}
```

### PATCH /users/me
Cập nhật thông tin user.

**Request**:
```json
{
  "name": "Minh Nguyễn",
  "avatarUrl": "https://..."
}
```

### PUT /users/me/preferences
Cập nhật preferences.

**Request**:
```json
{
  "region": "south",
  "officeLat": 10.7769,
  "officeLng": 106.7009,
  "officeAddress": "Quận 1, TP.HCM",
  "searchRadiusMeters": 1000,
  "dietType": "normal",
  "excludedFoods": ["seafood", "peanut"],
  "budgetMin": 30000,
  "budgetMax": 80000,
  "favoriteCuisines": ["vietnamese", "korean"]
}
```

### POST /users/me/onboarding/complete
Đánh dấu onboarding hoàn thành.

**Request**: Full preferences object.

### DELETE /users/me
Xóa tài khoản (soft delete).

---

## 🍜 Recommendation Endpoints (CORE)

### GET /recommendations
**Endpoint quan trọng nhất** - Lấy 3 gợi ý cho user.

**Auth**: Required

**Query params**:
- `lat` (required): Current latitude
- `lng` (required): Current longitude
- `mealType` (optional): `breakfast`, `lunch`, `dinner`. Default: auto-detect by time
- `excludeRestaurantIds` (optional): Comma-separated list to exclude
- `forceRefresh` (optional): `true` to bypass cache

**Response 200**:
```json
{
  "data": {
    "recommendationId": "uuid",
    "context": {
      "weather": {
        "temp": 32,
        "condition": "sunny",
        "humidity": 75
      },
      "location": {
        "lat": 10.7769,
        "lng": 106.7009,
        "district": "Quận 1"
      },
      "time": "2026-04-23T11:45:00+07:00",
      "mealType": "lunch"
    },
    "recommendations": [
      {
        "category": "safe",
        "restaurant": {
          "id": "uuid",
          "name": "Bún chả Hương Liên",
          "cuisine": "vietnamese",
          "avgPrice": 45000,
          "distance": 350,
          "rating": 4.6,
          "imageUrl": "https://...",
          "deliveryLinks": {
            "grabfood": "https://grab.onelink.me/...",
            "shopeefood": "https://shopee.onelink.me/..."
          }
        },
        "dish": {
          "id": "uuid",
          "name": "Bún chả truyền thống",
          "price": 45000,
          "imageUrl": "https://..."
        },
        "explanation": "Bạn đã ăn 3 lần, đánh giá rất tích cực",
        "estimatedDeliveryMinutes": 15
      },
      {
        "category": "familiar",
        "restaurant": { ... },
        "explanation": "Phù hợp thời tiết nóng, đủ đạm cho chiều làm việc",
        "isTopPick": true
      },
      {
        "category": "discovery",
        "restaurant": { ... },
        "explanation": "Món mới, nhẹ bụng, hợp người giống bạn"
      }
    ],
    "generationMethod": "llm_claude",
    "generationTimeMs": 1850
  }
}
```

### POST /recommendations/{id}/click
Track khi user click vào 1 recommendation.

**Auth**: Required

**Request**:
```json
{
  "recommendationIndex": 1,
  "restaurantId": "uuid",
  "deliveryPartner": "grabfood"
}
```

### POST /recommendations/anti
Lấy recommendations với loại trừ.

**Request**:
```json
{
  "lat": 10.7769,
  "lng": 106.7009,
  "excludeCuisines": ["noodle", "rice"],
  "excludeFeelings": ["spicy", "oily"]
}
```

---

## 🏪 Restaurant Endpoints

### GET /restaurants
Search restaurants.

**Query params**:
- `lat`, `lng`: Location (required for radius search)
- `radius`: Meters (default 1000)
- `cuisine`: Filter by cuisine
- `minPrice`, `maxPrice`: Price range
- `search`: Text search
- `page`, `size`: Pagination

**Response 200**:
```json
{
  "data": [ { restaurant objects } ],
  "pagination": { ... }
}
```

### GET /restaurants/{id}
Chi tiết 1 restaurant.

**Response 200**:
```json
{
  "data": {
    "id": "uuid",
    "name": "Bún chả Hương Liên",
    "description": "...",
    "address": "...",
    "location": { "lat": ..., "lng": ... },
    "phone": "...",
    "openingHours": { "mon": "08:00-22:00", ... },
    "cuisine": "vietnamese",
    "priceRange": "budget",
    "avgPrice": 45000,
    "rating": 4.6,
    "totalReviews": 234,
    "images": ["url1", "url2"],
    "dishes": [
      { "id": "uuid", "name": "Bún chả", "price": 45000, ... }
    ],
    "deliveryLinks": { ... }
  }
}
```

### GET /restaurants/{id}/dishes
List dishes của restaurant.

---

## 📝 Feedback Endpoints

### POST /feedback
Submit feedback cho bữa ăn.

**Auth**: Required

**Request**:
```json
{
  "recommendationId": "uuid",
  "restaurantId": "uuid",
  "dishId": "uuid",
  "emoji": "happy",  // 'sad', 'neutral', 'happy'
  "regretLevel": "none",  // 'none', 'slight', 'high'
  "tags": ["delicious", "fast"],
  "notes": "Quán sạch sẽ, phục vụ nhanh"
}
```

**Response 201**:
```json
{
  "data": {
    "id": "uuid",
    "message": "Cảm ơn feedback! AI sẽ học thêm để gợi ý chính xác hơn."
  }
}
```

### GET /feedback/pending
Lấy các meals chưa feedback (cho notification).

---

## 📜 Meal History Endpoints

### GET /meals/history
Lịch sử bữa ăn của user.

**Query params**:
- `from`, `to`: Date range
- `hasFeedback`: Filter by feedback status
- `page`, `size`

**Response 200**:
```json
{
  "data": [
    {
      "id": "uuid",
      "restaurant": { ... },
      "dish": { ... },
      "mealAt": "2026-04-23T12:30:00Z",
      "pricePaid": 45000,
      "feedback": {
        "emoji": "happy",
        "regretLevel": "none",
        "tags": ["delicious", "fast"]
      }
    }
  ]
}
```

### GET /meals/stats
Stats của user.

**Query params**:
- `period`: `week`, `month`, `year`

**Response 200**:
```json
{
  "data": {
    "totalMeals": 45,
    "totalSpent": 2100000,
    "avgRating": 4.2,
    "topCuisines": [
      { "cuisine": "vietnamese", "count": 25 },
      { "cuisine": "korean", "count": 10 }
    ],
    "topDishes": [
      { "name": "Phở bò", "count": 8 }
    ],
    "healthPattern": {
      "oilyFoodPercentage": 60,
      "warning": "Nhiều dầu mỡ"
    }
  }
}
```

---

## 👥 Group Endpoints (Phase 2)

### POST /groups
Tạo team mới.

**Request**:
```json
{
  "name": "Team Dev Backend",
  "description": "Team backend công ty XYZ",
  "autoLunchPoll": true
}
```

**Response 201**:
```json
{
  "data": {
    "id": "uuid",
    "name": "Team Dev Backend",
    "inviteCode": "BACKEND2026",
    "inviteUrl": "https://angi.vn/join/BACKEND2026"
  }
}
```

### POST /groups/join
Tham gia team qua invite code.

**Request**: `{ "inviteCode": "BACKEND2026" }`

### GET /groups
List groups của user.

### GET /groups/{id}
Chi tiết group.

### GET /groups/{id}/members
List members của group.

### POST /groups/{id}/polls
Tạo lunch poll.

**Request**:
```json
{
  "title": "Trưa thứ 5",
  "mealType": "lunch",
  "closeInMinutes": 30
}
```

**Response 201**:
```json
{
  "data": {
    "id": "uuid",
    "candidateRestaurants": [
      { "restaurantId": "...", "aiReason": "..." }
    ],
    "closesAt": "..."
  }
}
```

### POST /polls/{id}/vote
Vote cho restaurant.

**Request**:
```json
{
  "restaurantId": "uuid",
  "voteType": "up"  // or "veto"
  "vetoReason": "allergy",  // required if veto
  "notes": "Tôi dị ứng hải sản"
}
```

### POST /polls/{id}/close
Team lead đóng poll sớm.

### GET /polls/{id}
Trạng thái hiện tại của poll + kết quả.

---

## 📊 Analytics Endpoints (Admin)

### POST /analytics/events
Track custom event từ frontend.

**Request**:
```json
{
  "eventName": "recommendation_card_viewed",
  "properties": {
    "recommendationId": "uuid",
    "position": 1,
    "category": "familiar"
  }
}
```

---

## 🌤 Context Endpoints (Internal)

### GET /context/weather
Lấy thời tiết (cached).

**Query**: `lat`, `lng`

**Response 200**:
```json
{
  "data": {
    "temp": 32,
    "feelsLike": 35,
    "humidity": 75,
    "condition": "sunny",
    "description": "Trời nắng nóng",
    "recommendation": "Nên chọn món mát, nhiều nước"
  }
}
```

---

## 🚦 Rate Limits

| Endpoint group | Limit |
|---------------|-------|
| `/auth/*` | 10 req/min/IP |
| `/recommendations` | 30 req/min/user |
| `/feedback` | 20 req/min/user |
| Other authenticated | 100 req/min/user |
| Public (GET restaurants) | 60 req/min/IP |

Rate limit response:
```json
{
  "error": {
    "code": "TOO_MANY_REQUESTS",
    "message": "Quá nhiều request. Thử lại sau 30 giây.",
    "retryAfterSeconds": 30
  }
}
```

Header: `X-RateLimit-Remaining: 5`, `X-RateLimit-Reset: 1714567890`

---

## 🔒 Security headers

Mọi response có:
- `X-Content-Type-Options: nosniff`
- `X-Frame-Options: DENY`
- `Strict-Transport-Security: max-age=31536000`
- `Content-Security-Policy: default-src 'self'`

---

## 📖 Swagger/OpenAPI

Full API docs tại runtime: `{base_url}/swagger-ui.html`

OpenAPI JSON: `{base_url}/v3/api-docs`
