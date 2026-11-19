package com.unitimetable.demo.ortakders;

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
@RequestMapping("/api/ortak-gruplar")
public class OrtakDersController {

    private final OrtakDersService service;

    public OrtakDersController(OrtakDersService service) {
        this.service = service;
    }

    @GetMapping("/adaylar")
    public List<OrtakDersDto.Aday> adaylar(@RequestParam Long projeId) {
        return service.adaylar(projeId);
    }

    @GetMapping
    public List<OrtakDersDto.Grup> listele(@RequestParam Long projeId) {
        return service.listele(projeId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrtakDersDto.Grup olustur(@Valid @RequestBody OrtakDersDto.Istek istek) {
        return service.olustur(istek);
    }

    @PostMapping("/{grupId}/onayla")
    public OrtakDersDto.Grup onayla(@PathVariable Long grupId) {
        return service.onayla(grupId);
    }

    @DeleteMapping("/{grupId}/uyeler/{subeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void ayril(@PathVariable Long grupId, @PathVariable Long subeId) {
        service.ayril(grupId, subeId);
    }
}
