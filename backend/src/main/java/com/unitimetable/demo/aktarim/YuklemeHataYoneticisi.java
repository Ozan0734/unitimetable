package com.unitimetable.demo.aktarim;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import com.unitimetable.demo.ortak.HataYoneticisi;

@RestControllerAdvice
public class YuklemeHataYoneticisi {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<HataYoneticisi.HataYaniti> cokBuyuk(MaxUploadSizeExceededException hata) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(new HataYoneticisi.HataYaniti("Dosya çok büyük: en fazla 5 MB yüklenebilir", Map.of()));
    }
}