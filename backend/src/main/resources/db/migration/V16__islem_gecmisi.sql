CREATE TABLE islem_gecmisi (
    id BIGSERIAL PRIMARY KEY,
    proje_id BIGINT NOT NULL REFERENCES program_projesi (id) ON DELETE CASCADE,
    blok_id BIGINT NOT NULL REFERENCES ders_blogu (id) ON DELETE CASCADE,
    kullanici_id BIGINT REFERENCES kullanici (id) ON DELETE SET NULL,
    tur VARCHAR(20) NOT NULL CHECK (tur IN ('YERLESTIR', 'TASI', 'KALDIR', 'KILIT')),
    onceki VARCHAR(200),
    sonraki VARCHAR(200),
    geri_alindi BOOLEAN NOT NULL DEFAULT FALSE,
    tarih TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_islem_gecmisi_proje ON islem_gecmisi (proje_id, id);