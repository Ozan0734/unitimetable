package com.unitimetable.demo.aktarim;

import java.util.List;

public final class ObsOnizlemeDto {

    private ObsOnizlemeDto() {
    }

    public record Ders(int satir, String kod, String ad, boolean zorunlu, int sinif, String donem,
            int teo, int uyg, int lab, int haftalikSaat, String onerilenSaatIfadesi,
            String kredi, String akts, String hocaUnvan, String hocaAd, String intibak,
            String onKosul, String etiket) {
    }

    public record SecmeliGrup(int satir, String kod, String ad, int sinif, List<Ders> dersler) {
    }

    public record Hoca(String unvan, String ad, int dersSayisi) {
    }

    public record Uyari(int satir, String kod, String mesaj) {
    }

    public record Yanit(String donem, int toplamSatir, int donemSatiri, int digerDonemSatiri,
            int zorunluSayisi, int secmeliGrupSayisi, int secmeliDersSayisi, int grupsuzSecmeliSayisi,
            List<Ders> zorunluDersler, List<SecmeliGrup> secmeliGruplar, List<Ders> grupsuzSecmeliler,
            List<Hoca> hocalar, List<Uyari> uyarilar) {
    }
}