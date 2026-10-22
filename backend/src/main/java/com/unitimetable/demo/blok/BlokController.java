package com.unitimetable.demo.blok;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BlokController {

    private final BlokService service;

    public BlokController(BlokService service) {
        this.service = service;
    }

    @GetMapping("/api/bloklar")
    public List<BlokDto.Yanit> listele(
            @RequestParam(required = false) Long subeId,
            @RequestParam(required = false) Long dersId) {
        return service.listele(subeId, dersId);
    }

    @PostMapping("/api/dersler/{dersId}/bloklari-yenile")
    public List<BlokDto.Yanit> yenile(@PathVariable Long dersId) {
        return service.yenile(dersId);
    }
}