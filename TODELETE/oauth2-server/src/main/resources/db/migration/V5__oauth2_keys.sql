CREATE TABLE IF NOT EXISTS oauth2.oauth2_keys (
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
CREATE INDEX IF NOT EXISTS idx_oauth2_keys_key_id ON oauth2.oauth2_keys (key_id);

CREATE INDEX IF NOT EXISTS idx_oauth2_keys_active ON oauth2.oauth2_keys (active);

CREATE INDEX IF NOT EXISTS idx_oauth2_keys_expires_at ON oauth2.oauth2_keys (expires_at);
