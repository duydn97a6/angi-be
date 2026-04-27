# 02. Database Schema

## 🗄 Database design

### Technology
- **RDBMS**: PostgreSQL 16
- **Extensions**:
  - `postgis` (geospatial queries cho restaurant search)
  - `pg_trgm` (fuzzy text search)
  - `uuid-ossp` (UUID generation)

### Naming conventions
- Tables: `snake_case`, plural (e.g., `users`, `restaurants`)
- Columns: `snake_case`
- Primary keys: `id` (UUID v4)
- Foreign keys: `{table_singular}_id` (e.g., `user_id`)
- Timestamps: `created_at`, `updated_at` (TIMESTAMPTZ)
- Boolean: prefix với `is_` hoặc `has_`

---

## 📐 Schema diagram (high-level)

```
┌─────────────┐
│   users     │
└──────┬──────┘
       │
       ├──────────────┐
       │              │
       ▼              ▼
┌──────────────┐  ┌────────────────┐
│user_prefer-  │  │user_meal_      │
│ences         │  │history         │
└──────────────┘  └────────┬───────┘
                            │
                            ▼
                   ┌────────────────┐
                   │  restaurants   │
                   └────────────────┘
                            │
                            ├─ restaurant_cuisines
                            ├─ restaurant_dishes
                            └─ restaurant_delivery_apps

┌──────────────┐
│   groups     │  (Phase 2)
└──────┬───────┘
       │
       ├──── group_members
       │
       └──── lunch_polls
                  │
                  └──── poll_votes
```

---

## 📋 Core tables (MVP)

### `users`
```sql
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255),  -- NULL if OAuth only
    name VARCHAR(100) NOT NULL,
    avatar_url TEXT,
    phone VARCHAR(20),

    -- OAuth
    google_id VARCHAR(255) UNIQUE,

    -- Status
    is_email_verified BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    deleted_at TIMESTAMPTZ,  -- Soft delete

    -- Timestamps
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    last_login_at TIMESTAMPTZ,

    -- Indexes
    CONSTRAINT chk_email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_google_id ON users(google_id) WHERE google_id IS NOT NULL;
CREATE INDEX idx_users_deleted_at ON users(deleted_at) WHERE deleted_at IS NULL;
```

### `user_preferences`
```sql
CREATE TABLE user_preferences (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,

    -- Region & location
    region VARCHAR(20),  -- 'north', 'central', 'south'
    office_lat DECIMAL(10, 7),
    office_lng DECIMAL(10, 7),
    office_address TEXT,
    search_radius_meters INTEGER DEFAULT 1000,  -- 500/1000/2000

    -- Dietary
    diet_type VARCHAR(30) DEFAULT 'normal',  -- 'normal', 'vegetarian', 'vegan', 'healthy'
    excluded_foods TEXT[],  -- Array: ['seafood', 'beef', 'pork', 'spicy', 'peanut']
    favorite_cuisines TEXT[],  -- Array: ['vietnamese', 'korean', 'japanese']

    -- Budget
    budget_min INTEGER DEFAULT 30000,  -- VND
    budget_max INTEGER DEFAULT 80000,

    -- Preferences
    prefers_delivery BOOLEAN DEFAULT FALSE,
    max_delivery_time_min INTEGER DEFAULT 30,

    -- AI personalization data (updated by feedback)
    loved_dish_ids UUID[],  -- Array of dishes with positive feedback
    disliked_dish_ids UUID[],  -- Array of dishes with negative feedback

    -- Timestamps
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_user_preferences_user ON user_preferences(user_id);
```

