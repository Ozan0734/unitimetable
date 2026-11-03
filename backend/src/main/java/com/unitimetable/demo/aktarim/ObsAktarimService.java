package com.unitimetable.demo.aktarim;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.unitimetable.demo.ortak.GecersizIslemException;

@Service
public class ObsAktarimService {

    private static final Locale TR = Locale.forLanguageTag("tr-TR");

    public record Okunan(List<ObsSatiri> satirlar, String donem) {
    }

    public Okunan oku(MultipartFile dosya, String donem) {
        if (dosya == null || dosya.isEmpty()) {
            throw new GecersizIslemException("Excel dosyası seçilmedi (dosya alanı boş)");
        }
        String ad = dosya.getOriginalFilename() == null ? "" : dosya.getOriginalFilename().toLowerCase(Locale.ROOT);
        if (!ad.endsWith(".xlsx")) {
            throw new GecersizIslemException("Yalnızca .xlsx uzantılı Excel dosyaları yüklenebilir");
        }
        String donemKodu = donemKodu(donem);
        try (InputStream akis = dosya.getInputStream()) {
            return new Okunan(ObsDersListesiOkuyucu.oku(akis), donemKodu);
        } catch (IOException hata) {
            throw new GecersizIslemException("Dosya okunamadı");
        }
    }

    public ObsOnizlemeDto.Yanit onizle(MultipartFile dosya, String donem) {
        Okunan okunan = oku(dosya, donem);
        return ObsOnizleyici.hazirla(okunan.satirlar(), okunan.donem());
    }

    private static String donemKodu(String donem) {
        String temiz = donem == null ? "" : donem.trim().toUpperCase(TR);
        return switch (temiz) {
            case "GUZ", "GÜZ", "G" -> "G";
            case "BAHAR", "B" -> "B";
            default -> throw new GecersizIslemException("Dönem GUZ ya da BAHAR olmalıdır");
        };
    }
}