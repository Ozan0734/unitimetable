package com.unitimetable.demo.planlama;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.unitimetable.demo.yerlesim.DerslikSecici;

import com.unitimetable.demo.blok.DersBlogu;
import com.unitimetable.demo.blok.DersBloguRepository;
import com.unitimetable.demo.ders.Ders;
import com.unitimetable.demo.ders.DersRepository;
import com.unitimetable.demo.ders.DersSinif;
import com.unitimetable.demo.ders.DersSinifRepository;
import com.unitimetable.demo.derslik.Derslik;
import com.unitimetable.demo.derslik.DerslikBolum;
import com.unitimetable.demo.derslik.DerslikBolumRepository;
import com.unitimetable.demo.derslik.DerslikRepository;
import com.unitimetable.demo.izgara.ZamanIzgarasi;
import com.unitimetable.demo.izgara.ZamanIzgarasiRepository;
import com.unitimetable.demo.ortak.KayitBulunamadiException;
import com.unitimetable.demo.ortak.YetkiKontrol;
import com.unitimetable.demo.proje.ProgramProjesi;
import com.unitimetable.demo.proje.ProgramProjesiRepository;
import com.unitimetable.demo.sube.Sube;
import com.unitimetable.demo.sube.SubeRepository;
import com.unitimetable.demo.yerlesim.BlokDurumu;
import com.unitimetable.demo.yerlesim.CakismaDenetleyici;
import com.unitimetable.demo.yerlesim.Ihlal;
import com.unitimetable.demo.yerlesim.IhlalTuru;
import com.unitimetable.demo.yerlesim.IslemGecmisiService;
import com.unitimetable.demo.yerlesim.IslemTuru;
import com.unitimetable.demo.yerlesim.Yerlesim;
import com.unitimetable.demo.yerlesim.YerlesimDerslik;
import com.unitimetable.demo.yerlesim.YerlesimDerslikRepository;
import com.unitimetable.demo.yerlesim.YerlesimRepository;

@Service
public class OtomatikPlanlamaService {

    private static final int VARSAYILAN_SURE_SN = 30;

    private record Aday(int gun, int baslangic, Long derslikId, int ceza) {
    }

    /**
     * Planlama sırasında bellekte tutulan yerleşim özeti. Tercih puanını veritabanına sormadan hesaplar.
     */
    private static final class Durum {
        private final Map<Long, Set<Integer>> subeGunleri = new HashMap<>();
        private final Map<Long, Set<Integer>> hocaGunleri = new HashMap<>();
        private final Map<String, List<int[]>> sinifAraliklari = new HashMap<>();

        void ekle(Sube sube, List<Long> sinifIdler, int gun, int bas, int sure) {
            subeGunleri.computeIfAbsent(sube.getId(), k -> new HashSet<>()).add(gun);
            if (sube.getOgretimElemaniId() != null) {
                hocaGunleri.computeIfAbsent(sube.getOgretimElemaniId(), k -> new HashSet<>()).add(gun);
            }
            for (Long sinifId : sinifIdler) {
                sinifAraliklari.computeIfAbsent(sinifId + ":" + gun, k -> new ArrayList<>())
                        .add(new int[] {bas, bas + sure - 1});
            }
        }

        int ceza(Sube sube, List<Long> sinifIdler, int gun, int bas, int sure) {
            int ceza = gun * 2 + bas;
            if (subeGunleri.getOrDefault(sube.getId(), Set.of()).contains(gun)) {
                ceza += 500;
            }
            Long hocaId = sube.getOgretimElemaniId();
            if (hocaId != null && !hocaGunleri.getOrDefault(hocaId, Set.of()).contains(gun)) {
                ceza += 50;
            }
            for (Long sinifId : sinifIdler) {
                int bosluk = PlanlamaPuani.bosluk(bas, bas + sure - 1,
                        sinifAraliklari.getOrDefault(sinifId + ":" + gun, List.of()));
                ceza += PlanlamaPuani.sinifCezasi(bosluk);
            }
            return ceza;
        }
    }

