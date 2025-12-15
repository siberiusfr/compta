-- V5__create_hr_tables.sql
-- HR Service Tables: Employees, Contracts, Payslips, Leaves, Attendances

-- Departments
CREATE TABLE departments (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL,
    description TEXT,
    manager_id BIGINT, -- References employees table (created below)
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(company_id, code)
);

CREATE INDEX idx_departments_company_id ON departments(company_id);
CREATE INDEX idx_departments_code ON departments(code);
CREATE INDEX idx_departments_manager_id ON departments(manager_id);

COMMENT ON TABLE departments IS 'Company departments';

-- Positions
CREATE TABLE positions (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL,
    department_id BIGINT,
    description TEXT,
    level VARCHAR(50), -- JUNIOR, SENIOR, MANAGER, DIRECTOR, etc.
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(company_id, code)
);

CREATE INDEX idx_positions_company_id ON positions(company_id);
CREATE INDEX idx_positions_code ON positions(code);
CREATE INDEX idx_positions_department_id ON positions(department_id);

COMMENT ON TABLE positions IS 'Job positions';

-- Employees
CREATE TABLE employees (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,
    user_id BIGINT, -- Optional: link to users table if employee has system access
    employee_number VARCHAR(50) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    cin VARCHAR(20) UNIQUE, -- National ID
    date_of_birth DATE,
    gender VARCHAR(10),
    marital_status VARCHAR(20),
    phone VARCHAR(20),
    email VARCHAR(255),
    address TEXT,
    city VARCHAR(100),
    postal_code VARCHAR(20),
    emergency_contact_name VARCHAR(255),
    emergency_contact_phone VARCHAR(20),
    hire_date DATE NOT NULL,
    termination_date DATE,
    department_id BIGINT,
    position_id BIGINT,
    manager_id BIGINT, -- References employees (self-reference)
    cnss_number VARCHAR(50), -- Social Security Number
    bank_account_number VARCHAR(50),
    bank_name VARCHAR(255),
    iban VARCHAR(34),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    photo_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(company_id, employee_number)
);

CREATE INDEX idx_employees_company_id ON employees(company_id);
CREATE INDEX idx_employees_user_id ON employees(user_id);
CREATE INDEX idx_employees_employee_number ON employees(employee_number);
CREATE INDEX idx_employees_cin ON employees(cin);
CREATE INDEX idx_employees_department_id ON employees(department_id);
CREATE INDEX idx_employees_position_id ON employees(position_id);
CREATE INDEX idx_employees_manager_id ON employees(manager_id);
CREATE INDEX idx_employees_is_active ON employees(is_active);

COMMENT ON TABLE employees IS 'Company employees';

