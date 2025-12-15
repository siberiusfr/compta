-- V6__create_notification_tables.sql
-- Notification Service Tables: Templates, Notifications

-- Notification Templates
CREATE TABLE notification_templates (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    template_type VARCHAR(50) NOT NULL, -- EMAIL, SMS, PUSH
    code VARCHAR(100) NOT NULL UNIQUE,
    subject_template TEXT,
    body_template TEXT NOT NULL,
    variables JSONB, -- {variable_name: description}
    language VARCHAR(10) NOT NULL DEFAULT 'fr',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notification_templates_code ON notification_templates(code);
CREATE INDEX idx_notification_templates_template_type ON notification_templates(template_type);

COMMENT ON TABLE notification_templates IS 'Notification templates (Email, SMS, Push)';

-- Notifications
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    company_id BIGINT,
    notification_type VARCHAR(50) NOT NULL, -- EMAIL, SMS, PUSH, IN_APP
    channel VARCHAR(50) NOT NULL, -- TRANSACTIONAL, MARKETING, SYSTEM
    template_id BIGINT,
    recipient VARCHAR(255) NOT NULL, -- Email address or phone number
    subject VARCHAR(500),
    body TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, SENT, DELIVERED, FAILED, BOUNCED
    priority VARCHAR(20) NOT NULL DEFAULT 'NORMAL', -- LOW, NORMAL, HIGH, URGENT
    scheduled_at TIMESTAMP,
    sent_at TIMESTAMP,
    delivered_at TIMESTAMP,
    read_at TIMESTAMP,
    failed_at TIMESTAMP,
    failure_reason TEXT,
    retry_count INT NOT NULL DEFAULT 0,
    max_retries INT NOT NULL DEFAULT 3,
    metadata JSONB, -- Additional data: attachments, tracking, etc.
    related_entity_type VARCHAR(50), -- LEAVE_REQUEST, PAYSLIP, INVOICE, etc.
    related_entity_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_company_id ON notifications(company_id);
CREATE INDEX idx_notifications_notification_type ON notifications(notification_type);
CREATE INDEX idx_notifications_status ON notifications(status);
CREATE INDEX idx_notifications_scheduled_at ON notifications(scheduled_at);
CREATE INDEX idx_notifications_sent_at ON notifications(sent_at);
CREATE INDEX idx_notifications_related_entity ON notifications(related_entity_type, related_entity_id);

COMMENT ON TABLE notifications IS 'Notification history and queue';

-- Notification Preferences (User settings)
CREATE TABLE notification_preferences (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    company_id BIGINT,
    channel VARCHAR(50) NOT NULL, -- EMAIL, SMS, PUSH, IN_APP
    event_type VARCHAR(100) NOT NULL, -- LEAVE_APPROVED, PAYSLIP_READY, etc.
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, company_id, channel, event_type)
);

CREATE INDEX idx_notification_preferences_user_id ON notification_preferences(user_id);
CREATE INDEX idx_notification_preferences_company_id ON notification_preferences(company_id);

COMMENT ON TABLE notification_preferences IS 'User notification preferences';

-- Email Queue (for async processing)
CREATE TABLE email_queue (
    id BIGSERIAL PRIMARY KEY,
    notification_id BIGINT,
    from_address VARCHAR(255) NOT NULL,
    to_address VARCHAR(255) NOT NULL,
    cc_address TEXT,
    bcc_address TEXT,
    subject VARCHAR(500) NOT NULL,
    body_html TEXT,
    body_text TEXT,
    attachments JSONB, -- Array of attachment info
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, PROCESSING, SENT, FAILED
    priority INT NOT NULL DEFAULT 5, -- 1=highest, 10=lowest
    retry_count INT NOT NULL DEFAULT 0,
    max_retries INT NOT NULL DEFAULT 3,
    scheduled_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP,
    failed_at TIMESTAMP,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_email_queue_notification_id ON email_queue(notification_id);
CREATE INDEX idx_email_queue_status ON email_queue(status);
CREATE INDEX idx_email_queue_scheduled_at ON email_queue(scheduled_at);
CREATE INDEX idx_email_queue_priority ON email_queue(priority);

COMMENT ON TABLE email_queue IS 'Email sending queue';

-- SMS Queue (for async processing)
CREATE TABLE sms_queue (
    id BIGSERIAL PRIMARY KEY,
    notification_id BIGINT,
    phone_number VARCHAR(20) NOT NULL,
    message TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, PROCESSING, SENT, FAILED
    priority INT NOT NULL DEFAULT 5,
    retry_count INT NOT NULL DEFAULT 0,
    max_retries INT NOT NULL DEFAULT 3,
    scheduled_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP,
    failed_at TIMESTAMP,
    error_message TEXT,
    provider_message_id VARCHAR(255), -- External provider reference
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_sms_queue_notification_id ON sms_queue(notification_id);
CREATE INDEX idx_sms_queue_status ON sms_queue(status);
CREATE INDEX idx_sms_queue_scheduled_at ON sms_queue(scheduled_at);
CREATE INDEX idx_sms_queue_priority ON sms_queue(priority);

COMMENT ON TABLE sms_queue IS 'SMS sending queue';

-- Create updated_at triggers
CREATE TRIGGER update_notification_templates_updated_at BEFORE UPDATE ON notification_templates
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_notifications_updated_at BEFORE UPDATE ON notifications
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_notification_preferences_updated_at BEFORE UPDATE ON notification_preferences
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_email_queue_updated_at BEFORE UPDATE ON email_queue
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_sms_queue_updated_at BEFORE UPDATE ON sms_queue
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
