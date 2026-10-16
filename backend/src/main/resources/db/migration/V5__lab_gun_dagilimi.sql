CREATE TABLE lab_gun_dagilimi (
    id BIGSERIAL PRIMARY KEY,
    derslik_id BIGINT NOT NULL REFERENCES derslik (id) ON DELETE CASCADE,
    bolum_id BIGINT NOT NULL REFERENCES bolum (id) ON DELETE CASCADE,
    gun INT NOT NULL CHECK (gun BETWEEN 1 AND 7),
    CONSTRAINT uq_lab_gun UNIQUE (derslik_id, gun)
);

CREATE INDEX idx_lab_gun_derslik ON lab_gun_dagilimi (derslik_id);
CREATE INDEX idx_lab_gun_bolum ON lab_gun_dagilimi (bolum_id);