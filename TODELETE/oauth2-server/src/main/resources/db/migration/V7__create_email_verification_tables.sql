-- Create email verification tables
-- These tables support email-based user verification
CREATE TABLE IF NOT EXISTS oauth2.email_verification_tokens (
  id BIGSERIAL PRIMARY KEY,
  user_id VARCHAR(255) NOT NULL,
  username VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL,
  token VARCHAR(255) NOT NULL UNIQUE,
  expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
  verified BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  ip_address VARCHAR(45),
  user_agent TEXT
);

-- Create indexes for email verification tokens
CREATE INDEX idx_email_verification_tokens_user_id ON oauth2.email_verification_tokens (user_id);

CREATE INDEX idx_email_verification_tokens_email ON oauth2.email_verification_tokens (email);

CREATE INDEX idx_email_verification_tokens_token ON oauth2.email_verification_tokens (token);

CREATE INDEX idx_email_verification_tokens_expires_at ON oauth2.email_verification_tokens (expires_at);

-- Add comment to the table
COMMENT ON TABLE oauth2.email_verification_tokens IS 'Email verification tokens for new user registration';

COMMENT ON COLUMN oauth2.email_verification_tokens.token IS 'Unique token for email verification (UUID)';

COMMENT ON COLUMN oauth2.email_verification_tokens.expires_at IS 'Expiration time of the verification token';

COMMENT ON COLUMN oauth2.email_verification_tokens.verified IS 'Whether the email has been verified';
