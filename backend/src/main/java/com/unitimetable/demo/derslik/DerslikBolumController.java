package com.unitimetable.demo.derslik;

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
@RequestMapping("/api/derslik-bolum")
public class DerslikBolumController {

    private final DerslikBolumService service;

    public DerslikBolumController(DerslikBolumService service) {
        this.service = service;
    }

    @GetMapping
    public List<DerslikBolumDto.Yanit> listele(
            @RequestParam(required = false) Long derslikId,
            @RequestParam(required = false) Long bolumId) {
        return service.listele(derslikId, bolumId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DerslikBolumDto.Yanit olustur(@Valid @RequestBody DerslikBolumDto.Istek istek) {
        return service.olustur(istek);
    }

    @PutMapping("/{id}")
    public DerslikBolumDto.Yanit guncelle(@PathVariable Long id,
            @Valid @RequestBody DerslikBolumDto.Guncelleme istek) {
        return service.guncelle(id, istek);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sil(@PathVariable Long id) {
        service.sil(id);
    }
}