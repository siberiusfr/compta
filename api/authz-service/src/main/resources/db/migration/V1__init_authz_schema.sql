-- Activer l'extension btree_gist (permet BIGINT et VARCHAR dans GIST)
CREATE EXTENSION IF NOT EXISTS btree_gist;

-- V1__init_authz_schema.sql
CREATE SCHEMA IF NOT EXISTS authz;

-- ============================================
-- 1. SOCIÉTÉS COMPTABLES (Cabinets)
-- ============================================
CREATE TABLE authz.societes_comptables (
  id BIGSERIAL PRIMARY KEY,
  raison_sociale VARCHAR(255) NOT NULL,
  matricule_fiscale VARCHAR(13) UNIQUE NOT NULL,
  code_tva VARCHAR(20),
  adresse VARCHAR(255),
  ville VARCHAR(100),
  code_postal VARCHAR(10),
  telephone VARCHAR(20),
  email VARCHAR(255),
  site_web VARCHAR(255),
  is_active BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 2. SOCIÉTÉS CLIENTES
-- ============================================
CREATE TABLE authz.societes (
  id BIGSERIAL PRIMARY KEY,
  raison_sociale VARCHAR(255) NOT NULL,
  matricule_fiscale VARCHAR(13) UNIQUE NOT NULL,
  code_tva VARCHAR(20),
  code_douane VARCHAR(20),
  registre_commerce VARCHAR(50),
  forme_juridique VARCHAR(100),
  capital_social DECIMAL(15, 2),
  date_creation DATE,
  adresse VARCHAR(255),
  ville VARCHAR(100),
  code_postal VARCHAR(10),
  telephone VARCHAR(20),
  fax VARCHAR(20),
  email VARCHAR(255),
  site_web VARCHAR(255),
  activite VARCHAR(255),
  secteur VARCHAR(100),
  -- Société gérée par quelle société comptable
  societe_comptable_id BIGINT REFERENCES authz.societes_comptables (id) ON DELETE SET NULL,
  is_active BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 3. USERS DE SOCIÉTÉ COMPTABLE
-- ============================================
CREATE TABLE authz.user_societe_comptable (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL UNIQUE,
  societe_comptable_id BIGINT NOT NULL REFERENCES authz.societes_comptables (id) ON DELETE CASCADE,
  role VARCHAR(50) NOT NULL, -- 'MANAGER', 'COMPTABLE', 'ASSISTANT'
  date_debut DATE NOT NULL DEFAULT CURRENT_DATE,
  date_fin DATE,
  is_active BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT check_one_manager_per_cabinet EXCLUDE USING gist (
    societe_comptable_id
    WITH
      =,
      role
    WITH
      =
  )
  WHERE
    (
      role = 'MANAGER'
      AND is_active = TRUE
    )
);

-- ============================================
-- 4. ACCÈS COMPTABLE → SOCIÉTÉS CLIENTES
-- ============================================
CREATE TABLE authz.comptable_societes (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  societe_id BIGINT NOT NULL REFERENCES authz.societes (id) ON DELETE CASCADE,
  can_read BOOLEAN DEFAULT TRUE,
  can_write BOOLEAN DEFAULT FALSE,
  can_validate BOOLEAN DEFAULT FALSE,
  date_debut DATE NOT NULL DEFAULT CURRENT_DATE,
  date_fin DATE,
  is_active BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE (user_id, societe_id),
  CONSTRAINT fk_user_is_comptable FOREIGN KEY (user_id) REFERENCES authz.user_societe_comptable (user_id) ON DELETE CASCADE
);

-- ============================================
-- 5. USERS DE SOCIÉTÉ (Clients)
-- ============================================
CREATE TABLE authz.user_societes (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL UNIQUE,
  societe_id BIGINT NOT NULL REFERENCES authz.societes (id) ON DELETE CASCADE,
  role VARCHAR(50) NOT NULL, -- 'MANAGER', 'FINANCE', 'VIEWER'
  date_debut DATE NOT NULL DEFAULT CURRENT_DATE,
  date_fin DATE,
  is_active BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT check_one_manager_per_societe EXCLUDE USING gist (
    societe_id
    WITH
      =,
      role
    WITH
      =
  )
  WHERE
    (
      role = 'MANAGER'
      AND is_active = TRUE
    )
);

-- ============================================
-- 6. PERMISSIONS (Granulaires)
-- ============================================
CREATE TABLE authz.permissions (
  id BIGSERIAL PRIMARY KEY,
  code VARCHAR(100) UNIQUE NOT NULL,
  resource VARCHAR(50) NOT NULL,
  action VARCHAR(50) NOT NULL,
  description TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 7. ROLE_PERMISSIONS (Mapping)
-- ============================================
CREATE TABLE authz.role_permissions (
  id BIGSERIAL PRIMARY KEY,
  role VARCHAR(50) NOT NULL,
  permission_id BIGINT NOT NULL REFERENCES authz.permissions (id) ON DELETE CASCADE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE (role, permission_id)
);

-- ============================================
-- INDEX
-- ============================================
CREATE INDEX idx_societes_comptables_matricule ON authz.societes_comptables (matricule_fiscale);

CREATE INDEX idx_societes_comptables_active ON authz.societes_comptables (is_active);

CREATE INDEX idx_societes_matricule ON authz.societes (matricule_fiscale);

CREATE INDEX idx_societes_active ON authz.societes (is_active);

CREATE INDEX idx_societes_comptable ON authz.societes (societe_comptable_id);

CREATE INDEX idx_user_sc_user ON authz.user_societe_comptable (user_id);

CREATE INDEX idx_user_sc_societe ON authz.user_societe_comptable (societe_comptable_id);

CREATE INDEX idx_user_sc_role ON authz.user_societe_comptable (role);

CREATE INDEX idx_comptable_soc_user ON authz.comptable_societes (user_id);

CREATE INDEX idx_comptable_soc_societe ON authz.comptable_societes (societe_id);

CREATE INDEX idx_comptable_soc_active ON authz.comptable_societes (is_active);

CREATE INDEX idx_user_soc_user ON authz.user_societes (user_id);

CREATE INDEX idx_user_soc_societe ON authz.user_societes (societe_id);

CREATE INDEX idx_user_soc_role ON authz.user_societes (role);

CREATE INDEX idx_permissions_code ON authz.permissions (code);

CREATE INDEX idx_permissions_resource ON authz.permissions (resource);

CREATE INDEX idx_role_permissions_role ON authz.role_permissions (role);
