package com.unitimetable.demo.sinif;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class SinifDto {

    private SinifDto() {
    }

    public record Istek(
            @NotNull(message = "Program zorunludur") Long programId,
            @NotBlank(message = "Sınıf adı boş olamaz") @Size(max = 100, message = "Sınıf adı en fazla 100 karakter olabilir") String ad,
            @NotNull(message = "Sınıf düzeyi zorunludur") @Min(value = 1, message = "Sınıf düzeyi en az 1 olmalıdır") @Max(value = 10, message = "Sınıf düzeyi en fazla 10 olabilir") Integer duzey) {
    }

    public record Guncelleme(
            @NotBlank(message = "Sınıf adı boş olamaz") @Size(max = 100, message = "Sınıf adı en fazla 100 karakter olabilir") String ad,
            @NotNull(message = "Sınıf düzeyi zorunludur") @Min(value = 1, message = "Sınıf düzeyi en az 1 olmalıdır") @Max(value = 10, message = "Sınıf düzeyi en fazla 10 olabilir") Integer duzey) {
    }

    public record Yanit(Long id, Long programId, String ad, int duzey) {
    }
}