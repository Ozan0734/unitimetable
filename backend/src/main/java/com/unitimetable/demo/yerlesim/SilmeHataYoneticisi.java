package com.unitimetable.demo.yerlesim;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class SilmeHataYoneticisi {

    public record SilmeYaniti(String mesaj, List<DerslikKullanimService.Kullanim> bloklar) {
    }

    @ExceptionHandler(SilinemezException.class)
    public ResponseEntity<SilmeYaniti> silinemez(SilinemezException hata) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new SilmeYaniti(hata.getMessage(), hata.getBloklar()));
    }
}