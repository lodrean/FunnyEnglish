-- SEC: удаление демо-пользователя, вставленного миграцией V1 (PROJECT-REVIEW-2026-08-28 §2.1 Б1).
-- V1 больше не является источником demo-юзера: создание — только в AdminUserInitializer
-- при app.demo-user.enabled=true (DEMO_USER_ENABLED), на prod флаг выключен.
-- Саму V1 не правим: она уже применена на существующих БД, изменение сломает
-- checksum-валидацию Flyway. На чистой БД demo вставляется V1 и тут же удаляется
-- этой миграцией в рамках того же прогона migrate (приложение стартует после миграций).
-- Зависимые строки (submissions, сообщения и пр.) удаляются каскадом FK ON DELETE CASCADE.
-- media_files.uploaded_by — единственная некаскадная nullable-ссылка на users
-- (FK без ON DELETE): обнуляем, чтобы DELETE не упал, если demo где-то оказался uploader'ом.
UPDATE media_files SET uploaded_by = NULL
WHERE uploaded_by IN (SELECT id FROM users WHERE email = 'demo@sotospeak.app');

DELETE FROM users WHERE email = 'demo@sotospeak.app';
