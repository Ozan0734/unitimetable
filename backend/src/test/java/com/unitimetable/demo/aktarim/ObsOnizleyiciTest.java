package com.unitimetable.demo.aktarim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class ObsOnizleyiciTest {

    private static ObsSatiri satir(int no, String kod, String ad, boolean zorunlu, int teo, int uyg,
            int lab, int sinif, String donem, String hoca, String grup) {
        return new ObsSatiri(no, kod, ad, zorunlu, "3", "5", teo, uyg, lab, sinif, donem, hoca, "", "", grup);
    }

    private static boolean uyariVar(ObsOnizlemeDto.Yanit yanit, int satir, String parca) {
        return yanit.uyarilar().stream()
                .anyMatch(u -> u.satir() == satir && u.mesaj().contains(parca));
    }

    @Test
    void donemFiltresiUygulanir() {
        List<ObsSatiri> veri = List.of(
                satir(2, "COM 101", "MATH", true, 4, 0, 0, 1, "G", "Prof. Dr. ALI VELI", ""),
                satir(3, "COM 102", "MATH 2", true, 4, 0, 0, 1, "B", "Prof. Dr. ALI VELI", ""));
        ObsOnizlemeDto.Yanit yanit = ObsOnizleyici.hazirla(veri, "G");

        assertEquals(2, yanit.toplamSatir());
        assertEquals(1, yanit.donemSatiri());
        assertEquals(1, yanit.digerDonemSatiri());
        assertEquals(1, yanit.zorunluSayisi());
    }

    @Test
    void secmeliGruplarVeAltDersleriBaglanir() {
        List<ObsSatiri> veri = List.of(
                satir(2, "SEC 101", "NON-TECH", false, 3, 0, 0, 1, "G", "", "[Ana Grup]"),
                satir(3, "SEC 102", "OUT OF AREA", false, 2, 0, 0, 1, "G", "", "[Ana Grup]"),
                satir(4, "SEC 101.1", "ECON", false, 3, 0, 0, 1, "G", "Dr. Öğr. Üyesi KÜBRA UYAR", "SEC 101"),
                satir(5, "SEC 101.2", "LAW", false, 3, 0, 0, 1, "G", "", "SEC 101,SEC 102"));
        ObsOnizlemeDto.Yanit yanit = ObsOnizleyici.hazirla(veri, "G");

        assertEquals(2, yanit.secmeliGrupSayisi());
        assertEquals(2, yanit.secmeliDersSayisi());
        assertEquals(0, yanit.grupsuzSecmeliSayisi());
        assertEquals("SEC 101", yanit.secmeliGruplar().get(0).kod());
        assertEquals(2, yanit.secmeliGruplar().get(0).dersler().size());
        assertEquals("SEC 102", yanit.secmeliGruplar().get(1).kod());
        assertEquals(1, yanit.secmeliGruplar().get(1).dersler().size());
        assertTrue(yanit.uyarilar().isEmpty());
    }

    @Test
    void grubaBaglanmayanSecmeliEtiketiniKorur() {
        List<ObsSatiri> veri = List.of(
                satir(2, "KRP 101", "CAREER", false, 2, 0, 0, 1, "G", "", "ITB 1/Spring (SGS)"));
        ObsOnizlemeDto.Yanit yanit = ObsOnizleyici.hazirla(veri, "G");

        assertEquals(1, yanit.grupsuzSecmeliSayisi());
        assertEquals("ITB 1/Spring (SGS)", yanit.grupsuzSecmeliler().get(0).etiket());
    }

    @Test
    void uyarilarUretilir() {
        List<ObsSatiri> veri = List.of(
                satir(2, "COM 101", "MATH", true, 4, 0, 0, 1, "G", "", ""),
                satir(3, "COM 101", "MATH", true, 4, 0, 0, 1, "G", "Prof. Dr. ALI VELI", ""),
                satir(4, "COM 105", "LINEAR", true, 0, 0, 0, 1, "G", "Prof. Dr. ALI VELI", ""),
                satir(5, "SEC 109", "EMPTY GROUP", false, 2, 0, 0, 1, "G", "", "[Ana Grup]"));
        ObsOnizlemeDto.Yanit yanit = ObsOnizleyici.hazirla(veri, "G");

        assertTrue(uyariVar(yanit, 2, "öğretim elemanı yok"));
        assertTrue(uyariVar(yanit, 3, "daha önce"));
        assertTrue(uyariVar(yanit, 4, "toplamı 0"));
        assertTrue(uyariVar(yanit, 5, "altında ders bulunamadı"));
    }

    @Test
    void saatOnerisiUretilir() {
        List<ObsSatiri> veri = List.of(
                satir(2, "COM 107", "COMPUTER SCIENCES", true, 3, 2, 0, 1, "G", "Doç. Dr. ALI VELI", ""));
        ObsOnizlemeDto.Ders ders = ObsOnizleyici.hazirla(veri, "G").zorunluDersler().get(0);

        assertEquals(5, ders.haftalikSaat());
        assertEquals("3+2", ders.onerilenSaatIfadesi());
        assertEquals("Doç. Dr.", ders.hocaUnvan());
        assertEquals("ALI VELI", ders.hocaAd());
    }

    @Test
    void hocalarBirlestirilirVeSayilir() {
        List<ObsSatiri> veri = List.of(
                satir(2, "COM 101", "A", true, 3, 0, 0, 1, "G", "Prof. Dr. ALI VELI", ""),
                satir(3, "COM 103", "B", true, 3, 0, 0, 1, "G", "Prof. Dr. ALI VELI", ""),
                satir(4, "COM 105", "C", true, 3, 0, 0, 1, "G", "Öğr. Gör. AYŞE DEMİR", ""));
        List<ObsOnizlemeDto.Hoca> hocalar = ObsOnizleyici.hazirla(veri, "G").hocalar();

        assertEquals(2, hocalar.size());
        assertEquals("ALI VELI", hocalar.get(0).ad());
        assertEquals("Prof. Dr.", hocalar.get(0).unvan());
        assertEquals(2, hocalar.get(0).dersSayisi());
        assertEquals("AYŞE DEMİR", hocalar.get(1).ad());
    }
}
