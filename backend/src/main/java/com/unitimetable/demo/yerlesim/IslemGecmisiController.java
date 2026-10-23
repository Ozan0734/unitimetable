package com.unitimetable.demo.yerlesim;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/program-projeleri/{projeId}")
public class IslemGecmisiController {

    private final IslemGecmisiService service;

    public IslemGecmisiController(IslemGecmisiService service) {
        this.service = service;
    }

    @GetMapping("/gecmis")
    public List<IslemGecmisiDto.Yanit> listele(@PathVariable Long projeId) {
        return service.listele(projeId);
    }

    @PostMapping("/geri-al")
    public IslemGecmisiDto.Yanit geriAl(@PathVariable Long projeId) {
        return service.geriAl(projeId);
    }

    @PostMapping("/yinele")
    public IslemGecmisiDto.Yanit yinele(@PathVariable Long projeId) {
        return service.yinele(projeId);
    }
}