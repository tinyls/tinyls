-- Migration: Add URL metadata fields (updated_at, title, description)
-- Version: V6
-- Description: Adds timestamp tracking, title, and description fields to URLs table
--              with proper constraints and triggers for automatic timestamp updates

-- Add new columns with proper constraints
ALTER TABLE urls
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    ADD COLUMN IF NOT EXISTS title VARCHAR(50) CHECK (length(trim(title)) > 0),
    ADD COLUMN IF NOT EXISTS description VARCHAR(300) CHECK (length(trim(description)) > 0);

-- Create function to automatically update the updated_at timestamp
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS trigger AS $$
BEGIN
    NEW.updated_at := now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Drop existing trigger if it exists (idempotent)
DROP TRIGGER IF EXISTS trg_set_updated_at ON urls;

-- Create trigger to automatically update updated_at on every row update
CREATE TRIGGER trg_set_updated_at
    BEFORE UPDATE ON urls
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

-- Update existing records to have updated_at set to created_at
UPDATE urls 
SET updated_at = created_at 
WHERE updated_at IS NULL;

-- Add index on updated_at for better query performance on time-based operations
CREATE INDEX IF NOT EXISTS idx_urls_updated_at ON urls(updated_at);

-- Add index on title for better search performance (if title is not null)
CREATE INDEX IF NOT EXISTS idx_urls_title ON urls(title) WHERE title IS NOT NULL;

-- Add index on description for better search performance (if description is not null)
CREATE INDEX IF NOT EXISTS idx_urls_description ON urls(description) WHERE description IS NOT NULL;
