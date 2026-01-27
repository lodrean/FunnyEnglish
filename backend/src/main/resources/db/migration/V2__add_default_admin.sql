-- Default admin user
-- Password: admin123 (BCrypt hash)
INSERT INTO users (id, email, password_hash, display_name, role, auth_provider)
VALUES (
    gen_random_uuid(),
    'admin@funnyenglish.app',
    '$2a$10$N9qo8uLOickgx2ZMRZoMye0sYzW6Iq5fGdP9d8lFZlLz/F3QdQGp6',
    'Admin',
    'ADMIN',
    'EMAIL'
);
