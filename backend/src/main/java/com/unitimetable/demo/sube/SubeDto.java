package com.unitimetable.demo.sube;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public final class SubeDto {

    private SubeDto() {
    }

    public record Istek(
            @NotNull(message = "Ders zorunludur") Long dersId,
            Long ogretimElemaniId,
            @Min(value = 1, message = "Şube numarası en az 1 olmalıdır") @Max(value = 50, message = "Şube numarası en fazla 50 olabilir") Integer subeNo,
            @Min(value = 1, message = "Tahmini öğrenci sayısı en az 1 olmalıdır") @Max(value = 2000, message = "Tahmini öğrenci sayısı en fazla 2000 olabilir") Integer tahminiOgrenci,
            Boolean sinifCakismaIzni,
            Boolean derslikCakismaIzni,
            Boolean cokluDerslikIzni) {
    }

    public record Guncelleme(
            Long ogretimElemaniId,
            @Min(value = 1, message = "Tahmini öğrenci sayısı en az 1 olmalıdır") @Max(value = 2000, message = "Tahmini öğrenci sayısı en fazla 2000 olabilir") Integer tahminiOgrenci,
            Boolean sinifCakismaIzni,
            Boolean derslikCakismaIzni,
            Boolean cokluDerslikIzni) {
    }

    public record Yanit(Long id, Long dersId, Long ogretimElemaniId, int subeNo, Integer tahminiOgrenci,
            boolean sinifCakismaIzni, boolean derslikCakismaIzni, boolean cokluDerslikIzni) {
    }
}