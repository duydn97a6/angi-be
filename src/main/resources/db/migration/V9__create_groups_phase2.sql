-- Phase 2 group/poll tables (created early so all schema is in place)
CREATE TABLE groups (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    invite_code VARCHAR(20) UNIQUE NOT NULL,

    creator_id UUID NOT NULL REFERENCES users(id),

    max_members INTEGER DEFAULT 20,
    auto_lunch_poll BOOLEAN DEFAULT TRUE,
    default_poll_duration_min INTEGER DEFAULT 30,

    preference_weights JSONB,

    is_active BOOLEAN DEFAULT TRUE,

    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_groups_invite_code ON groups(invite_code);
CREATE INDEX idx_groups_creator ON groups(creator_id);

CREATE TABLE group_members (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    group_id UUID NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,

    role VARCHAR(20) DEFAULT 'member',
    is_active BOOLEAN DEFAULT TRUE,

    vetos_used_today INTEGER DEFAULT 0,
    last_veto_at TIMESTAMPTZ,

    joined_at TIMESTAMPTZ DEFAULT NOW(),

    UNIQUE(group_id, user_id)
);

CREATE INDEX idx_group_members_group ON group_members(group_id);
CREATE INDEX idx_group_members_user ON group_members(user_id);

CREATE TABLE lunch_polls (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    group_id UUID NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
    creator_id UUID NOT NULL REFERENCES users(id),

    title VARCHAR(200),
    meal_type VARCHAR(20) DEFAULT 'lunch',

    candidate_restaurants JSONB NOT NULL,

    opens_at TIMESTAMPTZ DEFAULT NOW(),
    closes_at TIMESTAMPTZ NOT NULL,
    closed_at TIMESTAMPTZ,

    winner_restaurant_id UUID REFERENCES restaurants(id),
    status VARCHAR(20) DEFAULT 'active',

    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_lunch_polls_group ON lunch_polls(group_id, created_at DESC);
CREATE INDEX idx_lunch_polls_active ON lunch_polls(group_id, status) WHERE status = 'active';

CREATE TABLE poll_votes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    poll_id UUID NOT NULL REFERENCES lunch_polls(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,

    restaurant_id UUID NOT NULL REFERENCES restaurants(id),
    vote_type VARCHAR(20) NOT NULL,
    veto_reason VARCHAR(50),
    notes TEXT,

    created_at TIMESTAMPTZ DEFAULT NOW(),

    UNIQUE(poll_id, user_id, restaurant_id)
);

CREATE INDEX idx_poll_votes_poll ON poll_votes(poll_id);
CREATE INDEX idx_poll_votes_user ON poll_votes(user_id);
