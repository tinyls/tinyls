ALTER TABLE users
    ADD COLUMN provider VARCHAR(20) NOT NULL DEFAULT 'LOCAL',
    ADD COLUMN provider_id VARCHAR(255),
    ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER';

CREATE INDEX idx_users_provider_provider_id ON users(provider, provider_id); 