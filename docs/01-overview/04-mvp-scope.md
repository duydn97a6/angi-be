# 04. MVP Scope - Phạm vi MVP

## 🎯 Nguyên tắc scoping

> "Không phải làm ít đi, mà là chỉ làm những gì thực sự quan trọng"

### Rules cho MVP
1. **Feature nào không ảnh hưởng đến retention → cắt**
2. **Feature nào tốn > 1 tuần code → cân nhắc lại**
3. **"Nice to have" = "Not for MVP"**
4. **Luôn tự hỏi: "Nếu không có feature này, user có rời đi không?"**

---

## ✅ IN-SCOPE cho MVP (Phase 1)

### 1. Authentication (P0)
- [x] Đăng ký bằng email/password
- [x] Đăng nhập Google OAuth
- [x] JWT tokens (access + refresh)
- [x] Forgot password

### 2. Onboarding (P0)
5 màn hình tuần tự (60 giây hoàn thành):
- [x] **Bước 1**: Chọn vùng miền (Bắc/Trung/Nam)
- [x] **Bước 2**: Chọn văn phòng trên map + radius tìm kiếm (500m/1km/2km)
- [x] **Bước 3**: Món không ăn (multi-select chips)
- [x] **Bước 4**: Ngân sách/bữa (slider)
- [x] **Bước 5**: Chế độ ăn (Bình thường / Ăn chay / Eat healthy)

### 3. Home Screen - Core Feature (P0)
- [x] Weather + location widget (tự động)
- [x] 3 gợi ý cards (An toàn / Quen thuộc / Khám phá)
- [x] Nút "Gợi ý khác" (refresh)
- [x] Nút "Không muốn ăn..." (anti-filter)
- [x] Click card → deep link sang GrabFood/ShopeeFood
- [x] Explanation tooltip ("Tại sao AI gợi ý món này?")

### 4. Recommendation Engine v1 (P0)
- [x] Context aggregation:
  - User profile (preferences, history)
  - Weather (via OpenWeatherMap)
  - Time of day (morning/lunch/dinner)
  - Location (user's current GPS)
- [x] LLM-based recommendation (Claude API hoặc OpenAI)
- [x] Rule-based fallback khi LLM fail
- [x] Cache 15 phút/user (Redis)

### 5. Restaurant Database (P0)
- [x] Manual curate 500+ quán (HCM Q1, Q3, Q7 + HN Cầu Giấy, Đống Đa)
- [x] Metadata: name, address, lat/lng, cuisine type, avg price, delivery partners
- [x] CRUD admin endpoints (nội bộ)

### 6. Feedback Loop (P0)
- [x] Push notification sau 2h hỏi feedback
- [x] Modal 3 emoji: 😕 Tệ / 😐 Ổn / 😋 Tuyệt
- [x] Regret score: Có / Hơi / Không
- [x] Optional tags: Ngon / Rẻ / Nhanh / Gần / Dơ / Đắt...
- [x] Lưu vào user_meal_history

### 7. History Page (P1)
- [x] List các bữa ăn đã feedback
- [x] Stats đơn giản: món ăn nhiều nhất tuần, chi tiêu trung bình

### 8. Settings (P1)
- [x] Chỉnh sửa profile
- [x] Chỉnh sửa onboarding preferences
- [x] Delete account
- [x] Logout

---

## ❌ OUT-OF-SCOPE cho MVP

### Bỏ lại Phase 2 (Group features)
- ❌ Tạo team/nhóm
- ❌ Lunch poll cho nhóm
- ❌ Veto system
- ❌ Fairness tracker
- ❌ Team history
- ❌ Chat trong nhóm

### Bỏ lại Phase 3 (Smart AI)
- ❌ Mood detection
- ❌ Calendar integration
- ❌ Health-aware deep recommendations
- ❌ Voice input
- ❌ Photo recognition

### Không bao giờ làm (ít nhất năm đầu)
- ❌ Payment/Checkout trong app
- ❌ Tự giao hàng
- ❌ Social feed (like IG)
- ❌ Live chat với restaurant
- ❌ Reservations
- ❌ Loyalty program

---

## 📱 Platform priority

### MVP: Web app trước
**Tại sao?**
- Deploy nhanh hơn (1 codebase Next.js)
- Dễ iterate (không qua App Store review)
- Test được với nhiều người hơn (không cần cài)
- SEO-friendly cho growth organic

### Phase 2+: Mobile app (Flutter)
- Sau khi product-market fit
- Khi đã có user base muốn trải nghiệm tốt hơn
- Push notifications native
- Location always-on

---

## 🎨 Design constraints

### Web (MVP)
- **Responsive**: Mobile-first (375px) → tablet (768px) → desktop (1024px+)
- **PWA**: Có thể "Add to home screen"
- **Dark mode**: KHÔNG có trong MVP (để giảm scope)

### Visual
- Tối giản, flat design
- Không dùng effect glossy/shadow phức tạp
- Primary color: ấm áp (coral/amber) - tạo cảm giác đói
- Font: Inter (web) / system font (app)

---

## 🧪 MVP Testing Strategy

### Internal testing (Tuần 7)
- 10 người nội bộ dùng trong 3 ngày
- Feedback qua form Google

### Beta testing (Tuần 8)
- 100 người (tuyển từ landing page)
- Feedback qua Typeform hàng tuần
- 1 cuộc interview sâu với 5 power users

### Soft launch (Tháng 3)
- Public launch trên Product Hunt (Vietnam)
- Post trên Facebook Group (Tôi là dân văn phòng, J2Team...)
- LinkedIn post

---

## 📏 Definition of Done cho MVP

MVP được coi là XONG khi:

### Code quality
- [x] Tất cả endpoints có test (coverage > 70%)
- [x] API documentation đầy đủ (Swagger)
- [x] Error handling mọi edge case
- [x] Logging structured (JSON)
- [x] Monitoring cơ bản (uptime, error rate)

### Product quality
- [x] Onboarding hoàn thành trong < 90 giây
- [x] Home screen load < 2 giây (có cache)
- [x] Recommendation generation < 5 giây
- [x] Mobile-responsive hoàn hảo
- [x] Không có critical bug

### Analytics
- [x] Track: signup, onboarding_complete, recommendation_view, card_click, feedback_submit
- [x] Funnel dashboard
- [x] Retention cohort analysis

### Go-to-market
- [x] Landing page với feature overview
- [x] Privacy Policy + Terms of Service
- [x] Support email hoạt động
- [x] Onboarding email series (5 emails)

---

## 🚨 Risk & Mitigation

| Risk | Impact | Mitigation |
|------|--------|-----------|
| LLM cost quá cao | 🔴 High | Cache aggressive 15 phút, fallback rule-based |
| Database quán ăn không đủ chất | 🔴 High | Manual curate + crawl + partnership Foody |
| Retention thấp | 🔴 High | Obsess quality recommendation 1000 user đầu |
| Spam signup | 🟡 Medium | reCAPTCHA + email verification |
| Copy từ competitors | 🟡 Medium | Speed + brand strong |
| Privacy concerns (location) | 🟡 Medium | Transparent data usage, opt-in rõ ràng |
