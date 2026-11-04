package com.unitimetable.demo.aktarim;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.unitimetable.demo.derslik.Derslik;
import com.unitimetable.demo.derslik.DerslikRepository;
import com.unitimetable.demo.ortak.GecersizIslemException;
import com.unitimetable.demo.ortak.YetkiKontrol;

@Service
public class DerslikAktarimService {

    private record Hazir(Long kurumId, List<DerslikKararlari.Karar> kararlar, List<String> uyarilar) {
    }

    private final DerslikRepository derslikRepository;
    private final YetkiKontrol yetki;

    public DerslikAktarimService(DerslikRepository derslikRepository, YetkiKontrol yetki) {
        this.derslikRepository = derslikRepository;
        this.yetki = yetki;
    }

    @Transactional(readOnly = true)
    public DerslikAktarimDto.Yanit onizle(MultipartFile dosya, List<String> duzeltmeler) {
        Hazir hazir = hazirla(dosya, duzeltmeler);
        List<DerslikKararlari.Karar> kararlar = hazir.kararlar();
        return new DerslikAktarimDto.Yanit(kararlar.size(),
                say(kararlar, DerslikKararlari.Durum.EKLENECEK),
                say(kararlar, DerslikKararlari.Durum.ZATEN_VAR),
                say(kararlar, DerslikKararlari.Durum.PASIF),
                say(kararlar, DerslikKararlari.Durum.GECERSIZ),
                turler(kararlar), kararlar, hazir.uyarilar());
    }

    @Transactional
    public DerslikAktarimDto.Sonuc kaydet(MultipartFile dosya, List<String> duzeltmeler) {
        Hazir hazir = hazirla(dosya, duzeltmeler);
        List<DerslikKararlari.Karar> eklenecekler = hazir.kararlar().stream()
                .filter(k -> k.durum() == DerslikKararlari.Durum.EKLENECEK)
                .toList();
        for (DerslikKararlari.Karar k : eklenecekler) {
            derslikRepository.save(new Derslik(hazir.kurumId(), k.ad(), k.kapasite(), k.tur()));
        }
        derslikRepository.flush();
        List<DerslikKararlari.Karar> atlananlar = hazir.kararlar().stream()
                .filter(k -> k.durum() != DerslikKararlari.Durum.EKLENECEK)
                .toList();
        return new DerslikAktarimDto.Sonuc(eklenecekler.size(), turler(eklenecekler), atlananlar,
                hazir.uyarilar());
    }

    private Hazir hazirla(MultipartFile dosya, List<String> duzeltmeler) {
        if (dosya == null || dosya.isEmpty()) {
            throw new GecersizIslemException("Excel dosyası seçilmedi (dosya alanı boş)");
        }
        String ad = dosya.getOriginalFilename() == null ? "" : dosya.getOriginalFilename().toLowerCase(Locale.ROOT);
        if (!ad.endsWith(".xlsx")) {
            throw new GecersizIslemException("Yalnızca .xlsx uzantılı Excel dosyaları yüklenebilir");
        }
        Map<String, Integer> duzeltme = DerslikKararlari.duzeltmeler(duzeltmeler);

        List<DerslikSatiri> satirlar;
        try (InputStream akis = dosya.getInputStream()) {
            satirlar = DerslikListesiOkuyucu.oku(akis);
        } catch (IOException hata) {
            throw new GecersizIslemException("Dosya okunamadı");
        }

        Long kurumId = yetki.kullanici().kurumId();
        Set<String> mevcutAdlar = derslikRepository.findByKurumIdOrderByAd(kurumId).stream()
                .map(d -> MetinTemizleyici.kod(d.getAd()))
                .collect(Collectors.toSet());
        List<DerslikKararlari.Karar> kararlar = DerslikKararlari.hazirla(satirlar, mevcutAdlar, duzeltme);
        List<String> uyarilar = DerslikKararlari.bilinmeyenDuzeltmeler(satirlar, duzeltme).stream()
                .map(a -> "Düzeltme listesinde olup dosyada bulunmayan derslik: " + a)
                .toList();
        return new Hazir(kurumId, kararlar, uyarilar);
    }

    private static int say(List<DerslikKararlari.Karar> kararlar, DerslikKararlari.Durum durum) {
        return (int) kararlar.stream().filter(k -> k.durum() == durum).count();
    }

    private static Map<String, Integer> turler(List<DerslikKararlari.Karar> kararlar) {
        return kararlar.stream()
                .filter(k -> k.durum() == DerslikKararlari.Durum.EKLENECEK)
                .collect(Collectors.groupingBy(k -> k.tur().name(), TreeMap::new,
                        Collectors.summingInt(k -> 1)));
    }
}