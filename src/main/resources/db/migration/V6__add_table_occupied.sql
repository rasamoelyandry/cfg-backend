-- Occupation de table decouplee du statut de la commande (le paiement ne libere plus la table automatiquement)
ALTER TABLE restaurant_tables
    ADD COLUMN IF NOT EXISTS occupied BOOLEAN NOT NULL DEFAULT FALSE;
