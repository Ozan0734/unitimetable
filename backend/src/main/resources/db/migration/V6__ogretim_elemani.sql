CREATE TABLE ogretim_elemani (
    id BIGSERIAL PRIMARY KEY,
    bolum_id BIGINT NOT NULL REFERENCES bolum (id),
    ad_soyad VARCHAR(200) NOT NULL,
    unvan VARCHAR(50),
    renk VARCHAR(7),
    oncelik INT NOT NULL DEFAULT 0
);

CREATE INDEX idx_ogretim_elemani_bolum ON ogretim_elemani (bolum_id);