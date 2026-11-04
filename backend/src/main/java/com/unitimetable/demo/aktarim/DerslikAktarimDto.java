package com.unitimetable.demo.aktarim;

import java.util.List;
import java.util.Map;

public final class DerslikAktarimDto {

    private DerslikAktarimDto() {
    }

    public record Yanit(int toplamSatir, int eklenecek, int zatenVar, int pasif, int gecersiz,
            Map<String, Integer> eklenecekTurler, List<DerslikKararlari.Karar> dersliklar,
            List<String> uyarilar) {
    }

    public record Sonuc(int olusturulan, Map<String, Integer> olusturulanTurler,
            List<DerslikKararlari.Karar> atlananlar, List<String> uyarilar) {
    }
}