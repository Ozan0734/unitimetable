package com.unitimetable.demo.aktarim;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ice-aktarma/obs-ders-listesi")
public class ObsAktarimController {

    private final ObsAktarimService service;

    public ObsAktarimController(ObsAktarimService service) {
        this.service = service;
    }

    @PostMapping(value = "/onizleme", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ObsOnizlemeDto.Yanit onizle(
            @RequestParam(value = "dosya", required = false) MultipartFile dosya,
            @RequestParam(value = "donem", required = false) String donem) {
        return service.onizle(dosya, donem);
    }
}