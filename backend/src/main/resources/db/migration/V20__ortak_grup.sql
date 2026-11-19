-- Ortak şube grupları: farklı bölümlerin aynı dersi aynı hoca ile birlikte yürüten şubeleri.
-- Onaylı üyeler aynı gün, saat ve derslikte yerleştirilir, öğrenci sayıları derslik kapasitesine birlikte sayılır.
CREATE TABLE ortak_grup (
    id BIGSERIAL PRIMARY KEY,
    kurum_id BIGINT NOT NULL REFERENCES kurum (id) ON DELETE CASCADE,
    donem VARCHAR(100) NOT NULL,
    olusturan_bolum_id BIGINT NOT NULL REFERENCES bolum (id) ON DELETE CASCADE,
    olusturma_tarihi TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE ortak_grup_uye (
    id BIGSERIAL PRIMARY KEY,
    grup_id BIGINT NOT NULL REFERENCES ortak_grup (id) ON DELETE CASCADE,
    sube_id BIGINT NOT NULL UNIQUE REFERENCES sube (id) ON DELETE CASCADE,
    onayli BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_ortak_grup_kurum ON ortak_grup (kurum_id);
CREATE INDEX idx_ortak_grup_uye_grup ON ortak_grup_uye (grup_id);
