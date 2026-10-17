package com.unitimetable.demo.ders;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ders-sinif")
public class DersSinifController {

    private final DersSinifService service;

    public DersSinifController(DersSinifService service) {
        this.service = service;
    }

    @GetMapping
    public List<DersSinifDto.Yanit> listele(
            @RequestParam(required = false) Long dersId,
            @RequestParam(required = false) Long sinifId) {
        return service.listele(dersId, sinifId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DersSinifDto.Yanit olustur(@Valid @RequestBody DersSinifDto.Istek istek) {
        return service.olustur(istek);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sil(@PathVariable Long id) {
        service.sil(id);
    }
}