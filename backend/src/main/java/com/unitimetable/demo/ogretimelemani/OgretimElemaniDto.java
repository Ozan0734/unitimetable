package com.unitimetable.demo.ogretimelemani;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class OgretimElemaniDto {

    private OgretimElemaniDto() {
    }

    public record Istek(
            @NotNull(message = "Bölüm zorunludur") Long bolumId,
            @NotBlank(message = "Ad soyad boş olamaz") @Size(max = 200, message = "Ad soyad en fazla 200 karakter olabilir") String adSoyad,
            @Size(max = 50, message = "Unvan en fazla 50 karakter olabilir") String unvan,
            @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Renk #RRGGBB biçiminde olmalıdır (örn. #3B6EA8)") String renk,
            @Min(value = 0, message = "Öncelik en az 0 olmalıdır") @Max(value = 1000, message = "Öncelik en fazla 1000 olabilir") Integer oncelik) {
    }

    public record Guncelleme(
            @NotBlank(message = "Ad soyad boş olamaz") @Size(max = 200, message = "Ad soyad en fazla 200 karakter olabilir") String adSoyad,
            @Size(max = 50, message = "Unvan en fazla 50 karakter olabilir") String unvan,
            @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Renk #RRGGBB biçiminde olmalıdır (örn. #3B6EA8)") String renk,
            @Min(value = 0, message = "Öncelik en az 0 olmalıdır") @Max(value = 1000, message = "Öncelik en fazla 1000 olabilir") Integer oncelik) {
    }

    public record Yanit(Long id, Long bolumId, String adSoyad, String unvan, String renk, int oncelik) {
    }
}