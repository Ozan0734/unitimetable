package com.unitimetable.demo.yerlesim;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.unitimetable.demo.derslik.Derslik;
import com.unitimetable.demo.derslik.DerslikTuru;

public final class DerslikSecici {

    private DerslikSecici() {
    }

    /**
     * Bir dersin otomatik yerleştirmede deneyebileceği derslikler.
     * Sanal ders yalnızca sanal sınıfları, diğer dersler sanal olmayanları dener. Bölüme eşleştirilmiş
     * derslik varsa onlar, yoksa toplantı salonları dışındaki tüm derslikler kullanılır.
     */
    public static List<Derslik> adaylar(boolean sanalDers, Collection<Derslik> kurumDersliklari,
            Set<Long> bolumDerslikIdler) {
        if (sanalDers) {
            return kurumDersliklari.stream().filter(d -> d.getTur() == DerslikTuru.SANAL).toList();
        }
        List<Derslik> fiziki = kurumDersliklari.stream()
                .filter(d -> d.getTur() != DerslikTuru.SANAL)
                .toList();
        if (!bolumDerslikIdler.isEmpty()) {
            return fiziki.stream().filter(d -> bolumDerslikIdler.contains(d.getId())).toList();
        }
        return fiziki.stream().filter(d -> d.getTur() != DerslikTuru.SALON).toList();
    }
}