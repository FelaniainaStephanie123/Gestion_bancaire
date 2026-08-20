-- A executer une seule fois sur la base existante.
-- Les clients sont desactives au lieu d'etre supprimes physiquement,
-- afin de conserver les prets, remboursements et virements historiques.

ALTER TABLE client
    ADD COLUMN IF NOT EXISTS actif BOOLEAN NOT NULL DEFAULT TRUE;

UPDATE client
SET actif = TRUE
WHERE actif IS NULL;
