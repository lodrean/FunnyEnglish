-- Sample IMAGE_WORD_MATCH test data
-- Creates a "Kitchen Objects" test with IMAGE_WORD_MATCH question type

DO $$
DECLARE
    cat_objects UUID;
    test_id UUID;
    q_id UUID;
    iw_q_id UUID;
    word1_id VARCHAR(50) := 'word_fridge_001';
    word2_id VARCHAR(50) := 'word_table_002';
    word3_id VARCHAR(50) := 'word_knife_003';
    word4_id VARCHAR(50) := 'word_spoon_004';
    hotspot1_id VARCHAR(50) := 'hs_fridge_001';
    hotspot2_id VARCHAR(50) := 'hs_table_002';
    hotspot3_id VARCHAR(50) := 'hs_knife_003';
    hotspot4_id VARCHAR(50) := 'hs_spoon_004';
BEGIN
    -- =============================================
    -- Create "Objects" category if not exists
    -- =============================================
    SELECT id INTO cat_objects FROM categories WHERE name = 'Предметы';
    
    IF cat_objects IS NULL THEN
        cat_objects := gen_random_uuid();
        INSERT INTO categories (id, name, description, icon_url, display_order, is_active)
        VALUES (
            cat_objects, 
            'Предметы', 
            'Изучи названия предметов в доме',
            'https://cdn.funnyenglish.com/icons/objects.png',
            10,
            true
        );
    END IF;

    -- =============================================
    -- TEST: Kitchen Objects (IMAGE_WORD_MATCH)
    -- =============================================
    test_id := gen_random_uuid();
    INSERT INTO tests (
        id, 
        category_id, 
        title, 
        description, 
        difficulty, 
        points_reward, 
        is_published, 
        display_order,
        thumbnail_url
    ) VALUES (
        test_id, 
        cat_objects, 
        'Кухня: Найди предмет', 
        'Перетащи слова к предметам на картинке кухни',
        'EASY', 
        20, 
        true, 
        1,
        'https://cdn.funnyenglish.com/images/kitchen_thumbnail.jpg'
    );

    -- =============================================
    -- QUESTION: IMAGE_WORD_MATCH - Kitchen
    -- =============================================
    q_id := gen_random_uuid();
    INSERT INTO questions (
        id, 
        test_id, 
        type, 
        title,
        text, 
        display_order, 
        points,
        image_url,
        is_published
    ) VALUES (
        q_id, 
        test_id, 
        'IMAGE_WORD_MATCH', 
        'Match kitchen objects',
        'Match the words to the objects in the kitchen',
        1, 
        20,
        'https://images.unsplash.com/photo-1556911220-e15b29be8c8f?w=800&q=80',
        true
    );

    -- IMAGE_WORD_MATCH question data
    iw_q_id := q_id;
    INSERT INTO image_word_match_questions (
        id,
        question_id,
        test_id,
        image_url,
        instruction,
        points
    ) VALUES (
        gen_random_uuid(),
        iw_q_id,
        test_id,
        'https://images.unsplash.com/photo-1556911220-e15b29be8c8f?w=800&q=80',
        'Match the words to the objects in the kitchen',
        20
    );

    -- Words for the question
    INSERT INTO image_word_match_words (
        id,
        question_id,
        word_id,
        text,
        translation,
        display_order
    ) VALUES 
        (gen_random_uuid(), iw_q_id, word1_id, 'fridge', 'холодильник', 1),
        (gen_random_uuid(), iw_q_id, word2_id, 'table', 'стол', 2),
        (gen_random_uuid(), iw_q_id, word3_id, 'knife', 'нож', 3),
        (gen_random_uuid(), iw_q_id, word4_id, 'spoon', 'ложка', 4);

    -- Hotspots (relative coordinates 0.0-1.0)
    -- Fridge: top-left area (0.05, 0.15, 0.20, 0.35)
    -- Table: center-bottom area (0.30, 0.55, 0.40, 0.30)
    -- Knife: right side (0.75, 0.60, 0.15, 0.08)
    -- Spoon: near knife (0.75, 0.72, 0.15, 0.08)
    INSERT INTO image_word_match_hotspots (
        id,
        question_id,
        hotspot_id,
        x,
        y,
        width,
        height,
        shape,
        word_id
    ) VALUES 
        (gen_random_uuid(), iw_q_id, hotspot1_id, 0.05, 0.15, 0.20, 0.35, 'RECTANGLE', word1_id),
        (gen_random_uuid(), iw_q_id, hotspot2_id, 0.30, 0.55, 0.40, 0.30, 'RECTANGLE', word2_id),
        (gen_random_uuid(), iw_q_id, hotspot3_id, 0.75, 0.60, 0.15, 0.08, 'RECTANGLE', word3_id),
        (gen_random_uuid(), iw_q_id, hotspot4_id, 0.75, 0.72, 0.15, 0.08, 'RECTANGLE', word4_id);

    -- =============================================
    -- TEST 2: Living Room Objects
    -- =============================================
    test_id := gen_random_uuid();
    INSERT INTO tests (
        id, 
        category_id, 
        title, 
        description, 
        difficulty, 
        points_reward, 
        is_published, 
        display_order,
        thumbnail_url
    ) VALUES (
        test_id, 
        cat_objects, 
        'Гостиная: Найди предмет', 
        'Перетащи слова к предметам в гостиной',
        'EASY', 
        15, 
        true, 
        2,
        'https://cdn.funnyenglish.com/images/livingroom_thumbnail.jpg'
    );

    -- Reset word/hotspot IDs for second test
    word1_id := 'word_sofa_001';
    word2_id := 'word_tv_002';
    word3_id := 'word_lamp_003';
    hotspot1_id := 'hs_sofa_001';
    hotspot2_id := 'hs_tv_002';
    hotspot3_id := 'hs_lamp_003';

    q_id := gen_random_uuid();
    INSERT INTO questions (
        id, 
        test_id, 
        type, 
        title,
        text, 
        display_order, 
        points,
        image_url,
        is_published
    ) VALUES (
        q_id, 
        test_id, 
        'IMAGE_WORD_MATCH', 
        'Match living room objects',
        'Match the words to the objects in the living room',
        1, 
        15,
        'https://images.unsplash.com/photo-1586023492125-27b2c045efd7?w=800&q=80',
        true
    );

    iw_q_id := q_id;
    INSERT INTO image_word_match_questions (
        id,
        question_id,
        test_id,
        image_url,
        instruction,
        points
    ) VALUES (
        gen_random_uuid(),
        iw_q_id,
        test_id,
        'https://images.unsplash.com/photo-1586023492125-27b2c045efd7?w=800&q=80',
        'Match the words to the objects in the living room',
        15
    );

    INSERT INTO image_word_match_words (
        id,
        question_id,
        word_id,
        text,
        translation,
        display_order
    ) VALUES 
        (gen_random_uuid(), iw_q_id, word1_id, 'sofa', 'диван', 1),
        (gen_random_uuid(), iw_q_id, word2_id, 'TV', 'телевизор', 2),
        (gen_random_uuid(), iw_q_id, word3_id, 'lamp', 'лампа', 3);

    INSERT INTO image_word_match_hotspots (
        id,
        question_id,
        hotspot_id,
        x,
        y,
        width,
        height,
        shape,
        word_id
    ) VALUES 
        (gen_random_uuid(), iw_q_id, hotspot1_id, 0.15, 0.50, 0.35, 0.35, 'RECTANGLE', word1_id),
        (gen_random_uuid(), iw_q_id, hotspot2_id, 0.60, 0.40, 0.25, 0.20, 'RECTANGLE', word2_id),
        (gen_random_uuid(), iw_q_id, hotspot3_id, 0.75, 0.15, 0.10, 0.25, 'RECTANGLE', word3_id);

END $$;
