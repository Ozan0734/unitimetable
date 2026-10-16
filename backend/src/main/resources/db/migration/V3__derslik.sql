CREATE TABLE derslik (
    id BIGSERIAL PRIMARY KEY,
    kurum_id BIGINT NOT NULL REFERENCES kurum (id),
    ad VARCHAR(100) NOT NULL,
    kapasite INT NOT NULL CHECK (kapasite > 0),
    tur VARCHAR(20) NOT NULL CHECK (tur IN ('SINIF', 'LABORATUVAR', 'AMFI')),
    CONSTRAINT uq_derslik_kurum_ad UNIQUE (kurum_id, ad)
);

CREATE INDEX idx_derslik_kurum ON derslik (kurum_id);