    private final ProgramProjesiRepository projeRepository;
    private final ZamanIzgarasiRepository izgaraRepository;
    private final YerlesimRepository yerlesimRepository;
    private final YerlesimDerslikRepository yerlesimDerslikRepository;
    private final DersBloguRepository blokRepository;
    private final SubeRepository subeRepository;
    private final DersRepository dersRepository;
    private final DersSinifRepository dersSinifRepository;
    private final DerslikRepository derslikRepository;
    private final DerslikBolumRepository derslikBolumRepository;
    private final CakismaDenetleyici denetleyici;
    private final IslemGecmisiService gecmis;
    private final YetkiKontrol yetki;

    public OtomatikPlanlamaService(ProgramProjesiRepository projeRepository,
            ZamanIzgarasiRepository izgaraRepository,
            YerlesimRepository yerlesimRepository,
            YerlesimDerslikRepository yerlesimDerslikRepository,
            DersBloguRepository blokRepository,
            SubeRepository subeRepository,
            DersRepository dersRepository,
            DersSinifRepository dersSinifRepository,
            DerslikRepository derslikRepository,
            DerslikBolumRepository derslikBolumRepository,
            CakismaDenetleyici denetleyici,
            IslemGecmisiService gecmis,
            YetkiKontrol yetki) {
        this.projeRepository = projeRepository;
        this.izgaraRepository = izgaraRepository;
        this.yerlesimRepository = yerlesimRepository;
        this.yerlesimDerslikRepository = yerlesimDerslikRepository;
        this.blokRepository = blokRepository;
        this.subeRepository = subeRepository;
        this.dersRepository = dersRepository;
        this.dersSinifRepository = dersSinifRepository;
        this.derslikRepository = derslikRepository;
        this.derslikBolumRepository = derslikBolumRepository;
        this.denetleyici = denetleyici;
        this.gecmis = gecmis;
        this.yetki = yetki;
    }

