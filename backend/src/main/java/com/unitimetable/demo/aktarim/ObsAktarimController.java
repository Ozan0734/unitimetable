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
@RequestMapping("/api/ice-aktarma/obs-ders-listesi")
public class ObsAktarimController {

    private final ObsAktarimService service;
    private final ObsKayitService kayitService;

    public ObsAktarimController(ObsAktarimService service, ObsKayitService kayitService) {
        this.service = service;
        this.kayitService = kayitService;
    }

    @PostMapping(value = "/onizleme", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ObsOnizlemeDto.Yanit onizle(
            @RequestParam(value = "dosya", required = false) MultipartFile dosya,
            @RequestParam(value = "donem", required = false) String donem) {
        return service.onizle(dosya, donem);
    }

    @PostMapping(value = "/kaydet", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ObsKayitDto.Sonuc kaydet(
            @RequestParam(value = "dosya", required = false) MultipartFile dosya,
            @RequestParam(value = "donem", required = false) String donem,
            @RequestParam(value = "projeId", required = false) Long projeId,
            @RequestParam(value = "programId", required = false) Long programId,
            @RequestParam(value = "secmeliDersler", required = false) List<String> secmeliDersler,
            @RequestParam(value = "sanalOnekleri", required = false) List<String> sanalOnekleri,
            @RequestParam(value = "seminerKodlari", required = false) List<String> seminerKodlari) {
        return kayitService.kaydet(dosya, donem, projeId, programId, secmeliDersler, sanalOnekleri,
                seminerKodlari);
    }
}