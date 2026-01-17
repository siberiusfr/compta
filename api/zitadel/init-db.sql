-- =============================================================================
-- Zitadel Database Initialization Script
-- =============================================================================
-- Run this script BEFORE starting Zitadel for the first time
-- Creates a dedicated 'zitadel' database - Zitadel will manage its own schemas
--
-- Usage:
--   psql -h localhost -U postgres -f init-db.sql
--   OR
--   docker exec -i compta-postgres psql -U postgres < init-db.sql
-- =============================================================================
-- Create the zitadel database
CREATE DATABASE zitadel
WITH
  OWNER = postgres ENCODING = 'UTF8' LC_COLLATE = 'en_US.utf8' LC_CTYPE = 'en_US.utf8' TEMPLATE = template0 CONNECTION
LIMIT
  = -1;

-- Grant all privileges to postgres user
GRANT ALL PRIVILEGES ON DATABASE zitadel TO postgres;

-- Verify the database was created
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_database WHERE datname = 'zitadel') THEN
        RAISE NOTICE 'Database "zitadel" created successfully!';
        RAISE NOTICE 'Zitadel will manage its own schemas internally.';
    ELSE
        RAISE EXCEPTION 'Failed to create database "zitadel"';
    END IF;
END
$$;
