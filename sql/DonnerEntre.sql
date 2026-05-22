-- ============================================
-- DONNÉES INITIALES - PostgreSQL
-- ============================================
\c forage;

-- ============================================
-- STATUS
-- ============================================
INSERT INTO status (libelle) VALUES 
    ('Dossier créé'),
    ('Devis Étude créé'),
    ('Devis Forage créé'),
    ('Devis Etude accepte'),
    ('Devis Forage accepte'),
    ('Forage commence'),
    ('Forage terminé');

-- ============================================
-- TYPES DE DEVIS
-- ============================================
INSERT INTO types_devis (libelle) VALUES 
    ('Forage'),
    ('Etude');

-- ============================================
-- CLIENTS
-- ============================================
INSERT INTO clients (nom, contact) VALUES 
    ('RAKOTO', '034 12 345 67');

-- ============================================
-- INDICATEURS
-- ============================================
-- Cree (1) → Devis_Etude_Cree (2) : élevé si 24h-48h
-- Devis_Etude_Cree (2) → Devis_Forage_Cree (3) : critique si 40h-80h

INSERT INTO indicateur (status_id_1, status_id_2, heure_1, heure_2, level) VALUES
    (1, 2, 8, 10, 'eleve'),
    (1, 2, 24, 48, 'critique'),
    (2, 3, 4, 5, 'eleve'),
    (2, 3, 6, 8, 'critique'),
    (3, 4, 1, 2, 'eleve'),
    (3, 4, 3, 4, 'critique'),
    (4, 5, 4, 8, 'eleve'),
    (4, 5, 10, 12, 'critique'),
    (5, 6, 20, 30, 'eleve'),
    (5, 6, 30, 60, 'critique'),
    (6, 7, 60, 70, 'eleve'),
    (6, 7, 80, 100, 'critique');

-- -- ============================================
-- -- DEMANDES DE TEST
-- -- ============================================
-- INSERT INTO demandes (date_demande, lieu, district, client_id) VALUES 
--     ('2024-01-10', 'Fokontany Ambohitraivo', 'Antananarivo Atsimondrano', 1),
--     ('2024-01-15', 'Village Mahazoarivo', 'Antsirabe II', 4),
--     ('2024-02-01', 'Quartier Isotry', 'Antananarivo Renivohitra', 3),
--     ('2024-02-10', 'Commune Ambatolampy', 'Ambatolampy', 2),
--     ('2024-02-15', 'Zone Rurale Betafo', 'Betafo', 10);

-- -- ============================================
-- -- DEMANDE STATUS DE TEST
-- -- ============================================

-- -- Demande 1 : Cree → Devis_Etude_Cree → Devis_Forage_Cree (complète)
-- INSERT INTO demande_status (date_status, demande_id, status_id, commentaire, duree_estimer, duree_reel) VALUES 
--     ('2024-01-10 08:00:00', 1, 1, 'Demande créée', 0, 0),
--     ('2024-01-12 10:00:00', 1, 2, 'Devis étude créé', 50, 18, ),
--     ('2024-01-18 14:00:00', 1, 3, 'Devis forage créé', 148, 44);

-- -- Demande 2 : Cree → Devis_Etude_Cree (en cours - élevé)
-- INSERT INTO demande_status (date_status, demande_id, status_id, commentaire, duree_estimer, duree_reel) VALUES 
--     ('2024-01-15 09:00:00', 2, 1, 'Demande créée', 0, 0),
--     ('2024-01-17 11:00:00', 2, 2, 'Devis étude en cours', 50, 18);

-- -- Demande 3 : Cree seulement (peut être critique si trop longtemps)
-- INSERT INTO demande_status (date_status, demande_id, status_id, commentaire, duree_estimer, duree_reel) VALUES 
--     ('2024-02-01 10:00:00', 3, 1, 'Demande urgente créée', 0, 0);

-- -- Demande 4 : Cree seulement
-- INSERT INTO demande_status (date_status, demande_id, status_id, commentaire, duree_estimer, duree_reel) VALUES 
--     ('2024-02-10 08:30:00', 4, 1, 'Demande ONG créée', 0, 0);

-- -- Demande 5 : Cree seulement  
-- INSERT INTO demande_status (date_status, demande_id, status_id, commentaire, duree_estimer, duree_reel) VALUES 
--     ('2024-02-15 09:00:00', 5, 1, 'Demande créée', 0, 0);

-- ============================================
-- FIN DU SCRIPT DATA
-- ============================================