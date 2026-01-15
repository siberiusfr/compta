-- V1__create_referentiel_schema.sql
-- Création du schéma
CREATE SCHEMA IF NOT EXISTS referentiel;

-- Table des familles de produits
CREATE TABLE referentiel.familles_produits (
  id BIGSERIAL PRIMARY KEY,
  code VARCHAR(50) NOT NULL,
  libelle VARCHAR(255) NOT NULL,
  description TEXT,
  entreprise_id BIGINT NOT NULL,
  actif BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_famille_code_entreprise UNIQUE (code, entreprise_id)
);

CREATE INDEX idx_familles_produits_entreprise ON referentiel.familles_produits (entreprise_id);

CREATE INDEX idx_familles_produits_actif ON referentiel.familles_produits (actif);

-- Table des produits et services
CREATE TABLE referentiel.produits (
  id BIGSERIAL PRIMARY KEY,
  reference VARCHAR(50) NOT NULL,
  designation VARCHAR(255) NOT NULL,
  description TEXT,
  prix_achat DECIMAL(15, 3),
  prix_vente DECIMAL(15, 3),
  taux_tva DECIMAL(5, 2) DEFAULT 19.00,
  unite VARCHAR(20) DEFAULT 'U',
  type_stock VARCHAR(20) NOT NULL CHECK (type_stock IN ('STOCKABLE', 'NON_STOCKABLE')),
  type_article VARCHAR(20) NOT NULL CHECK (type_article IN ('PRODUIT', 'SERVICE')),
  famille_id BIGINT,
  entreprise_id BIGINT NOT NULL,
  actif BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_produit_famille FOREIGN KEY (famille_id) REFERENCES referentiel.familles_produits (id) ON DELETE SET NULL,
  CONSTRAINT uk_produit_reference_entreprise UNIQUE (reference, entreprise_id)
);

CREATE INDEX idx_produits_entreprise ON referentiel.produits (entreprise_id);

CREATE INDEX idx_produits_famille ON referentiel.produits (famille_id);

CREATE INDEX idx_produits_type_article ON referentiel.produits (type_article);

CREATE INDEX idx_produits_type_stock ON referentiel.produits (type_stock);

CREATE INDEX idx_produits_actif ON referentiel.produits (actif);

CREATE INDEX idx_produits_reference ON referentiel.produits (reference);

-- Table des clients
CREATE TABLE referentiel.clients (
  id BIGSERIAL PRIMARY KEY,
  code VARCHAR(50) NOT NULL,
  raison_sociale VARCHAR(255) NOT NULL,
  matricule_fiscal VARCHAR(50),
  adresse TEXT,
  ville VARCHAR(100),
  code_postal VARCHAR(10),
  telephone VARCHAR(20),
  email VARCHAR(100),
  type_client VARCHAR(20) NOT NULL CHECK (type_client IN ('PARTICULIER', 'ENTREPRISE')),
  entreprise_id BIGINT NOT NULL,
  actif BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_client_code_entreprise UNIQUE (code, entreprise_id)
);

CREATE INDEX idx_clients_entreprise ON referentiel.clients (entreprise_id);

CREATE INDEX idx_clients_type ON referentiel.clients (type_client);

CREATE INDEX idx_clients_matricule ON referentiel.clients (matricule_fiscal);

CREATE INDEX idx_clients_actif ON referentiel.clients (actif);

CREATE INDEX idx_clients_raison_sociale ON referentiel.clients (raison_sociale);

-- Table des fournisseurs
CREATE TABLE referentiel.fournisseurs (
  id BIGSERIAL PRIMARY KEY,
  code VARCHAR(50) NOT NULL,
  raison_sociale VARCHAR(255) NOT NULL,
  matricule_fiscal VARCHAR(50),
  adresse TEXT,
  ville VARCHAR(100),
  code_postal VARCHAR(10),
  telephone VARCHAR(20),
  email VARCHAR(100),
  entreprise_id BIGINT NOT NULL,
  actif BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_fournisseur_code_entreprise UNIQUE (code, entreprise_id)
);

CREATE INDEX idx_fournisseurs_entreprise ON referentiel.fournisseurs (entreprise_id);

CREATE INDEX idx_fournisseurs_matricule ON referentiel.fournisseurs (matricule_fiscal);

CREATE INDEX idx_fournisseurs_actif ON referentiel.fournisseurs (actif);

CREATE INDEX idx_fournisseurs_raison_sociale ON referentiel.fournisseurs (raison_sociale);

-- Fonction trigger pour updated_at
CREATE OR REPLACE FUNCTION referentiel.update_updated_at_column () RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = CURRENT_TIMESTAMP;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Triggers pour updated_at
CREATE TRIGGER update_familles_produits_updated_at BEFORE
UPDATE ON referentiel.familles_produits FOR EACH ROW
EXECUTE FUNCTION referentiel.update_updated_at_column ();

CREATE TRIGGER update_produits_updated_at BEFORE
UPDATE ON referentiel.produits FOR EACH ROW
EXECUTE FUNCTION referentiel.update_updated_at_column ();

CREATE TRIGGER update_clients_updated_at BEFORE
UPDATE ON referentiel.clients FOR EACH ROW
EXECUTE FUNCTION referentiel.update_updated_at_column ();

CREATE TRIGGER update_fournisseurs_updated_at BEFORE
UPDATE ON referentiel.fournisseurs FOR EACH ROW
EXECUTE FUNCTION referentiel.update_updated_at_column ();

-- Commentaires sur les tables
COMMENT ON TABLE referentiel.familles_produits IS 'Familles de produits et services';

COMMENT ON TABLE referentiel.produits IS 'Produits et services vendus ou achetés';

COMMENT ON TABLE referentiel.clients IS 'Base clients';

COMMENT ON TABLE referentiel.fournisseurs IS 'Base fournisseurs';

-- Commentaires sur les colonnes importantes
COMMENT ON COLUMN referentiel.produits.type_stock IS 'STOCKABLE pour les produits physiques, NON_STOCKABLE pour les services';

COMMENT ON COLUMN referentiel.produits.type_article IS 'PRODUIT pour marchandises, SERVICE pour prestations';

COMMENT ON COLUMN referentiel.clients.type_client IS 'PARTICULIER ou ENTREPRISE';

COMMENT ON COLUMN referentiel.clients.matricule_fiscal IS 'Matricule fiscal tunisien';

COMMENT ON COLUMN referentiel.fournisseurs.matricule_fiscal IS 'Matricule fiscal tunisien';
