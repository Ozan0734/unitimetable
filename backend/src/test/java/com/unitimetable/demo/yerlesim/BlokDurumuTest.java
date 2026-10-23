package com.unitimetable.demo.yerlesim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.Test;

class BlokDurumuTest {

    @Test
    void derslikSirasiOnemsizdir() {
        assertEquals(new BlokDurumu(1, 2, false, List.of(2L, 1L)),
                new BlokDurumu(1, 2, false, List.of(1L, 2L)));
    }

    @Test
    void metinGidisDonus() {
        BlokDurumu durum = new BlokDurumu(3, 7, true, List.of(5L, 2L));
        assertEquals("3|7|true|2,5", durum.metin());
        assertEquals(durum, BlokDurumu.coz(durum.metin()));
    }

    @Test
    void bosDerslikListesiCozulur() {
        BlokDurumu durum = BlokDurumu.coz("1|1|false|");
        assertEquals(List.of(), durum.derslikIdler());
    }

    @Test
    void farkliDurumlarEsitDegildir() {
        assertNotEquals(new BlokDurumu(1, 2, false, List.of(1L)),
                new BlokDurumu(1, 2, true, List.of(1L)));
        assertNotEquals(new BlokDurumu(1, 2, false, List.of(1L)),
                new BlokDurumu(1, 3, false, List.of(1L)));
    }

    @Test
    void nullMetinNullDurumdur() {
        assertNull(BlokDurumu.coz(null));
    }
}
