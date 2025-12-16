-- Migration initiale pour le service de comptabilité
-- Création du schéma dédié et des tables

-- Créer le schéma accounting
CREATE SCHEMA IF NOT EXISTS accounting;

-- Fonction pour mettre à jour automatiquement updated_at
CREATE OR REPLACE FUNCTION accounting.update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Table des entreprises
CREATE TABLE IF NOT EXISTS accounting.companies (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    siret VARCHAR(14),
    address TEXT,
    phone VARCHAR(20),
    email VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Table des comptes comptables
CREATE TABLE IF NOT EXISTS accounting.accounts (
    id BIGSERIAL PRIMARY KEY,
    account_number VARCHAR(20) NOT NULL,
    account_name VARCHAR(255) NOT NULL,
    account_type VARCHAR(50) NOT NULL,
    parent_account_id BIGINT REFERENCES accounting.accounts(id),
    company_id BIGINT NOT NULL REFERENCES accounting.companies(id),
    balance DECIMAL(15, 2) DEFAULT 0.00,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(company_id, account_number)
);

-- Table des écritures comptables
CREATE TABLE IF NOT EXISTS accounting.journal_entries (
    id BIGSERIAL PRIMARY KEY,
    entry_date DATE NOT NULL,
    description TEXT,
    reference VARCHAR(100),
    company_id BIGINT NOT NULL REFERENCES accounting.companies(id),
    created_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Table des lignes d'écriture
CREATE TABLE IF NOT EXISTS accounting.journal_entry_lines (
    id BIGSERIAL PRIMARY KEY,
    journal_entry_id BIGINT NOT NULL REFERENCES accounting.journal_entries(id) ON DELETE CASCADE,
    account_id BIGINT NOT NULL REFERENCES accounting.accounts(id),
    debit DECIMAL(15, 2) DEFAULT 0.00,
    credit DECIMAL(15, 2) DEFAULT 0.00,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index pour les performances
CREATE INDEX idx_companies_name ON accounting.companies(name);
CREATE INDEX idx_accounts_company ON accounting.accounts(company_id);
CREATE INDEX idx_accounts_type ON accounting.accounts(account_type);
CREATE INDEX idx_accounts_parent ON accounting.accounts(parent_account_id);
CREATE INDEX idx_accounts_number ON accounting.accounts(account_number);
CREATE INDEX idx_journal_entries_company ON accounting.journal_entries(company_id);
CREATE INDEX idx_journal_entries_date ON accounting.journal_entries(entry_date);
CREATE INDEX idx_journal_entry_lines_entry ON accounting.journal_entry_lines(journal_entry_id);
CREATE INDEX idx_journal_entry_lines_account ON accounting.journal_entry_lines(account_id);

-- Triggers pour updated_at
CREATE TRIGGER update_companies_updated_at BEFORE UPDATE ON accounting.companies
    FOR EACH ROW EXECUTE FUNCTION accounting.update_updated_at_column();

CREATE TRIGGER update_accounts_updated_at BEFORE UPDATE ON accounting.accounts
    FOR EACH ROW EXECUTE FUNCTION accounting.update_updated_at_column();

CREATE TRIGGER update_journal_entries_updated_at BEFORE UPDATE ON accounting.journal_entries
    FOR EACH ROW EXECUTE FUNCTION accounting.update_updated_at_column();
