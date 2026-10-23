package com.unitimetable.demo.yerlesim;

import java.util.List;

public final class UygunYerDto {

    private UygunYerDto() {
    }

    public enum Durum {
        UYGUN,
        UYARILI,
        ENGELLI
    }

    public record Yer(int gun, int baslangicSlot, int bitisSlot, Long derslikId, Durum durum,
            List<Ihlal> ihlaller) {
    }
}