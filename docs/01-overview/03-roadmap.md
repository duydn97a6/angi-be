# 03. Roadmap - Lộ trình 6 tháng đầu

## 🎯 Nguyên tắc lập kế hoạch

1. **Mỗi phase 2 tháng, 1 mục tiêu duy nhất** - Không phân tán
2. **Ship nhanh, học nhanh** - Không cầu toàn
3. **Data-driven** - Mỗi phase có metrics rõ ràng để quyết định continue/pivot
4. **User obsession** - Phỏng vấn user mỗi 2 tuần

---

## 📅 Phase 0: Pre-development (Tuần 1-2)

### Mục tiêu: Validate trước khi viết code

### Deliverables
- [ ] Landing page với form đăng ký email
- [ ] Chạy FB Ads 1-2 triệu targeting dân văn phòng HCM/HN
- [ ] Phỏng vấn 20 users thật (không phải bạn bè)
- [ ] Prototype Figma cho 5 màn hình chính
- [ ] Database quán ăn seed data (100 quán ở 1 quận)

### Success criteria
- CTR landing page > 1.5%
- Email signup > 100 người trong 1 tuần
- ≥ 15/20 người phỏng vấn xác nhận có pain point
- ≥ 10/20 sẵn sàng dùng thử app

### Tools
- Landing page: Framer hoặc Next.js + Vercel (free)
- Analytics: PostHog (free tier)
- Prototype: Figma
- Interview: Google Meet + Otter.ai

---

## 🚀 Phase 1: Solo Mode MVP (Tháng 1-2)

### Mục tiêu DUY NHẤT: Chứng minh cá nhân dùng hàng ngày

### Features (MVP)

#### Phải có (P0)
- [ ] **Authentication**: Email/password + Google OAuth
- [ ] **Onboarding 60 giây**: 5 câu hỏi cốt lõi
  - Vùng miền (Bắc/Trung/Nam)
  - Món không ăn (multi-select: hải sản, thịt bò, đồ cay...)
  - Ngân sách trung bình/bữa (slider 20k-200k)
  - Ăn chay? (Không / Thi thoảng / Trường kỳ)
  - Văn phòng ở đâu (chọn địa điểm trên map)
- [ ] **Home screen**: 3 gợi ý tức thì (An toàn / Quen thuộc / Khám phá)
- [ ] **Context detection**:
  - Lấy GPS hiện tại
  - Lấy thời tiết qua OpenWeatherMap API
  - Detect giờ (sáng/trưa/tối)
- [ ] **Recommendation engine v1**: Rule-based + LLM
- [ ] **Affiliate link**: Deep link sang GrabFood/ShopeeFood
- [ ] **Feedback loop**: Notification sau 2 tiếng hỏi "Bữa vừa rồi thế nào?"
- [ ] **Database quán ăn**: 500+ quán ở HCM + HN (manual curate)

#### Không làm (cố ý bỏ để ship nhanh)
- ❌ Group mode
- ❌ Health tracking phức tạp
- ❌ Calendar integration
- ❌ Payment
- ❌ Chat
- ❌ Social features

### Tech milestones

**Tuần 1-2**: Backend setup
- Spring Boot project structure
- PostgreSQL schema
- Authentication (JWT)
- Basic CRUD APIs

**Tuần 3-4**: Recommendation engine
- LLM integration (Claude API hoặc OpenAI)
- Context aggregation (weather, time, user history)
- Caching với Redis
- Rule-based fallback

**Tuần 5-6**: Web frontend
- Next.js app setup
- Onboarding flow
- Home screen với 3 cards
- Feedback modal

**Tuần 7-8**: Polish & Launch
- Error handling
- Analytics integration
- Bug fixes
- Soft launch 100 người đầu

### Success metrics (cuối tháng 2)
- **Users**: 500-1,000 signups
- **D1 retention**: > 30%
- **D7 retention**: > 20%
- **Daily usage**: 3-5 lần/tuần/active user
- **Feedback rate**: > 40% (tỷ lệ user trả lời feedback sau bữa ăn)

---

## 🌱 Phase 2: Group Mode + Viral (Tháng 3-4)

### Mục tiêu DUY NHẤT: Mỗi user thành 1 người giới thiệu

### Features

#### Phải có (P0)
- [ ] **Create team**: Invite qua link/QR code
- [ ] **Lunch Poll**:
  - Tạo poll cho nhóm
  - AI gợi ý 5 món phù hợp với cả nhóm
  - Vote up/down
  - Veto system (mỗi người 1 veto/ngày)
  - Timer đóng poll (30 phút)
