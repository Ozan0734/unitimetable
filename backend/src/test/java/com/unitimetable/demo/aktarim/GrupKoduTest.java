package com.unitimetable.demo.aktarim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Set;

import org.junit.jupiter.api.Test;

class GrupKoduTest {

    private static ObsSatiri satir(String grup) {
        return new ObsSatiri(2, "SEC 101.1", "ECONOMICS", false, "3", "5", 3, 0, 0, 1, "G", "", "", "", grup);
    }

    private static final Set<String> GRUPLAR = Set.of("SEC 101", "SEC 301", "SEC 432");

    @Test
    void anaGrupKoduBulunur() {
        assertEquals("SEC 101", ObsKayitService.grupKodu(satir("SEC 101"), GRUPLAR));
        assertEquals("SEC 101", ObsKayitService.grupKodu(satir("sec  101"), GRUPLAR));
    }

    @Test
    void virgullerdenDosyadaOlanGrupSecilir() {
        assertEquals("SEC 432", ObsKayitService.grupKodu(satir("SEC 233,SEC 432"), GRUPLAR));
    }

    @Test
    void ilgisizEtiketVeBosDegerNullDoner() {
        assertNull(ObsKayitService.grupKodu(satir("ITB 1/Spring (SGS)"), GRUPLAR));
        assertNull(ObsKayitService.grupKodu(satir(""), GRUPLAR));
    }

    @Test
    void dosyadaOlmayanGrupNullDoner() {
        assertNull(ObsKayitService.grupKodu(satir("SEC 999"), GRUPLAR));
    }
}
