package com.unitimetable.demo.aktarim;

import java.util.List;

public final class ObsKayitDto {

    private ObsKayitDto() {
    }

    public record Atlanan(int satir, String kod, String sebep) {
    }

    public record Sonuc(int olusturulanDers, int olusturulanSube, int olusturulanBlok,
            int yerlesimsizDers, int sanalDers, int seminerDers, int hocasizSube, int eslesenHoca,
            List<String> olusturulanHocalar, List<String> olusturulanSiniflar, List<Atlanan> atlananlar) {
    }
}