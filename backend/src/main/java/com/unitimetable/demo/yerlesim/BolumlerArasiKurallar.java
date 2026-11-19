package com.unitimetable.demo.yerlesim;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Bölümler arası çakışma kuralları. Veritabanına dokunmaz, yalnızca hazır verilerle karar verir.
 */
public final class BolumlerArasiKurallar {

    private static final String[] GUN_ADLARI = {
            "Pazartesi", "Salı", "Çarşamba", "Perşembe", "Cuma", "Cumartesi", "Pazar"
    };

    public record SecilenDerslik(Long id, String ad) {
    }

    private BolumlerArasiKurallar() {
    }

    static boolean araliklarCakisir(int bas1, int sure1, int bas2, int sure2) {
        return bas1 <= bas2 + sure2 - 1 && bas2 <= bas1 + sure1 - 1;
    }

    /**
     * Karşılaştırma anahtarı: büyük harfe çevirir, Türkçe karakterlerin noktalı ve noktasız hallerini ve
     * aksanları eşitler (İ, I, ı hepsi I), boşlukları teke indirir. "Ali Yılmaz" ile "ALİ YILMAZ" ile
     * "ALI YILMAZ" aynı anahtarı verir.
     */
    public static String katla(String metin) {
        if (metin == null) {
            return "";
        }
        String ayrik = Normalizer.normalize(metin.trim().toUpperCase(java.util.Locale.ROOT), Normalizer.Form.NFD);
        return ayrik.replaceAll("\\p{M}+", "").replaceAll("\\s+", " ");
    }

    /** Öğretim elemanını bölümler arası eşleştirme anahtarı. Boş ad için null. */
    public static String hocaAnahtari(String adSoyad) {
        String anahtar = katla(adSoyad);
        return anahtar.isEmpty() ? null : anahtar;
    }

    /** Dönem adlarını karşılaştırma anahtarı: "2026-2027 Güz" ile "2026-2027  guz" aynı sayılır. */
    public static String donemAnahtari(String donem) {
        return katla(donem);
    }

    /**
     * @param subeId  denetlenen bloğun şubesi
     * @param siraNo  denetlenen bloğun şube içindeki sırası (ortak şubelerin aynı sıradaki blokları eşleşir)
     */
    public static List<Ihlal> degerlendir(DigerBolumBaglami baglam, Long subeId, int siraNo, int gun,
            int baslangicSlot, int sure, String hocaAnahtari, String hocaAdi, boolean subeDerslikCakismaIzni,
            List<SecilenDerslik> derslikler) {
        List<Ihlal> ihlaller = new ArrayList<>();
        Set<Long> partnerler = baglam.partnerler(subeId);
        Set<Long> secilenDerslikIdler = derslikler.stream().map(SecilenDerslik::id).collect(Collectors.toSet());

        // Ortak şubeler aynı gün, saat ve derslikte olmalıdır.
        for (Long partner : partnerler) {
            DigerBolumBaglami.DigerBlok partnerBlogu = baglam.blok(partner, siraNo);
            if (partnerBlogu == null) {
                continue;
            }
            String partnerAdi = partnerBlogu.bolumAdi() + " bölümü, " + partnerBlogu.dersKodu();
            boolean ayniYer = partnerBlogu.gun() == gun
                    && partnerBlogu.baslangicSlot() == baslangicSlot
                    && partnerBlogu.sure() == sure
                    && new HashSet<>(partnerBlogu.derslikIdler()).equals(secilenDerslikIdler);
            if (ayniYer) {
                ihlaller.add(new Ihlal(IhlalTuru.BILGI, "ORTAK_SUBE_AYNI_YERDE",
                        "Ortak şube (" + partnerAdi + ") bu bloğu aynı yerde yapıyor", null));
            } else {
                ihlaller.add(new Ihlal(IhlalTuru.ENGEL, "ORTAK_SUBE_FARKLI_YERDE",
                        "Ortak şube (" + partnerAdi + ") bu bloğu " + GUN_ADLARI[partnerBlogu.gun() - 1]
                                + " günü " + partnerBlogu.baslangicSlot() + ". derste, "
                                + partnerBlogu.derslikAdlari() + " dersliğinde yapıyor, ortak şubeler aynı yerde olmalı",
                        null));
            }
        }

        for (DigerBolumBaglami.DigerBlok diger : baglam.gununBloklari(gun)) {
            if (partnerler.contains(diger.subeId()) && diger.siraNo() == siraNo) {
                continue; // ortak şubenin eşleşen bloğu yukarıda değerlendirildi
            }
            if (!araliklarCakisir(baslangicSlot, sure, diger.baslangicSlot(), diger.sure())) {
                continue;
            }
            String digerDers = diger.bolumAdi() + " bölümünün " + diger.dersKodu() + " dersi";

            if (hocaAnahtari != null && hocaAnahtari.equals(diger.hocaAnahtari())) {
                ihlaller.add(new Ihlal(IhlalTuru.ENGEL, "HOCA_BASKA_BOLUMDE_DERSTE",
                        "Öğretim elemanı " + hocaAdi + " bu saatlerde " + digerDers + " için derste", null));
            }

            for (SecilenDerslik derslik : derslikler) {
                if (!diger.derslikIdler().contains(derslik.id())) {
                    continue;
                }
                if (subeDerslikCakismaIzni || diger.derslikCakismaIzni()) {
                    ihlaller.add(new Ihlal(IhlalTuru.BILGI, "DERSLIK_BASKA_BOLUMLE_ORTAK",
                            "Derslik " + derslik.ad() + " bu saatlerde " + digerDers
                                    + " ile ortak kullanılıyor, çakışma izni tanımlı",
                            null));
                } else {
                    ihlaller.add(new Ihlal(IhlalTuru.ENGEL, "DERSLIK_BASKA_BOLUME_AYRILMIS",
                            "Derslik " + derslik.ad() + " bu saatlerde " + digerDers + " için ayrılmış", null));
                }
            }
        }
        return ihlaller;
    }
}
