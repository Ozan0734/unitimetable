package com.unitimetable.demo.ortakders;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class OrtakDersDto {

    private OrtakDersDto() {
    }

    public record Eslesen(Long subeId, Long bolumId, String bolumAdi, String dersKodu, String dersAdi, int subeNo,
            Integer ogrenci, boolean bloklarUyumlu) {
    }

    public record Aday(Long subeId, String dersKodu, String dersAdi, int subeNo, Integer ogrenci, String hoca,
            List<Eslesen> eslesenler) {
    }

    public record Istek(
            @NotNull(message = "Şube listesi zorunludur") @Size(min = 2, max = 12, message = "Ortak grup en az 2, en fazla 12 şubeden oluşur") List<Long> subeIdler) {
    }

    public record Uye(Long subeId, Long bolumId, String bolumAdi, String dersKodu, String dersAdi, int subeNo,
            String hoca, Integer ogrenci, boolean onayli, boolean benim) {
    }

    public record Grup(Long id, String donem, List<Uye> uyeler, int toplamOgrenci, boolean onayimiBekliyor,
            boolean tamOnayli) {
    }
}
