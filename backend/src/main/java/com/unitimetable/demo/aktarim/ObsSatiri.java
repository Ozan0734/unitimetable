package com.unitimetable.demo.aktarim;

public record ObsSatiri(int satir, String kod, String ad, boolean zorunlu, String kredi, String akts,
        int teo, int uyg, int lab, int sinif, String donem, String hocaMetni, String intibak,
        String onKosul, String grup) {

    public int toplamSaat() {
        return teo + uyg + lab;
    }
}