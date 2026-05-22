CREATE DATABASE forage;

-- Se connecter à la base (dans psql: \c forage)
\c forage;

CREATE TABLE clients (
    id SERIAL PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    contact VARCHAR(255) NOT NULL
);


-- ============================================
-- TABLE: status
-- ============================================
CREATE TABLE status (
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(100) NOT NULL UNIQUE
);

-- ============================================
-- TABLE: types_devis
-- ============================================
CREATE TABLE types_devis (
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(100) NOT NULL UNIQUE
);

-- ============================================
-- TABLE: demandes
-- ============================================
CREATE TABLE demandes (
    id SERIAL PRIMARY KEY,
    date_demande DATE NOT NULL,
    lieu VARCHAR(255) NOT NULL,
    district VARCHAR(255) NOT NULL,
    client_id INT NOT NULL,

    CONSTRAINT fk_demandes_client 
        FOREIGN KEY (client_id) 
        REFERENCES clients(id) 
        ON DELETE RESTRICT
);

-- ============================================
-- TABLE: devis
-- ============================================
CREATE TABLE devis (
    id SERIAL PRIMARY KEY,
    date_devis DATE NOT NULL,
    montant_total DECIMAL(15, 2) DEFAULT 0.00,
    type_devis_id INT NOT NULL,
    demande_id INT,

    CONSTRAINT fk_devis_type 
        FOREIGN KEY (type_devis_id) 
        REFERENCES types_devis(id) 
        ON DELETE RESTRICT,
    
    CONSTRAINT fk_devis_demande 
        FOREIGN KEY (demande_id) 
        REFERENCES demandes(id) 
        ON DELETE CASCADE
);

SELECT SUM(montant_total) FROM Devis;

-- ============================================
-- TABLE: details_devis
-- ============================================
CREATE TABLE details_devis (
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(255) NOT NULL,
    prix_unitaire DECIMAL(15, 2) NOT NULL,
    quantite INT NOT NULL,
    total DECIMAL(15, 2) GENERATED ALWAYS AS (prix_unitaire * quantite) STORED,
    devis_id INT NOT NULL,

    CONSTRAINT fk_details_devis 
        FOREIGN KEY (devis_id) 
        REFERENCES devis(id) 
        ON DELETE CASCADE
);

-- ============================================v
-- TABLE: demande_status
-- ============================================
CREATE TABLE demande_status (
    id SERIAL PRIMARY KEY,
    date_status TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    commentaire TEXT,
    demande_id INT NOT NULL,
    status_id INT NOT NULL,
    duree_estimer INT NOT NULL DEFAULT 0,
    duree_reel INT NOT NULL DEFAULT 0,

    CONSTRAINT fk_demande_status_demande 
        FOREIGN KEY (demande_id) 
        REFERENCES demandes(id) 
        ON DELETE CASCADE,
    
    CONSTRAINT fk_demande_status_status 
        FOREIGN KEY (status_id) 
        REFERENCES status(id) 
        ON DELETE RESTRICT
);

-- ============================================
-- TABLE: indicateur
-- ============================================

CREATE TABLE indicateur(
    id SERIAL PRIMARY KEY,
    status_id_1 INT NOT NULL,
    status_id_2 INT NOT NULL,
    heure_1 INT NOT NULL,
    heure_2 INT NOT NULL,
    level TEXT NOT NULL,
    
    CONSTRAINT fk_indicateur_status_1 
        FOREIGN KEY (status_id_1) 
        REFERENCES status(id) 
        ON DELETE RESTRICT,
    
    CONSTRAINT fk_indicateur_status_2 
        FOREIGN KEY (status_id_2) 
        REFERENCES status(id) 
        ON DELETE RESTRICT
);