package com.unitimetable.demo.yerlesim;

import static com.unitimetable.demo.yerlesim.CakismaDenetleyici.araliklarCakisir;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CakismaDenetleyiciTest {

    @Test
    void ardisikAraliklarCakismaz() {
        // 1-3. saat ile 4-5. saat
        assertFalse(araliklarCakisir(1, 3, 4, 2));
    }

    @Test
    void birSaatOrtusmeCakismadir() {
        // 1-4. saat ile 4-5. saat
        assertTrue(araliklarCakisir(1, 4, 4, 2));
    }

    @Test
    void ayniAralikCakisir() {
        assertTrue(araliklarCakisir(2, 2, 2, 2));
    }

    @Test
    void icIceAraliklarCakisir() {
        // 1-9. saat ile 4-5. saat
        assertTrue(araliklarCakisir(1, 9, 4, 2));
    }

    @Test
    void onceBitenAralikCakismaz() {
        // 5-6. saat ile 1-4. saat
        assertFalse(araliklarCakisir(5, 2, 1, 4));
    }

    @Test
    void sonucSiraDegisinceDegismez() {
        assertEquals(araliklarCakisir(1, 4, 4, 2), araliklarCakisir(4, 2, 1, 4));
        assertEquals(araliklarCakisir(1, 3, 4, 2), araliklarCakisir(4, 2, 1, 3));
    }
}