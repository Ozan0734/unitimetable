package com.unitimetable.demo.ortakders;

import java.util.List;

import com.unitimetable.demo.yerlesim.BolumlerArasiKurallar;

/**
 * İki bölümün dersinin ortak olup olamayacağına dair saf kurallar. Aynı ders sayılması için kodlar ya da
 * adlar aynı olmalı, öğretim elemanı aynı kişi olmalı ve blok yapıları (süre sırası) birebir aynı olmalıdır.
 */
public final class OrtakDersEsleme {

    private OrtakDersEsleme() {
    }

    public static String adAnahtari(String ad) {
        return BolumlerArasiKurallar.katla(ad);
    }

    public static String kodAnahtari(String kod) {
        return BolumlerArasiKurallar.katla(kod);
    }

    /** Aynı ders: kodlar ya da adlar eşit (boş değerler eşleşmez). Matematik I farklı bölümlerde farklı kodlu olabilir. */
    public static boolean ayniDers(String kod1, String ad1, String kod2, String ad2) {
        String k1 = kodAnahtari(kod1);
        String k2 = kodAnahtari(kod2);
        String a1 = adAnahtari(ad1);
        String a2 = adAnahtari(ad2);
        return (!k1.isEmpty() && k1.equals(k2)) || (!a1.isEmpty() && a1.equals(a2));
    }

    /** Öğretim elemanı anahtarları (BolumlerArasiKurallar.hocaAnahtari çıktısı) aynı kişiyi göstermeli. */
    public static boolean ayniHoca(String hocaAnahtari1, String hocaAnahtari2) {
        return hocaAnahtari1 != null && hocaAnahtari1.equals(hocaAnahtari2);
    }

    public static boolean blokYapilariAyni(List<Integer> bloklar1, List<Integer> bloklar2) {
        return !bloklar1.isEmpty() && bloklar1.equals(bloklar2);
    }
}
