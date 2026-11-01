package com.unitimetable.demo.aktarim;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.unitimetable.demo.ders.SaatIfadesi;
import com.unitimetable.demo.ortak.GecersizIslemException;

public final class ObsOnizleyici {

    private static final Locale TR = Locale.forLanguageTag("tr-TR");
    private static final String ANA_GRUP = "[ANA GRUP]";

    private ObsOnizleyici() {
    }

    public static ObsOnizlemeDto.Yanit hazirla(List<ObsSatiri> tumSatirlar, String donem) {
        List<ObsSatiri> satirlar = tumSatirlar.stream().filter(s -> donem.equals(s.donem())).toList();
        List<ObsOnizlemeDto.Uyari> uyarilar = new ArrayList<>();

        Map<String, ObsSatiri> anaGruplar = new LinkedHashMap<>();
        for (ObsSatiri s : satirlar) {
            if (anaGrupMu(s)) {
                anaGruplar.putIfAbsent(s.kod(), s);
            }
        }
        Map<String, List<ObsOnizlemeDto.Ders>> grupDersleri = new LinkedHashMap<>();
        for (String grupKodu : anaGruplar.keySet()) {
            grupDersleri.put(grupKodu, new ArrayList<>());
        }

        List<ObsOnizlemeDto.Ders> zorunlular = new ArrayList<>();
        List<ObsOnizlemeDto.Ders> grupsuzlar = new ArrayList<>();
        Set<Integer> grupluSatirlar = new HashSet<>();
        Map<String, Integer> gorulenKodlar = new HashMap<>();

        for (ObsSatiri s : satirlar) {
            if (anaGrupMu(s)) {
                continue;
            }
            Integer onceki = gorulenKodlar.putIfAbsent(s.kod(), s.satir());
            if (onceki != null) {
                uyarilar.add(new ObsOnizlemeDto.Uyari(s.satir(), s.kod(),
                        "Bu ders kodu dosyada daha önce de geçiyor (satır " + onceki + ")"));
            }
            List<String> eslesenler = s.zorunlu()
                    ? List.of()
                    : grupKodlari(s.grup()).stream().filter(anaGruplar::containsKey).toList();
            String etiket = eslesenler.isEmpty() ? s.grup() : "";
            ObsOnizlemeDto.Ders ders = dersOlustur(s, etiket, uyarilar);

            if (s.zorunlu()) {
                zorunlular.add(ders);
            } else if (eslesenler.isEmpty()) {
                grupsuzlar.add(ders);
            } else {
                for (String grupKodu : eslesenler) {
                    grupDersleri.get(grupKodu).add(ders);
                }
                grupluSatirlar.add(s.satir());
            }
        }

        Comparator<ObsOnizlemeDto.Ders> dersSirasi = Comparator
                .comparingInt(ObsOnizlemeDto.Ders::sinif)
                .thenComparing(ObsOnizlemeDto.Ders::kod);
        zorunlular.sort(dersSirasi);
        grupsuzlar.sort(dersSirasi);

        List<ObsOnizlemeDto.SecmeliGrup> gruplar = new ArrayList<>();
        anaGruplar.values().stream()
                .sorted(Comparator.comparingInt(ObsSatiri::sinif).thenComparing(ObsSatiri::kod))
                .forEach(g -> {
                    List<ObsOnizlemeDto.Ders> dersler = grupDersleri.get(g.kod());
                    dersler.sort(Comparator.comparing(ObsOnizlemeDto.Ders::kod));
                    if (dersler.isEmpty()) {
                        uyarilar.add(new ObsOnizlemeDto.Uyari(g.satir(), g.kod(),
                                "Seçmeli grubun altında ders bulunamadı"));
                    }
                    gruplar.add(new ObsOnizlemeDto.SecmeliGrup(g.satir(), g.kod(), g.ad(), g.sinif(), dersler));
                });

        uyarilar.sort(Comparator.comparingInt(ObsOnizlemeDto.Uyari::satir));

        return new ObsOnizlemeDto.Yanit(donem, tumSatirlar.size(), satirlar.size(),
                tumSatirlar.size() - satirlar.size(), zorunlular.size(), gruplar.size(),
                grupluSatirlar.size(), grupsuzlar.size(), zorunlular, gruplar, grupsuzlar,
                hocalar(satirlar), uyarilar);
    }

