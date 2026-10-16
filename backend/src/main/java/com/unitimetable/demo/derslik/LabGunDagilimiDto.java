package com.unitimetable.demo.derslik;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public final class LabGunDagilimiDto {

    private LabGunDagilimiDto() {
    }

    public record Istek(
            @NotNull(message = "Laboratuvar zorunludur") Long derslikId,
            @NotNull(message = "Bölüm zorunludur") Long bolumId,
            @NotNull(message = "Gün zorunludur") @Min(value = 1, message = "Gün en az 1 olmalıdır") @Max(value = 7, message = "Gün en fazla 7 olabilir") Integer gun) {
    }

    public record Guncelleme(
            @NotNull(message = "Bölüm zorunludur") Long bolumId) {
    }

    public record Yanit(Long id, Long derslikId, Long bolumId, int gun) {
    }
}