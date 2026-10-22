package com.unitimetable.demo.ogretimelemani;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ogretim-elemanlari/{hocaId}/gun-tercihleri")
public class HocaGunTercihiController {

    private final HocaGunTercihiService service;

    public HocaGunTercihiController(HocaGunTercihiService service) {
        this.service = service;
    }

    @GetMapping
    public List<HocaGunTercihiDto.Yanit> listele(@PathVariable Long hocaId) {
        return service.listele(hocaId);
    }

    @PutMapping
    public List<HocaGunTercihiDto.Yanit> degistir(@PathVariable Long hocaId,
            @Valid @RequestBody HocaGunTercihiDto.Istek istek) {
        return service.degistir(hocaId, istek);
    }
}