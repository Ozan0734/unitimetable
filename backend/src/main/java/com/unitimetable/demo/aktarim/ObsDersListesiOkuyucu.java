package com.unitimetable.demo.aktarim;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.unitimetable.demo.ortak.GecersizIslemException;

public final class ObsDersListesiOkuyucu {

    private static final Locale TR = Locale.forLanguageTag("tr-TR");
    private static final int EN_FAZLA_SATIR = 5000;
    private static final List<String> ZORUNLU_SUTUNLAR = List.of(
            "ders kodu", "ders adı", "z", "teo", "uyg", "lab", "snf", "d");

    private ObsDersListesiOkuyucu() {
    }

    public static List<ObsSatiri> oku(InputStream akis) {
        try (XSSFWorkbook kitap = new XSSFWorkbook(akis)) {
            if (kitap.getNumberOfSheets() == 0) {
                throw new GecersizIslemException("Dosyada hiç sayfa yok");
            }
            Sheet sayfa = kitap.getSheetAt(0);
            int ilk = sayfa.getFirstRowNum();
            Row baslik = ilk < 0 ? null : sayfa.getRow(ilk);
            if (baslik == null) {
                throw new GecersizIslemException("Dosya boş");
            }
            if (sayfa.getLastRowNum() - ilk > EN_FAZLA_SATIR) {
                throw new GecersizIslemException("Dosyada en fazla " + EN_FAZLA_SATIR + " satır olabilir");
            }

            DataFormatter bicimleyici = new DataFormatter(TR);
            Map<String, Integer> sutunlar = new HashMap<>();
            for (Cell hucre : baslik) {
                String ad = MetinTemizleyici.baslik(bicimleyici.formatCellValue(hucre));
                if (!ad.isEmpty()) {
                    sutunlar.putIfAbsent(ad, hucre.getColumnIndex());
                }
            }
            List<String> eksikler = ZORUNLU_SUTUNLAR.stream().filter(s -> !sutunlar.containsKey(s)).toList();
            if (!eksikler.isEmpty()) {
                throw new GecersizIslemException("Dosyada şu sütunlar bulunamadı: " + String.join(", ", eksikler)
                        + ". Bu dosya OBS dönem ders listesi biçiminde değil");
            }

            List<ObsSatiri> sonuc = new ArrayList<>();
            for (int i = ilk + 1; i <= sayfa.getLastRowNum(); i++) {
                Row r = sayfa.getRow(i);
                if (r == null) {
                    continue;
                }
                int satirNo = i + 1;
                String kod = MetinTemizleyici.kod(deger(r, sutunlar, "ders kodu", bicimleyici));
                if (kod.isEmpty()) {
                    continue;
                }
                String z = deger(r, sutunlar, "z", bicimleyici).toUpperCase(Locale.ROOT);
                boolean zorunlu;
                if (z.equals("Z")) {
                    zorunlu = true;
                } else if (z.equals("S")) {
                    zorunlu = false;
                } else {
                    throw new GecersizIslemException("Satır " + satirNo
                            + ": 'Z' sütunu Z (zorunlu) ya da S (seçmeli) olmalı (bulunan: '" + z + "')");
                }
                sonuc.add(new ObsSatiri(
                        satirNo,
                        kod,
                        deger(r, sutunlar, "ders adı", bicimleyici),
                        zorunlu,
                        deger(r, sutunlar, "krd", bicimleyici),
                        deger(r, sutunlar, "akts", bicimleyici),
                        sayi(deger(r, sutunlar, "teo", bicimleyici), "Teo", satirNo),
                        sayi(deger(r, sutunlar, "uyg", bicimleyici), "Uyg", satirNo),
                        sayi(deger(r, sutunlar, "lab", bicimleyici), "Lab", satirNo),
                        sayi(deger(r, sutunlar, "snf", bicimleyici), "Snf", satirNo),
                        deger(r, sutunlar, "d", bicimleyici).toUpperCase(Locale.ROOT),
                        deger(r, sutunlar, "öğretim elemanı", bicimleyici),
                        deger(r, sutunlar, "intibak", bicimleyici),
                        deger(r, sutunlar, "ön koşul", bicimleyici),
                        deger(r, sutunlar, "grup", bicimleyici)));
            }
            return sonuc;
        } catch (GecersizIslemException hata) {
            throw hata;
        } catch (IOException | RuntimeException hata) {
            throw new GecersizIslemException("Dosya okunamadı: geçerli bir .xlsx (Excel) dosyası yükleyin");
        }
    }

    private static String deger(Row satir, Map<String, Integer> sutunlar, String sutun,
            DataFormatter bicimleyici) {
        Integer indeks = sutunlar.get(sutun);
        if (indeks == null) {
            return "";
        }
        Cell hucre = satir.getCell(indeks);
        if (hucre == null) {
            return "";
        }
        return MetinTemizleyici.metin(bicimleyici.formatCellValue(hucre));
    }

    private static int sayi(String deger, String sutun, int satirNo) {
        if (deger.isEmpty()) {
            return 0;
        }
        double sayi;
        try {
            sayi = Double.parseDouble(deger.replace(',', '.'));
        } catch (NumberFormatException hata) {
            throw hatali(sutun, satirNo, deger);
        }
        if (sayi < 0 || sayi > 1000 || sayi != Math.rint(sayi)) {
            throw hatali(sutun, satirNo, deger);
        }
        return (int) sayi;
    }

    private static GecersizIslemException hatali(String sutun, int satirNo, String deger) {
        return new GecersizIslemException("Satır " + satirNo + ": '" + sutun
                + "' sütunu 0 ile 1000 arasında tam sayı olmalı (bulunan: '" + deger + "')");
    }
}