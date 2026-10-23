package com.unitimetable.demo.yerlesim;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.unitimetable.demo.blok.DersBlogu;
import com.unitimetable.demo.blok.DersBloguRepository;
import com.unitimetable.demo.bolum.Bolum;
import com.unitimetable.demo.bolum.BolumRepository;
import com.unitimetable.demo.ders.Ders;
import com.unitimetable.demo.ders.DersRepository;
import com.unitimetable.demo.ders.DersSinif;
import com.unitimetable.demo.ders.DersSinifRepository;
import com.unitimetable.demo.derslik.Derslik;
import com.unitimetable.demo.derslik.DerslikBolum;
import com.unitimetable.demo.derslik.DerslikBolumRepository;
import com.unitimetable.demo.derslik.DerslikRepository;
import com.unitimetable.demo.derslik.DerslikTuru;
import com.unitimetable.demo.derslik.LabGunDagilimi;
import com.unitimetable.demo.derslik.LabGunDagilimiRepository;
import com.unitimetable.demo.ogretimelemani.GunTercihTuru;
import com.unitimetable.demo.ogretimelemani.HocaGunTercihi;
import com.unitimetable.demo.ogretimelemani.HocaGunTercihiRepository;
import com.unitimetable.demo.ogretimelemani.HocaMusaitlik;
import com.unitimetable.demo.ogretimelemani.HocaMusaitlikRepository;
import com.unitimetable.demo.ogretimelemani.MusaitlikDurumu;
import com.unitimetable.demo.ogretimelemani.OgretimElemani;
import com.unitimetable.demo.ogretimelemani.OgretimElemaniRepository;
import com.unitimetable.demo.ogretimelemani.TercihKatiligi;
import com.unitimetable.demo.sube.Sube;
import com.unitimetable.demo.sube.SubeRepository;

@Component
public class CakismaDenetleyici {

    private static final String[] GUN_ADLARI = {
            "Pazartesi", "Salı", "Çarşamba", "Perşembe", "Cuma", "Cumartesi", "Pazar"
    };

    private final YerlesimRepository yerlesimRepository;
    private final YerlesimDerslikRepository yerlesimDerslikRepository;
    private final DersBloguRepository blokRepository;
    private final SubeRepository subeRepository;
    private final DersRepository dersRepository;
    private final DersSinifRepository dersSinifRepository;
    private final DerslikRepository derslikRepository;
    private final DerslikBolumRepository derslikBolumRepository;
    private final LabGunDagilimiRepository labGunRepository;
    private final HocaMusaitlikRepository musaitlikRepository;
    private final HocaGunTercihiRepository gunTercihiRepository;
    private final OgretimElemaniRepository hocaRepository;
    private final BolumRepository bolumRepository;

    public CakismaDenetleyici(YerlesimRepository yerlesimRepository,
            YerlesimDerslikRepository yerlesimDerslikRepository,
            DersBloguRepository blokRepository,
            SubeRepository subeRepository,
            DersRepository dersRepository,
            DersSinifRepository dersSinifRepository,
            DerslikRepository derslikRepository,
            DerslikBolumRepository derslikBolumRepository,
            LabGunDagilimiRepository labGunRepository,
            HocaMusaitlikRepository musaitlikRepository,
            HocaGunTercihiRepository gunTercihiRepository,
            OgretimElemaniRepository hocaRepository,
            BolumRepository bolumRepository) {
        this.yerlesimRepository = yerlesimRepository;
        this.yerlesimDerslikRepository = yerlesimDerslikRepository;
        this.blokRepository = blokRepository;
        this.subeRepository = subeRepository;
        this.dersRepository = dersRepository;
        this.dersSinifRepository = dersSinifRepository;
        this.derslikRepository = derslikRepository;
        this.derslikBolumRepository = derslikBolumRepository;
        this.labGunRepository = labGunRepository;
        this.musaitlikRepository = musaitlikRepository;
        this.gunTercihiRepository = gunTercihiRepository;
        this.hocaRepository = hocaRepository;
        this.bolumRepository = bolumRepository;
    }

