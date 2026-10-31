package com.unitimetable.demo.yerlesim;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.unitimetable.demo.derslik.Derslik;
import com.unitimetable.demo.derslik.DerslikTuru;

class DerslikSeciciTest {

    private static Derslik derslik(long id, String ad, DerslikTuru tur) {
        Derslik d = new Derslik(1L, ad, 50, tur);
        ReflectionTestUtils.setField(d, "id", id);
        return d;
    }

    private final List<Derslik> hepsi = List.of(
            derslik(1, "SINIF", DerslikTuru.SINIF),
            derslik(2, "LAB", DerslikTuru.LABORATUVAR),
            derslik(3, "AMFI", DerslikTuru.AMFI),
            derslik(4, "SANAL", DerslikTuru.SANAL),
            derslik(5, "SALON", DerslikTuru.SALON));

    private static List<Long> idler(List<Derslik> liste) {
        return liste.stream().map(Derslik::getId).toList();
    }

    @Test
    void sanalDersYalnizcaSanalDerslikDener() {
        assertEquals(List.of(4L), idler(DerslikSecici.adaylar(true, hepsi, Set.of())));
    }

    @Test
    void sanalDersBolumEslesmesiniYoksayar() {
        assertEquals(List.of(4L), idler(DerslikSecici.adaylar(true, hepsi, Set.of(1L, 2L))));
    }

    @Test
    void normalDersEslesmeYokkenSanalVeSalonDenemez() {
        assertEquals(List.of(1L, 2L, 3L), idler(DerslikSecici.adaylar(false, hepsi, Set.of())));
    }

    @Test
    void normalDersEslesmeVarkenYalnizcaEslesenlerdenSecer() {
        assertEquals(List.of(1L, 5L), idler(DerslikSecici.adaylar(false, hepsi, Set.of(1L, 5L))));
    }

    @Test
    void normalDersEslesmedeSanalVarsaAlinmaz() {
        assertEquals(List.of(1L), idler(DerslikSecici.adaylar(false, hepsi, Set.of(1L, 4L))));
    }
}
