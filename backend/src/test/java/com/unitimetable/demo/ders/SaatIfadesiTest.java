package com.unitimetable.demo.ders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.unitimetable.demo.ortak.GecersizIslemException;

class SaatIfadesiTest {

    @Test
    void tekSayiTekBlokOlur() {
        assertEquals(List.of(5), SaatIfadesi.bloklar("5"));
    }

    @Test
    void sureCarpanAdetSirasiDogruYorumlanir() {
        assertEquals(List.of(5), SaatIfadesi.bloklar("5*1"));
        assertEquals(List.of(1, 1, 1, 1, 1), SaatIfadesi.bloklar("1*5"));
    }

    @Test
    void toplamaIleBlokluDersler() {
        assertEquals(List.of(3, 2), SaatIfadesi.bloklar("3+2"));
        assertEquals(List.of(3, 2), SaatIfadesi.bloklar("3*1 + 2*1"));
    }

    @Test
    void karisikIfade() {
        assertEquals(List.of(2, 2, 1), SaatIfadesi.bloklar("2*2 + 1"));
    }

    @Test
    void bosluklarYoksayilir() {
        assertEquals(List.of(2, 2, 1), SaatIfadesi.bloklar("  2 * 2 +   1 "));
    }

    @Test
    void toplamHesaplanir() {
        assertEquals(5, SaatIfadesi.toplam("2*2+1"));
        assertEquals(5, SaatIfadesi.toplam("1*5"));
        assertEquals(3, SaatIfadesi.toplam("3"));
    }

    @Test
    void toplamHaftalikSaateEsitseGecerli() {
        SaatIfadesi.dogrula("3+2", 5);
        SaatIfadesi.dogrula("2*2+1", 5);
    }

    @Test
    void toplamHaftalikSaateEsitDegilseHataVerir() {
        GecersizIslemException hata = assertThrows(GecersizIslemException.class,
                () -> SaatIfadesi.dogrula("3+2", 4));
        assertTrue(hata.getMessage().contains("(5)"));
        assertTrue(hata.getMessage().contains("(4)"));
    }

    @Test
    void gecersizIfadelerHataVerir() {
        String[] gecersizler = {
                "", "   ", "abc", "3+", "+3", "3++2", "2*", "*2", "2*3*4",
                "0", "0*3", "3*0", "3-2", "2,5", "2.5", "21", "1*21"
        };
        for (String ifade : gecersizler) {
            assertThrows(GecersizIslemException.class, () -> SaatIfadesi.bloklar(ifade),
                    "Geçersiz sayılmalıydı: \"" + ifade + "\"");
        }
    }
}