-- Test Users
INSERT INTO users (id, email, password, name, created_at)
VALUES 
    ('11111111-1111-1111-1111-111111111111', 'test1@example.com', '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG', 'Test User 1', CURRENT_TIMESTAMP),
    ('22222222-2222-2222-2222-222222222222', 'test2@example.com', '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG', 'Test User 2', CURRENT_TIMESTAMP);

-- Test URLs
-- INSERT INTO urls (id, short_code, original_url, created_at, clicks, user_id)
-- VALUES 
--     (1, 'abc123', 'https://www.example.com/page1', CURRENT_TIMESTAMP, 0, '11111111-1111-1111-1111-111111111111'),
--     (2, 'def456', 'https://www.example.com/page2', CURRENT_TIMESTAMP, 5, '11111111-1111-1111-1111-111111111111'),
--     (3, 'ghi789', 'https://www.example.com/page3', CURRENT_TIMESTAMP, 10, '22222222-2222-2222-2222-222222222222'); 