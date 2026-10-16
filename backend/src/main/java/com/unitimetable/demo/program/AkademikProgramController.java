package com.unitimetable.demo.program;

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
@RequestMapping("/api/programlar")
public class AkademikProgramController {

    private final AkademikProgramService service;

    public AkademikProgramController(AkademikProgramService service) {
        this.service = service;
    }

    @GetMapping
    public List<AkademikProgramDto.Yanit> listele(@RequestParam(required = false) Long bolumId) {
        return service.listele(bolumId);
    }

    @GetMapping("/{id}")
    public AkademikProgramDto.Yanit getir(@PathVariable Long id) {
        return service.getir(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AkademikProgramDto.Yanit olustur(@Valid @RequestBody AkademikProgramDto.Istek istek) {
        return service.olustur(istek);
    }

    @PutMapping("/{id}")
    public AkademikProgramDto.Yanit guncelle(@PathVariable Long id,
            @Valid @RequestBody AkademikProgramDto.Guncelleme istek) {
        return service.guncelle(id, istek);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sil(@PathVariable Long id) {
        service.sil(id);
    }
}