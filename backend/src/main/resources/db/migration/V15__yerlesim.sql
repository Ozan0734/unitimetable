CREATE TABLE yerlesim (
    id BIGSERIAL PRIMARY KEY,
    blok_id BIGINT NOT NULL UNIQUE REFERENCES ders_blogu (id) ON DELETE CASCADE,
    gun INT NOT NULL CHECK (gun BETWEEN 1 AND 7),
    baslangic_slot INT NOT NULL CHECK (baslangic_slot BETWEEN 1 AND 24),
    kilitli BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE yerlesim_derslik (
    id BIGSERIAL PRIMARY KEY,
    yerlesim_id BIGINT NOT NULL REFERENCES yerlesim (id) ON DELETE CASCADE,
    derslik_id BIGINT NOT NULL REFERENCES derslik (id),
    CONSTRAINT uq_yerlesim_derslik UNIQUE (yerlesim_id, derslik_id)
);

CREATE INDEX idx_yerlesim_derslik_derslik ON yerlesim_derslik (derslik_id);