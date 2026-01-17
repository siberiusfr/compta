-- =============================================================================
-- Zitadel Database Initialization Script
-- =============================================================================
-- Run this script BEFORE starting Zitadel for the first time
-- Creates a dedicated 'zitadel' database and user with full permissions
--
-- Usage:
--   psql -h localhost -U postgres -f init-db.sql
--   OR
--   docker exec -i compta-postgres psql -U postgres < init-db.sql
-- =============================================================================
-- Drop existing database and user if they exist (clean start)
DROP DATABASE IF EXISTS zitadel;

DROP ROLE IF EXISTS zitadel;

-- Create the zitadel user with CREATEDB permission
CREATE ROLE zitadel
WITH
  LOGIN PASSWORD 'zitadel' CREATEDB;

-- Create the zitadel database owned by zitadel user
CREATE DATABASE zitadel OWNER zitadel;

-- Grant all privileges
GRANT ALL PRIVILEGES ON DATABASE zitadel TO zitadel;

-- Make zitadel the owner of public schema
ALTER SCHEMA public OWNER TO zitadel;

-- Grant all privileges on public schema
GRANT ALL ON SCHEMA public TO zitadel;

GRANT CREATE ON SCHEMA public TO zitadel;

-- Allow zitadel to create schemas
GRANT CREATE ON DATABASE zitadel TO zitadel;

-- Set default privileges
ALTER DEFAULT PRIVILEGES
GRANT ALL ON TABLES TO zitadel;

ALTER DEFAULT PRIVILEGES
GRANT ALL ON SEQUENCES TO zitadel;

ALTER DEFAULT PRIVILEGES
GRANT ALL ON FUNCTIONS TO zitadel;
