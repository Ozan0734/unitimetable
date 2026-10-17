CREATE TABLE sube (
    id BIGSERIAL PRIMARY KEY,
    ders_id BIGINT NOT NULL REFERENCES ders (id) ON DELETE CASCADE,
    ogretim_elemani_id BIGINT REFERENCES ogretim_elemani (id) ON DELETE SET NULL,
    sube_no INT NOT NULL CHECK (sube_no >= 1),
    tahmini_ogrenci INT CHECK (tahmini_ogrenci BETWEEN 1 AND 2000),
    sinif_cakisma_izni BOOLEAN NOT NULL DEFAULT FALSE,
    derslik_cakisma_izni BOOLEAN NOT NULL DEFAULT FALSE,
    coklu_derslik_izni BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_sube_ders_no UNIQUE (ders_id, sube_no)
);

CREATE INDEX idx_sube_ders ON sube (ders_id);
CREATE INDEX idx_sube_ogretim_elemani ON sube (ogretim_elemani_id);