    /**
     * İki saat aralığının (başlangıç saati ve süre) kesişip kesişmediği. Saatler dahil sayılır:
     * 1. saatte başlayan 3 saatlik blok 1, 2 ve 3. saatleri kaplar.
     */
    public static boolean araliklarCakisir(int bas1, int sure1, int bas2, int sure2) {
        return bas1 <= bas2 + sure2 - 1 && bas2 <= bas1 + sure1 - 1;
    }

    public List<Ihlal> kontrolEt(DersBlogu blok, Sube sube, Ders ders, int gun, int baslangicSlot,
            List<Long> derslikIdler) {
        List<Ihlal> ihlaller = new ArrayList<>();
        List<Derslik> secilenler = derslikRepository.findAllById(derslikIdler);

        cakismalariKontrolEt(ihlaller, blok, sube, ders, gun, baslangicSlot, secilenler);
        hocaKontrolu(ihlaller, blok, sube, gun, baslangicSlot);
        kapasiteKontrolu(ihlaller, sube, secilenler);
        bolumKontrolu(ihlaller, ders, gun, secilenler);

        ihlaller.sort(Comparator.comparing(Ihlal::tur));
        return ihlaller;
    }

    private void cakismalariKontrolEt(List<Ihlal> ihlaller, DersBlogu blok, Sube sube, Ders ders,
            int gun, int baslangicSlot, List<Derslik> secilenler) {
        List<Yerlesim> ayniGun = yerlesimRepository.findByProjeId(ders.getProjeId()).stream()
                .filter(y -> !y.getBlokId().equals(blok.getId()))
                .filter(y -> y.getGun() == gun)
                .toList();
        if (ayniGun.isEmpty()) {
            return;
        }

        Map<Long, DersBlogu> bloklar = blokRepository
                .findAllById(ayniGun.stream().map(Yerlesim::getBlokId).toList()).stream()
                .collect(Collectors.toMap(DersBlogu::getId, Function.identity()));

        List<Yerlesim> cakisanlar = ayniGun.stream()
                .filter(y -> araliklarCakisir(baslangicSlot, blok.getSure(),
                        y.getBaslangicSlot(), bloklar.get(y.getBlokId()).getSure()))
                .toList();
        if (cakisanlar.isEmpty()) {
            return;
        }

        Map<Long, Sube> subeler = subeRepository.findAllById(cakisanlar.stream()
                .map(y -> bloklar.get(y.getBlokId()).getSubeId()).collect(Collectors.toSet())).stream()
                .collect(Collectors.toMap(Sube::getId, Function.identity()));
        Map<Long, Ders> dersler = dersRepository.findAllById(subeler.values().stream()
                .map(Sube::getDersId).collect(Collectors.toSet())).stream()
                .collect(Collectors.toMap(Ders::getId, Function.identity()));

        Set<Long> tumDersler = new HashSet<>(dersler.keySet());
        tumDersler.add(ders.getId());
        Map<Long, Set<Long>> siniflar = dersSinifRepository.findByDersIdIn(tumDersler).stream()
                .collect(Collectors.groupingBy(DersSinif::getDersId,
                        Collectors.mapping(DersSinif::getSinifId, Collectors.toSet())));
        Map<Long, Set<Long>> yerlesimDerslikleri = yerlesimDerslikRepository
                .findByYerlesimIdIn(cakisanlar.stream().map(Yerlesim::getId).toList()).stream()
                .collect(Collectors.groupingBy(YerlesimDerslik::getYerlesimId,
                        Collectors.mapping(YerlesimDerslik::getDerslikId, Collectors.toSet())));
        Set<Long> adaySiniflar = siniflar.getOrDefault(ders.getId(), Set.of());

        for (Yerlesim diger : cakisanlar) {
            DersBlogu digerBlok = bloklar.get(diger.getBlokId());
            Sube digerSube = subeler.get(digerBlok.getSubeId());
            Ders digerDers = dersler.get(digerSube.getDersId());
            String digerAd = digerDers.getKod() + " Şube " + digerSube.getSubeNo();
            Long digerBlokId = diger.getBlokId();

            if (digerSube.getId().equals(sube.getId())) {
                ihlaller.add(new Ihlal(IhlalTuru.ENGEL, "SUBE_CAKISMA",
                        "Aynı şubenin başka bir bloğu bu saatlerde yerleşik (" + digerAd + ")",
                        digerBlokId));
            }

            if (sube.getOgretimElemaniId() != null
                    && sube.getOgretimElemaniId().equals(digerSube.getOgretimElemaniId())) {
                ihlaller.add(new Ihlal(IhlalTuru.ENGEL, "HOCA_CAKISMA",
                        "Öğretim elemanı " + hocaAdi(sube.getOgretimElemaniId())
                                + " bu saatlerde başka bir derste: " + digerAd,
                        digerBlokId));
            }

            Set<Long> ortakSiniflar = new HashSet<>(adaySiniflar);
            ortakSiniflar.retainAll(siniflar.getOrDefault(digerDers.getId(), Set.of()));
            if (!ortakSiniflar.isEmpty()) {
                if (sube.isSinifCakismaIzni() || digerSube.isSinifCakismaIzni()) {
                    ihlaller.add(new Ihlal(IhlalTuru.BILGI, "SINIF_CAKISMA_IZINLI",
                            "Aynı sınıfın dersi " + digerAd + " ile çakışıyor, çakışma izni tanımlı",
                            digerBlokId));
                } else {
                    ihlaller.add(new Ihlal(IhlalTuru.ENGEL, "SINIF_CAKISMA",
                            "Aynı sınıfın dersi " + digerAd + " bu saatlerde yerleşik",
                            digerBlokId));
                }
            }

            Set<Long> digerDerslikler = yerlesimDerslikleri.getOrDefault(diger.getId(), Set.of());
            for (Derslik derslik : secilenler) {
                if (!digerDerslikler.contains(derslik.getId())) {
                    continue;
                }
                if (sube.isDerslikCakismaIzni() || digerSube.isDerslikCakismaIzni()) {
                    ihlaller.add(new Ihlal(IhlalTuru.BILGI, "DERSLIK_CAKISMA_IZINLI",
                            "Derslik " + derslik.getAd() + " bu saatlerde " + digerAd
                                    + " ile ortak kullanılıyor, çakışma izni tanımlı",
                            digerBlokId));
                } else {
                    ihlaller.add(new Ihlal(IhlalTuru.ENGEL, "DERSLIK_CAKISMA",
                            "Derslik " + derslik.getAd() + " bu saatlerde " + digerAd + " için ayrılmış",
                            digerBlokId));
                }
            }
        }
    }

