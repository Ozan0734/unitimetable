CREATE TABLE ders_blogu (
    id BIGSERIAL PRIMARY KEY,
    sube_id BIGINT NOT NULL REFERENCES sube (id) ON DELETE CASCADE,
    sira_no INT NOT NULL CHECK (sira_no >= 1),
    sure INT NOT NULL CHECK (sure BETWEEN 1 AND 20),
    CONSTRAINT uq_blok_sube_sira UNIQUE (sube_id, sira_no)
);

CREATE INDEX idx_blok_sube ON ders_blogu (sube_id);