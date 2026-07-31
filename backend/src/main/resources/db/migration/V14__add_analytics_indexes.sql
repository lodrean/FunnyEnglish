-- Add indexes for analytics queries to fix 500 errors
-- These indexes significantly improve performance of daily activity queries

-- Index for user creation date analytics
CREATE INDEX IF NOT EXISTS idx_users_created_at ON users(created_at);

-- Index for progress completion date analytics
CREATE INDEX IF NOT EXISTS idx_progress_completed_at ON progress(completed_at);

-- Index for user achievements earned date analytics
CREATE INDEX IF NOT EXISTS idx_user_achievements_earned_at ON user_achievements(earned_at);

-- Composite indexes for common analytics queries
CREATE INDEX IF NOT EXISTS idx_progress_user_completed ON progress(user_id, completed_at);
CREATE INDEX IF NOT EXISTS idx_user_achievements_user_earned ON user_achievements(user_id, earned_at);
