package com.unitimetable.demo.aktarim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.unitimetable.demo.derslik.DerslikTuru;
import com.unitimetable.demo.ortak.GecersizIslemException;

class DerslikKararlariTest {

    private static DerslikSatiri satir(int no, String ad, String tip, String kapasite, String durum) {
        return new DerslikSatiri(no, "K-" + no, ad, tip, kapasite, "", durum);
    }

    @Test
    void tipYazimlariTekTureCevrilir() {
        assertEquals(DerslikTuru.SINIF, DerslikKararlari.tip("Sınıf"));
        assertEquals(DerslikTuru.SINIF, DerslikKararlari.tip("SINIF"));
        assertEquals(DerslikTuru.LABORATUVAR, DerslikKararlari.tip("Lab"));
        assertEquals(DerslikTuru.LABORATUVAR, DerslikKararlari.tip("LAB."));
        assertEquals(DerslikTuru.LABORATUVAR, DerslikKararlari.tip("Laboratuvar"));
        assertEquals(DerslikTuru.SALON, DerslikKararlari.tip("Salon"));
        assertEquals(DerslikTuru.SANAL, DerslikKararlari.tip("Sanal"));
        assertEquals(DerslikTuru.AMFI, DerslikKararlari.tip("Amfi"));
        assertNull(DerslikKararlari.tip("Bilinmez"));
        assertNull(DerslikKararlari.tip(""));
    }

    @Test
    void kararlarDogruVerilir() {
        List<DerslikSatiri> satirlar = List.of(
                satir(2, "MUHA-103", "Sınıf", "99", "1"),
                satir(3, "MUHA-108", "Sınıf", "99", "1"),
                satir(4, "TOPLANTI-1", "Salon", "20", "0"),
                satir(5, "X-1", "Tuhaf", "50", "1"),
                satir(6, "X-2", "Sınıf", "abc", "1"),
                satir(7, "MUHA-108", "Sınıf", "50", "1"),
                satir(8, "A-101", "Sınıf", "60", "1"));
        List<DerslikKararlari.Karar> kararlar = DerslikKararlari.hazirla(satirlar, Set.of("A-101"),
                Map.of("MUHA-103", 100));

        assertEquals(DerslikKararlari.Durum.EKLENECEK, kararlar.get(0).durum());
        assertEquals(100, kararlar.get(0).kapasite());
        assertEquals(99, kararlar.get(0).dosyadakiKapasite());
        assertTrue(kararlar.get(0).duzeltildi());

        assertEquals(DerslikKararlari.Durum.EKLENECEK, kararlar.get(1).durum());
        assertEquals(99, kararlar.get(1).kapasite());
        assertEquals(false, kararlar.get(1).duzeltildi());

        assertEquals(DerslikKararlari.Durum.PASIF, kararlar.get(2).durum());
        assertEquals(DerslikKararlari.Durum.GECERSIZ, kararlar.get(3).durum());
        assertTrue(kararlar.get(3).sebep().contains("tanınmadı"));
        assertEquals(DerslikKararlari.Durum.GECERSIZ, kararlar.get(4).durum());
        assertTrue(kararlar.get(4).sebep().contains("Kontenjan"));
        assertEquals(DerslikKararlari.Durum.GECERSIZ, kararlar.get(5).durum());
        assertTrue(kararlar.get(5).sebep().contains("birden fazla"));
        assertEquals(DerslikKararlari.Durum.ZATEN_VAR, kararlar.get(6).durum());
    }

    @Test
    void duzeltmelerOkunur() {
        Map<String, Integer> sonuc = DerslikKararlari.duzeltmeler(List.of("MUHA-103=100", " muhd-403 = 90 ", ""));
        assertEquals(2, sonuc.size());
        assertEquals(100, sonuc.get("MUHA-103"));
        assertEquals(90, sonuc.get("MUHD-403"));
        assertEquals(0, DerslikKararlari.duzeltmeler(null).size());
    }

    @Test
    void gecersizDuzeltmelerReddedilir() {
        for (String ham : new String[] {"abc", "A=0", "A=2001", "=5", "A=x"}) {
            assertThrows(GecersizIslemException.class,
                    () -> DerslikKararlari.duzeltmeler(List.of(ham)), ham);
        }
    }

    @Test
    void dosyadaOlmayanDuzeltmeBulunur() {
        List<DerslikSatiri> satirlar = List.of(satir(2, "MUHA-103", "Sınıf", "99", "1"));
        List<String> bilinmeyen = DerslikKararlari.bilinmeyenDuzeltmeler(satirlar,
                Map.of("MUHA-103", 100, "MUHX-1", 50));
        assertEquals(List.of("MUHX-1"), bilinmeyen);
    }
}