- [ ] **Smart filtering cho nhóm**:
  - Tự loại món có người dị ứng
  - Tự loại món out of budget thấp nhất
  - Tự loại món chay không phù hợp
- [ ] **Fairness tracker**: "Tuần này đã chiều A 2 lần, hôm nay ưu tiên B"
- [ ] **Team history**: Nhật ký team lunch
- [ ] **Referral rewards**: Invite 3 người → unlock premium features

#### Nice to have (P1)
- [ ] **Team stats**: "Món team thích nhất tháng"
- [ ] **Lunch reminder**: Thông báo 11h hàng ngày cho team lead
- [ ] **Split bill preview** (không thanh toán, chỉ preview)

### Success metrics (cuối tháng 4)
- **Users**: 10,000 MAU
- **Viral coefficient (K)**: > 1.2
- **Teams created**: > 500
- **Avg team size**: > 4 người
- **Team lunch frequency**: > 2 lần/tuần/team

### Marketing plan
- Content marketing trên TikTok/Facebook ("Team tôi ăn trưa gì")
- Partnership với HR communities
- Referral program: team nào dùng nhiều nhất tặng buffet

---

## 🧠 Phase 3: Smart AI (Tháng 5-6)

### Mục tiêu DUY NHẤT: AI thông minh hơn đối thủ rõ rệt

### Features

#### Phải có (P0)
- [ ] **Personalized pattern learning**:
  - "Bạn thường ăn phở sáng thứ 2"
  - Proactive suggestions
  - Context-aware reminders
- [ ] **Mood detection** (qua câu hỏi thông minh):
  - "Hôm nay có meeting căng không?"
  - "Bạn đang muốn ăn nhẹ hay nặng?"
- [ ] **Calendar integration** (optional):
  - Google Calendar
  - Detect busy day → gợi ý món nhanh
- [ ] **Health-aware recommendations**:
  - Track weekly: "Ăn quá nhiều dầu mỡ"
  - Cảnh báo nhẹ nhàng, không phán xét
- [ ] **Anti-recommendation**: Hỏi "Hôm nay KHÔNG muốn ăn gì?"
- [ ] **Regret Score**: Thay vì 5 stars, hỏi "Có hối hận không?"

#### Nice to have (P1)
- [ ] **Voice input**: "Cho tao gợi ý gì đó healthy"
- [ ] **Photo-based feedback**: Chụp ảnh bữa ăn → AI nhận diện món
- [ ] **Weather deep**: Hiểu độ ẩm, gió mùa

### Success metrics (cuối tháng 6)
- **Users**: 30,000-50,000 MAU
- **NPS**: > 40
- **Recommendation accuracy**: "An toàn" được chọn > 70%
- **Affiliate revenue**: 50-100 triệu/tháng
- **Regret score avg**: < 20%

---

## 📊 Key Decision Points

### End of Phase 1 (Tháng 2)
**Go/No-go criteria**:
- ✅ D7 retention > 20%? → Tiếp tục Phase 2
- ❌ D7 retention < 15%? → Pivot messaging hoặc features

### End of Phase 2 (Tháng 4)
**Go/No-go criteria**:
- ✅ Viral K > 1.0? → Tăng tốc Phase 3
- ❌ K < 0.8? → Fix viral loop trước khi làm tính năng mới

### End of Phase 3 (Tháng 6)
**Quyết định chiến lược**:
- Nếu thành công → Raise seed round để scale
- Bootstrapping được → Tiếp tục organic growth + B2B exploration

---

## 🛠 Resource Planning

### Team size tối thiểu
- **Phase 1**: 1 Backend, 1 Frontend, 1 Designer/PM (có thể part-time)
- **Phase 2**: +1 Mobile dev (Flutter)
- **Phase 3**: +1 ML engineer

### Budget estimate (6 tháng)
| Hạng mục | Chi phí/tháng |
|---------|---------------|
| Team (3 người full-time) | 60-90 triệu |
| Server (AWS/GCP) | 3-5 triệu |
| LLM API (Claude/OpenAI) | 5-15 triệu (tăng theo user) |
| Tools (Figma, GitHub, v.v.) | 2-3 triệu |
| Marketing (ads) | 10-30 triệu |
| **Tổng** | **80-140 triệu** |

### Budget 6 tháng: 500 triệu - 1 tỷ VND

---

## 🎬 North Star Focus

Mỗi sprint (2 tuần), cả team hỏi: **"Điều này có giúp tăng số quyết định ăn uống được AI hỗ trợ mỗi ngày không?"**

Nếu không → KHÔNG LÀM.