### `restaurants`
```sql
CREATE TABLE restaurants (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    -- Basic info
    name VARCHAR(200) NOT NULL,
    slug VARCHAR(250) UNIQUE NOT NULL,  -- For SEO URLs
    description TEXT,
    phone VARCHAR(20),
    website TEXT,

    -- Location (PostGIS point for geospatial queries)
    address TEXT NOT NULL,
    district VARCHAR(100),  -- 'Quận 1', 'Cầu Giấy'
    city VARCHAR(100) NOT NULL,  -- 'Hồ Chí Minh', 'Hà Nội'
    location GEOGRAPHY(POINT, 4326) NOT NULL,  -- PostGIS

    -- Categorization
    primary_cuisine VARCHAR(50),  -- 'vietnamese', 'korean', etc.
    price_range VARCHAR(20),  -- 'budget', 'mid', 'premium'
    avg_price_vnd INTEGER,  -- Average price per meal in VND

    -- Operating
    opening_hours JSONB,  -- {"mon": "08:00-22:00", ...}
    is_open BOOLEAN DEFAULT TRUE,

    -- Ratings (cached from aggregated feedback)
    avg_rating DECIMAL(2,1) DEFAULT 0,  -- 0.0 to 5.0
    total_reviews INTEGER DEFAULT 0,

    -- Images
    cover_image_url TEXT,
    images JSONB,  -- Array of image URLs

    -- External links (for affiliate)
    grabfood_url TEXT,
    shopeefood_url TEXT,
    baemin_url TEXT,
    google_place_id VARCHAR(100),

    -- Status
    is_verified BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,

    -- Timestamps
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Geospatial index (CRITICAL for performance)
CREATE INDEX idx_restaurants_location ON restaurants USING GIST(location);

-- Other indexes
CREATE INDEX idx_restaurants_city ON restaurants(city);
CREATE INDEX idx_restaurants_cuisine ON restaurants(primary_cuisine);
CREATE INDEX idx_restaurants_active ON restaurants(is_active) WHERE is_active = TRUE;
CREATE INDEX idx_restaurants_name_trgm ON restaurants USING GIN(name gin_trgm_ops);
```

### `dishes`
```sql
CREATE TABLE dishes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    restaurant_id UUID NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,

    -- Basic info
    name VARCHAR(200) NOT NULL,
    description TEXT,
    price_vnd INTEGER NOT NULL,

    -- Categorization
    category VARCHAR(50),  -- 'main', 'side', 'drink', 'dessert'
    cuisine_tags TEXT[],  -- ['vietnamese', 'noodle', 'soup']

    -- Dietary flags
    is_vegetarian BOOLEAN DEFAULT FALSE,
    is_vegan BOOLEAN DEFAULT FALSE,
    is_spicy BOOLEAN DEFAULT FALSE,
    allergens TEXT[],  -- ['peanut', 'seafood', 'dairy']

    -- Ratings
    avg_rating DECIMAL(2,1) DEFAULT 0,
    order_count INTEGER DEFAULT 0,  -- Track popularity

    -- Images
    image_url TEXT,

    -- Status
    is_available BOOLEAN DEFAULT TRUE,

    -- Timestamps
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_dishes_restaurant ON dishes(restaurant_id);
CREATE INDEX idx_dishes_category ON dishes(category);
CREATE INDEX idx_dishes_price ON dishes(price_vnd);
```

### `user_meal_history`
```sql
CREATE TABLE user_meal_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    restaurant_id UUID NOT NULL REFERENCES restaurants(id),
    dish_id UUID REFERENCES dishes(id),  -- Can be NULL if user just visited

    -- Context when recommendation was made
    recommendation_id UUID,  -- Link back to recommendation event
    recommendation_category VARCHAR(20),  -- 'safe', 'familiar', 'discovery'

    -- Meal info
    meal_type VARCHAR(20),  -- 'breakfast', 'lunch', 'dinner'
    meal_at TIMESTAMPTZ,  -- When the meal was eaten
    price_paid_vnd INTEGER,

    -- Context snapshot (for AI learning)
    weather_data JSONB,  -- {"temp": 32, "condition": "sunny", "humidity": 75}
    location_data JSONB,  -- {"lat": 10.77, "lng": 106.70}
    day_of_week INTEGER,  -- 1-7
    hour_of_day INTEGER,  -- 0-23

    -- Feedback (may be NULL until user provides it)
    feedback_emoji VARCHAR(10),  -- 'sad', 'neutral', 'happy'
    regret_level VARCHAR(10),  -- 'none', 'slight', 'high'
    feedback_tags TEXT[],  -- ['delicious', 'cheap', 'fast', 'far', 'dirty']
    feedback_notes TEXT,
    feedback_at TIMESTAMPTZ,

    -- Timestamps
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_meal_history_user ON user_meal_history(user_id, created_at DESC);
CREATE INDEX idx_meal_history_user_restaurant ON user_meal_history(user_id, restaurant_id);
CREATE INDEX idx_meal_history_feedback ON user_meal_history(user_id, feedback_emoji) WHERE feedback_emoji IS NOT NULL;
```

