CREATE
    TABLE
        IF NOT EXISTS oauth2.oauth2_authorization_consent(
            registered_client_id VARCHAR(255) NOT NULL,
            principal_name VARCHAR(256) NOT NULL,
            authorities VARCHAR(1000) NOT NULL,
            PRIMARY KEY(
                registered_client_id,
                principal_name
            )
        );
