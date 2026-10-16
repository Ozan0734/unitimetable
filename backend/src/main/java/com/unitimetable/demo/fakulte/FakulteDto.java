package com.unitimetable.demo.fakulte;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class FakulteDto {

    private FakulteDto() {
    }

    public record Istek(
            @NotNull(message = "Kurum zorunludur") Long kurumId,
            @NotBlank(message = "Fakülte adı boş olamaz") @Size(max = 200, message = "Fakülte adı en fazla 200 karakter olabilir") String ad) {
    }

    public record Guncelleme(
            @NotBlank(message = "Fakülte adı boş olamaz") @Size(max = 200, message = "Fakülte adı en fazla 200 karakter olabilir") String ad) {
    }

    public record Yanit(Long id, Long kurumId, String ad) {
    }
}