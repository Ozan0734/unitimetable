package com.unitimetable.demo.aktarim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import com.unitimetable.demo.ortak.GecersizIslemException;

class ObsDersListesiOkuyucuTest {

    private static final String[] BASLIK = {
            "Ders Kodu_218", "Ders Adı_218", "Z_218", "Krd_218", "AKTS_218", "Teo_218", "Uyg_218",
            "Lab_218", "Snf_218", "D_218", "KT_218", "Öğretim Elemanı_218", "İntibak_218",
            "Ön Koşul_218", "Grup_218"
    };

    private static byte[] dosya(String[] baslik, String[][] satirlar) throws IOException {
        try (XSSFWorkbook kitap = new XSSFWorkbook(); ByteArrayOutputStream cikis = new ByteArrayOutputStream()) {
            Sheet sayfa = kitap.createSheet("Sayfa1");
            Row b = sayfa.createRow(0);
            for (int i = 0; i < baslik.length; i++) {
                b.createCell(i).setCellValue(baslik[i]);
            }
            for (int r = 0; r < satirlar.length; r++) {
                Row satir = sayfa.createRow(r + 1);
                for (int c = 0; c < satirlar[r].length; c++) {
                    if (satirlar[r][c] != null) {
                        satir.createCell(c).setCellValue(satirlar[r][c]);
                    }
                }
            }
            kitap.write(cikis);
            return cikis.toByteArray();
        }
    }

    private static List<ObsSatiri> oku(byte[] icerik) {
        return ObsDersListesiOkuyucu.oku(new ByteArrayInputStream(icerik));
    }

    @Test
    void satirlarOkunurVeTemizlenir() throws IOException {
        String[][] veri = {
                {"BSM  105", "PHYSICS", "Z", "4", "5", "3", "0", "1", "1", "G", null,
                        "Doç. Dr. MUSTAFA TOKAÇ", null, null, null},
                {"SEC 101", "NON-TECHNICAL ELECTIVE", "S", "3", "3", "3", "0", "0", "1", "G", null,
                        null, null, null, "[Ana Grup]"},
                {"SEC 101.1", "ECONOMICS", "S", "3", "3", "3", "0", "0", "1", "G", null,
                        "Dr. Öğr. Üyesi KÜBRA UYAR", null, null, "SEC 101"},
                {"COM 301", "ALGORITHMS", "Z", "3", "5", "3", "0", "0", "3", "B", null,
                        null, null, null, "1. YY. ITB"}
        };
        List<ObsSatiri> sonuc = oku(dosya(BASLIK, veri));

        assertEquals(4, sonuc.size());
        ObsSatiri ilk = sonuc.get(0);
        assertEquals("BSM 105", ilk.kod());
        assertEquals(2, ilk.satir());
        assertTrue(ilk.zorunlu());
        assertEquals(3, ilk.teo());
        assertEquals(1, ilk.lab());
        assertEquals(1, ilk.sinif());
        assertEquals("G", ilk.donem());
        assertEquals("Doç. Dr. MUSTAFA TOKAÇ", ilk.hocaMetni());
        assertFalse(sonuc.get(1).zorunlu());
        assertEquals("[Ana Grup]", sonuc.get(1).grup());
        assertEquals("SEC 101", sonuc.get(2).grup());
        assertEquals("B", sonuc.get(3).donem());
        assertEquals("", sonuc.get(3).hocaMetni());
    }

    @Test
    void eksikSutunReddedilir() throws IOException {
        String[] baslik = {"Ders Kodu_218", "Ders Adı_218", "Z_218", "Uyg_218", "Lab_218", "Snf_218", "D_218"};
        GecersizIslemException hata = assertThrows(GecersizIslemException.class,
                () -> oku(dosya(baslik, new String[][] {{"COM 101", "MATH", "Z", "0", "0", "1", "G"}})));
        assertTrue(hata.getMessage().contains("teo"), hata.getMessage());
    }

    @Test
    void sayiOlmayanSaatReddedilir() throws IOException {
        String[][] veri = {{"COM 101", "MATH", "Z", "4", "5", "abc", "0", "0", "1", "G", null,
                null, null, null, null}};
        GecersizIslemException hata = assertThrows(GecersizIslemException.class,
                () -> oku(dosya(BASLIK, veri)));
        assertTrue(hata.getMessage().contains("Satır 2"), hata.getMessage());
        assertTrue(hata.getMessage().contains("Teo"), hata.getMessage());
    }

    @Test
    void gecersizZorunluDegeriReddedilir() throws IOException {
        String[][] veri = {{"COM 101", "MATH", "X", "4", "5", "3", "0", "0", "1", "G", null,
                null, null, null, null}};
        GecersizIslemException hata = assertThrows(GecersizIslemException.class,
                () -> oku(dosya(BASLIK, veri)));
        assertTrue(hata.getMessage().contains("Satır 2"), hata.getMessage());
    }

    @Test
    void excelOlmayanDosyaReddedilir() {
        byte[] sahte = "merhaba".getBytes(StandardCharsets.UTF_8);
        GecersizIslemException hata = assertThrows(GecersizIslemException.class, () -> oku(sahte));
        assertTrue(hata.getMessage().contains("okunamadı"), hata.getMessage());
    }
}
