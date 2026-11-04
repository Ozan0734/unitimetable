package com.unitimetable.demo.aktarim;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.unitimetable.demo.derslik.DerslikTuru;
import com.unitimetable.demo.ortak.GecersizIslemException;

public final class DerslikKararlari {

    private static final Locale TR = Locale.forLanguageTag("tr-TR");
    private static final int EN_FAZLA_KAPASITE = 2000;

    public enum Durum {
        EKLENECEK,
        ZATEN_VAR,
        PASIF,
        GECERSIZ
    }

    public record Karar(int satir, String kod, String ad, DerslikTuru tur, Integer kapasite,
            Integer dosyadakiKapasite, boolean duzeltildi, Durum durum, String sebep) {
    }

    private DerslikKararlari() {
    }

    /** "Sınıf", "SINIF", "LAB.", "Laboratuvar" gibi yazımları tek türe çevirir. Tanınmazsa null. */
    public static DerslikTuru tip(String ham) {
        String temiz = MetinTemizleyici.metin(ham).toLowerCase(TR);
        while (temiz.endsWith(".")) {
            temiz = temiz.substring(0, temiz.length() - 1);
        }
        return switch (temiz) {
            case "sınıf", "sinif" -> DerslikTuru.SINIF;
            case "lab", "laboratuvar", "laboratuar" -> DerslikTuru.LABORATUVAR;
            case "amfi" -> DerslikTuru.AMFI;
            case "sanal" -> DerslikTuru.SANAL;
            case "salon" -> DerslikTuru.SALON;
            default -> null;
        };
    }

    /** "MUHA-103=100" biçimindeki düzeltmeleri okur. */
    public static Map<String, Integer> duzeltmeler(List<String> ham) {
        Map<String, Integer> sonuc = new LinkedHashMap<>();
        if (ham == null) {
            return sonuc;
        }
        for (String parca : ham) {
            if (parca == null || parca.isBlank()) {
                continue;
            }
            int indeks = parca.lastIndexOf('=');
            if (indeks <= 0) {
                throw new GecersizIslemException(
                        "Düzeltme 'DERSLİK ADI=KONTENJAN' biçiminde olmalı (bulunan: '" + parca + "')");
            }
            String ad = MetinTemizleyici.kod(parca.substring(0, indeks));
            Integer kapasite = sayi(parca.substring(indeks + 1));
            if (ad.isEmpty() || kapasite == null || kapasite < 1 || kapasite > EN_FAZLA_KAPASITE) {
                throw new GecersizIslemException("Düzeltme geçersiz: '" + parca
                        + "' (kontenjan 1 ile " + EN_FAZLA_KAPASITE + " arasında tam sayı olmalı)");
            }
            sonuc.put(ad, kapasite);
        }
        return sonuc;
    }

    public static List<Karar> hazirla(List<DerslikSatiri> satirlar, Set<String> mevcutAdlar,
            Map<String, Integer> duzeltmeler) {
        List<Karar> sonuc = new ArrayList<>();
        Set<String> gorulenler = new HashSet<>();
        for (DerslikSatiri s : satirlar) {
            String anahtar = MetinTemizleyici.kod(s.ad());
            DerslikTuru tur = tip(s.tip());
            Integer dosyadaki = sayi(s.kapasite());
            boolean duzeltmeVar = duzeltmeler.containsKey(anahtar);
            Integer kapasite = duzeltmeVar ? duzeltmeler.get(anahtar) : dosyadaki;
            boolean duzeltildi = duzeltmeVar && !Objects.equals(dosyadaki, kapasite);

            Durum durum;
            String sebep = null;
            if ("0".equals(s.durum())) {
                durum = Durum.PASIF;
                sebep = "Derslik pasif (Durumu = 0)";
            } else if (s.ad().length() > 100) {
                durum = Durum.GECERSIZ;
                sebep = "Derslik adı 100 karakterden uzun";
            } else if (tur == null) {
                durum = Durum.GECERSIZ;
                sebep = "Derslik tipi tanınmadı: '" + s.tip() + "'";
            } else if (kapasite == null || kapasite < 1 || kapasite > EN_FAZLA_KAPASITE) {
                durum = Durum.GECERSIZ;
                sebep = "Kontenjan 1 ile " + EN_FAZLA_KAPASITE + " arasında tam sayı olmalı (bulunan: '"
                        + s.kapasite() + "')";
            } else if (!gorulenler.add(anahtar)) {
                durum = Durum.GECERSIZ;
                sebep = "Dosyada aynı derslik adı birden fazla kez geçiyor, ilki alındı";
            } else if (mevcutAdlar.contains(anahtar)) {
                durum = Durum.ZATEN_VAR;
                sebep = "Bu adlı derslik kurumda zaten var";
            } else {
                durum = Durum.EKLENECEK;
            }
            sonuc.add(new Karar(s.satir(), s.kod(), s.ad(), tur, kapasite, dosyadaki, duzeltildi, durum, sebep));
        }
        return sonuc;
    }

    /** Düzeltme listesinde olup dosyada bulunmayan derslik adları. */
    public static List<String> bilinmeyenDuzeltmeler(List<DerslikSatiri> satirlar,
            Map<String, Integer> duzeltmeler) {
        Set<String> dosyadakiler = new HashSet<>();
        for (DerslikSatiri s : satirlar) {
            dosyadakiler.add(MetinTemizleyici.kod(s.ad()));
        }
        return duzeltmeler.keySet().stream().filter(ad -> !dosyadakiler.contains(ad)).toList();
    }

    static Integer sayi(String ham) {
        String temiz = MetinTemizleyici.metin(ham).replace(',', '.');
        if (temiz.isEmpty()) {
            return null;
        }
        try {
            double deger = Double.parseDouble(temiz);
            if (deger != Math.rint(deger) || deger < Integer.MIN_VALUE || deger > Integer.MAX_VALUE) {
                return null;
            }
            return (int) deger;
        } catch (NumberFormatException hata) {
            return null;
        }
    }
}