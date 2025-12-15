-- V8__create_constraints.sql
-- Foreign Key Constraints

-- Auth Service Foreign Keys
ALTER TABLE role_permissions ADD CONSTRAINT fk_role_permissions_role
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE;

ALTER TABLE role_permissions ADD CONSTRAINT fk_role_permissions_permission
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE;

ALTER TABLE user_company_roles ADD CONSTRAINT fk_user_company_roles_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE user_company_roles ADD CONSTRAINT fk_user_company_roles_company
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE;

ALTER TABLE user_company_roles ADD CONSTRAINT fk_user_company_roles_role
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE;

ALTER TABLE user_permissions ADD CONSTRAINT fk_user_permissions_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE user_permissions ADD CONSTRAINT fk_user_permissions_company
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE;

ALTER TABLE user_permissions ADD CONSTRAINT fk_user_permissions_permission
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE;

-- Accounting Service Foreign Keys
ALTER TABLE accounts ADD CONSTRAINT fk_accounts_company
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE;

ALTER TABLE accounts ADD CONSTRAINT fk_accounts_parent
    FOREIGN KEY (parent_account_id) REFERENCES accounts(id) ON DELETE SET NULL;

ALTER TABLE journals ADD CONSTRAINT fk_journals_company
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE;

ALTER TABLE fiscal_years ADD CONSTRAINT fk_fiscal_years_company
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE;

ALTER TABLE fiscal_years ADD CONSTRAINT fk_fiscal_years_closed_by
    FOREIGN KEY (closed_by) REFERENCES users(id) ON DELETE SET NULL;

ALTER TABLE journal_entries ADD CONSTRAINT fk_journal_entries_company
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE;

ALTER TABLE journal_entries ADD CONSTRAINT fk_journal_entries_journal
    FOREIGN KEY (journal_id) REFERENCES journals(id) ON DELETE RESTRICT;

ALTER TABLE journal_entries ADD CONSTRAINT fk_journal_entries_fiscal_year
    FOREIGN KEY (fiscal_year_id) REFERENCES fiscal_years(id) ON DELETE RESTRICT;

ALTER TABLE journal_entries ADD CONSTRAINT fk_journal_entries_created_by
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE RESTRICT;

ALTER TABLE journal_entries ADD CONSTRAINT fk_journal_entries_validated_by
    FOREIGN KEY (validated_by) REFERENCES users(id) ON DELETE SET NULL;

ALTER TABLE journal_entry_lines ADD CONSTRAINT fk_journal_entry_lines_entry
    FOREIGN KEY (journal_entry_id) REFERENCES journal_entries(id) ON DELETE CASCADE;

ALTER TABLE journal_entry_lines ADD CONSTRAINT fk_journal_entry_lines_account
    FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE RESTRICT;

ALTER TABLE bank_accounts ADD CONSTRAINT fk_bank_accounts_company
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE;

ALTER TABLE bank_accounts ADD CONSTRAINT fk_bank_accounts_account
    FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE RESTRICT;

ALTER TABLE tax_declarations ADD CONSTRAINT fk_tax_declarations_company
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE;

ALTER TABLE tax_declarations ADD CONSTRAINT fk_tax_declarations_fiscal_year
    FOREIGN KEY (fiscal_year_id) REFERENCES fiscal_years(id) ON DELETE RESTRICT;

ALTER TABLE partners ADD CONSTRAINT fk_partners_company
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE;

-- Document Service Foreign Keys
ALTER TABLE document_categories ADD CONSTRAINT fk_document_categories_company
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE;

ALTER TABLE document_categories ADD CONSTRAINT fk_document_categories_parent
    FOREIGN KEY (parent_id) REFERENCES document_categories(id) ON DELETE CASCADE;

ALTER TABLE documents ADD CONSTRAINT fk_documents_company
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE;

ALTER TABLE documents ADD CONSTRAINT fk_documents_category
    FOREIGN KEY (category_id) REFERENCES document_categories(id) ON DELETE SET NULL;

ALTER TABLE documents ADD CONSTRAINT fk_documents_created_by
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE RESTRICT;

ALTER TABLE document_versions ADD CONSTRAINT fk_document_versions_document
    FOREIGN KEY (document_id) REFERENCES documents(id) ON DELETE CASCADE;

ALTER TABLE document_versions ADD CONSTRAINT fk_document_versions_created_by
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE RESTRICT;

ALTER TABLE document_shares ADD CONSTRAINT fk_document_shares_document
    FOREIGN KEY (document_id) REFERENCES documents(id) ON DELETE CASCADE;

