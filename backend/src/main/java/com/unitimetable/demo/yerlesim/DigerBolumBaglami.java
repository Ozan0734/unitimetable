package com.unitimetable.demo.yerlesim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Aynı kurumdaki ve aynı dönemdeki diğer bölümlerin yerleşik blokları ile ortak şube gruplarının
 * bilgisi. Çakışma denetimi bu verilere karşı derslik, öğretim elemanı ve ortak şube kontrolü yapar.
 * Mesajlarda yalnızca bölüm adı ve ders kodu kullanılır.
 */
public final class DigerBolumBaglami {

    public record DigerBlok(Long subeId, int siraNo, int gun, int baslangicSlot, int sure, String bolumAdi,
            String dersKodu, int subeNo, String hocaAnahtari, Set<Long> derslikIdler, String derslikAdlari,
            boolean derslikCakismaIzni) {
    }

    private static final DigerBolumBaglami BOS = new DigerBolumBaglami(List.of(), Map.of(), Map.of());

    private final Map<Integer, List<DigerBlok>> gunlere = new HashMap<>();
    private final Map<String, DigerBlok> subeBloklari = new HashMap<>();
    private final Map<Long, Set<Long>> ortakPartnerler;
    private final Map<Long, Integer> ogrenciSayilari;

    /**
     * @param ortakPartnerler  onaylı ortak grubunda olan her şube için gruptaki diğer şubeler
     * @param ogrenciSayilari  ortak grup üyelerinin tahmini öğrenci sayıları (bilinmeyen 0 sayılır)
     */
    public DigerBolumBaglami(List<DigerBlok> bloklar, Map<Long, Set<Long>> ortakPartnerler,
            Map<Long, Integer> ogrenciSayilari) {
        for (DigerBlok blok : bloklar) {
            gunlere.computeIfAbsent(blok.gun(), k -> new ArrayList<>()).add(blok);
            subeBloklari.put(blok.subeId() + ":" + blok.siraNo(), blok);
        }
        this.ortakPartnerler = ortakPartnerler;
        this.ogrenciSayilari = ogrenciSayilari;
    }

    public static DigerBolumBaglami bos() {
        return BOS;
    }

    public List<DigerBlok> gununBloklari(int gun) {
        return gunlere.getOrDefault(gun, List.of());
    }

    /** Başka bölümdeki bir şubenin verilen sıradaki bloğu. Yerleşmemişse null. */
    public DigerBlok blok(Long subeId, int siraNo) {
        return subeBloklari.get(subeId + ":" + siraNo);
    }

    /** Şubenin onaylı ortak grubundaki diğer şubeler. Ortak grupta değilse boş küme. */
    public Set<Long> partnerler(Long subeId) {
        return ortakPartnerler.getOrDefault(subeId, Set.of());
    }

    /** Ortak grubundaki diğer şubelerin tahmini öğrenci sayıları toplamı. */
    public int partnerOgrenciToplami(Long subeId) {
        int toplam = 0;
        for (Long partner : partnerler(subeId)) {
            toplam += ogrenciSayilari.getOrDefault(partner, 0);
        }
        return toplam;
    }

    public boolean bosMu() {
        return gunlere.isEmpty() && ortakPartnerler.isEmpty();
    }
}
