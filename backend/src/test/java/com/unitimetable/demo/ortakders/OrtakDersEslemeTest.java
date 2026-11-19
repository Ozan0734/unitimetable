package com.unitimetable.demo.ortakders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class OrtakDersEslemeTest {

    @Test
    void ayniKodluDersAdlariFarkliOlsaBileEslesir() {
        assertTrue(OrtakDersEsleme.ayniDers("TDB 101", "TÜRK DİLİ I", "tdb  101", "TURKISH LANGUAGE I"));
    }

    @Test
    void kodlariFarkliOlanMatematikAdindanEslesir() {
        assertTrue(OrtakDersEsleme.ayniDers("BSM 103", "Matematik I", "ENM 107", "  MATEMATİK   I "));
        assertTrue(OrtakDersEsleme.ayniDers("COM 101", "mathematics i", "MAT 101", "MATHEMATICS I"));
    }

    @Test
    void kodVeAdFarkliysaEslesmez() {
        assertTrue(!OrtakDersEsleme.ayniDers("COM 101", "FİZİK I", "ENM 107", "MATEMATİK I"));
        assertTrue(!OrtakDersEsleme.ayniDers("", "", "", ""));
        assertTrue(!OrtakDersEsleme.ayniDers(null, null, null, null));
    }

    @Test
    void hocaAnahtariBosOlamazVeEsitOlmali() {
        assertTrue(OrtakDersEsleme.ayniHoca("ALİ YILMAZ", "ALİ YILMAZ"));
        assertTrue(!OrtakDersEsleme.ayniHoca("ALİ YILMAZ", "VELİ KAYA"));
        assertTrue(!OrtakDersEsleme.ayniHoca(null, null));
        assertTrue(!OrtakDersEsleme.ayniHoca(null, "ALİ YILMAZ"));
    }

    @Test
    void blokYapilariBirebirAyniOlmali() {
        assertTrue(OrtakDersEsleme.blokYapilariAyni(List.of(2, 2), List.of(2, 2)));
        assertTrue(!OrtakDersEsleme.blokYapilariAyni(List.of(4), List.of(2, 2)));
        assertTrue(!OrtakDersEsleme.blokYapilariAyni(List.of(3), List.of(4)));
        assertTrue(!OrtakDersEsleme.blokYapilariAyni(List.of(), List.of()));
        assertEquals("MATEMATIK I", OrtakDersEsleme.adAnahtari("  matematik  i "));
        assertEquals(OrtakDersEsleme.adAnahtari("MATEMATİK I"), OrtakDersEsleme.adAnahtari("MATEMATIK I"));
    }
}
