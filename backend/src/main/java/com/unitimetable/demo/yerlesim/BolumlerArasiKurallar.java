package com.unitimetable.demo.yerlesim;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Bölümler arası çakışma kuralları. Veritabanına dokunmaz, yalnızca hazır verilerle karar verir.
 */
public final class BolumlerArasiKurallar {

    private static final Locale TR = Locale.forLanguageTag("tr-TR");

    public record SecilenDerslik(Long id, String ad) {
    }

    private BolumlerArasiKurallar() {
    }

    static boolean araliklarCakisir(int bas1, int sure1, int bas2, int sure2) {
        return bas1 <= bas2 + sure2 - 1 && bas2 <= bas1 + sure1 - 1;
    }

    /** Öğretim elemanını bölümler arası eşleştirme anahtarı: boşluklar teke inmiş, Türkçe büyük harf. */
    public static String hocaAnahtari(String adSoyad) {
        if (adSoyad == null) {
            return null;
        }
        String temiz = adSoyad.trim().replaceAll("\\s+", " ");
        return temiz.isEmpty() ? null : temiz.toUpperCase(TR);
    }

    /** Dönem adlarını karşılaştırma anahtarı: "2026-2027 Güz" ile "2026-2027  güz" aynı sayılır. */
    public static String donemAnahtari(String donem) {
        return donem == null ? "" : donem.trim().replaceAll("\\s+", " ").toLowerCase(TR);
    }

    public static List<Ihlal> degerlendir(DigerBolumBaglami baglam, int gun, int baslangicSlot, int sure,
            String hocaAnahtari, String hocaAdi, boolean subeDerslikCakismaIzni,
            List<SecilenDerslik> derslikler) {
        List<Ihlal> ihlaller = new ArrayList<>();
        for (DigerBolumBaglami.DigerBlok diger : baglam.gununBloklari(gun)) {
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
