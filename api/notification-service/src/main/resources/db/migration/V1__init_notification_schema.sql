-- Migration initiale pour le service de notifications
-- Création du schéma dédié et des tables

-- Créer le schéma notification
CREATE SCHEMA IF NOT EXISTS notification;

-- Fonction pour mettre à jour automatiquement updated_at
CREATE OR REPLACE FUNCTION notification.update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Table des templates de notifications
CREATE TABLE IF NOT EXISTS notification.templates (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    type VARCHAR(50) NOT NULL,
    subject VARCHAR(255),
    body TEXT NOT NULL,
    variables JSONB,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Table des notifications
CREATE TABLE IF NOT EXISTS notification.notifications (
    id BIGSERIAL PRIMARY KEY,
    recipient VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    subject VARCHAR(255),
    body TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    priority INT DEFAULT 0,
    scheduled_at TIMESTAMP,
    sent_at TIMESTAMP,
    read_at TIMESTAMP,
    error_message TEXT,
    retry_count INT DEFAULT 0,
    max_retries INT DEFAULT 3,
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Table des préférences de notification des utilisateurs
CREATE TABLE IF NOT EXISTS notification.user_preferences (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL UNIQUE,
    email_enabled BOOLEAN DEFAULT true,
    sms_enabled BOOLEAN DEFAULT false,
    push_enabled BOOLEAN DEFAULT true,
    quiet_hours_start TIME,
    quiet_hours_end TIME,
    preferences JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Table des abonnements aux notifications
CREATE TABLE IF NOT EXISTS notification.subscriptions (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, event_type, channel)
);

-- Table des logs d'envoi
CREATE TABLE IF NOT EXISTS notification.delivery_logs (
    id BIGSERIAL PRIMARY KEY,
    notification_id BIGINT NOT NULL REFERENCES notification.notifications(id) ON DELETE CASCADE,
    attempt_number INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    response_code VARCHAR(10),
    response_message TEXT,
    attempted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Table des webhooks
CREATE TABLE IF NOT EXISTS notification.webhooks (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    url VARCHAR(500) NOT NULL,
    event_types TEXT[] NOT NULL,
    secret VARCHAR(255),
    is_active BOOLEAN DEFAULT true,
    retry_policy JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index pour les performances
CREATE INDEX idx_templates_type ON notification.templates(type);
CREATE INDEX idx_templates_active ON notification.templates(is_active);
CREATE INDEX idx_notifications_recipient ON notification.notifications(recipient);
CREATE INDEX idx_notifications_status ON notification.notifications(status);
CREATE INDEX idx_notifications_type ON notification.notifications(type);
CREATE INDEX idx_notifications_channel ON notification.notifications(channel);
CREATE INDEX idx_notifications_scheduled ON notification.notifications(scheduled_at);
CREATE INDEX idx_notifications_created ON notification.notifications(created_at);
CREATE INDEX idx_user_preferences_user ON notification.user_preferences(user_id);
CREATE INDEX idx_subscriptions_user ON notification.subscriptions(user_id);
CREATE INDEX idx_subscriptions_event ON notification.subscriptions(event_type);
CREATE INDEX idx_delivery_logs_notification ON notification.delivery_logs(notification_id);
CREATE INDEX idx_webhooks_active ON notification.webhooks(is_active);

-- Triggers pour updated_at
CREATE TRIGGER update_templates_updated_at BEFORE UPDATE ON notification.templates
    FOR EACH ROW EXECUTE FUNCTION notification.update_updated_at_column();

CREATE TRIGGER update_notifications_updated_at BEFORE UPDATE ON notification.notifications
    FOR EACH ROW EXECUTE FUNCTION notification.update_updated_at_column();

CREATE TRIGGER update_user_preferences_updated_at BEFORE UPDATE ON notification.user_preferences
    FOR EACH ROW EXECUTE FUNCTION notification.update_updated_at_column();

CREATE TRIGGER update_webhooks_updated_at BEFORE UPDATE ON notification.webhooks
    FOR EACH ROW EXECUTE FUNCTION notification.update_updated_at_column();

-- Insertion des templates par défaut
INSERT INTO notification.templates (name, type, subject, body, variables) VALUES
    ('welcome_email', 'EMAIL', 'Bienvenue sur Compta', 'Bonjour {{name}}, bienvenue sur notre plateforme!', '["name"]'::jsonb),
    ('password_reset', 'EMAIL', 'Réinitialisation de mot de passe', 'Cliquez sur ce lien pour réinitialiser votre mot de passe: {{link}}', '["link"]'::jsonb),
    ('invoice_created', 'EMAIL', 'Nouvelle facture créée', 'Une nouvelle facture #{{invoice_number}} a été créée.', '["invoice_number"]'::jsonb)
ON CONFLICT (name) DO NOTHING;
