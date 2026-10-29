package com.unitimetable.demo.planlama;

import static com.unitimetable.demo.planlama.PlanlamaPuani.GUN_BOS;
import static com.unitimetable.demo.planlama.PlanlamaPuani.KESISIR;
import static com.unitimetable.demo.planlama.PlanlamaPuani.bosluk;
import static com.unitimetable.demo.planlama.PlanlamaPuani.sinifCezasi;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class PlanlamaPuaniTest {

    @Test
    void bosGunOzelDegerDoner() {
        assertEquals(GUN_BOS, bosluk(1, 3, List.of()));
    }

    @Test
    void bitisikBlokBoslukSifirdir() {
        assertEquals(0, bosluk(4, 5, List.of(new int[] {1, 3})));
        assertEquals(0, bosluk(1, 3, List.of(new int[] {4, 5})));
    }

    @Test
    void sonradanGelenBlokBoslukuHesaplanir() {
        assertEquals(2, bosluk(6, 7, List.of(new int[] {1, 3})));
    }

    @Test
    void oncedenGelenBlokBoslukuHesaplanir() {
        assertEquals(1, bosluk(1, 3, List.of(new int[] {5, 6})));
    }

    @Test
    void kesisenAraliklarOzelDegerDoner() {
        assertEquals(KESISIR, bosluk(3, 5, List.of(new int[] {1, 3})));
        assertEquals(KESISIR, bosluk(2, 2, List.of(new int[] {1, 4})));
    }

    @Test
    void enYakinAralikSecilir() {
        assertEquals(1, bosluk(4, 5, List.of(new int[] {1, 2}, new int[] {8, 9})));
    }

    @Test
    void cezaSiralamasiMantiklidir() {
        assertTrue(sinifCezasi(0) < sinifCezasi(GUN_BOS));
        assertTrue(sinifCezasi(GUN_BOS) < sinifCezasi(1));
        assertTrue(sinifCezasi(5) < sinifCezasi(KESISIR));
    }
}
