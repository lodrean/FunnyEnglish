-- Fix achievements table - add missing columns for gamification

-- Add category column
ALTER TABLE achievements 
ADD COLUMN IF NOT EXISTS category VARCHAR(50) NOT NULL DEFAULT 'GENERAL';

-- Add rarity column  
ALTER TABLE achievements 
ADD COLUMN IF NOT EXISTS rarity VARCHAR(20) NOT NULL DEFAULT 'COMMON';

-- Add condition_type column
ALTER TABLE achievements 
ADD COLUMN IF NOT EXISTS condition_type VARCHAR(50) NOT NULL DEFAULT 'TEST_COMPLETED';

-- Add condition_target column
ALTER TABLE achievements 
ADD COLUMN IF NOT EXISTS condition_target INTEGER NOT NULL DEFAULT 1;

-- Fix user_achievements table - add id column and change achievement_id to VARCHAR
-- First, drop the foreign key constraint and primary key
ALTER TABLE user_achievements DROP CONSTRAINT IF EXISTS user_achievements_pkey;
ALTER TABLE user_achievements DROP CONSTRAINT IF EXISTS user_achievements_achievement_id_fkey;

-- Add id column
ALTER TABLE user_achievements ADD COLUMN IF NOT EXISTS id UUID;
-- Set id for existing rows
UPDATE user_achievements SET id = gen_random_uuid() WHERE id IS NULL;
-- Make id not null and primary key
ALTER TABLE user_achievements ALTER COLUMN id SET NOT NULL;
ALTER TABLE user_achievements ADD PRIMARY KEY (id);

-- Change achievement_id type to VARCHAR to match entity
ALTER TABLE user_achievements ALTER COLUMN achievement_id TYPE VARCHAR(50);

-- Add back unique constraint
ALTER TABLE user_achievements ADD CONSTRAINT user_achievements_user_achievement_unique 
UNIQUE (user_id, achievement_id);

-- Add progress and is_earned columns
ALTER TABLE user_achievements ADD COLUMN IF NOT EXISTS progress FLOAT NOT NULL DEFAULT 0;
ALTER TABLE user_achievements ADD COLUMN IF NOT EXISTS is_earned BOOLEAN NOT NULL DEFAULT true;

-- Update existing achievements with appropriate categories and conditions
UPDATE achievements SET 
    category = CASE code
        WHEN 'FIRST_TEST' THEN 'PROGRESS'
        WHEN 'PERFECT_SCORE' THEN 'MASTERY'
        WHEN 'STREAK_3' THEN 'STREAK'
        WHEN 'STREAK_7' THEN 'STREAK'
        WHEN 'STREAK_30' THEN 'STREAK'
        WHEN 'TESTS_10' THEN 'PROGRESS'
        WHEN 'TESTS_50' THEN 'PROGRESS'
        WHEN 'ALL_STARS' THEN 'MASTERY'
        WHEN 'SPEED_DEMON' THEN 'MASTERY'
        ELSE 'GENERAL'
    END,
    rarity = CASE code
        WHEN 'FIRST_TEST' THEN 'COMMON'
        WHEN 'PERFECT_SCORE' THEN 'RARE'
        WHEN 'STREAK_3' THEN 'COMMON'
        WHEN 'STREAK_7' THEN 'RARE'
        WHEN 'STREAK_30' THEN 'LEGENDARY'
        WHEN 'TESTS_10' THEN 'COMMON'
        WHEN 'TESTS_50' THEN 'EPIC'
        WHEN 'ALL_STARS' THEN 'LEGENDARY'
        WHEN 'SPEED_DEMON' THEN 'EPIC'
        ELSE 'COMMON'
    END,
    condition_type = CASE code
        WHEN 'FIRST_TEST' THEN 'TESTS_COMPLETED'
        WHEN 'PERFECT_SCORE' THEN 'PERFECT_TESTS'
        WHEN 'STREAK_3' THEN 'STREAK_DAYS'
        WHEN 'STREAK_7' THEN 'STREAK_DAYS'
        WHEN 'STREAK_30' THEN 'STREAK_DAYS'
        WHEN 'TESTS_10' THEN 'TESTS_COMPLETED'
        WHEN 'TESTS_50' THEN 'TESTS_COMPLETED'
        WHEN 'ALL_STARS' THEN 'ALL_STARS_CATEGORY'
        WHEN 'SPEED_DEMON' THEN 'FAST_TEST'
        ELSE 'TESTS_COMPLETED'
    END,
    condition_target = CASE code
        WHEN 'FIRST_TEST' THEN 1
        WHEN 'PERFECT_SCORE' THEN 1
        WHEN 'STREAK_3' THEN 3
        WHEN 'STREAK_7' THEN 7
        WHEN 'STREAK_30' THEN 30
        WHEN 'TESTS_10' THEN 10
        WHEN 'TESTS_50' THEN 50
        WHEN 'ALL_STARS' THEN 1
        WHEN 'SPEED_DEMON' THEN 30
        ELSE 1
    END;
