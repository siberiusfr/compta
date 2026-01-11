-- Create token blacklist table for persisting revoked token JTIs
-- This ensures token revocation survives server restarts and works in clustered environments
CREATE TABLE IF NOT EXISTS oauth2.token_blacklist (
  id BIGSERIAL PRIMARY KEY,
  jti VARCHAR(255) NOT NULL UNIQUE,
  expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
  revoked_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  revoked_by VARCHAR(255),
  reason VARCHAR(255)
);

-- Create indexes for efficient lookups
CREATE INDEX idx_token_blacklist_jti ON oauth2.token_blacklist (jti);

CREATE INDEX idx_token_blacklist_expires_at ON oauth2.token_blacklist (expires_at);

-- Add comments
COMMENT ON TABLE oauth2.token_blacklist IS 'Blacklisted JWT tokens identified by their JTI claim';

COMMENT ON COLUMN oauth2.token_blacklist.jti IS 'JWT ID (unique identifier for the token)';

COMMENT ON COLUMN oauth2.token_blacklist.expires_at IS 'When the token would have expired (for cleanup)';

COMMENT ON COLUMN oauth2.token_blacklist.revoked_at IS 'When the token was revoked';

COMMENT ON COLUMN oauth2.token_blacklist.revoked_by IS 'User or client that revoked the token';

COMMENT ON COLUMN oauth2.token_blacklist.reason IS 'Reason for revocation (logout, security, etc.)';
