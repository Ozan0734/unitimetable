package com.unitimetable.demo.aktarim;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

class DerslikListesiOkuyucuTest {

    private static final String[] BASLIK = {"Yerleşke_218", "Bina_218", "Derslik Kodu_218",
            "Derslik Adı_218", "Derslik Tipi_218", "Kontenjanı_218", "Durumu_218"};

    private static byte[] dosya(String[] baslik, String[][] satirlar) throws IOException {
        try (XSSFWorkbook kitap = new XSSFWorkbook(); ByteArrayOutputStream cikis = new ByteArrayOutputStream()) {
            Sheet sayfa = kitap.createSheet("Page 1");
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

    private static List<DerslikSatiri> oku(byte[] icerik) {
        return DerslikListesiOkuyucu.oku(new ByteArrayInputStream(icerik));
    }

    @Test
    void satirlarOkunurBosAdliSatirAtlanir() throws IOException {
        String[][] veri = {
                {"Kestel Merkez Kampüs", " RAFET KAYIŞ MÜHENDİSLİK FAKÜLTESİ", "EA-K1-30", "MUHA-108", "Sınıf", "99", "1"},
                {"Kestel Merkez Kampüs", null, "EA-K1-31", "", "Sınıf", "50", "1"},
                {"Kestel Merkez Kampüs", null, "CS-Z-13", "KONFERANS  SALONU", "Salon", "80", "1"}
        };
        List<DerslikSatiri> sonuc = oku(dosya(BASLIK, veri));

        assertEquals(2, sonuc.size());
        assertEquals("MUHA-108", sonuc.get(0).ad());
        assertEquals("Sınıf", sonuc.get(0).tip());
        assertEquals("99", sonuc.get(0).kapasite());
        assertEquals("RAFET KAYIŞ MÜHENDİSLİK FAKÜLTESİ", sonuc.get(0).bina());
        assertEquals(2, sonuc.get(0).satir());
        assertEquals("KONFERANS SALONU", sonuc.get(1).ad());
        assertEquals("", sonuc.get(1).bina());
    }

    @Test
    void eksikSutunReddedilir() throws IOException {
        String[] baslik = {"Derslik Adı_218", "Derslik Tipi_218"};
        GecersizIslemException hata = assertThrows(GecersizIslemException.class,
                () -> oku(dosya(baslik, new String[][] {{"MUHA-108", "Sınıf"}})));
        assertTrue(hata.getMessage().contains("kontenjanı"), hata.getMessage());
    }

    @Test
    void excelOlmayanDosyaReddedilir() {
        byte[] sahte = "merhaba".getBytes(StandardCharsets.UTF_8);
        GecersizIslemException hata = assertThrows(GecersizIslemException.class, () -> oku(sahte));
        assertTrue(hata.getMessage().contains("okunamadı"), hata.getMessage());
    }
}
