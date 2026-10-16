package com.unitimetable.demo.derslik;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class DerslikDto {

    private DerslikDto() {
    }

    public record Istek(
            @NotNull(message = "Kurum zorunludur") Long kurumId,
            @NotBlank(message = "Derslik adı boş olamaz") @Size(max = 100, message = "Derslik adı en fazla 100 karakter olabilir") String ad,
            @NotNull(message = "Kapasite zorunludur") @Min(value = 1, message = "Kapasite en az 1 olmalıdır") @Max(value = 2000, message = "Kapasite en fazla 2000 olabilir") Integer kapasite,
            @NotNull(message = "Derslik türü zorunludur") DerslikTuru tur) {
    }

    public record Guncelleme(
            @NotBlank(message = "Derslik adı boş olamaz") @Size(max = 100, message = "Derslik adı en fazla 100 karakter olabilir") String ad,
            @NotNull(message = "Kapasite zorunludur") @Min(value = 1, message = "Kapasite en az 1 olmalıdır") @Max(value = 2000, message = "Kapasite en fazla 2000 olabilir") Integer kapasite,
            @NotNull(message = "Derslik türü zorunludur") DerslikTuru tur) {
    }

    public record Yanit(Long id, Long kurumId, String ad, int kapasite, DerslikTuru tur) {
    }
}