-- Users table
CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    display_name VARCHAR(100) NOT NULL,
    avatar_url VARCHAR(500),
    auth_provider VARCHAR(20) NOT NULL DEFAULT 'EMAIL',
    provider_id VARCHAR(255),
    level INTEGER NOT NULL DEFAULT 1,
    total_points INTEGER NOT NULL DEFAULT 0,
    current_streak INTEGER NOT NULL DEFAULT 0,
    last_activity_date TIMESTAMP,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_provider ON users(auth_provider, provider_id);

-- Categories table
CREATE TABLE categories (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    icon_url VARCHAR(500),
    display_order INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT true
);

CREATE INDEX idx_categories_active ON categories(is_active, display_order);

-- Tests table
CREATE TABLE tests (
    id UUID PRIMARY KEY,
    category_id UUID NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    thumbnail_url VARCHAR(500),
    difficulty VARCHAR(20) NOT NULL DEFAULT 'EASY',
    points_reward INTEGER NOT NULL DEFAULT 10,
    time_limit_seconds INTEGER,
    is_published BOOLEAN NOT NULL DEFAULT false,
    display_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_tests_category ON tests(category_id);
CREATE INDEX idx_tests_published ON tests(is_published, display_order);

-- Questions table
CREATE TABLE questions (
    id UUID PRIMARY KEY,
    test_id UUID NOT NULL REFERENCES tests(id) ON DELETE CASCADE,
    type VARCHAR(30) NOT NULL,
    text TEXT,
    audio_url VARCHAR(500),
    image_url VARCHAR(500),
    display_order INTEGER NOT NULL DEFAULT 0,
    points INTEGER NOT NULL DEFAULT 1
);

CREATE INDEX idx_questions_test ON questions(test_id, display_order);

-- Answers table
CREATE TABLE answers (
    id UUID PRIMARY KEY,
    question_id UUID NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    text VARCHAR(500),
    image_url VARCHAR(500),
    audio_url VARCHAR(500),
    is_correct BOOLEAN NOT NULL DEFAULT false,
    display_order INTEGER NOT NULL DEFAULT 0,
    match_target VARCHAR(255)
);

CREATE INDEX idx_answers_question ON answers(question_id);

-- Progress table
CREATE TABLE progress (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    test_id UUID NOT NULL REFERENCES tests(id) ON DELETE CASCADE,
    score INTEGER NOT NULL DEFAULT 0,
    max_score INTEGER NOT NULL,
    stars INTEGER NOT NULL DEFAULT 0,
    attempts_count INTEGER NOT NULL DEFAULT 1,
    best_score INTEGER NOT NULL DEFAULT 0,
    time_spent_seconds INTEGER,
    completed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_attempt_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, test_id)
);

CREATE INDEX idx_progress_user ON progress(user_id);
CREATE INDEX idx_progress_test ON progress(test_id);

-- Achievements table
CREATE TABLE achievements (
    id UUID PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    icon_url VARCHAR(500),
    points_reward INTEGER NOT NULL DEFAULT 0,
    is_hidden BOOLEAN NOT NULL DEFAULT false
);

-- User achievements (many-to-many)
CREATE TABLE user_achievements (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    achievement_id UUID NOT NULL REFERENCES achievements(id) ON DELETE CASCADE,
    earned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, achievement_id)
);

CREATE INDEX idx_user_achievements_user ON user_achievements(user_id);

-- Insert default achievements
INSERT INTO achievements (id, code, name, description, points_reward, is_hidden) VALUES
    (gen_random_uuid(), 'FIRST_TEST', 'Первый шаг', 'Пройди свой первый тест', 10, false),
    (gen_random_uuid(), 'PERFECT_SCORE', 'Отличник', 'Получи 100% в любом тесте', 25, false),
    (gen_random_uuid(), 'STREAK_3', 'На волне', 'Занимайся 3 дня подряд', 15, false),
    (gen_random_uuid(), 'STREAK_7', 'Целая неделя', 'Занимайся 7 дней подряд', 50, false),
    (gen_random_uuid(), 'STREAK_30', 'Месяц упорства', 'Занимайся 30 дней подряд', 200, false),
    (gen_random_uuid(), 'TESTS_10', 'Начинающий ученик', 'Пройди 10 тестов', 30, false),
    (gen_random_uuid(), 'TESTS_50', 'Опытный ученик', 'Пройди 50 тестов', 100, false),
    (gen_random_uuid(), 'ALL_STARS', 'Звёздный сборщик', 'Получи 3 звезды во всех тестах категории', 150, true),
    (gen_random_uuid(), 'SPEED_DEMON', 'Молния', 'Пройди тест менее чем за 30 секунд', 50, true);

-- Insert default categories
INSERT INTO categories (id, name, description, display_order) VALUES
    (gen_random_uuid(), 'Животные', 'Учим названия животных', 1),
    (gen_random_uuid(), 'Цвета', 'Учим цвета на английском', 2),
    (gen_random_uuid(), 'Числа', 'Учим числа от 1 до 100', 3),
    (gen_random_uuid(), 'Еда', 'Фрукты, овощи и другая еда', 4),
    (gen_random_uuid(), 'Семья', 'Члены семьи', 5),
    (gen_random_uuid(), 'Одежда', 'Предметы одежды', 6);

-- Default demo user (email: demo@funnyenglish.app, password: demo123)
INSERT INTO users (id, email, password_hash, display_name, role, auth_provider)
VALUES (
    gen_random_uuid(),
    'demo@funnyenglish.app',
    '$2b$12$QfOqjaz2o80LemjSz2mtQ.Q7vflyhczmzXOpkYUrN9oFTvfuvMVL6',
    'Demo',
    'USER',
    'EMAIL'
);
