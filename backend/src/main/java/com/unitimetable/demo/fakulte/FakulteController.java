package com.unitimetable.demo.fakulte;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/fakulteler")
public class FakulteController {

    private final FakulteService service;

    public FakulteController(FakulteService service) {
        this.service = service;
    }

    @GetMapping
    public List<FakulteDto.Yanit> listele() {
        return service.listele();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FakulteDto.Yanit olustur(@Valid @RequestBody FakulteDto.Istek istek) {
        return service.olustur(istek);
    }
}