    @Transactional
    public PlanlamaDto.Sonuc planla(Long projeId, PlanlamaDto.Istek istek) {
        long baslangicZamani = System.nanoTime();
        boolean uyariIzni = istek != null && Boolean.TRUE.equals(istek.uyarilaraIzinVer());
        int sureSn = (istek == null || istek.sureSaniye() == null) ? VARSAYILAN_SURE_SN : istek.sureSaniye();
        long bitisZamani = baslangicZamani + sureSn * 1_000_000_000L;

        ProgramProjesi proje = projeBul(projeId);
        yetki.bolumeErisim(proje.getBolumId());
        ZamanIzgarasi izgara = izgaraRepository.findByKurumId(proje.getKurumId())
                .orElseThrow(() -> new KayitBulunamadiException("Kurumun zaman ızgarası tanımlı değil"));

        List<DersBlogu> bekleyenler = new ArrayList<>(yerlesimRepository.yerlesmeyenBloklar(projeId));
        if (bekleyenler.isEmpty()) {
            return new PlanlamaDto.Sonuc(0, 0, false, gecenMs(baslangicZamani), List.of(), List.of());
        }
        List<Yerlesim> mevcutlar = yerlesimRepository.findByProjeId(projeId);

        Map<Long, DersBlogu> bloklar = new HashMap<>();
        for (DersBlogu b : bekleyenler) {
            bloklar.put(b.getId(), b);
        }
        blokRepository.findAllById(mevcutlar.stream().map(Yerlesim::getBlokId).toList())
                .forEach(b -> bloklar.put(b.getId(), b));
        Map<Long, Sube> subeler = subeRepository.findAllById(
                bloklar.values().stream().map(DersBlogu::getSubeId).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(Sube::getId, Function.identity()));
        Map<Long, Ders> dersler = dersRepository.findAllById(
                subeler.values().stream().map(Sube::getDersId).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(Ders::getId, Function.identity()));
        Map<Long, List<Long>> dersSiniflari = dersSinifRepository.findByDersIdIn(dersler.keySet())
                .stream().collect(Collectors.groupingBy(DersSinif::getDersId,
                        Collectors.mapping(DersSinif::getSinifId, Collectors.toList())));

        Durum durum = new Durum();
        for (Yerlesim y : mevcutlar) {
            DersBlogu b = bloklar.get(y.getBlokId());
            Sube s = subeler.get(b.getSubeId());
            durum.ekle(s, dersSiniflari.getOrDefault(s.getDersId(), List.of()),
                    y.getGun(), y.getBaslangicSlot(), b.getSure());
        }

        bekleyenler.sort(Comparator
                .comparingInt((DersBlogu b) -> -b.getSure())
                .thenComparingInt(b -> subeler.get(b.getSubeId()).getOgretimElemaniId() == null ? 1 : 0)
                .thenComparingLong(DersBlogu::getId));

        List<Derslik> kurumDersliklari = derslikRepository.findByKurumIdOrderByAd(proje.getKurumId());
         Map<String, List<Derslik>> bolumOnbellegi = new HashMap<>();

        List<PlanlamaDto.Yerlesen> yerlesenler = new ArrayList<>();
        List<PlanlamaDto.Yerlesmeyen> yerlesmeyenler = new ArrayList<>();
        boolean sureDoldu = false;

        for (DersBlogu blok : bekleyenler) {
            Sube sube = subeler.get(blok.getSubeId());
            Ders ders = dersler.get(sube.getDersId());

            if (sureDoldu) {
                yerlesmeyenler.add(yerlesmeyen(blok, ders, sube, "Süre doldu, bu blok için deneme yapılmadı"));
                continue;
            }

            List<Long> sinifIdler = dersSiniflari.getOrDefault(ders.getId(), List.of());
            List<Derslik> derslikler = sirala(
                    bolumDersliklari(ders, kurumDersliklari, bolumOnbellegi), sube.getTahminiOgrenci());
            
            // Eklenen kontrol bloğu
            if (derslikler.isEmpty()) {
                yerlesmeyenler.add(yerlesmeyen(blok, ders, sube, ders.isSanal()
                        ? "Ders sanal ama kurumda sanal sınıf tanımlı değil (Derslikler sayfasından derslik listesini yükleyin)"
                        : "Bu ders için denenecek derslik yok (bölüme tanımlı derslik bulunamadı)"));
                continue;
            }
            
            List<Aday> adaylar = adaylariUret(izgara, blok, sube, sinifIdler, derslikler, durum);

            Aday secilen = null;
            int secilenUyari = Integer.MAX_VALUE;
            Map<String, Integer> sebepler = new HashMap<>();

            for (Aday aday : adaylar) {
                if (System.nanoTime() > bitisZamani) {
                    sureDoldu = true;
                    break;
                }
                List<Ihlal> ihlaller = denetleyici.kontrolEt(blok, sube, ders, aday.gun(),
                        aday.baslangic(), List.of(aday.derslikId()));
                boolean engelVar = false;
                int uyari = 0;
                for (Ihlal i : ihlaller) {
                    if (i.tur() == IhlalTuru.ENGEL) {
                        engelVar = true;
                        sebepler.merge("ENGEL " + i.kod(), 1, Integer::sum);
                    } else if (i.tur() == IhlalTuru.UYARI) {
                        uyari++;
                    }
                }
                if (engelVar) {
                    continue;
                }
                if (uyari == 0) {
                    secilen = aday;
                    secilenUyari = 0;
                    break;
                }
                if (uyariIzni) {
                    if (uyari < secilenUyari) {
                        secilen = aday;
                        secilenUyari = uyari;
                    }
                } else {
                    for (Ihlal i : ihlaller) {
                        if (i.tur() == IhlalTuru.UYARI) {
                            sebepler.merge("UYARI " + i.kod(), 1, Integer::sum);
                        }
                    }
                }
            }

            if (secilen == null) {
                String sebep = sebepMetni(sebepler);
                yerlesmeyenler.add(yerlesmeyen(blok, ders, sube, sureDoldu ? "Süre doldu: " + sebep : sebep));
                continue;
            }

            Yerlesim yerlesim = yerlesimRepository.saveAndFlush(
                    new Yerlesim(blok.getId(), secilen.gun(), secilen.baslangic()));
            yerlesimDerslikRepository.saveAndFlush(new YerlesimDerslik(yerlesim.getId(), secilen.derslikId()));
            gecmis.kaydet(projeId, blok.getId(), IslemTuru.YERLESTIR, null,
                    new BlokDurumu(secilen.gun(), secilen.baslangic(), false, List.of(secilen.derslikId())));
            durum.ekle(sube, sinifIdler, secilen.gun(), secilen.baslangic(), blok.getSure());
            yerlesenler.add(new PlanlamaDto.Yerlesen(blok.getId(), ders.getKod(), sube.getSubeNo(),
                    blok.getSiraNo(), secilen.gun(), secilen.baslangic(),
                    secilen.baslangic() + blok.getSure() - 1, secilen.derslikId(), secilenUyari));
        }

        return new PlanlamaDto.Sonuc(yerlesenler.size(), yerlesmeyenler.size(), sureDoldu,
                gecenMs(baslangicZamani), yerlesenler, yerlesmeyenler);
    }

