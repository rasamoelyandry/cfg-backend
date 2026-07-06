-- Masquage d'une commande du tableau admin sans la supprimer (conservée pour la comptabilité)
ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS hidden BOOLEAN NOT NULL DEFAULT FALSE;