    private static ObsOnizlemeDto.Ders dersOlustur(ObsSatiri s, String etiket,
            List<ObsOnizlemeDto.Uyari> uyarilar) {
        int toplam = s.toplamSaat();
        String ifade = MetinTemizleyici.oneriIfade(s.teo(), s.uyg(), s.lab());

        if (s.zorunlu()) {
            if (s.hocaMetni().isBlank()) {
                uyarilar.add(new ObsOnizlemeDto.Uyari(s.satir(), s.kod(),
                        "Zorunlu ders için öğretim elemanı yok"));
            }
            if (toplam == 0) {
                uyarilar.add(new ObsOnizlemeDto.Uyari(s.satir(), s.kod(),
                        "Teo, Uyg ve Lab saatlerinin toplamı 0: haftalık saati olmayan ders "
                                + "(staj gibi), yerleşimsiz olarak açılabilir"));
            } else {
                try {
                    SaatIfadesi.bloklar(ifade);
                } catch (GecersizIslemException hata) {
                    uyarilar.add(new ObsOnizlemeDto.Uyari(s.satir(), s.kod(),
                            "Önerilen saat ifadesi (" + ifade + ") geçersiz: " + hata.getMessage()));
                }
            }
        }

        MetinTemizleyici.Hoca hoca = MetinTemizleyici.hocaAyir(s.hocaMetni());
        return new ObsOnizlemeDto.Ders(s.satir(), s.kod(), s.ad(), s.zorunlu(), s.sinif(), s.donem(),
                s.teo(), s.uyg(), s.lab(), toplam, ifade, s.kredi(), s.akts(),
                hoca == null ? "" : hoca.unvan(), hoca == null ? "" : hoca.ad(),
                s.intibak(), s.onKosul(), etiket);
    }

    private static List<ObsOnizlemeDto.Hoca> hocalar(List<ObsSatiri> satirlar) {
        Map<String, Integer> sayaclar = new TreeMap<>(Collator.getInstance(TR));
        Map<String, MetinTemizleyici.Hoca> kimlikler = new HashMap<>();
        for (ObsSatiri s : satirlar) {
            MetinTemizleyici.Hoca hoca = MetinTemizleyici.hocaAyir(s.hocaMetni());
            if (hoca == null) {
                continue;
            }
            String anahtar = MetinTemizleyici.hocaAnahtari(hoca.ad());
            kimlikler.putIfAbsent(anahtar, hoca);
            sayaclar.merge(anahtar, 1, Integer::sum);
        }
        List<ObsOnizlemeDto.Hoca> sonuc = new ArrayList<>();
        for (Map.Entry<String, Integer> e : sayaclar.entrySet()) {
            MetinTemizleyici.Hoca h = kimlikler.get(e.getKey());
            sonuc.add(new ObsOnizlemeDto.Hoca(h.unvan(), h.ad(), e.getValue()));
        }
        return sonuc;
    }

    private static boolean anaGrupMu(ObsSatiri s) {
        return MetinTemizleyici.kod(s.grup()).equals(ANA_GRUP);
    }

    private static List<String> grupKodlari(String grup) {
        if (grup == null || grup.isBlank()) {
            return List.of();
        }
        List<String> kodlar = new ArrayList<>();
        for (String parca : grup.split(",")) {
            String kod = MetinTemizleyici.kod(parca);
            if (!kod.isEmpty()) {
                kodlar.add(kod);
            }
        }
        return kodlar;
    }
}