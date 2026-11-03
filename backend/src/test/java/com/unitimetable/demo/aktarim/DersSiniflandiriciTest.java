package com.unitimetable.demo.aktarim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.unitimetable.demo.ders.DersTuru;

class DersSiniflandiriciTest {

    private static final Set<String> SANAL = Set.of("ATA", "TDB", "ISG");

    private static ObsSatiri satir(String kod, String ad, int teo, int uyg, int lab) {
        return new ObsSatiri(2, kod, ad, true, "3", "5", teo, uyg, lab, 1, "G", "", "", "", "");
    }

    @Test
    void sanalOnekleriTanınır() {
        for (String kod : new String[] {"ATA 101", "TDB 101", "ISG 401E"}) {
            DersSiniflandirici.Sonuc sonuc = DersSiniflandirici.siniflandir(
                    satir(kod, "ORTAK DERS", 2, 0, 0), SANAL, Set.of());
            assertEquals(DersTuru.NORMAL, sonuc.tur(), kod);
            assertTrue(sonuc.sanal(), kod);
        }
    }

    @Test
    void sanalOlmayanDerslerSanalSayilmaz() {
        for (String kod : new String[] {"COM 101", "ATAMER 1"}) {
            DersSiniflandirici.Sonuc sonuc = DersSiniflandirici.siniflandir(
                    satir(kod, "DERS", 3, 0, 0), SANAL, Set.of());
            assertEquals(DersTuru.NORMAL, sonuc.tur(), kod);
            assertFalse(sonuc.sanal(), kod);
        }
    }

    @Test
    void sifirSaatliDersYerlesimsizOlur() {
        DersSiniflandirici.Sonuc sonuc = DersSiniflandirici.siniflandir(
                satir("COM 301", "INTERNSHIP I", 0, 0, 0), SANAL, Set.of());
        assertEquals(DersTuru.YERLESIMSIZ, sonuc.tur());
        assertFalse(sonuc.sanal());
    }

    @Test
    void bitirmeDersleriYerlesimsizOlur() {
        String[] adlar = {"GRADUATION DESIGN PROJECT", "BİTİRME PROJESİ",
                "MANAGEMENT ENGINNERING GRADUATION PROJECT", "Graduation Thesis", "bitirme tezi"};
        for (String ad : adlar) {
            DersSiniflandirici.Sonuc sonuc = DersSiniflandirici.siniflandir(
                    satir("COM 402", ad, 0, 6, 0), SANAL, Set.of());
            assertEquals(DersTuru.YERLESIMSIZ, sonuc.tur(), ad);
        }
    }

    @Test
    void seminerYalnizcaListedekiKodlardaOlur() {
        ObsSatiri seminer = satir("COM 401", "COMPUTER ENGINEERING SEMINAR", 0, 4, 0);

        DersSiniflandirici.Sonuc listeli = DersSiniflandirici.siniflandir(seminer, SANAL, Set.of("COM 401"));
        assertEquals(DersTuru.SEMINER, listeli.tur());
        assertTrue(listeli.sanal());

        DersSiniflandirici.Sonuc listesiz = DersSiniflandirici.siniflandir(seminer, SANAL, Set.of());
        assertEquals(DersTuru.NORMAL, listesiz.tur());
        assertFalse(listesiz.sanal());
    }
}
