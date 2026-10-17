CREATE TABLE program_projesi (
    id BIGSERIAL PRIMARY KEY,
    kurum_id BIGINT NOT NULL REFERENCES kurum (id),
    bolum_id BIGINT NOT NULL REFERENCES bolum (id),
    ad VARCHAR(200) NOT NULL,
    donem VARCHAR(50) NOT NULL,
    durum VARCHAR(20) NOT NULL DEFAULT 'TASLAK' CHECK (durum IN ('TASLAK', 'YAYINDA')),
    olusturma_tarihi TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_proje_bolum_ad UNIQUE (bolum_id, ad)
);

CREATE INDEX idx_proje_bolum ON program_projesi (bolum_id);
CREATE INDEX idx_proje_kurum ON program_projesi (kurum_id);