    private void hocaKontrolu(List<Ihlal> ihlaller, DersBlogu blok, Sube sube, int gun, int baslangicSlot) {
        Long hocaId = sube.getOgretimElemaniId();
        if (hocaId == null) {
            return;
        }
        int bitis = baslangicSlot + blok.getSure() - 1;

        List<Integer> musaitOlmayanSaatler = musaitlikRepository
                .findByOgretimElemaniIdOrderByGunAscSlotAsc(hocaId).stream()
                .filter(m -> m.getDurum() == MusaitlikDurumu.MUSAIT_DEGIL)
                .filter(m -> m.getGun() == gun)
                .filter(m -> m.getSlot() >= baslangicSlot && m.getSlot() <= bitis)
                .map(HocaMusaitlik::getSlot)
                .toList();
        if (!musaitOlmayanSaatler.isEmpty()) {
            ihlaller.add(new Ihlal(IhlalTuru.ENGEL, "HOCA_MUSAIT_DEGIL",
                    "Öğretim elemanı " + hocaAdi(hocaId) + " " + GUN_ADLARI[gun - 1]
                            + " günü şu saatlerde müsait değil: "
                            + musaitOlmayanSaatler.stream().map(String::valueOf)
                                    .collect(Collectors.joining(", ")),
                    null));
        }

        List<HocaGunTercihi> tercihler = gunTercihiRepository.findByOgretimElemaniIdOrderByIdAsc(hocaId);
        for (HocaGunTercihi t : tercihler) {
            if (t.getTur() == GunTercihTuru.KACINILAN_GUN && t.getGun() != null && t.getGun() == gun) {
                boolean zorunlu = t.getKatilik() == TercihKatiligi.ZORUNLU;
                ihlaller.add(new Ihlal(zorunlu ? IhlalTuru.ENGEL : IhlalTuru.UYARI, "HOCA_KACINILAN_GUN",
                        "Öğretim elemanı " + hocaAdi(hocaId) + " " + GUN_ADLARI[gun - 1]
                                + " gününden kaçınıyor" + (zorunlu ? " (zorunlu)" : ""),
                        null));
            }
        }

        Set<Integer> tercihEdilenGunler = tercihler.stream()
                .filter(t -> t.getTur() == GunTercihTuru.TERCIH_EDILEN_GUN && t.getGun() != null)
                .map(HocaGunTercihi::getGun)
                .collect(Collectors.toSet());
        if (!tercihEdilenGunler.isEmpty() && !tercihEdilenGunler.contains(gun)) {
            boolean zorunlu = tercihler.stream().anyMatch(t -> t.getTur() == GunTercihTuru.TERCIH_EDILEN_GUN
                    && t.getKatilik() == TercihKatiligi.ZORUNLU);
            ihlaller.add(new Ihlal(zorunlu ? IhlalTuru.ENGEL : IhlalTuru.UYARI, "HOCA_TERCIH_DISI_GUN",
                    "Öğretim elemanı " + hocaAdi(hocaId) + " tercih ettiği günlerin dışında ("
                            + GUN_ADLARI[gun - 1] + ")" + (zorunlu ? " (zorunlu)" : ""),
                    null));
        }
    }

