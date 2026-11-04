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

public final class DerslikListesiOkuyucu {

    private static final Locale TR = Locale.forLanguageTag("tr-TR");
    private static final int EN_FAZLA_SATIR = 2000;
    private static final List<String> ZORUNLU_SUTUNLAR = List.of("derslik adı", "derslik tipi", "kontenjanı");

    private DerslikListesiOkuyucu() {
    }

    public static List<DerslikSatiri> oku(InputStream akis) {
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
                        + ". Bu dosya derslik listesi biçiminde değil");
            }

            List<DerslikSatiri> sonuc = new ArrayList<>();
            for (int i = ilk + 1; i <= sayfa.getLastRowNum(); i++) {
                Row r = sayfa.getRow(i);
                if (r == null) {
                    continue;
                }
                String ad = deger(r, sutunlar, "derslik adı", bicimleyici);
                if (ad.isEmpty()) {
                    continue;
                }
                sonuc.add(new DerslikSatiri(i + 1,
                        deger(r, sutunlar, "derslik kodu", bicimleyici),
                        ad,
                        deger(r, sutunlar, "derslik tipi", bicimleyici),
                        deger(r, sutunlar, "kontenjanı", bicimleyici),
                        deger(r, sutunlar, "bina", bicimleyici),
                        deger(r, sutunlar, "durumu", bicimleyici)));
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
}