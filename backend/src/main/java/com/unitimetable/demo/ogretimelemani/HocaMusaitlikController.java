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
@RequestMapping("/api/ogretim-elemanlari/{hocaId}/musaitlik")
public class HocaMusaitlikController {

    private final HocaMusaitlikService service;

    public HocaMusaitlikController(HocaMusaitlikService service) {
        this.service = service;
    }

    @GetMapping
    public List<HocaMusaitlikDto.Yanit> listele(@PathVariable Long hocaId) {
        return service.listele(hocaId);
    }

    @PutMapping
    public List<HocaMusaitlikDto.Yanit> degistir(@PathVariable Long hocaId,
            @Valid @RequestBody HocaMusaitlikDto.Istek istek) {
        return service.degistir(hocaId, istek);
    }
}