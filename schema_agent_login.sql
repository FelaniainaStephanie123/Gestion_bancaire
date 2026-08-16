-- ============================================================
-- Ajout : table AGENT pour l'interface de login
-- (fichier séparé exprès, pour ne pas toucher au schéma
--  partagé avec le reste de l'équipe)
-- ============================================================

CREATE TABLE IF NOT EXISTS agent (
    id_agent        SERIAL         PRIMARY KEY,
    nom_utilisateur VARCHAR(50)    NOT NULL UNIQUE,
    mot_de_passe    VARCHAR(255)   NOT NULL,   -- on stocke un HASH, jamais le mot de passe en clair
    nom_complet     VARCHAR(100),
    date_creation   TIMESTAMP      DEFAULT CURRENT_TIMESTAMP
);

-- Compte de test : identifiant "admin", mot de passe "admin123"
-- (le hash ci-dessous est le SHA-256 de "admin123")
INSERT INTO agent (nom_utilisateur, mot_de_passe, nom_complet)
VALUES ('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'Administrateur')
ON CONFLICT (nom_utilisateur) DO NOTHING;
