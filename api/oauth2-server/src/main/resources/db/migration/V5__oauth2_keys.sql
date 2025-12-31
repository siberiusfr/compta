-- OAuth2 Keys table for persistent RSA key storage
CREATE
    TABLE
        IF NOT EXISTS oauth2.oauth2_keys(
            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
            key_id VARCHAR(255) NOT NULL UNIQUE,
            public_key TEXT NOT NULL,
            private_key TEXT NOT NULL,
            key_algorithm VARCHAR(50) NOT NULL DEFAULT 'RSA',
            key_size INTEGER NOT NULL DEFAULT 2048,
            active BOOLEAN NOT NULL DEFAULT TRUE,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            expires_at TIMESTAMP,
            grace_period_ends_at TIMESTAMP
        );

-- Create indexes
CREATE
    INDEX IF NOT EXISTS idx_oauth2_keys_key_id ON
    oauth2.oauth2_keys(key_id);

CREATE
    INDEX IF NOT EXISTS idx_oauth2_keys_active ON
    oauth2.oauth2_keys(active);

CREATE
    INDEX IF NOT EXISTS idx_oauth2_keys_expires_at ON
    oauth2.oauth2_keys(expires_at);

-- Insert initial RSA key
INSERT
    INTO
        oauth2.oauth2_keys(
            key_id,
            public_key,
            private_key,
            key_algorithm,
            key_size,
            active,
            expires_at
        )
    VALUES(
        'initial-key',
        '', -- Will be populated by KeyManagementService on first startup'', -- Will be populated by KeyManagementService on first startup'RSA',2048,TRUE,CURRENT_TIMESTAMP + INTERVAL '90 days'
    ) ON
    CONFLICT(key_id) DO NOTHING;
