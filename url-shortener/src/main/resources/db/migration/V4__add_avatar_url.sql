-- Add avatar_url column to users table
ALTER TABLE users
ADD COLUMN avatar_url VARCHAR(255);

-- Add comment to explain the column's purpose
COMMENT ON COLUMN users.avatar_url IS 'The URL of the user''s profile picture. Set automatically for OAuth2 users from their provider. Can be updated for local users.'; 