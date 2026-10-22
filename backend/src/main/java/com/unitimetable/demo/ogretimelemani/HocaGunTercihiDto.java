package com.unitimetable.demo.ogretimelemani;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class HocaGunTercihiDto {

    private HocaGunTercihiDto() {
    }

    public record Kayit(
            @NotNull(message = "Tercih türü zorunludur") GunTercihTuru tur,
            @Min(value = 1, message = "Değer en az 1 olmalıdır") @Max(value = 6, message = "Değer en fazla 6 olabilir") Integer deger,
            @Min(value = 1, message = "Gün en az 1 olmalıdır") @Max(value = 7, message = "Gün en fazla 7 olabilir") Integer gun,
            TercihKatiligi katilik,
            Boolean asistanaBirakildi) {
    }

    public record Istek(
            @NotNull(message = "Kayıt listesi zorunludur") @Size(max = 30, message = "En fazla 30 kayıt gönderilebilir") List<@Valid Kayit> kayitlar) {
    }

    public record Yanit(Long id, GunTercihTuru tur, Integer deger, Integer gun,
            TercihKatiligi katilik, boolean asistanaBirakildi) {
    }
}