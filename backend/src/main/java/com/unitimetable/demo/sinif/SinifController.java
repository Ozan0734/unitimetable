package com.unitimetable.demo.sinif;

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
@RequestMapping("/api/siniflar")
public class SinifController {

    private final SinifService service;

    public SinifController(SinifService service) {
        this.service = service;
    }

    @GetMapping
    public List<SinifDto.Yanit> listele(@RequestParam(required = false) Long programId) {
        return service.listele(programId);
    }

    @GetMapping("/{id}")
    public SinifDto.Yanit getir(@PathVariable Long id) {
        return service.getir(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SinifDto.Yanit olustur(@Valid @RequestBody SinifDto.Istek istek) {
        return service.olustur(istek);
    }

    @PutMapping("/{id}")
    public SinifDto.Yanit guncelle(@PathVariable Long id, @Valid @RequestBody SinifDto.Guncelleme istek) {
        return service.guncelle(id, istek);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sil(@PathVariable Long id) {
        service.sil(id);
    }
}