### `recommendations`
```sql
CREATE TABLE recommendations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,

    -- Context at time of recommendation
    context_data JSONB NOT NULL,  -- All context used (weather, time, location, etc.)

    -- The 3 recommendations
    recommendations JSONB NOT NULL,  -- Array of {restaurant_id, dish_id, category, explanation}

    -- Performance tracking
    generation_method VARCHAR(30),  -- 'llm_claude', 'llm_openai', 'rule_based'
    generation_time_ms INTEGER,
    llm_tokens_used INTEGER,

    -- User actions
    clicked_recommendation_index INTEGER,  -- 0, 1, or 2. NULL if not clicked
    clicked_at TIMESTAMPTZ,

    -- Timestamps
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_recommendations_user ON recommendations(user_id, created_at DESC);
CREATE INDEX idx_recommendations_method ON recommendations(generation_method);
```

### `auth_tokens` (Refresh tokens tracking)
```sql
CREATE TABLE auth_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,

    token_hash VARCHAR(255) NOT NULL UNIQUE,  -- Hashed refresh token
    device_info JSONB,  -- {"os": "iOS", "browser": "Safari", "ip": "..."}

    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ DEFAULT NOW(),
    last_used_at TIMESTAMPTZ
);

CREATE INDEX idx_auth_tokens_user ON auth_tokens(user_id);
CREATE INDEX idx_auth_tokens_hash ON auth_tokens(token_hash);
CREATE INDEX idx_auth_tokens_expires ON auth_tokens(expires_at);
```

---

## 👥 Phase 2 tables (Group feature)

### `groups`
```sql
CREATE TABLE groups (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    invite_code VARCHAR(20) UNIQUE NOT NULL,  -- Short code for sharing

    creator_id UUID NOT NULL REFERENCES users(id),

    -- Group settings
    max_members INTEGER DEFAULT 20,
    auto_lunch_poll BOOLEAN DEFAULT TRUE,  -- Auto-create poll daily
    default_poll_duration_min INTEGER DEFAULT 30,

    -- Fairness tracking
    preference_weights JSONB,  -- {"user_id_1": 0.8, "user_id_2": 1.2} adjusted over time

    is_active BOOLEAN DEFAULT TRUE,

    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_groups_invite_code ON groups(invite_code);
CREATE INDEX idx_groups_creator ON groups(creator_id);
```

### `group_members`
```sql
CREATE TABLE group_members (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    group_id UUID NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,

    role VARCHAR(20) DEFAULT 'member',  -- 'admin', 'member'
    is_active BOOLEAN DEFAULT TRUE,

    -- Stats
    vetos_used_today INTEGER DEFAULT 0,
    last_veto_at TIMESTAMPTZ,

    joined_at TIMESTAMPTZ DEFAULT NOW(),

    UNIQUE(group_id, user_id)
);

CREATE INDEX idx_group_members_group ON group_members(group_id);
CREATE INDEX idx_group_members_user ON group_members(user_id);
```

### `lunch_polls`
```sql
CREATE TABLE lunch_polls (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    group_id UUID NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
    creator_id UUID NOT NULL REFERENCES users(id),

    title VARCHAR(200),
    meal_type VARCHAR(20) DEFAULT 'lunch',

    -- Candidate restaurants
    candidate_restaurants JSONB NOT NULL,  -- Array of {restaurant_id, dish_id, ai_reason}

    -- Timing
    opens_at TIMESTAMPTZ DEFAULT NOW(),
    closes_at TIMESTAMPTZ NOT NULL,
    closed_at TIMESTAMPTZ,

    -- Result
    winner_restaurant_id UUID REFERENCES restaurants(id),
    status VARCHAR(20) DEFAULT 'active',  -- 'active', 'closed', 'cancelled'

    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_lunch_polls_group ON lunch_polls(group_id, created_at DESC);
CREATE INDEX idx_lunch_polls_active ON lunch_polls(group_id, status) WHERE status = 'active';
```

