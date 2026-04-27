CREATE TABLE user_preferences (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,

    region VARCHAR(20),
    office_lat DECIMAL(10, 7),
    office_lng DECIMAL(10, 7),
    office_address TEXT,
    search_radius_meters INTEGER DEFAULT 1000,

    diet_type VARCHAR(30) DEFAULT 'normal',
    excluded_foods TEXT[],
    favorite_cuisines TEXT[],

    budget_min INTEGER DEFAULT 30000,
    budget_max INTEGER DEFAULT 80000,

    prefers_delivery BOOLEAN DEFAULT FALSE,
    max_delivery_time_min INTEGER DEFAULT 30,

    loved_dish_ids UUID[],
    disliked_dish_ids UUID[],

    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_user_preferences_user ON user_preferences(user_id);
