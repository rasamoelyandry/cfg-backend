-- La contrainte CHECK d'origine ne listait pas CARD, ajoute a l'enum Java mais jamais repercute en base
ALTER TABLE payments DROP CONSTRAINT payments_method_check;
ALTER TABLE payments ADD CONSTRAINT payments_method_check CHECK (
    method IN ('CASH','CARD','ORANGE_MONEY','MVOLA','AIRTEL_MONEY')
);
