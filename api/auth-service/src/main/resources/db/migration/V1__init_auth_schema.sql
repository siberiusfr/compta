-- Création du schema
CREATE SCHEMA IF NOT EXISTS auth;

-- Définir le search_path pour utiliser ce schema par défaut
SET search_path TO auth, public;

-- Table des utilisateurs
CREATE TABLE auth.users (
                            id BIGSERIAL PRIMARY KEY,
                            username VARCHAR(100) UNIQUE NOT NULL,
                            email VARCHAR(255) UNIQUE NOT NULL,
                            password VARCHAR(255) NOT NULL,
                            first_name VARCHAR(100),
                            last_name VARCHAR(100),
                            phone VARCHAR(20),
                            is_active BOOLEAN DEFAULT true,
                            is_locked BOOLEAN DEFAULT false,
                            failed_login_attempts INT DEFAULT 0,
                            last_login_at TIMESTAMP,
                            password_changed_at TIMESTAMP,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            created_by BIGINT,
                            updated_by BIGINT
);

-- Table des rôles
CREATE TABLE auth.roles (
                            id BIGSERIAL PRIMARY KEY,
                            name VARCHAR(50) UNIQUE NOT NULL, -- ADMIN, COMPTABLE, SOCIETE, EMPLOYEE
                            description VARCHAR(255),
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table de relation user-role
CREATE TABLE auth.user_roles (
                                 id BIGSERIAL PRIMARY KEY,
                                 user_id BIGINT NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
                                 role_id BIGINT NOT NULL REFERENCES auth.roles(id) ON DELETE CASCADE,
                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 UNIQUE(user_id, role_id)
);

-- Table des sociétés
CREATE TABLE auth.societes (
                               id BIGSERIAL PRIMARY KEY,
                               raison_sociale VARCHAR(255) NOT NULL,
                               matricule_fiscale VARCHAR(13) UNIQUE NOT NULL, -- Format tunisien: 7 chiffres + lettre + 3 chiffres
                               code_tva VARCHAR(20),
                               code_douane VARCHAR(20),
                               registre_commerce VARCHAR(50),
                               forme_juridique VARCHAR(100), -- SARL, SA, SUARL, etc.
                               capital_social DECIMAL(15,2),
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
                               is_active BOOLEAN DEFAULT true,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               created_by BIGINT REFERENCES auth.users(id),
                               updated_by BIGINT REFERENCES auth.users(id)
);

-- Table de relation comptable-sociétés (un comptable peut gérer plusieurs sociétés)
CREATE TABLE auth.comptable_societes (
                                         id BIGSERIAL PRIMARY KEY,
                                         user_id BIGINT NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
                                         societe_id BIGINT NOT NULL REFERENCES auth.societes(id) ON DELETE CASCADE,
                                         date_debut DATE NOT NULL,
                                         date_fin DATE,
                                         is_active BOOLEAN DEFAULT true,
                                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                         UNIQUE(user_id, societe_id)
);

-- Table de relation user societe-sociétés (un user societe peut avoir plusieurs sociétés)
CREATE TABLE auth.user_societes (
                                    id BIGSERIAL PRIMARY KEY,
                                    user_id BIGINT NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
                                    societe_id BIGINT NOT NULL REFERENCES auth.societes(id) ON DELETE CASCADE,
                                    is_owner BOOLEAN DEFAULT false, -- Indique si c'est le propriétaire principal
                                    date_debut DATE NOT NULL,
                                    date_fin DATE,
                                    is_active BOOLEAN DEFAULT true,
                                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                    UNIQUE(user_id, societe_id)
);

-- Table de relation employee-societe (un employee appartient à UNE société)
CREATE TABLE auth.employees (
                                id BIGSERIAL PRIMARY KEY,
                                user_id BIGINT UNIQUE NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
                                societe_id BIGINT NOT NULL REFERENCES auth.societes(id) ON DELETE CASCADE,
                                matricule_employee VARCHAR(50),
                                poste VARCHAR(100),
                                departement VARCHAR(100),
                                date_embauche DATE,
                                date_fin_contrat DATE,
                                type_contrat VARCHAR(50), -- CDI, CDD, SIVP, KARAMA, etc.
                                is_active BOOLEAN DEFAULT true,
                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table des tokens de refresh (pour JWT)
CREATE TABLE auth.refresh_tokens (
                                     id BIGSERIAL PRIMARY KEY,
                                     user_id BIGINT NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
                                     token VARCHAR(500) UNIQUE NOT NULL,
                                     expires_at TIMESTAMP NOT NULL,
                                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     ip_address VARCHAR(45),
                                     user_agent VARCHAR(255)
);

-- Table d'audit/logs d'authentification
CREATE TABLE auth.auth_logs (
                                id BIGSERIAL PRIMARY KEY,
                                user_id BIGINT REFERENCES auth.users(id) ON DELETE SET NULL,
                                username VARCHAR(100),
                                action VARCHAR(50) NOT NULL, -- LOGIN_SUCCESS, LOGIN_FAILED, LOGOUT, PASSWORD_RESET, etc.
                                ip_address VARCHAR(45),
                                user_agent VARCHAR(255),
                                details TEXT,
                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index pour optimiser les performances
CREATE INDEX idx_users_email ON auth.users(email);
CREATE INDEX idx_users_username ON auth.users(username);
CREATE INDEX idx_users_is_active ON auth.users(is_active);
CREATE INDEX idx_user_roles_user_id ON auth.user_roles(user_id);
CREATE INDEX idx_user_roles_role_id ON auth.user_roles(role_id);
CREATE INDEX idx_societes_matricule ON auth.societes(matricule_fiscale);
CREATE INDEX idx_societes_is_active ON auth.societes(is_active);
CREATE INDEX idx_comptable_societes_user_id ON auth.comptable_societes(user_id);
CREATE INDEX idx_comptable_societes_societe_id ON auth.comptable_societes(societe_id);
CREATE INDEX idx_user_societes_user_id ON auth.user_societes(user_id);
CREATE INDEX idx_user_societes_societe_id ON auth.user_societes(societe_id);
CREATE INDEX idx_employees_user_id ON auth.employees(user_id);
CREATE INDEX idx_employees_societe_id ON auth.employees(societe_id);
CREATE INDEX idx_refresh_tokens_token ON auth.refresh_tokens(token);
CREATE INDEX idx_refresh_tokens_user_id ON auth.refresh_tokens(user_id);
CREATE INDEX idx_auth_logs_user_id ON auth.auth_logs(user_id);
CREATE INDEX idx_auth_logs_created_at ON auth.auth_logs(created_at);

-- Insertion des rôles de base
INSERT INTO auth.roles (name, description) VALUES
                                               ('ADMIN', 'Administrateur système avec tous les droits'),
                                               ('COMPTABLE', 'Comptable pouvant gérer plusieurs sociétés'),
                                               ('SOCIETE', 'Utilisateur de type société pouvant avoir plusieurs sociétés'),
                                               ('EMPLOYEE', 'Employé appartenant à une société');

-- Insertion d'un utilisateur admin par défaut
-- Mot de passe: Admin@123 (hashé avec BCrypt)
INSERT INTO auth.users (
    username,
    email,
    password,
    first_name,
    last_name,
    is_active,
    password_changed_at
) VALUES (
             'admin',
             'admin@compta.tn',
             '$2a$10$GrrAeBf6ksJ0t6pcY9la/.1W5G0ot.Q/0lmKEsmSI.kkFiSsTKTg2', -- Admin@123
             'Administrateur',
             'Système',
             true,
             CURRENT_TIMESTAMP
         );
INSERT INTO auth.user_roles (user_id, role_id)
SELECT u.id, r.id
FROM auth.users u
    CROSS JOIN auth.roles r
WHERE u.username = 'admin' AND r.name = 'ADMIN';