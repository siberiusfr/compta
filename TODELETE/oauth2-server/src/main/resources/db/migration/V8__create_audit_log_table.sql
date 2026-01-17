-- Create audit log table for tracking security events
-- This table stores all security-related events for compliance and auditing purposes
CREATE TABLE IF NOT EXISTS oauth2.audit_logs (
  id BIGSERIAL PRIMARY KEY,
  event_type VARCHAR(50) NOT NULL,
  event_category VARCHAR(50) NOT NULL,
  user_id VARCHAR(255),
  username VARCHAR(255),
  client_id VARCHAR(255),
  ip_address VARCHAR(45) NOT NULL,
  user_agent TEXT,
  request_uri VARCHAR(500),
  request_method VARCHAR(10),
  status VARCHAR(20) NOT NULL,
  error_message TEXT,
  details JSONB,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  tenant_id VARCHAR(255)
);

-- Create indexes for common queries
CREATE INDEX idx_audit_logs_event_type ON oauth2.audit_logs (event_type);

CREATE INDEX idx_audit_logs_user_id ON oauth2.audit_logs (user_id);

CREATE INDEX idx_audit_logs_client_id ON oauth2.audit_logs (client_id);

CREATE INDEX idx_audit_logs_ip_address ON oauth2.audit_logs (ip_address);

CREATE INDEX idx_audit_logs_created_at ON oauth2.audit_logs (created_at);

CREATE INDEX idx_audit_logs_status ON oauth2.audit_logs (status);

CREATE INDEX idx_audit_logs_tenant_id ON oauth2.audit_logs (tenant_id);

-- Add comment to the table
COMMENT ON TABLE oauth2.audit_logs IS 'Audit log table for tracking security events';

COMMENT ON COLUMN oauth2.audit_logs.event_type IS 'Type of the event (LOGIN, LOGOUT, TOKEN_ISSUED, TOKEN_REFRESHED, etc.)';

COMMENT ON COLUMN oauth2.audit_logs.event_category IS 'Category of the event (AUTHENTICATION, AUTHORIZATION, TOKEN, USER, etc.)';

COMMENT ON COLUMN oauth2.audit_logs.user_id IS 'ID of the user if applicable';

COMMENT ON COLUMN oauth2.audit_logs.username IS 'Username of the user if applicable';

COMMENT ON COLUMN oauth2.audit_logs.client_id IS 'OAuth2 client ID if applicable';

COMMENT ON COLUMN oauth2.audit_logs.ip_address IS 'IP address of the request';

COMMENT ON COLUMN oauth2.audit_logs.user_agent IS 'User agent string of the client';

COMMENT ON COLUMN oauth2.audit_logs.request_uri IS 'Request URI';

COMMENT ON COLUMN oauth2.audit_logs.request_method IS 'HTTP method (GET, POST, etc.)';

COMMENT ON COLUMN oauth2.audit_logs.status IS 'Status of the event (SUCCESS, FAILURE, WARNING)';

COMMENT ON COLUMN oauth2.audit_logs.error_message IS 'Error message if the event failed';

COMMENT ON COLUMN oauth2.audit_logs.details IS 'Additional details in JSON format';

COMMENT ON COLUMN oauth2.audit_logs.created_at IS 'Timestamp when the event occurred';

COMMENT ON COLUMN oauth2.audit_logs.tenant_id IS 'Tenant/Company ID for multi-tenancy';
