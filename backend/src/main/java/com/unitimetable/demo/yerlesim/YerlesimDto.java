package com.unitimetable.demo.yerlesim;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class YerlesimDto {

    private YerlesimDto() {
    }

    public record Istek(
            @NotNull(message = "Gün zorunludur") @Min(value = 1, message = "Gün en az 1 olmalıdır") @Max(value = 7, message = "Gün en fazla 7 olabilir") Integer gun,
            @NotNull(message = "Başlangıç saati zorunludur") @Min(value = 1, message = "Başlangıç saati en az 1 olmalıdır") @Max(value = 24, message = "Başlangıç saati en fazla 24 olabilir") Integer baslangicSlot,
            @NotNull(message = "Derslik listesi zorunludur") @Size(min = 1, max = 10, message = "1 ile 10 arasında derslik seçilmelidir") List<@NotNull(message = "Derslik numarası boş olamaz") Long> derslikIdler) {
    }

    public record KilitIstek(
            @NotNull(message = "Kilit durumu zorunludur") Boolean kilitli) {
    }

    public record Yanit(Long id, Long blokId, Long subeId, Long dersId, int gun, int baslangicSlot,
            int bitisSlot, int sure, List<Long> derslikIdler, boolean kilitli) {
    }

    public record YerlesmeyenBlok(Long blokId, Long subeId, Long dersId, String dersKodu, String dersAdi,
            int subeNo, int siraNo, int sure) {
    }
}