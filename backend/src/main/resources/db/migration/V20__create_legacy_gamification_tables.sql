-- Досоздание legacy-таблиц геймификации/адаптивных уроков, которые исторически
-- создавались Hibernate (ddl-auto=update в dev) и отсутствовали в Flyway.
-- Обнаружено на staging с ddl-auto=validate (prod-конфиг): Schema-validation missing table.
-- DDL соответствует схеме, выстроенной Hibernate в dev-БД (pg_dump --schema-only).

CREATE TABLE IF NOT EXISTS adaptive_lessons (
    id uuid NOT NULL,
    completed_at timestamp(6) with time zone,
    correct_answers integer NOT NULL,
    current_difficulty character varying(255) NOT NULL,
    current_segment_index integer NOT NULL,
    questions_answered integer NOT NULL,
    started_at timestamp(6) with time zone NOT NULL,
    status character varying(255) NOT NULL,
    time_spent_seconds integer NOT NULL,
    total_segments integer NOT NULL,
    user_id uuid NOT NULL,
    CONSTRAINT adaptive_lessons_pkey PRIMARY KEY (id),
    CONSTRAINT adaptive_lessons_current_difficulty_check CHECK (((current_difficulty)::text = ANY ((ARRAY['BEGINNER'::character varying, 'ELEMENTARY'::character varying, 'INTERMEDIATE'::character varying, 'ADVANCED'::character varying])::text[]))),
    CONSTRAINT adaptive_lessons_status_check CHECK (((status)::text = ANY ((ARRAY['IN_PROGRESS'::character varying, 'ON_BREAK'::character varying, 'COMPLETED'::character varying, 'ABANDONED'::character varying])::text[])))
);

CREATE TABLE IF NOT EXISTS adaptive_lesson_weak_areas (
    lesson_id uuid NOT NULL,
    skill_type character varying(255),
    CONSTRAINT fkq9trxgy8ipcmi6v2pg6q8tlm3 FOREIGN KEY (lesson_id) REFERENCES adaptive_lessons(id)
);

CREATE TABLE IF NOT EXISTS lesson_question_history (
    id uuid NOT NULL,
    answer_id uuid,
    answered_at timestamp(6) with time zone NOT NULL,
    difficulty_at_time character varying(255) NOT NULL,
    is_correct boolean NOT NULL,
    question_id uuid NOT NULL,
    time_spent_seconds integer NOT NULL,
    lesson_id uuid NOT NULL,
    CONSTRAINT lesson_question_history_pkey PRIMARY KEY (id),
    CONSTRAINT fk5mmw5x45py3wel8yiiwkbopbj FOREIGN KEY (lesson_id) REFERENCES adaptive_lessons(id)
);

CREATE TABLE IF NOT EXISTS lesson_segments (
    id uuid NOT NULL,
    completed_at timestamp(6) with time zone,
    display_order integer NOT NULL,
    estimated_duration_seconds integer NOT NULL,
    learning_objective character varying(255),
    type character varying(255) NOT NULL,
    lesson_id uuid NOT NULL,
    CONSTRAINT lesson_segments_pkey PRIMARY KEY (id),
    CONSTRAINT lesson_segments_type_check CHECK (((type)::text = ANY ((ARRAY['INTRO'::character varying, 'PRACTICE'::character varying, 'CHALLENGE'::character varying, 'REVIEW'::character varying, 'GRAMMAR_HINT'::character varying])::text[]))),
    CONSTRAINT fk30d1wmfy8lxj3e5p4yi9rrmay FOREIGN KEY (lesson_id) REFERENCES adaptive_lessons(id)
);

CREATE TABLE IF NOT EXISTS quests (
    id uuid NOT NULL,
    completed_at timestamp(6) with time zone,
    created_at timestamp(6) with time zone NOT NULL,
    current_value integer NOT NULL,
    description character varying(255) NOT NULL,
    expires_at timestamp(6) with time zone NOT NULL,
    is_completed boolean NOT NULL,
    is_reward_claimed boolean NOT NULL,
    quest_type character varying(255) NOT NULL,
    reward_gems integer NOT NULL,
    reward_xp integer NOT NULL,
    target_value integer NOT NULL,
    title character varying(255) NOT NULL,
    user_id uuid NOT NULL,
    CONSTRAINT quests_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS user_skills (
    id uuid NOT NULL,
    last_updated timestamp(6) with time zone NOT NULL,
    mastery_level real NOT NULL,
    questions_attempted integer NOT NULL,
    questions_correct integer NOT NULL,
    skill_type character varying(255) NOT NULL,
    user_id uuid NOT NULL,
    CONSTRAINT user_skills_pkey PRIMARY KEY (id),
    CONSTRAINT user_skills_skill_type_check CHECK (((skill_type)::text = ANY ((ARRAY['GRAMMAR_ARTICLES'::character varying, 'GRAMMAR_TENSES'::character varying, 'VOCABULARY_NOUNS'::character varying, 'VOCABULARY_VERBS'::character varying, 'VOCABULARY_ADJECTIVES'::character varying, 'PRONUNCIATION'::character varying, 'LISTENING'::character varying, 'READING'::character varying])::text[])))
);

CREATE TABLE IF NOT EXISTS user_streaks (
    user_id uuid NOT NULL,
    current_streak integer NOT NULL,
    freezes_used_this_week integer NOT NULL,
    last_activity_date date,
    longest_streak integer NOT NULL,
    previous_streak_before_break integer,
    updated_at timestamp(6) with time zone NOT NULL,
    week_reset_at timestamp(6) with time zone NOT NULL,
    CONSTRAINT user_streaks_pkey PRIMARY KEY (user_id)
);

CREATE TABLE IF NOT EXISTS xp_history (
    id uuid NOT NULL,
    amount integer NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    description character varying(255),
    source character varying(255) NOT NULL,
    user_id uuid NOT NULL,
    CONSTRAINT xp_history_pkey PRIMARY KEY (id)
);
