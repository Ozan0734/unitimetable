package com.unitimetable.demo.ders;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class DersDto {

    private DersDto() {
    }

    public record Istek(
            @NotNull(message = "Program projesi zorunludur") Long projeId,
            @NotNull(message = "Bölüm zorunludur") Long bolumId,
            @NotBlank(message = "Ders kodu boş olamaz") @Size(max = 30, message = "Ders kodu en fazla 30 karakter olabilir") String kod,
            @NotBlank(message = "Ders adı boş olamaz") @Size(max = 200, message = "Ders adı en fazla 200 karakter olabilir") String ad,
            @NotNull(message = "Ders türü zorunludur") DersTuru tur,
            Boolean sanal,
            @Min(value = 1, message = "Haftalık ders saati en az 1 olmalıdır") @Max(value = 40, message = "Haftalık ders saati en fazla 40 olabilir") Integer haftalikSaat,
            @Size(max = 100, message = "Ders saati ifadesi en fazla 100 karakter olabilir") String saatIfadesi) {
    }

    public record Guncelleme(
            @NotBlank(message = "Ders kodu boş olamaz") @Size(max = 30, message = "Ders kodu en fazla 30 karakter olabilir") String kod,
            @NotBlank(message = "Ders adı boş olamaz") @Size(max = 200, message = "Ders adı en fazla 200 karakter olabilir") String ad,
            @NotNull(message = "Ders türü zorunludur") DersTuru tur,
            Boolean sanal,
            @Min(value = 1, message = "Haftalık ders saati en az 1 olmalıdır") @Max(value = 40, message = "Haftalık ders saati en fazla 40 olabilir") Integer haftalikSaat,
            @Size(max = 100, message = "Ders saati ifadesi en fazla 100 karakter olabilir") String saatIfadesi) {
    }

    public record Yanit(Long id, Long projeId, Long bolumId, String kod, String ad, DersTuru tur,
            boolean sanal, boolean secmeli, String secmeliGrup, Integer haftalikSaat, String saatIfadesi,
            List<Integer> bloklar) {
    }
}