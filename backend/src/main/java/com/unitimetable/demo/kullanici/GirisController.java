package com.unitimetable.demo.kullanici;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/giris")
public class GirisController {

    private final GirisService service;

    public GirisController(GirisService service) {
        this.service = service;
    }

    @PostMapping
    public KullaniciDto.Yanit giris(@Valid @RequestBody GirisDto.Istek istek) {
        return service.giris(istek);
    }
}