    /**
     * Kilitsiz tüm yerleşimleri kaldırır. Kilitli bloklar yerinde kalır.
     */
    @Transactional
    public PlanlamaDto.TemizleSonuc temizle(Long projeId) {
        ProgramProjesi proje = projeBul(projeId);
        yetki.bolumeErisim(proje.getBolumId());
        int silinen = 0;
        int kilitli = 0;
        for (Yerlesim y : yerlesimRepository.findByProjeId(projeId)) {
            if (y.isKilitli()) {
                kilitli++;
                continue;
            }
            BlokDurumu onceki = gecmis.mevcutDurum(y.getBlokId());
            yerlesimRepository.delete(y);
            yerlesimRepository.flush();
            gecmis.kaydet(projeId, y.getBlokId(), IslemTuru.KALDIR, onceki, null);
            silinen++;
        }
        return new PlanlamaDto.TemizleSonuc(silinen, kilitli);
    }

    private static List<Aday> adaylariUret(ZamanIzgarasi izgara, DersBlogu blok, Sube sube,
            List<Long> sinifIdler, List<Derslik> derslikler, Durum durum) {
        List<Aday> liste = new ArrayList<>();
        int enGecBaslangic = izgara.getGunlukSlotSayisi() - blok.getSure() + 1;
        for (int gun = 1; gun <= izgara.getGunSayisi(); gun++) {
            for (int baslangic = 1; baslangic <= enGecBaslangic; baslangic++) {
                int ceza = durum.ceza(sube, sinifIdler, gun, baslangic, blok.getSure());
                for (int d = 0; d < derslikler.size(); d++) {
                    liste.add(new Aday(gun, baslangic, derslikler.get(d).getId(), ceza + d));
                }
            }
        }
        liste.sort(Comparator.comparingInt(Aday::ceza));
        return liste;
    }

    private List<Derslik> bolumDersliklari(Ders ders, List<Derslik> kurumDersliklari,
            Map<String, List<Derslik>> onbellek) {
        return onbellek.computeIfAbsent(ders.getBolumId() + ":" + ders.isSanal(), anahtar -> {
            Set<Long> idler = derslikBolumRepository.findByBolumId(ders.getBolumId()).stream()
                    .map(DerslikBolum::getDerslikId)
                    .collect(Collectors.toSet());
            return DerslikSecici.adaylar(ders.isSanal(), kurumDersliklari, idler);
        });
    }

    /**
     * Tahmini öğrenci sayısına sığan derslikleri küçükten büyüğe, sığmayanları büyükten küçüğe sıralar.
     */
    private static List<Derslik> sirala(List<Derslik> adaylar, Integer tahmini) {
        if (tahmini == null) {
            return adaylar;
        }
        List<Derslik> kopya = new ArrayList<>(adaylar);
        kopya.sort(Comparator
                .comparingInt((Derslik d) -> d.getKapasite() >= tahmini ? 0 : 1)
                .thenComparingInt(d -> d.getKapasite() >= tahmini ? d.getKapasite() : -d.getKapasite()));
        return kopya;
    }

    private static PlanlamaDto.Yerlesmeyen yerlesmeyen(DersBlogu blok, Ders ders, Sube sube, String sebep) {
        return new PlanlamaDto.Yerlesmeyen(blok.getId(), ders.getKod(), sube.getSubeNo(),
                blok.getSiraNo(), blok.getSure(), sebep);
    }

    private static String sebepMetni(Map<String, Integer> sayilar) {
        if (sayilar.isEmpty()) {
            return "Denenecek uygun gün, saat ve derslik bulunamadı";
        }
        return sayilar.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .map(e -> e.getKey() + " (" + e.getValue() + ")")
                .collect(Collectors.joining(", "));
    }

    private static long gecenMs(long baslangicZamani) {
        return (System.nanoTime() - baslangicZamani) / 1_000_000;
    }

    private ProgramProjesi projeBul(Long projeId) {
        return projeRepository.findById(projeId)
                .orElseThrow(() -> new KayitBulunamadiException(
                        "Program projesi bulunamadı (id: " + projeId + ")"));
    }
}