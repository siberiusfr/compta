-- Create password reset tables
-- These tables support email-based password reset functionality

CREATE TABLE IF NOT EXISTS oauth2.password_reset_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    user_agent TEXT
);

-- Create indexes for password reset tokens
CREATE INDEX idx_password_reset_tokens_user_id ON oauth2.password_reset_tokens(user_id);
CREATE INDEX idx_password_reset_tokens_email ON oauth2.password_reset_tokens(email);
CREATE INDEX idx_password_reset_tokens_token ON oauth2.password_reset_tokens(token);
CREATE INDEX idx_password_reset_tokens_expires_at ON oauth2.password_reset_tokens(expires_at);

-- Add comment to the table
COMMENT ON TABLE oauth2.password_reset_tokens IS 'Password reset tokens for email-based password reset';
COMMENT ON COLUMN oauth2.password_reset_tokens.token IS 'Unique token for password reset (UUID)';
COMMENT ON COLUMN oauth2.password_reset_tokens.expires_at IS 'Expiration time of the reset token';
COMMENT ON COLUMN oauth2.password_reset_tokens.used IS 'Whether the token has been used';
