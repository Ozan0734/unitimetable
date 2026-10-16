CREATE TABLE kurum (
    id BIGSERIAL PRIMARY KEY,
    ad VARCHAR(200) NOT NULL
);

CREATE TABLE zaman_izgarasi (
    id BIGSERIAL PRIMARY KEY,
    kurum_id BIGINT NOT NULL UNIQUE REFERENCES kurum (id),
    gun_sayisi INT NOT NULL DEFAULT 5,
    gunluk_slot_sayisi INT NOT NULL DEFAULT 9,
    ders_suresi_dk INT NOT NULL DEFAULT 45,
    ara_suresi_dk INT NOT NULL DEFAULT 15,
    baslangic_saati TIME NOT NULL DEFAULT '08:30'
);

CREATE TABLE fakulte (
    id BIGSERIAL PRIMARY KEY,
    kurum_id BIGINT NOT NULL REFERENCES kurum (id),
    ad VARCHAR(200) NOT NULL
);

CREATE TABLE bolum (
    id BIGSERIAL PRIMARY KEY,
    fakulte_id BIGINT NOT NULL REFERENCES fakulte (id),
    ad VARCHAR(200) NOT NULL
);

CREATE TABLE akademik_program (
    id BIGSERIAL PRIMARY KEY,
    bolum_id BIGINT NOT NULL REFERENCES bolum (id),
    ad VARCHAR(200) NOT NULL,
    seviye VARCHAR(20) NOT NULL CHECK (seviye IN ('LISANS', 'YUKSEK_LISANS', 'DOKTORA')),
    dil VARCHAR(2) NOT NULL CHECK (dil IN ('TR', 'EN'))
);

CREATE TABLE sinif (
    id BIGSERIAL PRIMARY KEY,
    program_id BIGINT NOT NULL REFERENCES akademik_program (id),
    ad VARCHAR(100) NOT NULL,
    duzey INT NOT NULL
);

CREATE INDEX idx_fakulte_kurum ON fakulte (kurum_id);
CREATE INDEX idx_bolum_fakulte ON bolum (fakulte_id);
CREATE INDEX idx_program_bolum ON akademik_program (bolum_id);
CREATE INDEX idx_sinif_program ON sinif (program_id);