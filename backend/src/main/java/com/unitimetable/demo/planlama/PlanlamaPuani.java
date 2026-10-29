package com.unitimetable.demo.planlama;

import java.util.List;

public final class PlanlamaPuani {

    public static final int GUN_BOS = -1;
    public static final int KESISIR = -2;

    private PlanlamaPuani() {
    }

    /**
     * Aday aralık ile aynı günün mevcut aralıkları arasındaki en küçük boşluk (saat olarak).
     * Gün boşsa GUN_BOS, aralık kesişiyorsa KESISIR döner. Aralıklar [başlangıç, bitiş] biçimindedir ve
     * uçları dahildir.
     */
    public static int bosluk(int bas, int bit, List<int[]> mevcutlar) {
        if (mevcutlar.isEmpty()) {
            return GUN_BOS;
        }
        int enKucuk = Integer.MAX_VALUE;
        for (int[] m : mevcutlar) {
            if (bas <= m[1] && m[0] <= bit) {
                return KESISIR;
            }
            int aralik = (bas > m[1]) ? bas - m[1] - 1 : m[0] - bit - 1;
            enKucuk = Math.min(enKucuk, aralik);
        }
        return enKucuk;
    }

    /**
     * Sınıf programı için ceza puanı: bitişik yerleşim en iyisi, yeni gün biraz kötü, boşluk saat
     * başına ve kesişme çok kötü.
     */
    public static int sinifCezasi(int bosluk) {
        if (bosluk == GUN_BOS) {
            return 5;
        }
        if (bosluk == KESISIR) {
            return 200;
        }
        return bosluk * 10;
    }
}