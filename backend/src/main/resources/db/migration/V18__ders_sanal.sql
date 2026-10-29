ALTER TABLE ders ADD COLUMN sanal BOOLEAN NOT NULL DEFAULT FALSE;

DO $$
DECLARE
    kisit text;
BEGIN
    FOR kisit IN
        SELECT conname FROM pg_constraint
        WHERE conrelid = 'ders'::regclass
          AND contype = 'c'
          AND pg_get_constraintdef(oid) LIKE '%tur%'
    LOOP
        EXECUTE format('ALTER TABLE ders DROP CONSTRAINT %I', kisit);
    END LOOP;
END $$;

ALTER TABLE ders
    ADD CONSTRAINT ck_ders_tur CHECK (tur IN ('NORMAL', 'YERLESIMSIZ', 'SEMINER'));

ALTER TABLE ders
    ADD CONSTRAINT ck_ders_saat CHECK (
        tur = 'YERLESIMSIZ' OR (haftalik_saat IS NOT NULL AND saat_ifadesi IS NOT NULL));