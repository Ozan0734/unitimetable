package com.unitimetable.demo.yerlesim;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UygunYerController {

    private final UygunYerService service;

    public UygunYerController(UygunYerService service) {
        this.service = service;
    }

    @GetMapping("/api/bloklar/{blokId}/uygun-yerler")
    public List<UygunYerDto.Yer> listele(@PathVariable Long blokId,
            @RequestParam(required = false) List<Long> derslikIdler,
            @RequestParam(required = false) UygunYerDto.Durum durum) {
        return service.listele(blokId, derslikIdler, durum);
    }
}