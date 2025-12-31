-- Migration initiale pour le service RH
-- Création du schéma dédié et des tables
-- Créer le schéma hr
CREATE
    SCHEMA IF NOT EXISTS hr;

-- Fonction pour mettre à jour automatiquement updated_at
CREATE
    OR REPLACE FUNCTION hr.update_updated_at_column() RETURNS TRIGGER AS $$ BEGIN NEW.updated_at = CURRENT_TIMESTAMP;

RETURN NEW;
END;

$$ LANGUAGE 'plpgsql';

-- Table des employés
CREATE
    TABLE
        IF NOT EXISTS hr.employees(
            id BIGSERIAL PRIMARY KEY,
            employee_number VARCHAR(50) NOT NULL UNIQUE,
            first_name VARCHAR(100) NOT NULL,
            last_name VARCHAR(100) NOT NULL,
            email VARCHAR(255) NOT NULL UNIQUE,
            phone VARCHAR(20),
            date_of_birth DATE,
            hire_date DATE NOT NULL,
            termination_date DATE,
            POSITION VARCHAR(100),
            department VARCHAR(100),
            salary DECIMAL(
                12,
                2
            ),
            is_active BOOLEAN DEFAULT TRUE,
            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        );

-- Table des départements
CREATE
    TABLE
        IF NOT EXISTS hr.departments(
            id BIGSERIAL PRIMARY KEY,
            name VARCHAR(100) NOT NULL UNIQUE,
            description TEXT,
            manager_id BIGINT REFERENCES hr.employees(id),
            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        );

-- Table des contrats
CREATE
    TABLE
        IF NOT EXISTS hr.contracts(
            id BIGSERIAL PRIMARY KEY,
            employee_id BIGINT NOT NULL REFERENCES hr.employees(id) ON
            DELETE
                CASCADE,
                contract_type VARCHAR(50) NOT NULL,
                start_date DATE NOT NULL,
                end_date DATE,
                salary DECIMAL(
                    12,
                    2
                ) NOT NULL,
                working_hours_per_week DECIMAL(
                    5,
                    2
                ),
                is_active BOOLEAN DEFAULT TRUE,
                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        );

-- Table des congés
CREATE
    TABLE
        IF NOT EXISTS hr.leaves(
            id BIGSERIAL PRIMARY KEY,
            employee_id BIGINT NOT NULL REFERENCES hr.employees(id) ON
            DELETE
                CASCADE,
                leave_type VARCHAR(50) NOT NULL,
                start_date DATE NOT NULL,
                end_date DATE NOT NULL,
                days_count DECIMAL(
                    5,
                    2
                ) NOT NULL,
                status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                reason TEXT,
                approved_by BIGINT REFERENCES hr.employees(id),
                approved_at TIMESTAMP,
                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        );

-- Table des évaluations
CREATE
    TABLE
        IF NOT EXISTS hr.evaluations(
            id BIGSERIAL PRIMARY KEY,
            employee_id BIGINT NOT NULL REFERENCES hr.employees(id) ON
            DELETE
                CASCADE,
                evaluator_id BIGINT NOT NULL REFERENCES hr.employees(id),
                evaluation_date DATE NOT NULL,
                period_start DATE NOT NULL,
                period_end DATE NOT NULL,
                overall_rating INT CHECK(
                    overall_rating BETWEEN 1 AND 5
                ),
                comments TEXT,
                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        );

-- Table des fiches de paie
CREATE
    TABLE
        IF NOT EXISTS hr.payslips(
            id BIGSERIAL PRIMARY KEY,
            employee_id BIGINT NOT NULL REFERENCES hr.employees(id) ON
            DELETE
                CASCADE,
                period_start DATE NOT NULL,
                period_end DATE NOT NULL,
                gross_salary DECIMAL(
                    12,
                    2
                ) NOT NULL,
                net_salary DECIMAL(
                    12,
                    2
                ) NOT NULL,
                deductions DECIMAL(
                    12,
                    2
                ) DEFAULT 0.00,
                bonuses DECIMAL(
                    12,
                    2
                ) DEFAULT 0.00,
                payment_date DATE,
                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        );

-- Index pour les performances
CREATE
    INDEX idx_employees_number ON
    hr.employees(employee_number);

CREATE
    INDEX idx_employees_email ON
    hr.employees(email);

CREATE
    INDEX idx_employees_active ON
    hr.employees(is_active);

CREATE
    INDEX idx_employees_department ON
    hr.employees(department);

CREATE
    INDEX idx_departments_name ON
    hr.departments(name);

CREATE
    INDEX idx_contracts_employee ON
    hr.contracts(employee_id);

CREATE
    INDEX idx_contracts_active ON
    hr.contracts(is_active);

CREATE
    INDEX idx_leaves_employee ON
    hr.leaves(employee_id);

CREATE
    INDEX idx_leaves_status ON
    hr.leaves(status);

CREATE
    INDEX idx_leaves_dates ON
    hr.leaves(
        start_date,
        end_date
    );

CREATE
    INDEX idx_evaluations_employee ON
    hr.evaluations(employee_id);

CREATE
    INDEX idx_evaluations_date ON
    hr.evaluations(evaluation_date);

CREATE
    INDEX idx_payslips_employee ON
    hr.payslips(employee_id);

CREATE
    INDEX idx_payslips_period ON
    hr.payslips(
        period_start,
        period_end
    );

-- Triggers pour updated_at
CREATE
    TRIGGER update_employees_updated_at BEFORE UPDATE
        ON
        hr.employees FOR EACH ROW EXECUTE FUNCTION hr.update_updated_at_column();

CREATE
    TRIGGER update_departments_updated_at BEFORE UPDATE
        ON
        hr.departments FOR EACH ROW EXECUTE FUNCTION hr.update_updated_at_column();

CREATE
    TRIGGER update_contracts_updated_at BEFORE UPDATE
        ON
        hr.contracts FOR EACH ROW EXECUTE FUNCTION hr.update_updated_at_column();

CREATE
    TRIGGER update_leaves_updated_at BEFORE UPDATE
        ON
        hr.leaves FOR EACH ROW EXECUTE FUNCTION hr.update_updated_at_column();

CREATE
    TRIGGER update_evaluations_updated_at BEFORE UPDATE
        ON
        hr.evaluations FOR EACH ROW EXECUTE FUNCTION hr.update_updated_at_column();

CREATE
    TRIGGER update_payslips_updated_at BEFORE UPDATE
        ON
        hr.payslips FOR EACH ROW EXECUTE FUNCTION hr.update_updated_at_column();
