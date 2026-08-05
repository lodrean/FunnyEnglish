-- Fix achievements.id type: entity использует String (varchar(255)), а V1 создал UUID.
-- Обнаружено на staging с ddl-auto=validate (prod-конфиг): Schema-validation wrong column type.
-- user_achievements.achievement_id уже VARCHAR(50) (V9), FK отсутствует — конверсия безопасна,
-- данные сохраняются через id::text.

ALTER TABLE achievements ALTER COLUMN id TYPE VARCHAR(255) USING id::text;
