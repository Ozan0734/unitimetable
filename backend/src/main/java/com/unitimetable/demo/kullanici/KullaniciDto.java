package com.unitimetable.demo.kullanici;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class KullaniciDto {

    private KullaniciDto() {
    }

    public record Istek(
            @NotNull(message = "Kurum zorunludur") Long kurumId,
            Long fakulteId,
            Long bolumId,
            @NotBlank(message = "Ad soyad boş olamaz") @Size(max = 200, message = "Ad soyad en fazla 200 karakter olabilir") String adSoyad,
            @NotBlank(message = "E-posta boş olamaz") @Email(message = "E-posta geçerli bir adres olmalıdır") @Size(max = 200, message = "E-posta en fazla 200 karakter olabilir") String eposta,
            @NotBlank(message = "Şifre boş olamaz") @Size(min = 8, max = 72, message = "Şifre 8 ile 72 karakter arasında olmalıdır") String sifre,
            @NotNull(message = "Rol zorunludur") KullaniciRolu rol) {
    }

    public record Guncelleme(
            Long fakulteId,
            Long bolumId,
            @NotBlank(message = "Ad soyad boş olamaz") @Size(max = 200, message = "Ad soyad en fazla 200 karakter olabilir") String adSoyad,
            @Size(min = 8, max = 72, message = "Şifre 8 ile 72 karakter arasında olmalıdır") String sifre,
            @NotNull(message = "Rol zorunludur") KullaniciRolu rol) {
    }

    public record Yanit(Long id, Long kurumId, Long fakulteId, Long bolumId, String adSoyad,
            String eposta, KullaniciRolu rol) {
    }
}