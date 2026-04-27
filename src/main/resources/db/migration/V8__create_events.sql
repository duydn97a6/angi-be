CREATE TABLE events (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id),
    session_id UUID,

    event_name VARCHAR(100) NOT NULL,
    event_properties JSONB,

    platform VARCHAR(20),
    user_agent TEXT,
    ip_address INET,

    occurred_at TIMESTAMPTZ DEFAULT NOW(),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_events_user_name ON events(user_id, event_name);
CREATE INDEX idx_events_occurred ON events(occurred_at DESC);
