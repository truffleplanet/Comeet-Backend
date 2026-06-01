-- Local-only seed data for manual API testing.
-- Password for all users is: password1234

INSERT INTO users (id, name, email, password, nick_name, social_id, role)
VALUES (1,
        'Local User',
        'user@example.com',
        '$2a$10$U0imOnbLo5iGNBWrxugoM.0GxE4F7sRFwmv19PMVeQ06oYaYiYOCm',
        'local-user',
        'user@example.com',
        'USER'),
       (2,
        'Local Manager',
        'manager@example.com',
        '$2a$10$U0imOnbLo5iGNBWrxugoM.0GxE4F7sRFwmv19PMVeQ06oYaYiYOCm',
        'local-manager',
        'manager@example.com',
        'MANAGER'),
       (3,
        'Local Admin',
        'admin@example.com',
        '$2a$10$U0imOnbLo5iGNBWrxugoM.0GxE4F7sRFwmv19PMVeQ06oYaYiYOCm',
        'local-admin',
        'admin@example.com',
        'ADMIN')
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    email = VALUES(email),
    password = VALUES(password),
    nick_name = VALUES(nick_name),
    social_id = VALUES(social_id),
    role = VALUES(role);

