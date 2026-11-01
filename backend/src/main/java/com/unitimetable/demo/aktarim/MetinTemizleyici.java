package com.unitimetable.demo.aktarim;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public final class MetinTemizleyici {

    private static final Locale TR = Locale.forLanguageTag("tr-TR");
    private static final Pattern BASLIK_EKI = Pattern.compile("_\\d+$");
    private static final Pattern BOSLUK = Pattern.compile("[\\s\\u00A0]+");

    public record Hoca(String unvan, String ad) {
    }

    private MetinTemizleyici() {
    }

    /** Baştaki ve sondaki boşlukları atar, aradaki boşlukları teke indirir. */
    public static String metin(String ham) {
        if (ham == null) {
            return "";
        }
        return BOSLUK.matcher(ham).replaceAll(" ").trim();
    }

    /** "Ders Kodu_218" -> "ders kodu" */
    public static String baslik(String ham) {
        String temiz = BASLIK_EKI.matcher(metin(ham)).replaceAll("");
        return metin(temiz).toLowerCase(TR);
    }

    /** "BSM  105" -> "BSM 105" */
    public static String kod(String ham) {
        return metin(ham).toUpperCase(Locale.ROOT);
    }

    /**
     * "Doç. Dr. İREM KÜÇÜKOĞLU" -> unvan "Doç. Dr.", ad "İREM KÜÇÜKOĞLU".
     * Sondaki tamamı büyük harfli sözcükler ad, öncesi unvan sayılır.
     */
    public static Hoca hocaAyir(String ham) {
        String temiz = metin(ham);
        if (temiz.isEmpty()) {
            return null;
        }
        String[] parcalar = temiz.split(" ");
        int adBaslangici = parcalar.length;
        for (int i = parcalar.length - 1; i >= 0; i--) {
            if (buyukHarfli(parcalar[i])) {
                adBaslangici = i;
            } else {
                break;
            }
        }
        if (adBaslangici == parcalar.length) {
            return new Hoca("", temiz);
        }
        String unvan = String.join(" ", Arrays.copyOfRange(parcalar, 0, adBaslangici));
        String ad = String.join(" ", Arrays.copyOfRange(parcalar, adBaslangici, parcalar.length));
        return new Hoca(unvan, ad);
    }

    /** Aynı kişiyi farklı yazımlarda eşleştirmek için anahtar. */
    public static String hocaAnahtari(String ad) {
        return metin(ad).toUpperCase(TR);
    }

    /** Teo, Uyg ve Lab saatlerinden önerilen saat ifadesi. Örnek: 3, 2, 0 -> "3+2". */
    public static String oneriIfade(int teo, int uyg, int lab) {
        List<String> parcalar = new ArrayList<>();
        if (teo > 0) {
            parcalar.add(String.valueOf(teo));
        }
        if (uyg > 0) {
            parcalar.add(String.valueOf(uyg));
        }
        if (lab > 0) {
            parcalar.add(String.valueOf(lab));
        }
        return String.join("+", parcalar);
    }

    private static boolean buyukHarfli(String sozcuk) {
        boolean harfVar = false;
        for (char c : sozcuk.toCharArray()) {
            if (Character.isLetter(c)) {
                harfVar = true;
                if (Character.isLowerCase(c)) {
                    return false;
                }
            }
        }
        return harfVar;
    }
}