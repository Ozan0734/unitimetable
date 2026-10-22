CREATE TABLE hoca_gun_tercihi (
    id BIGSERIAL PRIMARY KEY,
    ogretim_elemani_id BIGINT NOT NULL REFERENCES ogretim_elemani (id) ON DELETE CASCADE,
    tur VARCHAR(30) NOT NULL CHECK (tur IN (
        'BOS_GUN_SAYISI', 'ARDISIK_GUN', 'TERCIH_EDILEN_GUN', 'KACINILAN_GUN')),
    deger INT CHECK (deger BETWEEN 1 AND 6),
    gun INT CHECK (gun BETWEEN 1 AND 7),
    katilik VARCHAR(10) NOT NULL DEFAULT 'TERCIH' CHECK (katilik IN ('ZORUNLU', 'TERCIH')),
    asistana_birakildi BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_hoca_gun_tercihi_hoca ON hoca_gun_tercihi (ogretim_elemani_id);