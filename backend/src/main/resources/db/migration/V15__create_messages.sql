-- Сообщения от учителя/админа ученику (in-app inbox)
CREATE TABLE IF NOT EXISTS messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sender_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    recipient_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    text TEXT NOT NULL,
    -- MESSAGE — обычное сообщение, COMMENT — комментарий к результату теста
    type VARCHAR(20) NOT NULL DEFAULT 'MESSAGE',
    -- Для COMMENT: к какому тесту привязан комментарий (опционально)
    test_id UUID REFERENCES tests(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    read_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_messages_recipient ON messages(recipient_id, read_at);
CREATE INDEX IF NOT EXISTS idx_messages_sender ON messages(sender_id);
