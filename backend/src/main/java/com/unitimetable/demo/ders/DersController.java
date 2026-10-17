package com.unitimetable.demo.ders;

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
@RequestMapping("/api/dersler")
public class DersController {

    private final DersService service;

    public DersController(DersService service) {
        this.service = service;
    }

    @GetMapping
    public List<DersDto.Yanit> listele(@RequestParam(required = false) Long projeId) {
        return service.listele(projeId);
    }

    @GetMapping("/{id}")
    public DersDto.Yanit getir(@PathVariable Long id) {
        return service.getir(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DersDto.Yanit olustur(@Valid @RequestBody DersDto.Istek istek) {
        return service.olustur(istek);
    }

    @PutMapping("/{id}")
    public DersDto.Yanit guncelle(@PathVariable Long id, @Valid @RequestBody DersDto.Guncelleme istek) {
        return service.guncelle(id, istek);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sil(@PathVariable Long id) {
        service.sil(id);
    }
}