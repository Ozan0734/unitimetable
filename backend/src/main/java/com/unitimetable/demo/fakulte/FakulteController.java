package com.unitimetable.demo.fakulte;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/fakulteler")
public class FakulteController {

    private final FakulteService service;

    public FakulteController(FakulteService service) {
        this.service = service;
    }

    @GetMapping
    public List<FakulteDto.Yanit> listele() {
        return service.listele();
    }

    @GetMapping("/{id}")
    public FakulteDto.Yanit getir(@PathVariable Long id) {
        return service.getir(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FakulteDto.Yanit olustur(@Valid @RequestBody FakulteDto.Istek istek) {
        return service.olustur(istek);
    }

    @PutMapping("/{id}")
    public FakulteDto.Yanit guncelle(@PathVariable Long id, @Valid @RequestBody FakulteDto.Guncelleme istek) {
        return service.guncelle(id, istek);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sil(@PathVariable Long id) {
        service.sil(id);
    }
}