ALTER TABLE document_shares ADD CONSTRAINT fk_document_shares_user
    FOREIGN KEY (shared_with_user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE document_shares ADD CONSTRAINT fk_document_shares_shared_by
    FOREIGN KEY (shared_by) REFERENCES users(id) ON DELETE RESTRICT;

ALTER TABLE document_access_logs ADD CONSTRAINT fk_document_access_logs_document
    FOREIGN KEY (document_id) REFERENCES documents(id) ON DELETE CASCADE;

ALTER TABLE document_access_logs ADD CONSTRAINT fk_document_access_logs_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE document_templates ADD CONSTRAINT fk_document_templates_company
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE;

-- HR Service Foreign Keys
ALTER TABLE departments ADD CONSTRAINT fk_departments_company
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE;

-- Manager FK will be added after employees table exists
-- ALTER TABLE departments ADD CONSTRAINT fk_departments_manager
--     FOREIGN KEY (manager_id) REFERENCES employees(id) ON DELETE SET NULL;

ALTER TABLE positions ADD CONSTRAINT fk_positions_company
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE;

ALTER TABLE positions ADD CONSTRAINT fk_positions_department
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE SET NULL;

ALTER TABLE employees ADD CONSTRAINT fk_employees_company
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE;

ALTER TABLE employees ADD CONSTRAINT fk_employees_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL;

ALTER TABLE employees ADD CONSTRAINT fk_employees_department
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE SET NULL;

ALTER TABLE employees ADD CONSTRAINT fk_employees_position
    FOREIGN KEY (position_id) REFERENCES positions(id) ON DELETE SET NULL;

ALTER TABLE employees ADD CONSTRAINT fk_employees_manager
    FOREIGN KEY (manager_id) REFERENCES employees(id) ON DELETE SET NULL;

-- Now add the department manager FK
ALTER TABLE departments ADD CONSTRAINT fk_departments_manager
    FOREIGN KEY (manager_id) REFERENCES employees(id) ON DELETE SET NULL;

ALTER TABLE contracts ADD CONSTRAINT fk_contracts_company
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE;

ALTER TABLE contracts ADD CONSTRAINT fk_contracts_employee
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE;

ALTER TABLE contracts ADD CONSTRAINT fk_contracts_document
    FOREIGN KEY (document_id) REFERENCES documents(id) ON DELETE SET NULL;

ALTER TABLE payslips ADD CONSTRAINT fk_payslips_company
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE;

ALTER TABLE payslips ADD CONSTRAINT fk_payslips_employee
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE;

ALTER TABLE payslips ADD CONSTRAINT fk_payslips_contract
    FOREIGN KEY (contract_id) REFERENCES contracts(id) ON DELETE RESTRICT;

ALTER TABLE payslips ADD CONSTRAINT fk_payslips_validated_by
    FOREIGN KEY (validated_by) REFERENCES users(id) ON DELETE SET NULL;

ALTER TABLE payslips ADD CONSTRAINT fk_payslips_document
    FOREIGN KEY (document_id) REFERENCES documents(id) ON DELETE SET NULL;

ALTER TABLE leave_types ADD CONSTRAINT fk_leave_types_company
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE;

ALTER TABLE leave_requests ADD CONSTRAINT fk_leave_requests_company
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE;

ALTER TABLE leave_requests ADD CONSTRAINT fk_leave_requests_employee
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE;

ALTER TABLE leave_requests ADD CONSTRAINT fk_leave_requests_leave_type
    FOREIGN KEY (leave_type_id) REFERENCES leave_types(id) ON DELETE RESTRICT;

ALTER TABLE leave_requests ADD CONSTRAINT fk_leave_requests_reviewed_by
    FOREIGN KEY (reviewed_by) REFERENCES users(id) ON DELETE SET NULL;

ALTER TABLE leave_balances ADD CONSTRAINT fk_leave_balances_company
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE;

ALTER TABLE leave_balances ADD CONSTRAINT fk_leave_balances_employee
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE;

ALTER TABLE leave_balances ADD CONSTRAINT fk_leave_balances_leave_type
    FOREIGN KEY (leave_type_id) REFERENCES leave_types(id) ON DELETE CASCADE;

ALTER TABLE attendances ADD CONSTRAINT fk_attendances_company
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE;

ALTER TABLE attendances ADD CONSTRAINT fk_attendances_employee
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE;

-- Notification Service Foreign Keys
ALTER TABLE notifications ADD CONSTRAINT fk_notifications_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE notifications ADD CONSTRAINT fk_notifications_company
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE;

ALTER TABLE notifications ADD CONSTRAINT fk_notifications_template
    FOREIGN KEY (template_id) REFERENCES notification_templates(id) ON DELETE SET NULL;

ALTER TABLE notification_preferences ADD CONSTRAINT fk_notification_preferences_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE notification_preferences ADD CONSTRAINT fk_notification_preferences_company
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE;

ALTER TABLE email_queue ADD CONSTRAINT fk_email_queue_notification
    FOREIGN KEY (notification_id) REFERENCES notifications(id) ON DELETE CASCADE;

ALTER TABLE sms_queue ADD CONSTRAINT fk_sms_queue_notification
    FOREIGN KEY (notification_id) REFERENCES notifications(id) ON DELETE CASCADE;
