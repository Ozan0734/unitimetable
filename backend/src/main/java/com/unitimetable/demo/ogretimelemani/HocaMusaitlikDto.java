package com.unitimetable.demo.ogretimelemani;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class HocaMusaitlikDto {

    private HocaMusaitlikDto() {
    }

    public record Kayit(
            @NotNull(message = "Gün zorunludur") @Min(value = 1, message = "Gün en az 1 olmalıdır") @Max(value = 7, message = "Gün en fazla 7 olabilir") Integer gun,
            @NotNull(message = "Saat zorunludur") @Min(value = 1, message = "Saat en az 1 olmalıdır") @Max(value = 24, message = "Saat en fazla 24 olabilir") Integer slot,
            @NotNull(message = "Durum zorunludur") MusaitlikDurumu durum) {
    }

    public record Istek(
            @NotNull(message = "Kayıt listesi zorunludur") @Size(max = 168, message = "En fazla 168 kayıt gönderilebilir") List<@Valid Kayit> kayitlar) {
    }

    public record Yanit(Long id, int gun, int slot, MusaitlikDurumu durum) {
    }
}