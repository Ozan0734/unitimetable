DO $$
DECLARE
    kisit text;
BEGIN
    FOR kisit IN
        SELECT conname FROM pg_constraint
        WHERE conrelid = 'derslik'::regclass
          AND contype = 'c'
          AND pg_get_constraintdef(oid) LIKE '%tur%'
    LOOP
        EXECUTE format('ALTER TABLE derslik DROP CONSTRAINT %I', kisit);
    END LOOP;
END $$;

ALTER TABLE derslik
    ADD CONSTRAINT ck_derslik_tur CHECK (tur IN ('SINIF', 'LABORATUVAR', 'AMFI', 'SANAL', 'SALON'));