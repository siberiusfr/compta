-- V3__create_accounting_tables.sql
-- Accounting Service Tables: Chart of Accounts, Journals, Entries, Fiscal Years

-- Chart of Accounts
CREATE TABLE accounts (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,
    account_number VARCHAR(20) NOT NULL,
    account_name VARCHAR(255) NOT NULL,
    account_type VARCHAR(50) NOT NULL, -- ASSET, LIABILITY, EQUITY, REVENUE, EXPENSE
    parent_account_id BIGINT,
    level INT NOT NULL DEFAULT 1,
    is_header BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(company_id, account_number)
);

CREATE INDEX idx_accounts_company_id ON accounts(company_id);
CREATE INDEX idx_accounts_account_number ON accounts(account_number);
CREATE INDEX idx_accounts_account_type ON accounts(account_type);
CREATE INDEX idx_accounts_parent_account_id ON accounts(parent_account_id);

COMMENT ON TABLE accounts IS 'Chart of accounts (Plan comptable)';

-- Journals
CREATE TABLE journals (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,
    code VARCHAR(10) NOT NULL,
    name VARCHAR(255) NOT NULL,
    journal_type VARCHAR(50) NOT NULL, -- PURCHASE, SALE, BANK, CASH, GENERAL, OPENING
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(company_id, code)
);

CREATE INDEX idx_journals_company_id ON journals(company_id);
CREATE INDEX idx_journals_code ON journals(code);
CREATE INDEX idx_journals_journal_type ON journals(journal_type);

COMMENT ON TABLE journals IS 'Accounting journals';

-- Fiscal Years
CREATE TABLE fiscal_years (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,
    year INT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    is_closed BOOLEAN NOT NULL DEFAULT FALSE,
    closed_at TIMESTAMP,
    closed_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(company_id, year)
);

CREATE INDEX idx_fiscal_years_company_id ON fiscal_years(company_id);
CREATE INDEX idx_fiscal_years_year ON fiscal_years(year);
CREATE INDEX idx_fiscal_years_is_closed ON fiscal_years(is_closed);

COMMENT ON TABLE fiscal_years IS 'Fiscal years (Exercices comptables)';

-- Journal Entries (Headers)
CREATE TABLE journal_entries (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,
    journal_id BIGINT NOT NULL,
    fiscal_year_id BIGINT NOT NULL,
    entry_number VARCHAR(50) NOT NULL,
    entry_date DATE NOT NULL,
    reference VARCHAR(100),
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT', -- DRAFT, POSTED, VALIDATED, CANCELLED
    posted_at TIMESTAMP,
    validated_at TIMESTAMP,
    validated_by BIGINT,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(company_id, journal_id, entry_number)
);

CREATE INDEX idx_journal_entries_company_id ON journal_entries(company_id);
CREATE INDEX idx_journal_entries_journal_id ON journal_entries(journal_id);
CREATE INDEX idx_journal_entries_fiscal_year_id ON journal_entries(fiscal_year_id);
CREATE INDEX idx_journal_entries_entry_date ON journal_entries(entry_date);
CREATE INDEX idx_journal_entries_status ON journal_entries(status);
CREATE INDEX idx_journal_entries_created_by ON journal_entries(created_by);

COMMENT ON TABLE journal_entries IS 'Journal entry headers (Ecritures comptables)';

-- Journal Entry Lines
CREATE TABLE journal_entry_lines (
    id BIGSERIAL PRIMARY KEY,
    journal_entry_id BIGINT NOT NULL,
    account_id BIGINT NOT NULL,
    line_number INT NOT NULL,
    label VARCHAR(255) NOT NULL,
    debit DECIMAL(15, 3) NOT NULL DEFAULT 0,
    credit DECIMAL(15, 3) NOT NULL DEFAULT 0,
    partner_type VARCHAR(50), -- CUSTOMER, SUPPLIER, EMPLOYEE, OTHER
    partner_id BIGINT,
    reconciled BOOLEAN NOT NULL DEFAULT FALSE,
    reconciliation_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_debit_or_credit CHECK ((debit > 0 AND credit = 0) OR (credit > 0 AND debit = 0))
);

