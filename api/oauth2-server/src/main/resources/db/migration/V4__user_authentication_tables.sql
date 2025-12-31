-- Users table
CREATE
    TABLE
        IF NOT EXISTS oauth2.users(
            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
            username VARCHAR(255) NOT NULL UNIQUE,
            password VARCHAR(255) NOT NULL,
            email VARCHAR(255) NOT NULL UNIQUE,
            first_name VARCHAR(100),
            last_name VARCHAR(100),
            enabled BOOLEAN NOT NULL DEFAULT TRUE,
            account_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
            account_non_locked BOOLEAN NOT NULL DEFAULT TRUE,
            credentials_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        );

-- Roles table
CREATE
    TABLE
        IF NOT EXISTS oauth2.roles(
            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
            name VARCHAR(50) NOT NULL UNIQUE,
            description VARCHAR(255),
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        );

-- User roles junction table
CREATE
    TABLE
        IF NOT EXISTS oauth2.user_roles(
            user_id UUID NOT NULL,
            role_id UUID NOT NULL,
            assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            PRIMARY KEY(
                user_id,
                role_id
            ),
            FOREIGN KEY(user_id) REFERENCES oauth2.users(id) ON
            DELETE
                CASCADE,
                FOREIGN KEY(role_id) REFERENCES oauth2.roles(id) ON
                DELETE
                    CASCADE
        );

-- Create indexes
CREATE
    INDEX IF NOT EXISTS idx_users_username ON
    oauth2.users(username);

CREATE
    INDEX IF NOT EXISTS idx_users_email ON
    oauth2.users(email);

CREATE
    INDEX IF NOT EXISTS idx_user_roles_user_id ON
    oauth2.user_roles(user_id);

CREATE
    INDEX IF NOT EXISTS idx_user_roles_role_id ON
    oauth2.user_roles(role_id);

-- Insert default roles
INSERT
    INTO
        oauth2.roles(
            name,
            description
        )
    VALUES(
        'ROLE_ADMIN',
        'Administrator with full access'
    ),
    (
        'ROLE_USER',
        'Standard user with limited access'
    ),
    (
        'ROLE_MANAGER',
        'Manager with elevated permissions'
    ) ON
    CONFLICT(name) DO NOTHING;

-- Insert default admin user (password: admin123)
INSERT
    INTO
        oauth2.users(
            username,
            password,
            email,
            first_name,
            last_name
        )
    VALUES(
        'admin',
        '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH',
        'admin@compta.tn',
        'Admin',
        'User'
    ) ON
    CONFLICT(username) DO NOTHING;

-- Assign admin role to admin user
INSERT
    INTO
        oauth2.user_roles(
            user_id,
            role_id
        ) SELECT
            u.id,
            r.id
        FROM
            oauth2.users u,
            oauth2.roles r
        WHERE
            u.username = 'admin'
            AND r.name = 'ROLE_ADMIN' ON
            CONFLICT DO NOTHING;

-- Insert default regular user (password: user123)
INSERT
    INTO
        oauth2.users(
            username,
            password,
            email,
            first_name,
            last_name
        )
    VALUES(
        'user',
        '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH',
        'user@compta.tn',
        'Regular',
        'User'
    ) ON
    CONFLICT(username) DO NOTHING;

-- Assign user role to regular user
INSERT
    INTO
        oauth2.user_roles(
            user_id,
            role_id
        ) SELECT
            u.id,
            r.id
        FROM
            oauth2.users u,
            oauth2.roles r
        WHERE
            u.username = 'user'
            AND r.name = 'ROLE_USER' ON
            CONFLICT DO NOTHING;
