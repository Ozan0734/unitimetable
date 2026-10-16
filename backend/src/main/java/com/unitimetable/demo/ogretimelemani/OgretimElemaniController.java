package com.unitimetable.demo.ogretimelemani;

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
@RequestMapping("/api/ogretim-elemanlari")
public class OgretimElemaniController {

    private final OgretimElemaniService service;

    public OgretimElemaniController(OgretimElemaniService service) {
        this.service = service;
    }

    @GetMapping
    public List<OgretimElemaniDto.Yanit> listele(@RequestParam(required = false) Long bolumId) {
        return service.listele(bolumId);
    }

    @GetMapping("/{id}")
    public OgretimElemaniDto.Yanit getir(@PathVariable Long id) {
        return service.getir(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OgretimElemaniDto.Yanit olustur(@Valid @RequestBody OgretimElemaniDto.Istek istek) {
        return service.olustur(istek);
    }

    @PutMapping("/{id}")
    public OgretimElemaniDto.Yanit guncelle(@PathVariable Long id,
            @Valid @RequestBody OgretimElemaniDto.Guncelleme istek) {
        return service.guncelle(id, istek);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sil(@PathVariable Long id) {
        service.sil(id);
    }
}