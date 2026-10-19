CREATE TABLE kullanici (
    id BIGSERIAL PRIMARY KEY,
    kurum_id BIGINT NOT NULL REFERENCES kurum (id),
    fakulte_id BIGINT REFERENCES fakulte (id),
    bolum_id BIGINT REFERENCES bolum (id),
    ad_soyad VARCHAR(200) NOT NULL,
    eposta VARCHAR(200) NOT NULL,
    sifre_hash VARCHAR(100) NOT NULL,
    rol VARCHAR(20) NOT NULL CHECK (rol IN ('SUPERADMIN', 'FAKULTE', 'ASISTAN')),
    CONSTRAINT uq_kullanici_eposta UNIQUE (eposta),
    CONSTRAINT ck_kullanici_kapsam CHECK (
        (rol = 'ASISTAN' AND bolum_id IS NOT NULL)
        OR (rol = 'FAKULTE' AND fakulte_id IS NOT NULL)
        OR rol = 'SUPERADMIN')
);

CREATE INDEX idx_kullanici_kurum ON kullanici (kurum_id);
CREATE INDEX idx_kullanici_bolum ON kullanici (bolum_id);
CREATE INDEX idx_kullanici_fakulte ON kullanici (fakulte_id);