package com.unitimetable.demo.aktarim;

import java.util.Locale;
import java.util.Set;

import com.unitimetable.demo.ders.DersTuru;

public final class DersSiniflandirici {

    private static final Locale TR = Locale.forLanguageTag("tr-TR");

    public record Sonuc(DersTuru tur, boolean sanal) {
    }

    private DersSiniflandirici() {
    }

    public static Sonuc siniflandir(ObsSatiri satir, Set<String> sanalOnekleri, Set<String> seminerKodlari) {
        if (seminerKodlari.contains(satir.kod())) {
            return new Sonuc(DersTuru.SEMINER, true);
        }
        if (satir.toplamSaat() == 0 || bitirmeMi(satir.ad())) {
            return new Sonuc(DersTuru.YERLESIMSIZ, false);
        }
        return new Sonuc(DersTuru.NORMAL, sanalMi(satir.kod(), sanalOnekleri));
    }

    static boolean sanalMi(String kod, Set<String> onekler) {
        for (String onek : onekler) {
            if (!kod.startsWith(onek)) {
                continue;
            }
            if (kod.length() == onek.length()) {
                return true;
            }
            char sonraki = kod.charAt(onek.length());
            if (sonraki == ' ' || sonraki == '.' || Character.isDigit(sonraki)) {
                return true;
            }
        }
        return false;
    }

    static boolean bitirmeMi(String ad) {
        String koku = ad.toUpperCase(Locale.ROOT);
        String turkce = ad.toUpperCase(TR);
        return koku.contains("GRADUATION") || koku.contains("BITIRME") || turkce.contains("BİTİRME");
    }
}