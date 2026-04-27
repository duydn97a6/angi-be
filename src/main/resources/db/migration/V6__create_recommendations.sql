CREATE TABLE recommendations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,

    context_data JSONB NOT NULL,
    recommendations JSONB NOT NULL,

    generation_method VARCHAR(30),
    generation_time_ms INTEGER,
    llm_tokens_used INTEGER,

    clicked_recommendation_index INTEGER,
    clicked_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_recommendations_user ON recommendations(user_id, created_at DESC);
CREATE INDEX idx_recommendations_method ON recommendations(generation_method);
