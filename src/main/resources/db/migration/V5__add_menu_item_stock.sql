-- Suivi de stock optionnel par article (Waiterio-like: certains articles restent en dispo manuelle illimitee)
ALTER TABLE menu_items
    ADD COLUMN IF NOT EXISTS track_stock BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS stock_quantity INT NOT NULL DEFAULT 0;
