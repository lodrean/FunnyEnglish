# admin-login Specification

## Purpose
TBD - created by archiving change fix-mvp-acceptance. Update Purpose after archive.
## Requirements
### Requirement: Дефолтные креды администратора
Default admin email in `application.yml` MUST be `admin@sotospeak.com` (matching `docker-compose.yml` and README). On startup `AdminUserInitializer` MUST ensure exactly one admin user exists with: email = `${ADMIN_EMAIL}`, password = `${ADMIN_PASSWORD}` (hashed), role = `ADMIN`, `emailVerified = true`. Demo user behavior (`demo@sotospeak.app/demo123`) MUST remain unchanged.

#### Scenario: Вход админа на свежем окружении
- **WHEN** backend стартует на чистой БД и выполняется login `admin@sotospeak.com / admin123`
- **THEN** возвращается 200 и JWT с role `ADMIN`

### Requirement: Синхронизация существующего админа
If an admin already exists with a different email (e.g. legacy `admin@sotospeak.app`), initializer MUST update the email as well as the password/hash. If the admin password env changes, initializer MUST re-hash and save the new password.

#### Scenario: Смена email админа через env
- **GIVEN** в БД есть admin с legacy-email
- **WHEN** backend стартует с новым `ADMIN_EMAIL`
- **THEN** существующая запись админа обновляется (email, passwordHash, role, emailVerified), дубликат не создаётся

