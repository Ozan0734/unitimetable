package com.unitimetable.demo.aktarim;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ice-aktarma/derslik-listesi")
public class DerslikAktarimController {

    private final DerslikAktarimService service;

    public DerslikAktarimController(DerslikAktarimService service) {
        this.service = service;
    }

    @PostMapping(value = "/onizleme", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DerslikAktarimDto.Yanit onizle(
            @RequestParam(value = "dosya", required = false) MultipartFile dosya,
            @RequestParam(value = "duzeltmeler", required = false) List<String> duzeltmeler) {
        return service.onizle(dosya, duzeltmeler);
    }

    @PostMapping(value = "/kaydet", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public DerslikAktarimDto.Sonuc kaydet(
            @RequestParam(value = "dosya", required = false) MultipartFile dosya,
            @RequestParam(value = "duzeltmeler", required = false) List<String> duzeltmeler) {
        return service.kaydet(dosya, duzeltmeler);
    }
}