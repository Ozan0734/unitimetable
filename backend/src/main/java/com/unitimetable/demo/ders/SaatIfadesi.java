package com.unitimetable.demo.ders;

import java.util.ArrayList;
import java.util.List;

import com.unitimetable.demo.ortak.GecersizIslemException;

public final class SaatIfadesi {

    private static final int MAKS_SAYI = 20;
    private static final int MAKS_BLOK = 40;

    private SaatIfadesi() {
    }

    /**
     * İfadeyi blok sürelerinin listesine çevirir. Örnek: "2*2+1" -> [2, 2, 1].
     */
    public static List<Integer> bloklar(String ifade) {
        if (ifade == null || ifade.isBlank()) {
            throw new GecersizIslemException("Ders saati ifadesi boş olamaz");
        }
        String temiz = ifade.replaceAll("\\s+", "");
        List<Integer> sonuc = new ArrayList<>();

        for (String terim : temiz.split("\\+", -1)) {
            if (terim.isEmpty()) {
                throw hata(ifade, "boş terim var");
            }
            String[] carpanlar = terim.split("\\*", -1);
            if (carpanlar.length > 2) {
                throw hata(ifade, "bir terimde yalnızca bir * kullanılabilir");
            }
            int sure = sayi(carpanlar[0], ifade);
            int adet = (carpanlar.length == 2) ? sayi(carpanlar[1], ifade) : 1;
            for (int i = 0; i < adet; i++) {
                sonuc.add(sure);
            }
            if (sonuc.size() > MAKS_BLOK) {
                throw hata(ifade, "en fazla " + MAKS_BLOK + " blok oluşturulabilir");
            }
        }
        return List.copyOf(sonuc);
    }

    /**
     * Tüm blokların toplam saati. Örnek: "2*2+1" -> 5.
     */
    public static int toplam(String ifade) {
        return bloklar(ifade).stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * İfadenin geçerli olduğunu ve toplamının haftalık ders saatine eşit olduğunu doğrular.
     */
    public static void dogrula(String ifade, int haftalikSaat) {
        int toplam = toplam(ifade);
        if (toplam != haftalikSaat) {
            throw new GecersizIslemException("Ders saati ifadesinin toplamı (" + toplam
                    + ") haftalık ders saatine (" + haftalikSaat + ") eşit olmalıdır");
        }
    }

    private static int sayi(String metin, String ifade) {
        if (!metin.matches("[0-9]{1,2}")) {
            throw hata(ifade, "yalnızca pozitif tam sayılar, + ve * kullanılabilir");
        }
        int deger = Integer.parseInt(metin);
        if (deger < 1 || deger > MAKS_SAYI) {
            throw hata(ifade, "sayılar 1 ile " + MAKS_SAYI + " arasında olmalıdır");
        }
        return deger;
    }

    private static GecersizIslemException hata(String ifade, String sebep) {
        return new GecersizIslemException("Ders saati ifadesi geçersiz (\"" + ifade + "\"): " + sebep);
    }
}