    private void kapasiteKontrolu(List<Ihlal> ihlaller, Sube sube, List<Derslik> secilenler) {
        if (sube.getTahminiOgrenci() == null) {
            return;
        }
        int kapasite = secilenler.stream().mapToInt(Derslik::getKapasite).sum();
        int tahmini = sube.getTahminiOgrenci();
        if (tahmini > kapasite) {
            ihlaller.add(new Ihlal(IhlalTuru.UYARI, "KAPASITE_ASIMI",
                    "Tahmini öğrenci sayısı " + tahmini + ", derslik kapasitesi " + kapasite
                            + ", kapasiteyi aşan öğrenci sayısı " + (tahmini - kapasite),
                    null));
        }
    }

    private void bolumKontrolu(List<Ihlal> ihlaller, Ders ders, int gun, List<Derslik> secilenler) {
        for (Derslik derslik : secilenler) {
            List<DerslikBolum> eslesmeler = derslikBolumRepository.findByDerslikId(derslik.getId());
            if (!eslesmeler.isEmpty()
                    && eslesmeler.stream().noneMatch(e -> e.getBolumId().equals(ders.getBolumId()))) {
                ihlaller.add(new Ihlal(IhlalTuru.UYARI, "BOLUM_UYUSMAZLIGI",
                        "Dersin bölümü: " + bolumAdi(ders.getBolumId()) + ". Derslik " + derslik.getAd()
                                + " şu bölümlere tanımlı: "
                                + bolumAdlari(eslesmeler.stream().map(DerslikBolum::getBolumId).toList()),
                        null));
            }

            if (derslik.getTur() == DerslikTuru.LABORATUVAR) {
                List<LabGunDagilimi> baskaBolumunGunu = labGunRepository
                        .findByDerslikIdOrderByGun(derslik.getId()).stream()
                        .filter(l -> l.getGun() == gun && !l.getBolumId().equals(ders.getBolumId()))
                        .toList();
                if (!baskaBolumunGunu.isEmpty()) {
                    ihlaller.add(new Ihlal(IhlalTuru.UYARI, "LAB_GUNU_BASKA_BOLUME_AYRILMIS",
                            "Laboratuvar " + derslik.getAd() + " " + GUN_ADLARI[gun - 1] + " günü "
                                    + bolumAdlari(baskaBolumunGunu.stream().map(LabGunDagilimi::getBolumId).toList())
                                    + " bölümüne ayrılmış. Dersin bölümü: " + bolumAdi(ders.getBolumId()),
                            null));
                }
            }
        }
    }

    private String hocaAdi(Long hocaId) {
        return hocaRepository.findById(hocaId).map(OgretimElemani::getAdSoyad).orElse("(bilinmiyor)");
    }

    private String bolumAdi(Long bolumId) {
        return bolumRepository.findById(bolumId).map(Bolum::getAd).orElse("(bilinmiyor)");
    }

    private String bolumAdlari(Collection<Long> bolumIdler) {
        return bolumRepository.findAllById(bolumIdler).stream()
                .map(Bolum::getAd)
                .collect(Collectors.joining(", "));
    }
}