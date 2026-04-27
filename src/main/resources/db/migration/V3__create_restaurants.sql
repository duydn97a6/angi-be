CREATE TABLE restaurants (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    name VARCHAR(200) NOT NULL,
    slug VARCHAR(250) UNIQUE NOT NULL,
    description TEXT,
    phone VARCHAR(20),
    website TEXT,

    address TEXT NOT NULL,
    district VARCHAR(100),
    city VARCHAR(100) NOT NULL,
    location GEOGRAPHY(POINT, 4326) NOT NULL,

    primary_cuisine VARCHAR(50),
    price_range VARCHAR(20),
    avg_price_vnd INTEGER,

    opening_hours JSONB,
    is_open BOOLEAN DEFAULT TRUE,

    avg_rating DECIMAL(2,1) DEFAULT 0,
    total_reviews INTEGER DEFAULT 0,

    cover_image_url TEXT,
    images JSONB,

    grabfood_url TEXT,
    shopeefood_url TEXT,
    baemin_url TEXT,
    google_place_id VARCHAR(100),

    is_verified BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,

    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_restaurants_location ON restaurants USING GIST(location);
CREATE INDEX idx_restaurants_city ON restaurants(city);
CREATE INDEX idx_restaurants_cuisine ON restaurants(primary_cuisine);
CREATE INDEX idx_restaurants_active ON restaurants(is_active) WHERE is_active = TRUE;
CREATE INDEX idx_restaurants_name_trgm ON restaurants USING GIN(name gin_trgm_ops);
