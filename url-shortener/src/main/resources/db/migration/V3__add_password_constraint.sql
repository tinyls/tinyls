-- First make the column nullable
ALTER TABLE users
    ALTER COLUMN password DROP NOT NULL;

-- Add check constraint to ensure password is set for LOCAL users and null for OAuth users
ALTER TABLE users
    ADD CONSTRAINT check_password_provider
    CHECK (
        (provider = 'LOCAL' AND password IS NOT NULL) OR
        (provider != 'LOCAL' AND password IS NULL)
    ); 