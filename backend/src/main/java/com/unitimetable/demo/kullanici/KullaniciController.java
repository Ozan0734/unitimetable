package com.unitimetable.demo.kullanici;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/kullanicilar")
public class KullaniciController {

    private final KullaniciService service;

    public KullaniciController(KullaniciService service) {
        this.service = service;
    }

    @GetMapping
    public List<KullaniciDto.Yanit> listele(@RequestParam(required = false) Long kurumId) {
        return service.listele(kurumId);
    }

    @GetMapping("/{id}")
    public KullaniciDto.Yanit getir(@PathVariable Long id) {
        return service.getir(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public KullaniciDto.Yanit olustur(@Valid @RequestBody KullaniciDto.Istek istek) {
        return service.olustur(istek);
    }

    @PutMapping("/{id}")
    public KullaniciDto.Yanit guncelle(@PathVariable Long id, @Valid @RequestBody KullaniciDto.Guncelleme istek) {
        return service.guncelle(id, istek);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sil(@PathVariable Long id) {
        service.sil(id);
    }
}