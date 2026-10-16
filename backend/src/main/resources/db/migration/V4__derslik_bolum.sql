CREATE TABLE derslik_bolum (
    id BIGSERIAL PRIMARY KEY,
    derslik_id BIGINT NOT NULL REFERENCES derslik (id) ON DELETE CASCADE,
    bolum_id BIGINT NOT NULL REFERENCES bolum (id) ON DELETE CASCADE,
    ortak_mi BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_derslik_bolum UNIQUE (derslik_id, bolum_id)
);

CREATE INDEX idx_derslik_bolum_derslik ON derslik_bolum (derslik_id);
CREATE INDEX idx_derslik_bolum_bolum ON derslik_bolum (bolum_id);