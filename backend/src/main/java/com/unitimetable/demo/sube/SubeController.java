package com.unitimetable.demo.sube;

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
@RequestMapping("/api/subeler")
public class SubeController {

    private final SubeService service;

    public SubeController(SubeService service) {
        this.service = service;
    }

    @GetMapping
    public List<SubeDto.Yanit> listele(
            @RequestParam(required = false) Long dersId,
            @RequestParam(required = false) Long ogretimElemaniId) {
        return service.listele(dersId, ogretimElemaniId);
    }

    @GetMapping("/{id}")
    public SubeDto.Yanit getir(@PathVariable Long id) {
        return service.getir(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SubeDto.Yanit olustur(@Valid @RequestBody SubeDto.Istek istek) {
        return service.olustur(istek);
    }

    @PutMapping("/{id}")
    public SubeDto.Yanit guncelle(@PathVariable Long id, @Valid @RequestBody SubeDto.Guncelleme istek) {
        return service.guncelle(id, istek);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sil(@PathVariable Long id) {
        service.sil(id);
    }
}