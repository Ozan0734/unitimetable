package com.unitimetable.demo.yerlesim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Aynı kurumdaki ve aynı dönemdeki diğer bölümlerin yerleşik blokları. Çakışma denetimi bu bloklara karşı
 * derslik ve öğretim elemanı kontrolü yapar. Mesajlarda yalnızca bölüm adı ve ders kodu kullanılır.
 */
public final class DigerBolumBaglami {

    public record DigerBlok(int gun, int baslangicSlot, int sure, String bolumAdi, String dersKodu, int subeNo,
            String hocaAnahtari, Set<Long> derslikIdler, boolean derslikCakismaIzni) {
    }

    private static final DigerBolumBaglami BOS = new DigerBolumBaglami(List.of());

    private final Map<Integer, List<DigerBlok>> gunlere = new HashMap<>();

    public DigerBolumBaglami(List<DigerBlok> bloklar) {
        for (DigerBlok blok : bloklar) {
            gunlere.computeIfAbsent(blok.gun(), k -> new ArrayList<>()).add(blok);
        }
    }

    public static DigerBolumBaglami bos() {
        return BOS;
    }

    public List<DigerBlok> gununBloklari(int gun) {
        return gunlere.getOrDefault(gun, List.of());
    }

    public boolean bosMu() {
        return gunlere.isEmpty();
    }
}
