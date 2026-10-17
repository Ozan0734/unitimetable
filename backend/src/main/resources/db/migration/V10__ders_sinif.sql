CREATE TABLE ders_sinif (
    id BIGSERIAL PRIMARY KEY,
    ders_id BIGINT NOT NULL REFERENCES ders (id) ON DELETE CASCADE,
    sinif_id BIGINT NOT NULL REFERENCES sinif (id) ON DELETE CASCADE,
    CONSTRAINT uq_ders_sinif UNIQUE (ders_id, sinif_id)
);

CREATE INDEX idx_ders_sinif_ders ON ders_sinif (ders_id);
CREATE INDEX idx_ders_sinif_sinif ON ders_sinif (sinif_id);