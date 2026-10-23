package com.unitimetable.demo.yerlesim;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
public class YerlesimController {

    private final YerlesimService service;

    public YerlesimController(YerlesimService service) {
        this.service = service;
    }

    @GetMapping("/api/yerlesimler")
    public List<YerlesimDto.Yanit> listele(
            @RequestParam(required = false) Long projeId,
            @RequestParam(required = false) Long subeId) {
        return service.listele(projeId, subeId);
    }

    @GetMapping("/api/yerlesmeyen-bloklar")
    public List<YerlesimDto.YerlesmeyenBlok> yerlesmeyenler(@RequestParam Long projeId) {
        return service.yerlesmeyenler(projeId);
    }

    @PutMapping("/api/bloklar/{blokId}/yerlesim")
    public YerlesimDto.Yanit yerlestir(@PathVariable Long blokId,
            @Valid @RequestBody YerlesimDto.Istek istek) {
        return service.yerlestir(blokId, istek);
    }

    @DeleteMapping("/api/bloklar/{blokId}/yerlesim")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void kaldir(@PathVariable Long blokId) {
        service.kaldir(blokId);
    }

    @PutMapping("/api/bloklar/{blokId}/yerlesim/kilit")
    public YerlesimDto.Yanit kilitle(@PathVariable Long blokId,
            @Valid @RequestBody YerlesimDto.KilitIstek istek) {
        return service.kilitle(blokId, istek.kilitli());
    }
}