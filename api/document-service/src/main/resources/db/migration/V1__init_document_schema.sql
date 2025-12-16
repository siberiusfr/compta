-- Migration initiale pour le service de gestion documentaire
-- Création du schéma dédié et des tables

-- Créer le schéma document
CREATE SCHEMA IF NOT EXISTS document;

-- Fonction pour mettre à jour automatiquement updated_at
CREATE OR REPLACE FUNCTION document.update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Table des catégories de documents
CREATE TABLE IF NOT EXISTS document.categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    parent_category_id BIGINT REFERENCES document.categories(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Table des documents
CREATE TABLE IF NOT EXISTS document.documents (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    category_id BIGINT REFERENCES document.categories(id),
    uploaded_by VARCHAR(100) NOT NULL,
    is_public BOOLEAN DEFAULT false,
    version INT DEFAULT 1,
    checksum VARCHAR(64),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Table des versions de documents
CREATE TABLE IF NOT EXISTS document.document_versions (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL REFERENCES document.documents(id) ON DELETE CASCADE,
    version_number INT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    uploaded_by VARCHAR(100) NOT NULL,
    change_description TEXT,
    checksum VARCHAR(64),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(document_id, version_number)
);

-- Table des tags
CREATE TABLE IF NOT EXISTS document.tags (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Table de liaison documents-tags
CREATE TABLE IF NOT EXISTS document.document_tags (
    document_id BIGINT NOT NULL REFERENCES document.documents(id) ON DELETE CASCADE,
    tag_id BIGINT NOT NULL REFERENCES document.tags(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (document_id, tag_id)
);

-- Table des partages de documents
CREATE TABLE IF NOT EXISTS document.document_shares (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL REFERENCES document.documents(id) ON DELETE CASCADE,
    shared_with VARCHAR(100) NOT NULL,
    permission VARCHAR(20) NOT NULL DEFAULT 'READ',
    expires_at TIMESTAMP,
    created_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Table des métadonnées personnalisées
CREATE TABLE IF NOT EXISTS document.document_metadata (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL REFERENCES document.documents(id) ON DELETE CASCADE,
    key VARCHAR(100) NOT NULL,
    value TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(document_id, key)
);

-- Index pour les performances
CREATE INDEX idx_categories_parent ON document.categories(parent_category_id);
CREATE INDEX idx_documents_category ON document.documents(category_id);
CREATE INDEX idx_documents_uploaded_by ON document.documents(uploaded_by);
CREATE INDEX idx_documents_created ON document.documents(created_at);
CREATE INDEX idx_documents_public ON document.documents(is_public);
CREATE INDEX idx_document_versions_document ON document.document_versions(document_id);
CREATE INDEX idx_document_tags_document ON document.document_tags(document_id);
CREATE INDEX idx_document_tags_tag ON document.document_tags(tag_id);
CREATE INDEX idx_document_shares_document ON document.document_shares(document_id);
CREATE INDEX idx_document_shares_shared_with ON document.document_shares(shared_with);
CREATE INDEX idx_document_metadata_document ON document.document_metadata(document_id);

-- Triggers pour updated_at
CREATE TRIGGER update_categories_updated_at BEFORE UPDATE ON document.categories
    FOR EACH ROW EXECUTE FUNCTION document.update_updated_at_column();

CREATE TRIGGER update_documents_updated_at BEFORE UPDATE ON document.documents
    FOR EACH ROW EXECUTE FUNCTION document.update_updated_at_column();

-- Insertion des catégories par défaut
INSERT INTO document.categories (name, description) VALUES
    ('Comptabilité', 'Documents comptables et financiers'),
    ('RH', 'Documents relatifs aux ressources humaines'),
    ('Juridique', 'Documents juridiques et contrats'),
    ('Administratif', 'Documents administratifs généraux')
ON CONFLICT (name) DO NOTHING;
