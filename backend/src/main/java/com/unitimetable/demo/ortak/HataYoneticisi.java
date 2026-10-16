package com.unitimetable.demo.ortak;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class HataYoneticisi {

    public record HataYaniti(String mesaj, Map<String, String> alanlar) {
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<HataYaniti> dogrulamaHatasi(MethodArgumentNotValidException hata) {
        Map<String, String> alanlar = new LinkedHashMap<>();
        hata.getBindingResult().getFieldErrors()
                .forEach(f -> alanlar.putIfAbsent(f.getField(), f.getDefaultMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new HataYaniti("Gönderilen bilgilerde hata var", alanlar));
    }
}