CREATE TABLE dishes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    restaurant_id UUID NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,

    name VARCHAR(200) NOT NULL,
    description TEXT,
    price_vnd INTEGER NOT NULL,

    category VARCHAR(50),
    cuisine_tags TEXT[],

    is_vegetarian BOOLEAN DEFAULT FALSE,
    is_vegan BOOLEAN DEFAULT FALSE,
    is_spicy BOOLEAN DEFAULT FALSE,
    allergens TEXT[],

    avg_rating DECIMAL(2,1) DEFAULT 0,
    order_count INTEGER DEFAULT 0,

    image_url TEXT,
    is_available BOOLEAN DEFAULT TRUE,

    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_dishes_restaurant ON dishes(restaurant_id);
CREATE INDEX idx_dishes_category ON dishes(category);
CREATE INDEX idx_dishes_price ON dishes(price_vnd);
