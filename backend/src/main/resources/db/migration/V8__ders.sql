CREATE TABLE ders (
    id BIGSERIAL PRIMARY KEY,
    proje_id BIGINT NOT NULL REFERENCES program_projesi (id) ON DELETE CASCADE,
    bolum_id BIGINT NOT NULL REFERENCES bolum (id),
    kod VARCHAR(30) NOT NULL,
    ad VARCHAR(200) NOT NULL,
    tur VARCHAR(20) NOT NULL DEFAULT 'NORMAL' CHECK (tur IN ('NORMAL', 'YERLESIMSIZ')),
    haftalik_saat INT CHECK (haftalik_saat BETWEEN 1 AND 40),
    saat_ifadesi VARCHAR(100),
    CONSTRAINT uq_ders_proje_kod UNIQUE (proje_id, kod),
    CONSTRAINT ck_ders_saat CHECK (
        tur = 'YERLESIMSIZ' OR (haftalik_saat IS NOT NULL AND saat_ifadesi IS NOT NULL))
);

CREATE INDEX idx_ders_proje ON ders (proje_id);
CREATE INDEX idx_ders_bolum ON ders (bolum_id);