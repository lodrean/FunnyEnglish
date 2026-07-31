-- Test data for H2 database
-- This script runs when using 'test' profile

-- Insert default categories with fixed UUIDs for test references
INSERT INTO categories (id, name, description, display_order, icon_url, is_active) VALUES
    ('11111111-1111-1111-1111-111111111111', 'Test Category', 'For E2E testing', 1, '📚', true),
    ('22222222-2222-2222-2222-222222222222', 'Животные', 'Учим названия животных', 2, '🦁', true),
    ('33333333-3333-3333-3333-333333333333', 'Цвета', 'Учим цвета на английском', 3, '🎨', true);

-- Insert test 'Цвета: Базовый' into category 'Цвета'
INSERT INTO tests (id, category_id, title, description, difficulty, points_reward, time_limit_seconds, is_published, display_order, created_at, updated_at) VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '33333333-3333-3333-3333-333333333333', 'Цвета: Базовый', 'Базовые цвета на английском', 'EASY', 50, 300, true, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert questions for 'Цвета: Базовый' test (TEXT_SELECT type)
INSERT INTO questions (id, test_id, type, title, text, display_order, points, is_published, created_at, updated_at) VALUES
    ('b0000000-0000-0000-0000-000000000001', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'TEXT_SELECT', 'Как будет красный?', 'Как будет красный?', 1, 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('b0000000-0000-0000-0000-000000000002', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'TEXT_SELECT', 'Как будет синий?', 'Как будет синий?', 2, 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('b0000000-0000-0000-0000-000000000003', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'TEXT_SELECT', 'Как будет зелёный?', 'Как будет зелёный?', 3, 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('b0000000-0000-0000-0000-000000000004', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'TEXT_SELECT', 'Как будет жёлтый?', 'Как будет жёлтый?', 4, 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('b0000000-0000-0000-0000-000000000005', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'TEXT_SELECT', 'Как будет чёрный?', 'Как будет чёрный?', 5, 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert answers for Question 1 (Red)
INSERT INTO answers (id, question_id, text, is_correct, display_order) VALUES
    ('c0000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000001', 'Red', true, 1),
    ('c0000000-0000-0000-0000-000000000002', 'b0000000-0000-0000-0000-000000000001', 'Blue', false, 2),
    ('c0000000-0000-0000-0000-000000000003', 'b0000000-0000-0000-0000-000000000001', 'Green', false, 3),
    ('c0000000-0000-0000-0000-000000000004', 'b0000000-0000-0000-0000-000000000001', 'Yellow', false, 4);

-- Insert answers for Question 2 (Blue)
INSERT INTO answers (id, question_id, text, is_correct, display_order) VALUES
    ('c0000000-0000-0000-0000-000000000005', 'b0000000-0000-0000-0000-000000000002', 'Red', false, 1),
    ('c0000000-0000-0000-0000-000000000006', 'b0000000-0000-0000-0000-000000000002', 'Blue', true, 2),
    ('c0000000-0000-0000-0000-000000000007', 'b0000000-0000-0000-0000-000000000002', 'Green', false, 3),
    ('c0000000-0000-0000-0000-000000000008', 'b0000000-0000-0000-0000-000000000002', 'Yellow', false, 4);

-- Insert answers for Question 3 (Green)
INSERT INTO answers (id, question_id, text, is_correct, display_order) VALUES
    ('c0000000-0000-0000-0000-000000000009',  'b0000000-0000-0000-0000-000000000003', 'Red', false, 1),
    ('c0000000-0000-0000-0000-000000000010', 'b0000000-0000-0000-0000-000000000003', 'Blue', false, 2),
    ('c0000000-0000-0000-0000-000000000011', 'b0000000-0000-0000-0000-000000000003', 'Green', true, 3),
    ('c0000000-0000-0000-0000-000000000012', 'b0000000-0000-0000-0000-000000000003', 'Yellow', false, 4);

-- Insert answers for Question 4 (Yellow)
INSERT INTO answers (id, question_id, text, is_correct, display_order) VALUES
    ('c0000000-0000-0000-0000-000000000013', 'b0000000-0000-0000-0000-000000000004', 'Red', false, 1),
    ('c0000000-0000-0000-0000-000000000014', 'b0000000-0000-0000-0000-000000000004', 'Blue', false, 2),
    ('c0000000-0000-0000-0000-000000000015', 'b0000000-0000-0000-0000-000000000004', 'Green', false, 3),
    ('c0000000-0000-0000-0000-000000000016', 'b0000000-0000-0000-0000-000000000004', 'Yellow', true, 4);

-- Insert answers for Question 5 (Black)
INSERT INTO answers (id, question_id, text, is_correct, display_order) VALUES
    ('c0000000-0000-0000-0000-000000000017', 'b0000000-0000-0000-0000-000000000005', 'Red', false, 1),
    ('c0000000-0000-0000-0000-000000000018', 'b0000000-0000-0000-0000-000000000005', 'Blue', false, 2),
    ('c0000000-0000-0000-0000-000000000019', 'b0000000-0000-0000-0000-000000000005', 'Green', false, 3),
    ('c0000000-0000-0000-0000-000000000020', 'b0000000-0000-0000-0000-000000000005', 'Black', true, 4);
