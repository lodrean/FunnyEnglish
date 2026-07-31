-- Обезличенные события гостевых пользователей (анонимная аналитика)
CREATE TABLE IF NOT EXISTS guest_events (
    id UUID PRIMARY KEY,
    anonymous_id UUID NOT NULL,
    type VARCHAR(32) NOT NULL,
    test_id UUID,
    score INTEGER,
    max_score INTEGER,
    time_spent_seconds INTEGER,
    converted_user_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_guest_events_anonymous_id ON guest_events(anonymous_id);
CREATE INDEX IF NOT EXISTS idx_guest_events_created_at ON guest_events(created_at);
CREATE INDEX IF NOT EXISTS idx_guest_events_type ON guest_events(type);
CREATE INDEX IF NOT EXISTS idx_guest_events_converted ON guest_events(converted_user_id) WHERE converted_user_id IS NOT NULL;
