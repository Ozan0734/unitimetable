package com.unitimetable.demo.yerlesim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.unitimetable.demo.yerlesim.BolumlerArasiKurallar.SecilenDerslik;
import com.unitimetable.demo.yerlesim.DigerBolumBaglami.DigerBlok;

class BolumlerArasiKurallarTest {

    private static final SecilenDerslik MUHA_403 = new SecilenDerslik(7L, "MUHA-403");
    private static final Long BENIM_SUBE = 1L;
    private static final Long DIGER_SUBE = 50L;

    private static DigerBolumBaglami baglam(DigerBlok... bloklar) {
        return new DigerBolumBaglami(List.of(bloklar), Map.of(), Map.of());
    }

    private static DigerBolumBaglami ortakBaglam(Map<Long, Integer> ogrenciler, DigerBlok... bloklar) {
        return new DigerBolumBaglami(List.of(bloklar), Map.of(BENIM_SUBE, Set.of(DIGER_SUBE)), ogrenciler);
    }

    private static DigerBlok blok(Long subeId, int siraNo, int gun, int bas, int sure, String hoca, boolean izin) {
        return new DigerBlok(subeId, siraNo, gun, bas, sure, "Endüstri Mühendisliği", "ENM 101", 1, hoca,
                Set.of(7L), "MUHA-403", izin);
    }

    private static DigerBlok endustri(int gun, int bas, int sure, String hoca, boolean izin) {
        return blok(DIGER_SUBE, 1, gun, bas, sure, hoca, izin);
    }

    private static List<Ihlal> degerlendir(DigerBolumBaglami baglam, int gun, int bas, int sure,
            String hocaAnahtari, boolean subeIzni, SecilenDerslik... derslikler) {
        return BolumlerArasiKurallar.degerlendir(baglam, BENIM_SUBE, 1, gun, bas, sure, hocaAnahtari,
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
    void ortakSubeAyniYerdeyseTekBilgiVerirHocaVeDerslikCakismasiSayilmaz() {
        String anahtar = BolumlerArasiKurallar.hocaAnahtari("Ali Yılmaz");
        DigerBolumBaglami b = ortakBaglam(Map.of(), endustri(2, 1, 3, anahtar, false));

        List<Ihlal> ihlaller = degerlendir(b, 2, 1, 3, anahtar, false, MUHA_403);
        assertEquals(1, ihlaller.size());
        assertEquals(IhlalTuru.BILGI, ihlaller.get(0).tur());
        assertEquals("ORTAK_SUBE_AYNI_YERDE", ihlaller.get(0).kod());
    }

    @Test
    void ortakSubeFarkliYerdeyseEngelOlurVeNeredeOlduguSoylenir() {
        DigerBolumBaglami b = ortakBaglam(Map.of(), endustri(2, 1, 3, null, false));

        List<Ihlal> farkliGun = degerlendir(b, 3, 1, 3, null, false, MUHA_403);
        assertEquals(1, farkliGun.size());
        assertEquals(IhlalTuru.ENGEL, farkliGun.get(0).tur());
        assertEquals("ORTAK_SUBE_FARKLI_YERDE", farkliGun.get(0).kod());
        assertEquals("Ortak şube (Endüstri Mühendisliği bölümü, ENM 101) bu bloğu Salı günü 1. derste, "
                + "MUHA-403 dersliğinde yapıyor, ortak şubeler aynı yerde olmalı", farkliGun.get(0).mesaj());

        assertEquals("ORTAK_SUBE_FARKLI_YERDE",
                degerlendir(b, 2, 2, 3, null, false, MUHA_403).get(0).kod());
        assertEquals("ORTAK_SUBE_FARKLI_YERDE",
                degerlendir(b, 2, 1, 3, null, false, new SecilenDerslik(8L, "MUHA-108")).get(0).kod());
    }

    @Test
    void ortakSubeHenuzYerlesmediyseKisitYoktur() {
        DigerBolumBaglami b = ortakBaglam(Map.of());
        assertTrue(degerlendir(b, 2, 1, 3, null, false, MUHA_403).isEmpty());
    }

    @Test
    void ortakSubeninBaskaSiradakiBloguNormalBlokGibiDegerlendirilir() {
        String anahtar = BolumlerArasiKurallar.hocaAnahtari("Ali Yılmaz");
        DigerBlok partnerinIkinciBlogu = blok(DIGER_SUBE, 2, 2, 1, 3, anahtar, false);
        DigerBolumBaglami b = ortakBaglam(Map.of(), partnerinIkinciBlogu);

        List<Ihlal> ihlaller = degerlendir(b, 2, 1, 3, anahtar, false, MUHA_403);
        assertEquals(2, ihlaller.size());
        assertTrue(ihlaller.stream().allMatch(i -> i.tur() == IhlalTuru.ENGEL));
    }

    @Test
    void ortakOgrenciToplamiVeBosluk() {
        DigerBolumBaglami b = ortakBaglam(Map.of(DIGER_SUBE, 60));
        assertEquals(60, b.partnerOgrenciToplami(BENIM_SUBE));
        assertEquals(0, b.partnerOgrenciToplami(99L));
        assertTrue(!b.bosMu());
        assertTrue(DigerBolumBaglami.bos().bosMu());
        assertEquals(0, DigerBolumBaglami.bos().partnerOgrenciToplami(BENIM_SUBE));
    }

    @Test
    void anahtarlarBoslukVeHarfFarkiniYokSayar() {
        assertEquals("ALI YILMAZ", BolumlerArasiKurallar.hocaAnahtari("  Ali   Yılmaz "));
        assertEquals(BolumlerArasiKurallar.hocaAnahtari("ALİ YILMAZ"), BolumlerArasiKurallar.hocaAnahtari("ali yılmaz"));
        assertEquals(BolumlerArasiKurallar.hocaAnahtari("ALİ YILMAZ"), BolumlerArasiKurallar.hocaAnahtari("ALI YILMAZ"));
        assertTrue(!BolumlerArasiKurallar.hocaAnahtari("ALİ YILMAZ")
                .equals(BolumlerArasiKurallar.hocaAnahtari("VELİ YILMAZ")));
        assertNull(BolumlerArasiKurallar.hocaAnahtari(null));
        assertNull(BolumlerArasiKurallar.hocaAnahtari("   "));

        assertEquals(BolumlerArasiKurallar.donemAnahtari("2026-2027 Güz"),
                BolumlerArasiKurallar.donemAnahtari("2026-2027   guz "));
        assertTrue(!BolumlerArasiKurallar.donemAnahtari("2026-2027 Güz")
                .equals(BolumlerArasiKurallar.donemAnahtari("2026-2027 Bahar")));
        assertEquals("", BolumlerArasiKurallar.donemAnahtari(null));
    }
}
