-- Полноценные refresh-токены (bd FunnyEnglish-nj2.7, SEC: PROJECT-REVIEW-2026-08-28 §2.1 п.6,
-- PROJECT_AUDIT_2026-08-29 AR-6). Заменяют схему «refresh = обмен истёкшего access-токена».
-- Храним ТОЛЬКО SHA-256-хэш токена (утечка БД не даёт валидных токенов), JTI — для поиска/отзыва.
-- rotated_at — ротация (одноразовость); revoked_at — отзыв (logout, reuse-detection).

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    jti VARCHAR(64) NOT NULL UNIQUE,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    rotated_at TIMESTAMPTZ,
    revoked_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);
