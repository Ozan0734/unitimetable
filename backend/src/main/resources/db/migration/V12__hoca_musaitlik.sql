CREATE TABLE hoca_musaitlik (
    id BIGSERIAL PRIMARY KEY,
    ogretim_elemani_id BIGINT NOT NULL REFERENCES ogretim_elemani (id) ON DELETE CASCADE,
    gun INT NOT NULL CHECK (gun BETWEEN 1 AND 7),
    slot INT NOT NULL CHECK (slot BETWEEN 1 AND 24),
    durum VARCHAR(20) NOT NULL CHECK (durum IN ('MUSAIT_DEGIL', 'TERCIH')),
    CONSTRAINT uq_hoca_musaitlik UNIQUE (ogretim_elemani_id, gun, slot)
);

CREATE INDEX idx_hoca_musaitlik_hoca ON hoca_musaitlik (ogretim_elemani_id);