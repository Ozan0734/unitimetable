package com.unitimetable.demo.yerlesim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.unitimetable.demo.yerlesim.BolumlerArasiKurallar.SecilenDerslik;
import com.unitimetable.demo.yerlesim.DigerBolumBaglami.DigerBlok;

class BolumlerArasiKurallarTest {

    private static final SecilenDerslik MUHA_403 = new SecilenDerslik(7L, "MUHA-403");

    private static DigerBolumBaglami baglam(DigerBlok... bloklar) {
        return new DigerBolumBaglami(List.of(bloklar));
    }

    private static DigerBlok endustri(int gun, int bas, int sure, String hoca, boolean izin) {
        return new DigerBlok(gun, bas, sure, "Endüstri Mühendisliği", "ENM 101", 1, hoca, Set.of(7L), izin);
    }

    private static List<Ihlal> degerlendir(DigerBolumBaglami baglam, int gun, int bas, int sure,
            String hocaAnahtari, boolean subeIzni, SecilenDerslik... derslikler) {
        return BolumlerArasiKurallar.degerlendir(baglam, gun, bas, sure, hocaAnahtari,
                hocaAnahtari == null ? null : "ALİ YILMAZ", subeIzni, List.of(derslikler));
    }

    @Test
    void ayniDerslikVeSaatEngelOlurVeMesajBolumuSoyler() {
        List<Ihlal> ihlaller = degerlendir(baglam(endustri(2, 1, 3, null, false)), 2, 2, 2, null, false,
                MUHA_403);
        assertEquals(1, ihlaller.size());
        assertEquals(IhlalTuru.ENGEL, ihlaller.get(0).tur());
        assertEquals("DERSLIK_BASKA_BOLUME_AYRILMIS", ihlaller.get(0).kod());
        assertEquals("Derslik MUHA-403 bu saatlerde Endüstri Mühendisliği bölümünün ENM 101 dersi için ayrılmış",
                ihlaller.get(0).mesaj());
        assertNull(ihlaller.get(0).iliskiliBlokId());
    }

    @Test
    void farkliSaatGunVeDerslikIhlalUretmez() {
        DigerBolumBaglami b = baglam(endustri(2, 1, 3, null, false));
        assertTrue(degerlendir(b, 2, 4, 2, null, false, MUHA_403).isEmpty());
        assertTrue(degerlendir(b, 3, 1, 3, null, false, MUHA_403).isEmpty());
        assertTrue(degerlendir(b, 2, 1, 3, null, false, new SecilenDerslik(8L, "MUHA-108")).isEmpty());
        assertTrue(degerlendir(DigerBolumBaglami.bos(), 2, 1, 3, null, false, MUHA_403).isEmpty());
    }

    @Test
    void ucuPaylasanBloklarCakisirArdisikOlanlarCakismaz() {
        DigerBolumBaglami b = baglam(endustri(1, 1, 3, null, false));
        assertEquals(1, degerlendir(b, 1, 3, 2, null, false, MUHA_403).size());
        assertTrue(degerlendir(b, 1, 4, 2, null, false, MUHA_403).isEmpty());
    }

    @Test
    void cakismaIzniVarsaEngelYerineBilgiOlur() {
        List<Ihlal> subeIzni = degerlendir(baglam(endustri(2, 1, 3, null, false)), 2, 1, 3, null, true, MUHA_403);
        assertEquals(IhlalTuru.BILGI, subeIzni.get(0).tur());
        assertEquals("DERSLIK_BASKA_BOLUMLE_ORTAK", subeIzni.get(0).kod());

        List<Ihlal> digerIzni = degerlendir(baglam(endustri(2, 1, 3, null, true)), 2, 1, 3, null, false, MUHA_403);
        assertEquals(IhlalTuru.BILGI, digerIzni.get(0).tur());
    }

    @Test
    void ayniOgretimElemaniBaskaBolumdeDersteyseEngelOlur() {
        String anahtar = BolumlerArasiKurallar.hocaAnahtari("Ali Yılmaz");
        DigerBolumBaglami b = baglam(endustri(4, 1, 2, anahtar, false));

        List<Ihlal> ihlaller = degerlendir(b, 4, 2, 2, anahtar, false, new SecilenDerslik(8L, "MUHA-108"));
        assertEquals(1, ihlaller.size());
        assertEquals("HOCA_BASKA_BOLUMDE_DERSTE", ihlaller.get(0).kod());
        assertEquals(IhlalTuru.ENGEL, ihlaller.get(0).tur());
        assertEquals("Öğretim elemanı ALİ YILMAZ bu saatlerde Endüstri Mühendisliği bölümünün ENM 101 dersi için derste",
                ihlaller.get(0).mesaj());

        assertTrue(degerlendir(b, 4, 2, 2, BolumlerArasiKurallar.hocaAnahtari("Veli Kaya"), false,
                new SecilenDerslik(8L, "MUHA-108")).isEmpty());
        assertTrue(degerlendir(b, 4, 2, 2, null, false, new SecilenDerslik(8L, "MUHA-108")).isEmpty());
    }

    @Test
    void hocaVeDerslikAyniAndaCakisirsaIkisiDeRaporlanir() {
        String anahtar = BolumlerArasiKurallar.hocaAnahtari("Ali Yılmaz");
        List<Ihlal> ihlaller = degerlendir(baglam(endustri(1, 1, 3, anahtar, false)), 1, 1, 3, anahtar, false,
                MUHA_403);
        assertEquals(2, ihlaller.size());
    }

    @Test
    void anahtarlarBoslukVeHarfFarkiniYokSayar() {
        assertEquals("ALİ YILMAZ", BolumlerArasiKurallar.hocaAnahtari("  Ali   Yılmaz "));
        assertEquals(BolumlerArasiKurallar.hocaAnahtari("ALİ YILMAZ"), BolumlerArasiKurallar.hocaAnahtari("ali yılmaz"));
        assertNull(BolumlerArasiKurallar.hocaAnahtari(null));
        assertNull(BolumlerArasiKurallar.hocaAnahtari("   "));

        assertEquals(BolumlerArasiKurallar.donemAnahtari("2026-2027 Güz"),
                BolumlerArasiKurallar.donemAnahtari("2026-2027   GÜZ "));
        assertTrue(!BolumlerArasiKurallar.donemAnahtari("2026-2027 Güz")
                .equals(BolumlerArasiKurallar.donemAnahtari("2026-2027 Bahar")));
        assertEquals("", BolumlerArasiKurallar.donemAnahtari(null));
    }
}
