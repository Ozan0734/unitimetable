package com.unitimetable.demo.planlama;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public final class PlanlamaDto {

    private PlanlamaDto() {
    }

    public record Istek(
            Boolean uyarilaraIzinVer,
            @Min(value = 1, message = "Süre en az 1 saniye olmalıdır") @Max(value = 300, message = "Süre en fazla 300 saniye olabilir") Integer sureSaniye) {
    }

    public record Yerlesen(Long blokId, String dersKodu, int subeNo, int siraNo, int gun,
            int baslangicSlot, int bitisSlot, Long derslikId, int uyariSayisi) {
    }

    public record Yerlesmeyen(Long blokId, String dersKodu, int subeNo, int siraNo, int sure, String sebep) {
    }

    public record Sonuc(int yerlesenSayisi, int yerlesmeyenSayisi, boolean sureDoldu, long gecenMs,
            List<Yerlesen> yerlesenler, List<Yerlesmeyen> yerlesmeyenler) {
    }

    public record TemizleSonuc(int silinen, int kilitliKalan) {
    }
}