-- Contracts
CREATE TABLE contracts (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    contract_type VARCHAR(50) NOT NULL, -- CDI, CDD, CIVP, INTERNSHIP, etc.
    start_date DATE NOT NULL,
    end_date DATE,
    salary DECIMAL(15, 3) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'TND',
    work_hours_per_week DECIMAL(5, 2) DEFAULT 40,
    document_id BIGINT, -- Reference to documents table
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- DRAFT, ACTIVE, TERMINATED, EXPIRED
    signed_date DATE,
    terminated_date DATE,
    termination_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_contracts_company_id ON contracts(company_id);
CREATE INDEX idx_contracts_employee_id ON contracts(employee_id);
CREATE INDEX idx_contracts_contract_type ON contracts(contract_type);
CREATE INDEX idx_contracts_status ON contracts(status);
CREATE INDEX idx_contracts_document_id ON contracts(document_id);

COMMENT ON TABLE contracts IS 'Employee contracts';

-- Payslips
CREATE TABLE payslips (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    contract_id BIGINT NOT NULL,
    payslip_number VARCHAR(50) NOT NULL,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    worked_days DECIMAL(5, 2) NOT NULL,
    worked_hours DECIMAL(8, 2),
    gross_salary DECIMAL(15, 3) NOT NULL,
    cnss_employee DECIMAL(15, 3) NOT NULL DEFAULT 0, -- 9.18%
    cnss_employer DECIMAL(15, 3) NOT NULL DEFAULT 0, -- 16.57%
    tax_amount DECIMAL(15, 3) NOT NULL DEFAULT 0, -- IRPP
    other_deductions DECIMAL(15, 3) NOT NULL DEFAULT 0,
    bonuses DECIMAL(15, 3) NOT NULL DEFAULT 0,
    net_salary DECIMAL(15, 3) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT', -- DRAFT, VALIDATED, PAID
    validated_at TIMESTAMP,
    validated_by BIGINT,
    paid_at TIMESTAMP,
    document_id BIGINT, -- Reference to generated payslip PDF
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(company_id, payslip_number)
);

CREATE INDEX idx_payslips_company_id ON payslips(company_id);
CREATE INDEX idx_payslips_employee_id ON payslips(employee_id);
CREATE INDEX idx_payslips_contract_id ON payslips(contract_id);
CREATE INDEX idx_payslips_payslip_number ON payslips(payslip_number);
CREATE INDEX idx_payslips_period_start ON payslips(period_start);
CREATE INDEX idx_payslips_status ON payslips(status);

COMMENT ON TABLE payslips IS 'Employee payslips';

-- Leave Types
CREATE TABLE leave_types (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL,
    days_per_year DECIMAL(5, 2) NOT NULL DEFAULT 0,
    is_paid BOOLEAN NOT NULL DEFAULT TRUE,
    requires_approval BOOLEAN NOT NULL DEFAULT TRUE,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(company_id, code)
);

CREATE INDEX idx_leave_types_company_id ON leave_types(company_id);
CREATE INDEX idx_leave_types_code ON leave_types(code);

COMMENT ON TABLE leave_types IS 'Types of leaves (Annual, Sick, etc.)';

-- Leave Requests
CREATE TABLE leave_requests (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    leave_type_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    days_count DECIMAL(5, 2) NOT NULL,
    reason TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED, CANCELLED
    reviewed_by BIGINT,
    reviewed_at TIMESTAMP,
    review_comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_leave_requests_company_id ON leave_requests(company_id);
CREATE INDEX idx_leave_requests_employee_id ON leave_requests(employee_id);
CREATE INDEX idx_leave_requests_leave_type_id ON leave_requests(leave_type_id);
CREATE INDEX idx_leave_requests_status ON leave_requests(status);
CREATE INDEX idx_leave_requests_start_date ON leave_requests(start_date);

COMMENT ON TABLE leave_requests IS 'Employee leave requests';

-- Leave Balances
CREATE TABLE leave_balances (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    leave_type_id BIGINT NOT NULL,
    year INT NOT NULL,
    total_days DECIMAL(5, 2) NOT NULL DEFAULT 0,
    used_days DECIMAL(5, 2) NOT NULL DEFAULT 0,
    remaining_days DECIMAL(5, 2) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(company_id, employee_id, leave_type_id, year)
);

CREATE INDEX idx_leave_balances_company_id ON leave_balances(company_id);
CREATE INDEX idx_leave_balances_employee_id ON leave_balances(employee_id);
CREATE INDEX idx_leave_balances_leave_type_id ON leave_balances(leave_type_id);
CREATE INDEX idx_leave_balances_year ON leave_balances(year);

COMMENT ON TABLE leave_balances IS 'Employee leave balances per year';

-- Attendances
CREATE TABLE attendances (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    attendance_date DATE NOT NULL,
    check_in TIMESTAMP,
    check_out TIMESTAMP,
    hours_worked DECIMAL(5, 2),
    status VARCHAR(20) NOT NULL DEFAULT 'PRESENT', -- PRESENT, ABSENT, LEAVE, HOLIDAY, WEEKEND
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(company_id, employee_id, attendance_date)
);

CREATE INDEX idx_attendances_company_id ON attendances(company_id);
CREATE INDEX idx_attendances_employee_id ON attendances(employee_id);
CREATE INDEX idx_attendances_attendance_date ON attendances(attendance_date);
CREATE INDEX idx_attendances_status ON attendances(status);

COMMENT ON TABLE attendances IS 'Employee daily attendance';

-- Create updated_at triggers
CREATE TRIGGER update_departments_updated_at BEFORE UPDATE ON departments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_positions_updated_at BEFORE UPDATE ON positions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_employees_updated_at BEFORE UPDATE ON employees
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_contracts_updated_at BEFORE UPDATE ON contracts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_payslips_updated_at BEFORE UPDATE ON payslips
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_leave_types_updated_at BEFORE UPDATE ON leave_types
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_leave_requests_updated_at BEFORE UPDATE ON leave_requests
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_leave_balances_updated_at BEFORE UPDATE ON leave_balances
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_attendances_updated_at BEFORE UPDATE ON attendances
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
