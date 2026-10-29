package com.unitimetable.demo.planlama;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/program-projeleri/{projeId}")
public class OtomatikPlanlamaController {

    private final OtomatikPlanlamaService service;

    public OtomatikPlanlamaController(OtomatikPlanlamaService service) {
        this.service = service;
    }

    @PostMapping("/otomatik-planla")
    public PlanlamaDto.Sonuc planla(@PathVariable Long projeId,
            @Valid @RequestBody(required = false) PlanlamaDto.Istek istek) {
        return service.planla(projeId, istek);
    }

    @DeleteMapping("/yerlesimler")
    public PlanlamaDto.TemizleSonuc temizle(@PathVariable Long projeId) {
        return service.temizle(projeId);
    }
}