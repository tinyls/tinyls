-- Migration: Improve updated_at trigger to exclude system-managed fields
-- Version: V7
-- Description: Replaces the simple updated_at trigger with a conditional one that
--              only updates the timestamp when user-relevant fields change,
--              excluding system-managed fields like clicks.

-- Drop the existing trigger and function
DROP TRIGGER IF EXISTS trg_set_updated_at ON urls;
DROP FUNCTION IF EXISTS set_updated_at();

-- Create improved function that only updates timestamp for relevant field changes
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS trigger AS $$
BEGIN
    -- Only update updated_at if user-relevant fields have changed
    -- Exclude system-managed fields like clicks, updated_at itself
    IF (OLD.original_url IS DISTINCT FROM NEW.original_url) OR
       (OLD.status IS DISTINCT FROM NEW.status) OR
       (OLD.title IS DISTINCT FROM NEW.title) OR
       (OLD.description IS DISTINCT FROM NEW.description) THEN
        
        NEW.updated_at := now();
        RETURN NEW;
    END IF;
    
    -- If no relevant fields changed, return NEW without updating timestamp
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create the improved trigger
CREATE TRIGGER trg_set_updated_at
    BEFORE UPDATE ON urls
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

-- Add comment explaining the trigger behavior
COMMENT ON FUNCTION set_updated_at() IS 
'Updates updated_at timestamp only when user-relevant fields change (original_url, status, title, description). 
Excludes system-managed fields like clicks to provide accurate user activity tracking.';
