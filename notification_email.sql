CREATE TABLE IF NOT EXISTS notification_email (
    id           BIGSERIAL PRIMARY KEY,
    destinataire VARCHAR(255) NOT NULL,
    sujet        VARCHAR(255) NOT NULL,
    contenu      TEXT NOT NULL,
    date_envoi   TIMESTAMP NOT NULL,
    envoyee      BOOLEAN NOT NULL DEFAULT FALSE,
    envoyee_le   TIMESTAMP NULL
);

CREATE INDEX IF NOT EXISTS idx_notification_email_a_traiter
    ON notification_email (envoyee, date_envoi);
