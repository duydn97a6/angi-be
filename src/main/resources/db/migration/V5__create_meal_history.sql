CREATE TABLE user_meal_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    restaurant_id UUID NOT NULL REFERENCES restaurants(id),
    dish_id UUID REFERENCES dishes(id),

    recommendation_id UUID,
    recommendation_category VARCHAR(20),

    meal_type VARCHAR(20),
    meal_at TIMESTAMPTZ,
    price_paid_vnd INTEGER,

    weather_data JSONB,
    location_data JSONB,
    day_of_week INTEGER,
    hour_of_day INTEGER,

    feedback_emoji VARCHAR(10),
    regret_level VARCHAR(10),
    feedback_tags TEXT[],
    feedback_notes TEXT,
    feedback_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_meal_history_user ON user_meal_history(user_id, created_at DESC);
CREATE INDEX idx_meal_history_user_restaurant ON user_meal_history(user_id, restaurant_id);
CREATE INDEX idx_meal_history_feedback ON user_meal_history(user_id, feedback_emoji) WHERE feedback_emoji IS NOT NULL;
