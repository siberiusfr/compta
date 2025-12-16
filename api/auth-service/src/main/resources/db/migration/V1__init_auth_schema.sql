-- Migration initiale pour le service d'authentification
-- Création du schéma dédié et des tables

-- Créer le schéma auth
CREATE SCHEMA IF NOT EXISTS auth;

-- Fonction pour mettre à jour automatiquement updated_at
CREATE OR REPLACE FUNCTION auth.update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Table des utilisateurs
CREATE TABLE IF NOT EXISTS auth.users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    is_active BOOLEAN DEFAULT true,
    is_locked BOOLEAN DEFAULT false,
    failed_login_attempts INT DEFAULT 0,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Table des rôles
CREATE TABLE IF NOT EXISTS auth.roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Table de liaison utilisateurs-rôles
CREATE TABLE IF NOT EXISTS auth.user_roles (
    user_id BIGINT NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES auth.roles(id) ON DELETE CASCADE,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id)
);

-- Table des permissions
CREATE TABLE IF NOT EXISTS auth.permissions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    resource VARCHAR(100) NOT NULL,
    action VARCHAR(50) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Table de liaison rôles-permissions
CREATE TABLE IF NOT EXISTS auth.role_permissions (
    role_id BIGINT NOT NULL REFERENCES auth.roles(id) ON DELETE CASCADE,
    permission_id BIGINT NOT NULL REFERENCES auth.permissions(id) ON DELETE CASCADE,
    granted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (role_id, permission_id)
);

-- Table des tokens de rafraîchissement
CREATE TABLE IF NOT EXISTS auth.refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    token VARCHAR(500) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revoked_at TIMESTAMP
);

-- Table d'audit des connexions
CREATE TABLE IF NOT EXISTS auth.login_audit (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES auth.users(id) ON DELETE SET NULL,
    username VARCHAR(100) NOT NULL,
    ip_address VARCHAR(45),
    user_agent TEXT,
    success BOOLEAN NOT NULL,
    failure_reason VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index pour les performances
CREATE INDEX idx_users_email ON auth.users(email);
CREATE INDEX idx_users_username ON auth.users(username);
CREATE INDEX idx_users_active ON auth.users(is_active);
CREATE INDEX idx_user_roles_user ON auth.user_roles(user_id);
CREATE INDEX idx_user_roles_role ON auth.user_roles(role_id);
CREATE INDEX idx_role_permissions_role ON auth.role_permissions(role_id);
CREATE INDEX idx_role_permissions_permission ON auth.role_permissions(permission_id);
CREATE INDEX idx_refresh_tokens_user ON auth.refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token ON auth.refresh_tokens(token);
CREATE INDEX idx_refresh_tokens_expires ON auth.refresh_tokens(expires_at);
CREATE INDEX idx_login_audit_user ON auth.login_audit(user_id);
CREATE INDEX idx_login_audit_created ON auth.login_audit(created_at);

-- Triggers pour updated_at
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON auth.users
    FOR EACH ROW EXECUTE FUNCTION auth.update_updated_at_column();

CREATE TRIGGER update_roles_updated_at BEFORE UPDATE ON auth.roles
    FOR EACH ROW EXECUTE FUNCTION auth.update_updated_at_column();

-- Insertion des rôles par défaut
INSERT INTO auth.roles (name, description) VALUES
    ('ADMIN', 'Administrateur système avec tous les droits'),
    ('USER', 'Utilisateur standard'),
    ('ACCOUNTANT', 'Comptable avec accès aux fonctionnalités comptables'),
    ('MANAGER', 'Manager avec accès aux fonctionnalités de gestion')
ON CONFLICT (name) DO NOTHING;

-- Insertion des permissions par défaut
INSERT INTO auth.permissions (name, resource, action, description) VALUES
    ('users.read', 'users', 'read', 'Lire les informations des utilisateurs'),
    ('users.write', 'users', 'write', 'Créer et modifier les utilisateurs'),
    ('users.delete', 'users', 'delete', 'Supprimer les utilisateurs'),
    ('accounting.read', 'accounting', 'read', 'Consulter les données comptables'),
    ('accounting.write', 'accounting', 'write', 'Créer et modifier les données comptables'),
    ('accounting.delete', 'accounting', 'delete', 'Supprimer les données comptables')
ON CONFLICT (name) DO NOTHING;
