-- Création du schema
CREATE SCHEMA IF NOT EXISTS authz;


-- Table des sociétés
CREATE TABLE authz.societes (
                                        id BIGSERIAL PRIMARY KEY,
                                        raison_sociale VARCHAR(255) NOT NULL,
                                        matricule_fiscale VARCHAR(13) UNIQUE NOT NULL,
                                        code_tva VARCHAR(20),
                                        code_douane VARCHAR(20),
                                        registre_commerce VARCHAR(50),
                                        forme_juridique VARCHAR(100),
                                        capital_social DECIMAL(15,2),
                                        date_creation DATE,
                                        adresse VARCHAR(255),
                                        ville VARCHAR(100),
                                        code_postal VARCHAR(10),
                                        telephone VARCHAR(20),
                                        fax VARCHAR(20),
                                        email VARCHAR(255),
                                        site_web VARCHAR(255),
                                        activite VARCHAR(255),
                                        secteur VARCHAR(100),
                                        is_active BOOLEAN DEFAULT true,
                                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                        created_by BIGINT,  -- Référence auth.users.id (logique, pas FK cross-schema)
                                        updated_by BIGINT
);

-- Relation comptable-sociétés
CREATE TABLE authz.comptable_societes (
                                                  id BIGSERIAL PRIMARY KEY,
                                                  user_id BIGINT NOT NULL,  -- Référence auth.users.id
                                                  societe_id BIGINT NOT NULL REFERENCES authz.societes(id) ON DELETE CASCADE,
                                                  date_debut DATE NOT NULL,
                                                  date_fin DATE,
                                                  is_active BOOLEAN DEFAULT true,
                                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                  UNIQUE(user_id, societe_id)
);

-- Relation user société-sociétés
CREATE TABLE authz.user_societes (
                                             id BIGSERIAL PRIMARY KEY,
                                             user_id BIGINT NOT NULL,  -- Référence auth.users.id
                                             societe_id BIGINT NOT NULL REFERENCES authz.societes(id) ON DELETE CASCADE,
                                             is_owner BOOLEAN DEFAULT false,
                                             date_debut DATE NOT NULL,
                                             date_fin DATE,
                                             is_active BOOLEAN DEFAULT true,
                                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                             UNIQUE(user_id, societe_id)
);

-- Relation employee-societe
CREATE TABLE authz.employees (
                                         id BIGSERIAL PRIMARY KEY,
                                         user_id BIGINT UNIQUE NOT NULL,  -- Référence auth.users.id
                                         societe_id BIGINT NOT NULL REFERENCES authz.societes(id) ON DELETE CASCADE,
                                         matricule_employee VARCHAR(50),
                                         poste VARCHAR(100),
                                         departement VARCHAR(100),
                                         date_embauche DATE,
                                         date_fin_contrat DATE,
                                         type_contrat VARCHAR(50),
                                         is_active BOOLEAN DEFAULT true,
                                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index
CREATE INDEX idx_societes_matricule ON authz.societes(matricule_fiscale);
CREATE INDEX idx_societes_is_active ON authz.societes(is_active);
CREATE INDEX idx_comptable_societes_user_id ON authz.comptable_societes(user_id);
CREATE INDEX idx_comptable_societes_societe_id ON authz.comptable_societes(societe_id);
CREATE INDEX idx_user_societes_user_id ON authz.user_societes(user_id);
CREATE INDEX idx_user_societes_societe_id ON authz.user_societes(societe_id);
CREATE INDEX idx_employees_user_id ON authz.employees(user_id);
CREATE INDEX idx_employees_societe_id ON authz.employees(societe_id);
