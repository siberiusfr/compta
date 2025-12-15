-- V4__create_document_tables.sql
-- Document Service Tables: GED, Categories, Versions, Shares

-- Document Categories
CREATE TABLE document_categories (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50),
    parent_id BIGINT,
    level INT NOT NULL DEFAULT 1,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(company_id, code)
);

CREATE INDEX idx_document_categories_company_id ON document_categories(company_id);
CREATE INDEX idx_document_categories_parent_id ON document_categories(parent_id);
CREATE INDEX idx_document_categories_code ON document_categories(code);

COMMENT ON TABLE document_categories IS 'Document categories hierarchy';

-- Documents
CREATE TABLE documents (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,
    category_id BIGINT,
    document_type VARCHAR(50) NOT NULL, -- INVOICE, CONTRACT, PAYSLIP, REPORT, OTHER
    title VARCHAR(255) NOT NULL,
    description TEXT,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    version INT NOT NULL DEFAULT 1,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, ARCHIVED, DELETED
    tags TEXT[], -- Array of tags
    metadata JSONB, -- Additional flexible metadata
    ocr_text TEXT, -- Extracted text from OCR
    ocr_processed_at TIMESTAMP,
    related_entity_type VARCHAR(50), -- JOURNAL_ENTRY, EMPLOYEE, PARTNER, etc.
    related_entity_id BIGINT,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_documents_company_id ON documents(company_id);
CREATE INDEX idx_documents_category_id ON documents(category_id);
CREATE INDEX idx_documents_document_type ON documents(document_type);
CREATE INDEX idx_documents_status ON documents(status);
CREATE INDEX idx_documents_created_by ON documents(created_by);
CREATE INDEX idx_documents_related_entity ON documents(related_entity_type, related_entity_id);
CREATE INDEX idx_documents_tags ON documents USING gin(tags);
CREATE INDEX idx_documents_ocr_text ON documents USING gin(to_tsvector('french', ocr_text));

COMMENT ON TABLE documents IS 'Document management (GED)';

-- Document Versions
CREATE TABLE document_versions (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL,
    version_number INT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    comment TEXT,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(document_id, version_number)
);

CREATE INDEX idx_document_versions_document_id ON document_versions(document_id);
CREATE INDEX idx_document_versions_created_by ON document_versions(created_by);

COMMENT ON TABLE document_versions IS 'Document version history';

-- Document Shares
CREATE TABLE document_shares (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL,
    shared_with_user_id BIGINT NOT NULL,
    can_read BOOLEAN NOT NULL DEFAULT TRUE,
    can_write BOOLEAN NOT NULL DEFAULT FALSE,
    can_delete BOOLEAN NOT NULL DEFAULT FALSE,
    can_share BOOLEAN NOT NULL DEFAULT FALSE,
    expires_at TIMESTAMP,
    shared_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(document_id, shared_with_user_id)
);

CREATE INDEX idx_document_shares_document_id ON document_shares(document_id);
CREATE INDEX idx_document_shares_shared_with_user_id ON document_shares(shared_with_user_id);
CREATE INDEX idx_document_shares_shared_by ON document_shares(shared_by);

COMMENT ON TABLE document_shares IS 'Document sharing permissions';

-- Document Access Logs
CREATE TABLE document_access_logs (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL, -- VIEW, DOWNLOAD, UPDATE, DELETE, SHARE
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_document_access_logs_document_id ON document_access_logs(document_id);
CREATE INDEX idx_document_access_logs_user_id ON document_access_logs(user_id);
CREATE INDEX idx_document_access_logs_created_at ON document_access_logs(created_at);

COMMENT ON TABLE document_access_logs IS 'Document access audit trail';

-- Document Templates
CREATE TABLE document_templates (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    template_type VARCHAR(50) NOT NULL, -- INVOICE, CONTRACT, REPORT, etc.
    file_path VARCHAR(500) NOT NULL,
    description TEXT,
    variables JSONB, -- Template variables definition
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(company_id, name)
);

CREATE INDEX idx_document_templates_company_id ON document_templates(company_id);
CREATE INDEX idx_document_templates_template_type ON document_templates(template_type);

COMMENT ON TABLE document_templates IS 'Document generation templates';

-- Create updated_at triggers
CREATE TRIGGER update_document_categories_updated_at BEFORE UPDATE ON document_categories
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_documents_updated_at BEFORE UPDATE ON documents
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_document_shares_updated_at BEFORE UPDATE ON document_shares
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_document_templates_updated_at BEFORE UPDATE ON document_templates
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
