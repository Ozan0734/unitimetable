package com.unitimetable.demo.derslik;

import jakarta.validation.constraints.NotNull;

public final class DerslikBolumDto {

    private DerslikBolumDto() {
    }

    public record Istek(
            @NotNull(message = "Derslik zorunludur") Long derslikId,
            @NotNull(message = "Bölüm zorunludur") Long bolumId,
            @NotNull(message = "Ortak kullanım bilgisi zorunludur") Boolean ortakMi) {
    }

    public record Guncelleme(
            @NotNull(message = "Ortak kullanım bilgisi zorunludur") Boolean ortakMi) {
    }

    public record Yanit(Long id, Long derslikId, Long bolumId, boolean ortakMi) {
    }
}