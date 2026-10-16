package com.unitimetable.demo.program;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class AkademikProgramDto {

    private AkademikProgramDto() {
    }

    public record Istek(
            @NotNull(message = "Bölüm zorunludur") Long bolumId,
            @NotBlank(message = "Program adı boş olamaz") @Size(max = 200, message = "Program adı en fazla 200 karakter olabilir") String ad,
            @NotNull(message = "Seviye zorunludur") Seviye seviye,
            @NotNull(message = "Dil zorunludur") Dil dil) {
    }

    public record Guncelleme(
            @NotBlank(message = "Program adı boş olamaz") @Size(max = 200, message = "Program adı en fazla 200 karakter olabilir") String ad,
            @NotNull(message = "Seviye zorunludur") Seviye seviye,
            @NotNull(message = "Dil zorunludur") Dil dil) {
    }

    public record Yanit(Long id, Long bolumId, String ad, Seviye seviye, Dil dil) {
    }
}