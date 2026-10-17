package com.unitimetable.demo.ders;

import jakarta.validation.constraints.NotNull;

public final class DersSinifDto {

    private DersSinifDto() {
    }

    public record Istek(
            @NotNull(message = "Ders zorunludur") Long dersId,
            @NotNull(message = "Sınıf zorunludur") Long sinifId) {
    }

    public record Yanit(Long id, Long dersId, Long sinifId) {
    }
}