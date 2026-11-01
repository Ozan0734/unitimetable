package com.unitimetable.demo.aktarim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class MetinTemizleyiciTest {

    private static void hoca(String ham, String unvan, String ad) {
        MetinTemizleyici.Hoca hoca = MetinTemizleyici.hocaAyir(ham);
        assertEquals(unvan, hoca.unvan(), "unvan: " + ham);
        assertEquals(ad, hoca.ad(), "ad: " + ham);
    }

    @Test
    void baslikEkiVeHarfBicimiTemizlenir() {
        assertEquals("ders kodu", MetinTemizleyici.baslik("Ders Kodu_218"));
        assertEquals("öğretim elemanı", MetinTemizleyici.baslik("Öğretim Elemanı_218"));
        assertEquals("intibak", MetinTemizleyici.baslik("İntibak_218"));
        assertEquals("ön koşul", MetinTemizleyici.baslik("Ön Koşul_218"));
        assertEquals("kt", MetinTemizleyici.baslik("  KT_218 "));
        assertEquals("teo", MetinTemizleyici.baslik("Teo"));
    }

    @Test
    void dersKoduBosluklariTemizlenir() {
        assertEquals("BSM 105", MetinTemizleyici.kod("BSM  105"));
        assertEquals("COM 101", MetinTemizleyici.kod(" com 101 "));
        assertEquals("SEC 101.11", MetinTemizleyici.kod("SEC 101.11"));
        assertEquals("", MetinTemizleyici.kod(null));
    }

    @Test
    void hocaUnvanVeAdAyrilir() {
        hoca("Doç. Dr. İREM KÜÇÜKOĞLU", "Doç. Dr.", "İREM KÜÇÜKOĞLU");
        hoca("Dr. Öğr. Üyesi YILMAZ KEMAL YÜCE", "Dr. Öğr. Üyesi", "YILMAZ KEMAL YÜCE");
        hoca("Öğr. Gör. Dr. IRMAK KARABULUT", "Öğr. Gör. Dr.", "IRMAK KARABULUT");
        hoca("Kamu Personeli Dr. AHMET YILMAZ", "Kamu Personeli Dr.", "AHMET YILMAZ");
        hoca("Özel/Bağımsız Çalışan İLKNUR SEZER", "Özel/Bağımsız Çalışan", "İLKNUR SEZER");
        hoca("  Prof.  Dr.   ALPER  KÜRŞAT UYSAL ", "Prof. Dr.", "ALPER KÜRŞAT UYSAL");
        hoca("ALİ VELİ", "", "ALİ VELİ");
        hoca("Mehmet Ali", "", "Mehmet Ali");
    }

    @Test
    void bosHocaNullDoner() {
        assertNull(MetinTemizleyici.hocaAyir(""));
        assertNull(MetinTemizleyici.hocaAyir("   "));
        assertNull(MetinTemizleyici.hocaAyir(null));
    }

    @Test
    void oneriIfadeSaatlerdenUretilir() {
        assertEquals("3+2", MetinTemizleyici.oneriIfade(3, 2, 0));
        assertEquals("4", MetinTemizleyici.oneriIfade(4, 0, 0));
        assertEquals("3+1", MetinTemizleyici.oneriIfade(3, 0, 1));
        assertEquals("4", MetinTemizleyici.oneriIfade(0, 4, 0));
        assertEquals("", MetinTemizleyici.oneriIfade(0, 0, 0));
    }
}
