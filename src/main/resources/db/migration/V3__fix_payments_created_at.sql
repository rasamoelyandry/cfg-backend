-- Colonne manquante dans la table payments (ajoutée dans l'entité Payment via @CreatedDate)
ALTER TABLE payments
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ NOT NULL DEFAULT NOW();
