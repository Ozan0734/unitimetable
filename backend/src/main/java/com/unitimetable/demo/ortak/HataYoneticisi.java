package com.unitimetable.demo.ortak;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
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

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<HataYaniti> okunamayanIstek(HttpMessageNotReadableException hata) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new HataYaniti(
                        "İstek okunamadı. JSON biçimini ve değerleri kontrol edin "
                                + "(seviye: LISANS, YUKSEK_LISANS, DOKTORA; dil: TR, EN)",
                        Map.of()));
    }

    @ExceptionHandler(KayitBulunamadiException.class)
    public ResponseEntity<HataYaniti> kayitBulunamadi(KayitBulunamadiException hata) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new HataYaniti(hata.getMessage(), Map.of()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<HataYaniti> iliskiHatasi(DataIntegrityViolationException hata) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new HataYaniti(
                        "İşlem yapılamadı: kayıt başka kayıtlarla ilişkili ya da bağlı olduğu kayıt bulunamadı",
                        Map.of()));
    }
}