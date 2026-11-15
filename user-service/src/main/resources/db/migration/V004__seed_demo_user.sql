INSERT INTO users (email, password_hash, name, created_at)
VALUES ('demo@lifeos.no',
        '$2a$10$RGM5ymUkZrdDZ2OqtiLIT.IUON6GARSPGUyEbdj5WAYlqjvGd1zSW',
        'Demo User',
        now())
ON CONFLICT (email) DO NOTHING;