CREATE INDEX idx_journal_entry_lines_journal_entry_id ON journal_entry_lines(journal_entry_id);
CREATE INDEX idx_journal_entry_lines_account_id ON journal_entry_lines(account_id);
CREATE INDEX idx_journal_entry_lines_partner_id ON journal_entry_lines(partner_id);
CREATE INDEX idx_journal_entry_lines_reconciled ON journal_entry_lines(reconciled);

COMMENT ON TABLE journal_entry_lines IS 'Journal entry lines (Lignes d\'écritures)';

-- Bank Accounts
CREATE TABLE bank_accounts (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,
    account_id BIGINT NOT NULL,
    bank_name VARCHAR(255) NOT NULL,
    account_number VARCHAR(50) NOT NULL,
    iban VARCHAR(34),
    swift_bic VARCHAR(11),
    currency VARCHAR(3) NOT NULL DEFAULT 'TND',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(company_id, account_number)
);

CREATE INDEX idx_bank_accounts_company_id ON bank_accounts(company_id);
CREATE INDEX idx_bank_accounts_account_id ON bank_accounts(account_id);

COMMENT ON TABLE bank_accounts IS 'Company bank accounts';

-- Tax Declarations
CREATE TABLE tax_declarations (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,
    fiscal_year_id BIGINT NOT NULL,
    period_type VARCHAR(20) NOT NULL, -- MONTHLY, QUARTERLY, YEARLY
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    declaration_type VARCHAR(50) NOT NULL, -- TVA, IS, IRPP, etc.
    tva_collected DECIMAL(15, 3) NOT NULL DEFAULT 0,
    tva_paid DECIMAL(15, 3) NOT NULL DEFAULT 0,
    tva_to_pay DECIMAL(15, 3) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT', -- DRAFT, SUBMITTED, PAID
    submitted_at TIMESTAMP,
    paid_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_tax_declarations_company_id ON tax_declarations(company_id);
CREATE INDEX idx_tax_declarations_fiscal_year_id ON tax_declarations(fiscal_year_id);
CREATE INDEX idx_tax_declarations_period_start ON tax_declarations(period_start);
CREATE INDEX idx_tax_declarations_status ON tax_declarations(status);

COMMENT ON TABLE tax_declarations IS 'Tax declarations (Déclarations fiscales)';

-- Partners (Customers/Suppliers)
CREATE TABLE partners (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,
    partner_type VARCHAR(20) NOT NULL, -- CUSTOMER, SUPPLIER, BOTH
    code VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    legal_name VARCHAR(255),
    tax_id VARCHAR(50),
    address TEXT,
    city VARCHAR(100),
    postal_code VARCHAR(20),
    country VARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(255),
    website VARCHAR(255),
    payment_terms INT DEFAULT 30, -- Days
    credit_limit DECIMAL(15, 3),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(company_id, code)
);

CREATE INDEX idx_partners_company_id ON partners(company_id);
CREATE INDEX idx_partners_partner_type ON partners(partner_type);
CREATE INDEX idx_partners_code ON partners(code);
CREATE INDEX idx_partners_tax_id ON partners(tax_id);

COMMENT ON TABLE partners IS 'Business partners (customers and suppliers)';

-- Create updated_at triggers
CREATE TRIGGER update_accounts_updated_at BEFORE UPDATE ON accounts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_journals_updated_at BEFORE UPDATE ON journals
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_fiscal_years_updated_at BEFORE UPDATE ON fiscal_years
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_journal_entries_updated_at BEFORE UPDATE ON journal_entries
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_journal_entry_lines_updated_at BEFORE UPDATE ON journal_entry_lines
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_bank_accounts_updated_at BEFORE UPDATE ON bank_accounts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_tax_declarations_updated_at BEFORE UPDATE ON tax_declarations
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_partners_updated_at BEFORE UPDATE ON partners
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
