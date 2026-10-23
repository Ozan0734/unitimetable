package com.unitimetable.demo.yerlesim;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Bir bloğun yerleşim durumu. Derslik sırası önemsizdir, her zaman sıralı tutulur.
 */
public record BlokDurumu(int gun, int baslangicSlot, boolean kilitli, List<Long> derslikIdler) {

    public BlokDurumu {
        derslikIdler = derslikIdler.stream().sorted().toList();
    }

    public String metin() {
        return gun + "|" + baslangicSlot + "|" + kilitli + "|"
                + derslikIdler.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    public static BlokDurumu coz(String metin) {
        if (metin == null) {
            return null;
        }
        String[] p = metin.split("\\|", -1);
        List<Long> derslikler = p[3].isEmpty()
                ? List.of()
                : Arrays.stream(p[3].split(",")).map(Long::valueOf).toList();
        return new BlokDurumu(Integer.parseInt(p[0]), Integer.parseInt(p[1]),
                Boolean.parseBoolean(p[2]), derslikler);
    }
}