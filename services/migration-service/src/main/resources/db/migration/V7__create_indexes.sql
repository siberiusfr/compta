-- V7__create_indexes.sql
-- Additional performance indexes

-- Composite indexes for common queries

-- Users
CREATE INDEX idx_users_email_is_active ON users(email, is_active);

-- Companies
CREATE INDEX idx_companies_name_is_active ON companies(name, is_active);

-- User Company Roles (Multi-tenant queries)
CREATE INDEX idx_user_company_roles_user_company ON user_company_roles(user_id, company_id) WHERE is_active = true;
CREATE INDEX idx_user_company_roles_company_role ON user_company_roles(company_id, role_id) WHERE is_active = true;

-- Accounts (Accounting)
CREATE INDEX idx_accounts_company_type ON accounts(company_id, account_type) WHERE is_active = true;
CREATE INDEX idx_accounts_company_number ON accounts(company_id, account_number);

-- Journal Entries
CREATE INDEX idx_journal_entries_company_date ON journal_entries(company_id, entry_date);
CREATE INDEX idx_journal_entries_company_status ON journal_entries(company_id, status);
CREATE INDEX idx_journal_entries_company_journal_date ON journal_entries(company_id, journal_id, entry_date);

-- Journal Entry Lines (for balance/ledger queries)
CREATE INDEX idx_journal_entry_lines_account_date ON journal_entry_lines(account_id, journal_entry_id);
CREATE INDEX idx_journal_entry_lines_partner ON journal_entry_lines(partner_type, partner_id) WHERE partner_id IS NOT NULL;

-- Partners
CREATE INDEX idx_partners_company_type ON partners(company_id, partner_type) WHERE is_active = true;

-- Documents
CREATE INDEX idx_documents_company_created ON documents(company_id, created_at DESC);
CREATE INDEX idx_documents_company_type_status ON documents(company_id, document_type, status);
CREATE INDEX idx_documents_company_category ON documents(company_id, category_id) WHERE status = 'ACTIVE';

-- Document Shares (permission checks)
CREATE INDEX idx_document_shares_user_doc ON document_shares(shared_with_user_id, document_id);

-- Employees
CREATE INDEX idx_employees_company_active ON employees(company_id, is_active);
CREATE INDEX idx_employees_company_department ON employees(company_id, department_id) WHERE is_active = true;

-- Contracts
CREATE INDEX idx_contracts_company_status ON contracts(company_id, status);
CREATE INDEX idx_contracts_employee_active ON contracts(employee_id, status) WHERE status = 'ACTIVE';

-- Payslips
CREATE INDEX idx_payslips_company_period ON payslips(company_id, period_start DESC);
CREATE INDEX idx_payslips_employee_period ON payslips(employee_id, period_start DESC);

-- Leave Requests
CREATE INDEX idx_leave_requests_company_status ON leave_requests(company_id, status);
CREATE INDEX idx_leave_requests_employee_date ON leave_requests(employee_id, start_date DESC);
CREATE INDEX idx_leave_requests_date_range ON leave_requests(start_date, end_date);

-- Attendances
CREATE INDEX idx_attendances_company_date ON attendances(company_id, attendance_date DESC);
CREATE INDEX idx_attendances_employee_date ON attendances(employee_id, attendance_date DESC);

-- Notifications
CREATE INDEX idx_notifications_user_created ON notifications(user_id, created_at DESC);
CREATE INDEX idx_notifications_user_status ON notifications(user_id, status);
CREATE INDEX idx_notifications_status_scheduled ON notifications(status, scheduled_at) WHERE status = 'PENDING';

-- Email Queue (for processing)
CREATE INDEX idx_email_queue_pending ON email_queue(status, priority, scheduled_at)
    WHERE status IN ('PENDING', 'PROCESSING');

-- SMS Queue (for processing)
CREATE INDEX idx_sms_queue_pending ON sms_queue(status, priority, scheduled_at)
    WHERE status IN ('PENDING', 'PROCESSING');

-- Full-text search indexes
CREATE INDEX idx_users_name_search ON users USING gin(to_tsvector('french', first_name || ' ' || last_name));
CREATE INDEX idx_companies_name_search ON companies USING gin(to_tsvector('french', name));
CREATE INDEX idx_employees_name_search ON employees USING gin(to_tsvector('french', first_name || ' ' || last_name));
CREATE INDEX idx_partners_name_search ON partners USING gin(to_tsvector('french', name));

-- Partial indexes for specific queries
CREATE INDEX idx_fiscal_years_open ON fiscal_years(company_id, year) WHERE is_closed = false;
CREATE INDEX idx_journal_entries_draft ON journal_entries(company_id) WHERE status = 'DRAFT';
CREATE INDEX idx_journal_entry_lines_unreconciled ON journal_entry_lines(account_id) WHERE reconciled = false;
