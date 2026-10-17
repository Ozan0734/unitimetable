package com.unitimetable.demo.proje;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class ProgramProjesiDto {

    private ProgramProjesiDto() {
    }

    public record Istek(
            @NotNull(message = "Kurum zorunludur") Long kurumId,
            @NotNull(message = "Bölüm zorunludur") Long bolumId,
            @NotBlank(message = "Proje adı boş olamaz") @Size(max = 200, message = "Proje adı en fazla 200 karakter olabilir") String ad,
            @NotBlank(message = "Dönem boş olamaz") @Size(max = 50, message = "Dönem en fazla 50 karakter olabilir") String donem) {
    }

    public record Guncelleme(
            @NotBlank(message = "Proje adı boş olamaz") @Size(max = 200, message = "Proje adı en fazla 200 karakter olabilir") String ad,
            @NotBlank(message = "Dönem boş olamaz") @Size(max = 50, message = "Dönem en fazla 50 karakter olabilir") String donem,
            @NotNull(message = "Durum zorunludur") ProjeDurumu durum) {
    }

    public record Yanit(Long id, Long kurumId, Long bolumId, String ad, String donem,
            ProjeDurumu durum, LocalDateTime olusturmaTarihi) {
    }
}