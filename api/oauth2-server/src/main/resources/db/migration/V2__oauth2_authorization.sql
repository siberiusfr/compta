CREATE TABLE IF NOT EXISTS oauth2.oauth2_authorization (
    id VARCHAR(255) NOT NULL,
    registered_client_id VARCHAR(255) NOT NULL,
    principal_name VARCHAR(256) NOT NULL,
    authorization_grant_type VARCHAR(256) NOT NULL,
    authorized_scopes VARCHAR(1000),
    attributes TEXT,
    state VARCHAR(500),
    authorization_code_value TEXT,
    authorization_code_issued_at TIMESTAMP,
    authorization_code_expires_at TIMESTAMP,
    authorization_code_metadata TEXT,
    access_token_value TEXT,
    access_token_issued_at TIMESTAMP,
    access_token_expires_at TIMESTAMP,
    access_token_metadata TEXT,
    access_token_type VARCHAR(256),
    access_token_scopes VARCHAR(1000),
    oidc_id_token_value TEXT,
    oidc_id_token_issued_at TIMESTAMP,
    oidc_id_token_expires_at TIMESTAMP,
    oidc_id_token_metadata TEXT,
    refresh_token_value TEXT,
    refresh_token_issued_at TIMESTAMP,
    refresh_token_expires_at TIMESTAMP,
    refresh_token_metadata TEXT,
    user_code_value TEXT,
    user_code_issued_at TIMESTAMP,
    user_code_expires_at TIMESTAMP,
    user_code_metadata TEXT,
    device_code_value TEXT,
    device_code_issued_at TIMESTAMP,
    device_code_expires_at TIMESTAMP,
    device_code_metadata TEXT,
    PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS oauth2_authorization_registered_client_id_idx
    ON oauth2.oauth2_authorization (registered_client_id);
CREATE INDEX IF NOT EXISTS oauth2_authorization_principal_name_idx
    ON oauth2.oauth2_authorization (principal_name);
CREATE INDEX IF NOT EXISTS oauth2_authorization_state_idx
    ON oauth2.oauth2_authorization (state);
