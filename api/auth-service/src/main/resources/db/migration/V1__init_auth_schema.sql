-- Création du schema
CREATE
    SCHEMA IF NOT EXISTS auth;
SET
search_path TO auth,
public;

-- Table des utilisateurs (CORE AUTHENTICATION)
CREATE
    TABLE
        auth.users(
            id BIGSERIAL PRIMARY KEY,
            username VARCHAR(100) UNIQUE NOT NULL,
            email VARCHAR(255) UNIQUE NOT NULL,
            password VARCHAR(255) NOT NULL,
            first_name VARCHAR(100),
            last_name VARCHAR(100),
            phone VARCHAR(20),
            is_active BOOLEAN DEFAULT TRUE,
            is_locked BOOLEAN DEFAULT FALSE,
            failed_login_attempts INT DEFAULT 0,
            last_login_at TIMESTAMP,
            password_changed_at TIMESTAMP,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            created_by BIGINT,
            updated_by BIGINT
        );

-- Table des rôles (PROFILES DE BASE)
CREATE
    TABLE
        auth.roles(
            id BIGSERIAL PRIMARY KEY,
            name VARCHAR(50) UNIQUE NOT NULL, -- ADMIN, COMPTABLE, SOCIETE, EMPLOYEE
            description VARCHAR(255),
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        );

-- Table de relation user-role
CREATE
    TABLE
        auth.user_roles(
            id BIGSERIAL PRIMARY KEY,
            user_id BIGINT NOT NULL REFERENCES auth.users(id) ON
            DELETE
                CASCADE,
                role_id BIGINT NOT NULL REFERENCES auth.roles(id) ON
                DELETE
                    CASCADE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE(
                        user_id,
                        role_id
                    )
        );

-- Table des tokens de refresh (JWT)
CREATE
    TABLE
        auth.refresh_tokens(
            id BIGSERIAL PRIMARY KEY,
            user_id BIGINT NOT NULL REFERENCES auth.users(id) ON
            DELETE
                CASCADE,
                token VARCHAR(500) UNIQUE NOT NULL,
                expires_at TIMESTAMP NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                ip_address VARCHAR(45),
                user_agent VARCHAR(255)
        );

-- Table d'audit/logs d'authentification
CREATE
    TABLE
        auth.auth_logs(
            id BIGSERIAL PRIMARY KEY,
            user_id BIGINT REFERENCES auth.users(id) ON
            DELETE
            SET
                NULL,
                username VARCHAR(100),
                ACTION VARCHAR(50) NOT NULL, -- LOGIN_SUCCESS, LOGIN_FAILED, LOGOUT, PASSWORD_RESET
                ip_address VARCHAR(45),
                user_agent VARCHAR(255),
                details TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        );

-- Index
CREATE
    INDEX idx_users_email ON
    auth.users(email);

CREATE
    INDEX idx_users_username ON
    auth.users(username);

CREATE
    INDEX idx_users_is_active ON
    auth.users(is_active);

CREATE
    INDEX idx_user_roles_user_id ON
    auth.user_roles(user_id);

CREATE
    INDEX idx_user_roles_role_id ON
    auth.user_roles(role_id);

CREATE
    INDEX idx_refresh_tokens_token ON
    auth.refresh_tokens(token);

CREATE
    INDEX idx_refresh_tokens_user_id ON
    auth.refresh_tokens(user_id);

CREATE
    INDEX idx_auth_logs_user_id ON
    auth.auth_logs(user_id);

CREATE
    INDEX idx_auth_logs_created_at ON
    auth.auth_logs(created_at);

-- Rôles de base
INSERT
    INTO
        auth.roles(
            name,
            description
        )
    VALUES(
        'ADMIN',
        'Administrateur système avec tous les droits'
    ),
    (
        'COMPTABLE',
        'Comptable pouvant gérer plusieurs sociétés'
    ),
    (
        'SOCIETE',
        'Utilisateur de type société pouvant avoir plusieurs sociétés'
    ),
    (
        'EMPLOYEE',
        'Employé appartenant à une société'
    );

-- Admin par défaut (mot de passe: Admin@123)
INSERT
    INTO
        auth.users(
            username,
            email,
            password,
            first_name,
            last_name,
            is_active,
            password_changed_at
        )
    VALUES(
        'admin',
        'admin@compta.tn',
        '$2a$10$GrrAeBf6ksJ0t6pcY9la/.1W5G0ot.Q/0lmKEsmSI.kkFiSsTKTg2',
        'Administrateur',
        'Système',
        TRUE,
        CURRENT_TIMESTAMP
    );

INSERT
    INTO
        auth.user_roles(
            user_id,
            role_id
        ) SELECT
            u.id,
            r.id
        FROM
            auth.users u CROSS
        JOIN auth.roles r
        WHERE
            u.username = 'admin'
            AND r.name = 'ADMIN';
