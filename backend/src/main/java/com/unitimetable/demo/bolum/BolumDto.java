package com.unitimetable.demo.bolum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class BolumDto {

    private BolumDto() {
    }

    public record Istek(
            @NotNull(message = "Fakülte zorunludur") Long fakulteId,
            @NotBlank(message = "Bölüm adı boş olamaz") @Size(max = 200, message = "Bölüm adı en fazla 200 karakter olabilir") String ad) {
    }

    public record Guncelleme(
            @NotBlank(message = "Bölüm adı boş olamaz") @Size(max = 200, message = "Bölüm adı en fazla 200 karakter olabilir") String ad) {
    }

    public record Yanit(Long id, Long fakulteId, String ad) {
    }
}