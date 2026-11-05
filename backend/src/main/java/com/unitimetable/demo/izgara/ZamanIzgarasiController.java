package com.unitimetable.demo.izgara;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unitimetable.demo.ortak.KayitBulunamadiException;
import com.unitimetable.demo.ortak.YetkiKontrol;

@RestController
@RequestMapping("/api/zaman-izgarasi")
public class ZamanIzgarasiController {

    public record Yanit(int gunSayisi, int gunlukSlotSayisi, int dersSuresiDk, int araSuresiDk,
            String baslangicSaati) {
    }

    private final ZamanIzgarasiRepository repository;
    private final YetkiKontrol yetki;

    public ZamanIzgarasiController(ZamanIzgarasiRepository repository, YetkiKontrol yetki) {
        this.repository = repository;
        this.yetki = yetki;
    }

    @GetMapping
    public Yanit getir() {
        Long kurumId = yetki.kullanici().kurumId();
        ZamanIzgarasi izgara = repository.findByKurumId(kurumId)
                .orElseThrow(() -> new KayitBulunamadiException("Kurumun zaman ızgarası tanımlı değil"));
        return new Yanit(izgara.getGunSayisi(), izgara.getGunlukSlotSayisi(), izgara.getDersSuresiDk(),
                izgara.getAraSuresiDk(), izgara.getBaslangicSaati().toString());
    }
}