### `poll_votes`
```sql
CREATE TABLE poll_votes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    poll_id UUID NOT NULL REFERENCES lunch_polls(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,

    restaurant_id UUID NOT NULL REFERENCES restaurants(id),
    vote_type VARCHAR(20) NOT NULL,  -- 'up', 'veto'
    veto_reason VARCHAR(50),  -- 'allergy', 'budget', 'diet', 'repeated'
    notes TEXT,

    created_at TIMESTAMPTZ DEFAULT NOW(),

    UNIQUE(poll_id, user_id, restaurant_id)
);

CREATE INDEX idx_poll_votes_poll ON poll_votes(poll_id);
CREATE INDEX idx_poll_votes_user ON poll_votes(user_id);
```

---

## 📊 Analytics tables

### `events`
```sql
CREATE TABLE events (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id),
    session_id UUID,

    event_name VARCHAR(100) NOT NULL,  -- 'signup', 'recommendation_viewed', etc.
    event_properties JSONB,

    -- Context
    platform VARCHAR(20),  -- 'web', 'ios', 'android'
    user_agent TEXT,
    ip_address INET,

    occurred_at TIMESTAMPTZ DEFAULT NOW(),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Partition by month for performance
CREATE INDEX idx_events_user_name ON events(user_id, event_name);
CREATE INDEX idx_events_occurred ON events(occurred_at DESC);
```

---

## 🔧 Migration strategy

Dùng **Flyway** cho migrations. Cấu trúc:
```
backend/src/main/resources/db/migration/
├── V1__create_users.sql
├── V2__create_user_preferences.sql
├── V3__create_restaurants.sql
├── V4__create_dishes.sql
├── V5__create_meal_history.sql
├── V6__create_recommendations.sql
├── V7__create_auth_tokens.sql
├── V8__create_events.sql
├── V9__seed_restaurants.sql  -- Seed data
```

### Migration rules
- Never modify existing migration files after they're run in production
- Always use `V{n}__description.sql` naming
- Test migrations on copy of production data
- Include rollback notes in commit message

---

## 🌱 Seed data

File `V9__seed_restaurants.sql` cần có ít nhất:
- 500 restaurants (distributed across HCM Q1/3/7 + HN CG/ĐĐ)
- 3000+ dishes
- Mix cuisines: Việt (60%), Hàn/Nhật (15%), Thái/Trung (10%), Tây (10%), Khác (5%)
- Price ranges balanced

Có thể crawl từ Foody/ShopeeFood để seed ban đầu (lưu ý về ToS).

---

## 📏 Data retention

- `events`: 90 days rolling (move to data warehouse after)
- `auth_tokens`: Delete after revoked + 30 days
- `recommendations`: 1 year rolling
- `user_meal_history`: Forever (with user consent) - dùng cho AI training
- Deleted users (soft delete): Hard delete after 30 days

---

## 🔍 Common queries (reference)

### Tìm restaurants gần user trong bán kính
```sql
SELECT
    r.*,
    ST_Distance(r.location, ST_MakePoint($1, $2)::geography) as distance
FROM restaurants r
WHERE ST_DWithin(
    r.location,
    ST_MakePoint($1, $2)::geography,
    $3  -- radius in meters
)
AND r.is_active = TRUE
AND r.primary_cuisine NOT IN (SELECT unnest($4::text[]))  -- excluded cuisines
ORDER BY distance
LIMIT 100;
```

### User's recent meals với feedback
```sql
SELECT
    h.*,
    r.name as restaurant_name,
    r.primary_cuisine,
    d.name as dish_name
FROM user_meal_history h
LEFT JOIN restaurants r ON h.restaurant_id = r.id
LEFT JOIN dishes d ON h.dish_id = d.id
WHERE h.user_id = $1
  AND h.created_at > NOW() - INTERVAL '30 days'
ORDER BY h.created_at DESC;
```

### Top dishes user has loved
```sql
SELECT
    d.id, d.name, COUNT(*) as times_loved
FROM user_meal_history h
JOIN dishes d ON h.dish_id = d.id
WHERE h.user_id = $1
  AND h.feedback_emoji = 'happy'
  AND h.regret_level = 'none'
GROUP BY d.id, d.name
ORDER BY times_loved DESC
LIMIT 10;
```
