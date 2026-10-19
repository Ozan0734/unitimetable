package com.unitimetable.demo.kullanici;

import jakarta.validation.constraints.NotBlank;

public final class GirisDto {

    private GirisDto() {
    }

    public record Istek(
            @NotBlank(message = "E-posta boş olamaz") String eposta,
            @NotBlank(message = "Şifre boş olamaz") String sifre) {
    }
}