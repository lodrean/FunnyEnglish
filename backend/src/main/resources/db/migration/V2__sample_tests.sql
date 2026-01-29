-- Sample tests for MVP
-- Text-based tests (TEXT_SELECT type) for learning English

-- Helper: Get category IDs
DO $$
DECLARE
    cat_animals UUID;
    cat_colors UUID;
    cat_numbers UUID;
    cat_food UUID;
    cat_family UUID;
    cat_clothes UUID;

    test_id UUID;
    q_id UUID;
BEGIN
    -- Get category IDs
    SELECT id INTO cat_animals FROM categories WHERE name = 'Животные';
    SELECT id INTO cat_colors FROM categories WHERE name = 'Цвета';
    SELECT id INTO cat_numbers FROM categories WHERE name = 'Числа';
    SELECT id INTO cat_food FROM categories WHERE name = 'Еда';
    SELECT id INTO cat_family FROM categories WHERE name = 'Семья';
    SELECT id INTO cat_clothes FROM categories WHERE name = 'Одежда';

    -- =============================================
    -- TEST 1: Animals - Basic (Животные - Базовый)
    -- =============================================
    test_id := gen_random_uuid();
    INSERT INTO tests (id, category_id, title, description, difficulty, points_reward, is_published, display_order)
    VALUES (test_id, cat_animals, 'Животные: Базовый', 'Изучи основные названия животных', 'EASY', 10, true, 1);

    -- Question 1: What is "собака" in English?
    q_id := gen_random_uuid();
    INSERT INTO questions (id, test_id, type, text, display_order, points)
    VALUES (q_id, test_id, 'TEXT_SELECT', 'Как будет "собака" по-английски?', 1, 1);
    INSERT INTO answers (id, question_id, text, is_correct, display_order) VALUES
        (gen_random_uuid(), q_id, 'Dog', true, 1),
        (gen_random_uuid(), q_id, 'Cat', false, 2),
        (gen_random_uuid(), q_id, 'Bird', false, 3),
        (gen_random_uuid(), q_id, 'Fish', false, 4);

    -- Question 2: What is "кошка" in English?
    q_id := gen_random_uuid();
    INSERT INTO questions (id, test_id, type, text, display_order, points)
    VALUES (q_id, test_id, 'TEXT_SELECT', 'Как будет "кошка" по-английски?', 2, 1);
    INSERT INTO answers (id, question_id, text, is_correct, display_order) VALUES
        (gen_random_uuid(), q_id, 'Cat', true, 1),
        (gen_random_uuid(), q_id, 'Dog', false, 2),
        (gen_random_uuid(), q_id, 'Mouse', false, 3),
        (gen_random_uuid(), q_id, 'Rat', false, 4);

    -- Question 3: What is "птица" in English?
    q_id := gen_random_uuid();
    INSERT INTO questions (id, test_id, type, text, display_order, points)
    VALUES (q_id, test_id, 'TEXT_SELECT', 'Как будет "птица" по-английски?', 3, 1);
    INSERT INTO answers (id, question_id, text, is_correct, display_order) VALUES
        (gen_random_uuid(), q_id, 'Bird', true, 1),
        (gen_random_uuid(), q_id, 'Bear', false, 2),
        (gen_random_uuid(), q_id, 'Bee', false, 3),
        (gen_random_uuid(), q_id, 'Butterfly', false, 4);

    -- Question 4: What is "рыба" in English?
    q_id := gen_random_uuid();
    INSERT INTO questions (id, test_id, type, text, display_order, points)
    VALUES (q_id, test_id, 'TEXT_SELECT', 'Как будет "рыба" по-английски?', 4, 1);
    INSERT INTO answers (id, question_id, text, is_correct, display_order) VALUES
        (gen_random_uuid(), q_id, 'Fish', true, 1),
        (gen_random_uuid(), q_id, 'Frog', false, 2),
        (gen_random_uuid(), q_id, 'Fox', false, 3),
        (gen_random_uuid(), q_id, 'Fly', false, 4);

    -- Question 5: What is "лошадь" in English?
    q_id := gen_random_uuid();
    INSERT INTO questions (id, test_id, type, text, display_order, points)
    VALUES (q_id, test_id, 'TEXT_SELECT', 'Как будет "лошадь" по-английски?', 5, 1);
    INSERT INTO answers (id, question_id, text, is_correct, display_order) VALUES
        (gen_random_uuid(), q_id, 'Horse', true, 1),
        (gen_random_uuid(), q_id, 'House', false, 2),
        (gen_random_uuid(), q_id, 'Hippo', false, 3),
        (gen_random_uuid(), q_id, 'Hen', false, 4);

    -- =============================================
    -- TEST 2: Animals - Intermediate (Животные - Средний)
    -- =============================================
    test_id := gen_random_uuid();
    INSERT INTO tests (id, category_id, title, description, difficulty, points_reward, is_published, display_order)
    VALUES (test_id, cat_animals, 'Животные: Средний', 'Больше животных!', 'MEDIUM', 15, true, 2);

    -- Question 1
    q_id := gen_random_uuid();
    INSERT INTO questions (id, test_id, type, text, display_order, points)
    VALUES (q_id, test_id, 'TEXT_SELECT', 'Как будет "слон" по-английски?', 1, 1);
    INSERT INTO answers (id, question_id, text, is_correct, display_order) VALUES
        (gen_random_uuid(), q_id, 'Elephant', true, 1),
        (gen_random_uuid(), q_id, 'Eagle', false, 2),
        (gen_random_uuid(), q_id, 'Elk', false, 3),
        (gen_random_uuid(), q_id, 'Eel', false, 4);

    -- Question 2
    q_id := gen_random_uuid();
    INSERT INTO questions (id, test_id, type, text, display_order, points)
    VALUES (q_id, test_id, 'TEXT_SELECT', 'Как будет "обезьяна" по-английски?', 2, 1);
    INSERT INTO answers (id, question_id, text, is_correct, display_order) VALUES
        (gen_random_uuid(), q_id, 'Monkey', true, 1),
        (gen_random_uuid(), q_id, 'Mouse', false, 2),
        (gen_random_uuid(), q_id, 'Moose', false, 3),
        (gen_random_uuid(), q_id, 'Mole', false, 4);

    -- Question 3
    q_id := gen_random_uuid();
    INSERT INTO questions (id, test_id, type, text, display_order, points)
    VALUES (q_id, test_id, 'TEXT_SELECT', 'Как будет "тигр" по-английски?', 3, 1);
    INSERT INTO answers (id, question_id, text, is_correct, display_order) VALUES
        (gen_random_uuid(), q_id, 'Tiger', true, 1),
        (gen_random_uuid(), q_id, 'Turtle', false, 2),
        (gen_random_uuid(), q_id, 'Turkey', false, 3),
        (gen_random_uuid(), q_id, 'Toad', false, 4);

    -- Question 4
    q_id := gen_random_uuid();
    INSERT INTO questions (id, test_id, type, text, display_order, points)
    VALUES (q_id, test_id, 'TEXT_SELECT', 'Как будет "лев" по-английски?', 4, 1);
    INSERT INTO answers (id, question_id, text, is_correct, display_order) VALUES
        (gen_random_uuid(), q_id, 'Lion', true, 1),
        (gen_random_uuid(), q_id, 'Leopard', false, 2),
        (gen_random_uuid(), q_id, 'Lynx', false, 3),
        (gen_random_uuid(), q_id, 'Llama', false, 4);

    -- Question 5
    q_id := gen_random_uuid();
    INSERT INTO questions (id, test_id, type, text, display_order, points)
    VALUES (q_id, test_id, 'TEXT_SELECT', 'Как будет "медведь" по-английски?', 5, 1);
    INSERT INTO answers (id, question_id, text, is_correct, display_order) VALUES
        (gen_random_uuid(), q_id, 'Bear', true, 1),
        (gen_random_uuid(), q_id, 'Boar', false, 2),
        (gen_random_uuid(), q_id, 'Beaver', false, 3),
        (gen_random_uuid(), q_id, 'Bat', false, 4);

    -- =============================================
    -- TEST 3: Colors - Basic (Цвета - Базовый)
    -- =============================================
    test_id := gen_random_uuid();
    INSERT INTO tests (id, category_id, title, description, difficulty, points_reward, is_published, display_order)
    VALUES (test_id, cat_colors, 'Цвета: Базовый', 'Изучи основные цвета', 'EASY', 10, true, 1);

    -- Question 1
    q_id := gen_random_uuid();
    INSERT INTO questions (id, test_id, type, text, display_order, points)
    VALUES (q_id, test_id, 'TEXT_SELECT', 'Как будет "красный" по-английски?', 1, 1);
    INSERT INTO answers (id, question_id, text, is_correct, display_order) VALUES
        (gen_random_uuid(), q_id, 'Red', true, 1),
        (gen_random_uuid(), q_id, 'Blue', false, 2),
        (gen_random_uuid(), q_id, 'Green', false, 3),
        (gen_random_uuid(), q_id, 'Yellow', false, 4);

    -- Question 2
    q_id := gen_random_uuid();
    INSERT INTO questions (id, test_id, type, text, display_order, points)
    VALUES (q_id, test_id, 'TEXT_SELECT', 'Как будет "синий" по-английски?', 2, 1);
    INSERT INTO answers (id, question_id, text, is_correct, display_order) VALUES
        (gen_random_uuid(), q_id, 'Blue', true, 1),
        (gen_random_uuid(), q_id, 'Black', false, 2),
        (gen_random_uuid(), q_id, 'Brown', false, 3),
        (gen_random_uuid(), q_id, 'Beige', false, 4);

    -- Question 3
    q_id := gen_random_uuid();
    INSERT INTO questions (id, test_id, type, text, display_order, points)
    VALUES (q_id, test_id, 'TEXT_SELECT', 'Как будет "зелёный" по-английски?', 3, 1);
    INSERT INTO answers (id, question_id, text, is_correct, display_order) VALUES
        (gen_random_uuid(), q_id, 'Green', true, 1),
        (gen_random_uuid(), q_id, 'Grey', false, 2),
        (gen_random_uuid(), q_id, 'Gold', false, 3),
        (gen_random_uuid(), q_id, 'Grape', false, 4);

    -- Question 4
    q_id := gen_random_uuid();
    INSERT INTO questions (id, test_id, type, text, display_order, points)
    VALUES (q_id, test_id, 'TEXT_SELECT', 'Как будет "жёлтый" по-английски?', 4, 1);
    INSERT INTO answers (id, question_id, text, is_correct, display_order) VALUES
        (gen_random_uuid(), q_id, 'Yellow', true, 1),
        (gen_random_uuid(), q_id, 'Orange', false, 2),
        (gen_random_uuid(), q_id, 'Pink', false, 3),
        (gen_random_uuid(), q_id, 'Purple', false, 4);

    -- Question 5
    q_id := gen_random_uuid();
    INSERT INTO questions (id, test_id, type, text, display_order, points)
    VALUES (q_id, test_id, 'TEXT_SELECT', 'Как будет "чёрный" по-английски?', 5, 1);
    INSERT INTO answers (id, question_id, text, is_correct, display_order) VALUES
        (gen_random_uuid(), q_id, 'Black', true, 1),
        (gen_random_uuid(), q_id, 'White', false, 2),
        (gen_random_uuid(), q_id, 'Blue', false, 3),
        (gen_random_uuid(), q_id, 'Brown', false, 4);

    -- =============================================
    -- TEST 4: Numbers - Basic (Числа - Базовый)
    -- =============================================
    test_id := gen_random_uuid();
    INSERT INTO tests (id, category_id, title, description, difficulty, points_reward, is_published, display_order)
    VALUES (test_id, cat_numbers, 'Числа 1-10', 'Изучи числа от 1 до 10', 'EASY', 10, true, 1);

    -- Question 1
    q_id := gen_random_uuid();
    INSERT INTO questions (id, test_id, type, text, display_order, points)
    VALUES (q_id, test_id, 'TEXT_SELECT', 'Как будет "один" по-английски?', 1, 1);
    INSERT INTO answers (id, question_id, text, is_correct, display_order) VALUES
        (gen_random_uuid(), q_id, 'One', true, 1),
        (gen_random_uuid(), q_id, 'Two', false, 2),
        (gen_random_uuid(), q_id, 'Three', false, 3),
        (gen_random_uuid(), q_id, 'Four', false, 4);

    -- Question 2
    q_id := gen_random_uuid();
    INSERT INTO questions (id, test_id, type, text, display_order, points)
    VALUES (q_id, test_id, 'TEXT_SELECT', 'Как будет "пять" по-английски?', 2, 1);
    INSERT INTO answers (id, question_id, text, is_correct, display_order) VALUES
        (gen_random_uuid(), q_id, 'Five', true, 1),
        (gen_random_uuid(), q_id, 'Four', false, 2),
        (gen_random_uuid(), q_id, 'Six', false, 3),
        (gen_random_uuid(), q_id, 'Seven', false, 4);

    -- Question 3
    q_id := gen_random_uuid();
    INSERT INTO questions (id, test_id, type, text, display_order, points)
    VALUES (q_id, test_id, 'TEXT_SELECT', 'Как будет "десять" по-английски?', 3, 1);
    INSERT INTO answers (id, question_id, text, is_correct, display_order) VALUES
        (gen_random_uuid(), q_id, 'Ten', true, 1),
        (gen_random_uuid(), q_id, 'Twelve', false, 2),
        (gen_random_uuid(), q_id, 'Twenty', false, 3),
        (gen_random_uuid(), q_id, 'Thirteen', false, 4);

    -- Question 4
    q_id := gen_random_uuid();
    INSERT INTO questions (id, test_id, type, text, display_order, points)
    VALUES (q_id, test_id, 'TEXT_SELECT', 'Как будет "три" по-английски?', 4, 1);
    INSERT INTO answers (id, question_id, text, is_correct, display_order) VALUES
        (gen_random_uuid(), q_id, 'Three', true, 1),
        (gen_random_uuid(), q_id, 'Tree', false, 2),
        (gen_random_uuid(), q_id, 'Thirty', false, 3),
        (gen_random_uuid(), q_id, 'Two', false, 4);

    -- Question 5
    q_id := gen_random_uuid();
    INSERT INTO questions (id, test_id, type, text, display_order, points)
    VALUES (q_id, test_id, 'TEXT_SELECT', 'Как будет "семь" по-английски?', 5, 1);
    INSERT INTO answers (id, question_id, text, is_correct, display_order) VALUES
        (gen_random_uuid(), q_id, 'Seven', true, 1),
        (gen_random_uuid(), q_id, 'Six', false, 2),
        (gen_random_uuid(), q_id, 'Seventeen', false, 3),
        (gen_random_uuid(), q_id, 'Seventy', false, 4);

    -- =============================================
    -- TEST 5: Food - Basic (Еда - Базовый)
    -- =============================================
    test_id := gen_random_uuid();
    INSERT INTO tests (id, category_id, title, description, difficulty, points_reward, is_published, display_order)
    VALUES (test_id, cat_food, 'Еда: Фрукты', 'Изучи названия фруктов', 'EASY', 10, true, 1);

    -- Question 1
    q_id := gen_random_uuid();
    INSERT INTO questions (id, test_id, type, text, display_order, points)
    VALUES (q_id, test_id, 'TEXT_SELECT', 'Как будет "яблоко" по-английски?', 1, 1);
    INSERT INTO answers (id, question_id, text, is_correct, display_order) VALUES
        (gen_random_uuid(), q_id, 'Apple', true, 1),
        (gen_random_uuid(), q_id, 'Apricot', false, 2),
        (gen_random_uuid(), q_id, 'Avocado', false, 3),
        (gen_random_uuid(), q_id, 'Almond', false, 4);

    -- Question 2
    q_id := gen_random_uuid();
    INSERT INTO questions (id, test_id, type, text, display_order, points)
    VALUES (q_id, test_id, 'TEXT_SELECT', 'Как будет "банан" по-английски?', 2, 1);
    INSERT INTO answers (id, question_id, text, is_correct, display_order) VALUES
        (gen_random_uuid(), q_id, 'Banana', true, 1),
        (gen_random_uuid(), q_id, 'Berry', false, 2),
        (gen_random_uuid(), q_id, 'Bread', false, 3),
        (gen_random_uuid(), q_id, 'Bean', false, 4);

    -- Question 3
    q_id := gen_random_uuid();
    INSERT INTO questions (id, test_id, type, text, display_order, points)
    VALUES (q_id, test_id, 'TEXT_SELECT', 'Как будет "апельсин" по-английски?', 3, 1);
    INSERT INTO answers (id, question_id, text, is_correct, display_order) VALUES
        (gen_random_uuid(), q_id, 'Orange', true, 1),
        (gen_random_uuid(), q_id, 'Onion', false, 2),
        (gen_random_uuid(), q_id, 'Olive', false, 3),
        (gen_random_uuid(), q_id, 'Oat', false, 4);

    -- Question 4
    q_id := gen_random_uuid();
    INSERT INTO questions (id, test_id, type, text, display_order, points)
    VALUES (q_id, test_id, 'TEXT_SELECT', 'Как будет "виноград" по-английски?', 4, 1);
    INSERT INTO answers (id, question_id, text, is_correct, display_order) VALUES
        (gen_random_uuid(), q_id, 'Grapes', true, 1),
        (gen_random_uuid(), q_id, 'Grapefruit', false, 2),
        (gen_random_uuid(), q_id, 'Guava', false, 3),
        (gen_random_uuid(), q_id, 'Gooseberry', false, 4);

    -- Question 5
    q_id := gen_random_uuid();
    INSERT INTO questions (id, test_id, type, text, display_order, points)
    VALUES (q_id, test_id, 'TEXT_SELECT', 'Как будет "клубника" по-английски?', 5, 1);
    INSERT INTO answers (id, question_id, text, is_correct, display_order) VALUES
        (gen_random_uuid(), q_id, 'Strawberry', true, 1),
        (gen_random_uuid(), q_id, 'Raspberry', false, 2),
        (gen_random_uuid(), q_id, 'Blueberry', false, 3),
        (gen_random_uuid(), q_id, 'Blackberry', false, 4);

    -- =============================================
    -- TEST 6: Family (Семья)
    -- =============================================
    test_id := gen_random_uuid();
    INSERT INTO tests (id, category_id, title, description, difficulty, points_reward, is_published, display_order)
    VALUES (test_id, cat_family, 'Семья: Базовый', 'Изучи членов семьи', 'EASY', 10, true, 1);

    -- Question 1
    q_id := gen_random_uuid();
    INSERT INTO questions (id, test_id, type, text, display_order, points)
    VALUES (q_id, test_id, 'TEXT_SELECT', 'Как будет "мама" по-английски?', 1, 1);
    INSERT INTO answers (id, question_id, text, is_correct, display_order) VALUES
        (gen_random_uuid(), q_id, 'Mother', true, 1),
        (gen_random_uuid(), q_id, 'Father', false, 2),
        (gen_random_uuid(), q_id, 'Sister', false, 3),
        (gen_random_uuid(), q_id, 'Brother', false, 4);

    -- Question 2
    q_id := gen_random_uuid();
    INSERT INTO questions (id, test_id, type, text, display_order, points)
    VALUES (q_id, test_id, 'TEXT_SELECT', 'Как будет "папа" по-английски?', 2, 1);
    INSERT INTO answers (id, question_id, text, is_correct, display_order) VALUES
        (gen_random_uuid(), q_id, 'Father', true, 1),
        (gen_random_uuid(), q_id, 'Mother', false, 2),
        (gen_random_uuid(), q_id, 'Uncle', false, 3),
        (gen_random_uuid(), q_id, 'Aunt', false, 4);

    -- Question 3
    q_id := gen_random_uuid();
    INSERT INTO questions (id, test_id, type, text, display_order, points)
    VALUES (q_id, test_id, 'TEXT_SELECT', 'Как будет "брат" по-английски?', 3, 1);
    INSERT INTO answers (id, question_id, text, is_correct, display_order) VALUES
        (gen_random_uuid(), q_id, 'Brother', true, 1),
        (gen_random_uuid(), q_id, 'Sister', false, 2),
        (gen_random_uuid(), q_id, 'Son', false, 3),
        (gen_random_uuid(), q_id, 'Daughter', false, 4);

    -- Question 4
    q_id := gen_random_uuid();
    INSERT INTO questions (id, test_id, type, text, display_order, points)
    VALUES (q_id, test_id, 'TEXT_SELECT', 'Как будет "сестра" по-английски?', 4, 1);
    INSERT INTO answers (id, question_id, text, is_correct, display_order) VALUES
        (gen_random_uuid(), q_id, 'Sister', true, 1),
        (gen_random_uuid(), q_id, 'Brother', false, 2),
        (gen_random_uuid(), q_id, 'Cousin', false, 3),
        (gen_random_uuid(), q_id, 'Niece', false, 4);

    -- Question 5
    q_id := gen_random_uuid();
    INSERT INTO questions (id, test_id, type, text, display_order, points)
    VALUES (q_id, test_id, 'TEXT_SELECT', 'Как будет "бабушка" по-английски?', 5, 1);
    INSERT INTO answers (id, question_id, text, is_correct, display_order) VALUES
        (gen_random_uuid(), q_id, 'Grandmother', true, 1),
        (gen_random_uuid(), q_id, 'Grandfather', false, 2),
        (gen_random_uuid(), q_id, 'Grandchild', false, 3),
        (gen_random_uuid(), q_id, 'Grandson', false, 4);

    -- =============================================
    -- TEST 7: Clothes (Одежда)
    -- =============================================
    test_id := gen_random_uuid();
    INSERT INTO tests (id, category_id, title, description, difficulty, points_reward, is_published, display_order)
    VALUES (test_id, cat_clothes, 'Одежда: Базовый', 'Изучи предметы одежды', 'EASY', 10, true, 1);

    -- Question 1
    q_id := gen_random_uuid();
    INSERT INTO questions (id, test_id, type, text, display_order, points)
    VALUES (q_id, test_id, 'TEXT_SELECT', 'Как будет "рубашка" по-английски?', 1, 1);
    INSERT INTO answers (id, question_id, text, is_correct, display_order) VALUES
        (gen_random_uuid(), q_id, 'Shirt', true, 1),
        (gen_random_uuid(), q_id, 'Shorts', false, 2),
        (gen_random_uuid(), q_id, 'Shoes', false, 3),
        (gen_random_uuid(), q_id, 'Skirt', false, 4);

    -- Question 2
    q_id := gen_random_uuid();
    INSERT INTO questions (id, test_id, type, text, display_order, points)
    VALUES (q_id, test_id, 'TEXT_SELECT', 'Как будет "брюки" по-английски?', 2, 1);
    INSERT INTO answers (id, question_id, text, is_correct, display_order) VALUES
        (gen_random_uuid(), q_id, 'Pants', true, 1),
        (gen_random_uuid(), q_id, 'Pajamas', false, 2),
        (gen_random_uuid(), q_id, 'Pullover', false, 3),
        (gen_random_uuid(), q_id, 'Poncho', false, 4);

    -- Question 3
    q_id := gen_random_uuid();
    INSERT INTO questions (id, test_id, type, text, display_order, points)
    VALUES (q_id, test_id, 'TEXT_SELECT', 'Как будет "платье" по-английски?', 3, 1);
    INSERT INTO answers (id, question_id, text, is_correct, display_order) VALUES
        (gen_random_uuid(), q_id, 'Dress', true, 1),
        (gen_random_uuid(), q_id, 'Denim', false, 2),
        (gen_random_uuid(), q_id, 'Down jacket', false, 3),
        (gen_random_uuid(), q_id, 'Duffle coat', false, 4);

    -- Question 4
    q_id := gen_random_uuid();
    INSERT INTO questions (id, test_id, type, text, display_order, points)
    VALUES (q_id, test_id, 'TEXT_SELECT', 'Как будет "шапка" по-английски?', 4, 1);
    INSERT INTO answers (id, question_id, text, is_correct, display_order) VALUES
        (gen_random_uuid(), q_id, 'Hat', true, 1),
        (gen_random_uuid(), q_id, 'Hood', false, 2),
        (gen_random_uuid(), q_id, 'Heel', false, 3),
        (gen_random_uuid(), q_id, 'Handbag', false, 4);

    -- Question 5
    q_id := gen_random_uuid();
    INSERT INTO questions (id, test_id, type, text, display_order, points)
    VALUES (q_id, test_id, 'TEXT_SELECT', 'Как будет "куртка" по-английски?', 5, 1);
    INSERT INTO answers (id, question_id, text, is_correct, display_order) VALUES
        (gen_random_uuid(), q_id, 'Jacket', true, 1),
        (gen_random_uuid(), q_id, 'Jeans', false, 2),
        (gen_random_uuid(), q_id, 'Jumper', false, 3),
        (gen_random_uuid(), q_id, 'Jersey', false, 4);

    -- =============================================
    -- TEST 8: Reverse Translation - Animals
    -- =============================================
    test_id := gen_random_uuid();
    INSERT INTO tests (id, category_id, title, description, difficulty, points_reward, is_published, display_order)
    VALUES (test_id, cat_animals, 'Животные: Перевод', 'Переведи с английского на русский', 'MEDIUM', 15, true, 3);

    -- Question 1
    q_id := gen_random_uuid();
    INSERT INTO questions (id, test_id, type, text, display_order, points)
    VALUES (q_id, test_id, 'TEXT_SELECT', 'What does "Dog" mean in Russian?', 1, 1);
    INSERT INTO answers (id, question_id, text, is_correct, display_order) VALUES
        (gen_random_uuid(), q_id, 'Собака', true, 1),
        (gen_random_uuid(), q_id, 'Кошка', false, 2),
        (gen_random_uuid(), q_id, 'Птица', false, 3),
        (gen_random_uuid(), q_id, 'Рыба', false, 4);

    -- Question 2
    q_id := gen_random_uuid();
    INSERT INTO questions (id, test_id, type, text, display_order, points)
    VALUES (q_id, test_id, 'TEXT_SELECT', 'What does "Elephant" mean in Russian?', 2, 1);
    INSERT INTO answers (id, question_id, text, is_correct, display_order) VALUES
        (gen_random_uuid(), q_id, 'Слон', true, 1),
        (gen_random_uuid(), q_id, 'Тигр', false, 2),
        (gen_random_uuid(), q_id, 'Лев', false, 3),
        (gen_random_uuid(), q_id, 'Медведь', false, 4);

    -- Question 3
    q_id := gen_random_uuid();
    INSERT INTO questions (id, test_id, type, text, display_order, points)
    VALUES (q_id, test_id, 'TEXT_SELECT', 'What does "Butterfly" mean in Russian?', 3, 1);
    INSERT INTO answers (id, question_id, text, is_correct, display_order) VALUES
        (gen_random_uuid(), q_id, 'Бабочка', true, 1),
        (gen_random_uuid(), q_id, 'Пчела', false, 2),
        (gen_random_uuid(), q_id, 'Муха', false, 3),
        (gen_random_uuid(), q_id, 'Жук', false, 4);

    -- Question 4
    q_id := gen_random_uuid();
    INSERT INTO questions (id, test_id, type, text, display_order, points)
    VALUES (q_id, test_id, 'TEXT_SELECT', 'What does "Rabbit" mean in Russian?', 4, 1);
    INSERT INTO answers (id, question_id, text, is_correct, display_order) VALUES
        (gen_random_uuid(), q_id, 'Кролик', true, 1),
        (gen_random_uuid(), q_id, 'Крыса', false, 2),
        (gen_random_uuid(), q_id, 'Белка', false, 3),
        (gen_random_uuid(), q_id, 'Мышь', false, 4);

    -- Question 5
    q_id := gen_random_uuid();
    INSERT INTO questions (id, test_id, type, text, display_order, points)
    VALUES (q_id, test_id, 'TEXT_SELECT', 'What does "Wolf" mean in Russian?', 5, 1);
    INSERT INTO answers (id, question_id, text, is_correct, display_order) VALUES
        (gen_random_uuid(), q_id, 'Волк', true, 1),
        (gen_random_uuid(), q_id, 'Лиса', false, 2),
        (gen_random_uuid(), q_id, 'Заяц', false, 3),
        (gen_random_uuid(), q_id, 'Олень', false, 4);

END $$;
