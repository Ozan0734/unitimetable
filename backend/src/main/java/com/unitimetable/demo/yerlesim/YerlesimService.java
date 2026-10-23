package com.unitimetable.demo.yerlesim;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unitimetable.demo.blok.DersBlogu;
import com.unitimetable.demo.blok.DersBloguRepository;
import com.unitimetable.demo.ders.Ders;
import com.unitimetable.demo.ders.DersRepository;
import com.unitimetable.demo.derslik.Derslik;
import com.unitimetable.demo.derslik.DerslikRepository;
import com.unitimetable.demo.izgara.ZamanIzgarasi;
import com.unitimetable.demo.izgara.ZamanIzgarasiRepository;
import com.unitimetable.demo.ortak.GecersizIslemException;
import com.unitimetable.demo.ortak.KayitBulunamadiException;
import com.unitimetable.demo.ortak.YetkiKontrol;
import com.unitimetable.demo.proje.ProgramProjesi;
import com.unitimetable.demo.proje.ProgramProjesiRepository;
import com.unitimetable.demo.sube.Sube;
import com.unitimetable.demo.sube.SubeRepository;

@Service
public class YerlesimService {

    private record Baglam(DersBlogu blok, Sube sube, Ders ders, ProgramProjesi proje) {
    }

    private final YerlesimRepository repository;
    private final YerlesimDerslikRepository derslikEslesmeRepository;
    private final DersBloguRepository blokRepository;
    private final SubeRepository subeRepository;
    private final DersRepository dersRepository;
    private final ProgramProjesiRepository projeRepository;
    private final DerslikRepository derslikRepository;
    private final ZamanIzgarasiRepository izgaraRepository;
    private final CakismaDenetleyici denetleyici;
    private final YetkiKontrol yetki;

    public YerlesimService(YerlesimRepository repository,
            YerlesimDerslikRepository derslikEslesmeRepository,
            DersBloguRepository blokRepository,
            SubeRepository subeRepository,
            DersRepository dersRepository,
            ProgramProjesiRepository projeRepository,
            DerslikRepository derslikRepository,
            ZamanIzgarasiRepository izgaraRepository,
            CakismaDenetleyici denetleyici,
            YetkiKontrol yetki) {
        this.repository = repository;
        this.derslikEslesmeRepository = derslikEslesmeRepository;
        this.blokRepository = blokRepository;
        this.subeRepository = subeRepository;
        this.dersRepository = dersRepository;
        this.projeRepository = projeRepository;
        this.derslikRepository = derslikRepository;
        this.izgaraRepository = izgaraRepository;
        this.denetleyici = denetleyici;
        this.yetki = yetki;
    }

    @Transactional(readOnly = true)
    public List<YerlesimDto.Yanit> listele(Long projeId, Long subeId) {
        List<Yerlesim> yerlesimler;
        if (subeId != null) {
            Sube sube = subeBul(subeId);
            yetki.bolumeErisim(dersBul(sube.getDersId()).getBolumId());
            yerlesimler = repository.findBySubeId(subeId);
        } else if (projeId != null) {
            ProgramProjesi proje = projeBul(projeId);
            yetki.bolumeErisim(proje.getBolumId());
            yerlesimler = repository.findByProjeId(projeId);
        } else {
            throw new GecersizIslemException("projeId ya da subeId verilmelidir");
        }
        return yanitlar(yerlesimler, List.of());
    }

    @Transactional(readOnly = true)
    public List<YerlesimDto.YerlesmeyenBlok> yerlesmeyenler(Long projeId) {
        ProgramProjesi proje = projeBul(projeId);
        yetki.bolumeErisim(proje.getBolumId());
        List<DersBlogu> bloklar = repository.yerlesmeyenBloklar(projeId);
        if (bloklar.isEmpty()) {
            return List.of();
        }
        Map<Long, Sube> subeler = subeRepository.findAllById(
                bloklar.stream().map(DersBlogu::getSubeId).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(Sube::getId, Function.identity()));
        Map<Long, Ders> dersler = dersRepository.findAllById(
                subeler.values().stream().map(Sube::getDersId).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(Ders::getId, Function.identity()));
        return bloklar.stream().map(b -> {
            Sube s = subeler.get(b.getSubeId());
            Ders d = dersler.get(s.getDersId());
            return new YerlesimDto.YerlesmeyenBlok(b.getId(), s.getId(), d.getId(), d.getKod(),
                    d.getAd(), s.getSubeNo(), b.getSiraNo(), b.getSure());
        }).toList();
    }

    /**
     * Bloğu bu yere koymadan, oluşacak ihlalleri döndürür. Sürükle-bırak sırasında anlık
     * geri bildirim için kullanılır.
     */
    @Transactional(readOnly = true)
    public List<Ihlal> kontrol(Long blokId, YerlesimDto.Istek istek) {
        Baglam b = baglam(blokId);
        List<Long> derslikIdler = girdiyiDogrula(b, istek);
        return denetleyici.kontrolEt(b.blok(), b.sube(), b.ders(), istek.gun(),
                istek.baslangicSlot(), derslikIdler);
    }

    @Transactional
    public YerlesimDto.Yanit yerlestir(Long blokId, YerlesimDto.Istek istek) {
        Baglam b = baglam(blokId);

        Yerlesim mevcut = repository.findByBlokId(blokId).orElse(null);
        if (mevcut != null && mevcut.isKilitli()) {
            throw new GecersizIslemException("Bu blok kilitli, taşımak için önce kilidi kaldırın");
        }

        List<Long> derslikIdler = girdiyiDogrula(b, istek);

        List<Ihlal> ihlaller = denetleyici.kontrolEt(b.blok(), b.sube(), b.ders(), istek.gun(),
                istek.baslangicSlot(), derslikIdler);
        boolean engelVar = ihlaller.stream().anyMatch(i -> i.tur() == IhlalTuru.ENGEL);
        boolean uyariVar = ihlaller.stream().anyMatch(i -> i.tur() == IhlalTuru.UYARI);
        if (engelVar) {
            throw new CakismaException(ihlaller, false);
        }
        if (uyariVar && !Boolean.TRUE.equals(istek.uyarilariOnayla())) {
            throw new CakismaException(ihlaller, true);
        }

        Yerlesim yerlesim;
        if (mevcut == null) {
            yerlesim = repository.saveAndFlush(new Yerlesim(blokId, istek.gun(), istek.baslangicSlot()));
        } else {
            mevcut.tasi(istek.gun(), istek.baslangicSlot());
            derslikEslesmeRepository.deleteByYerlesimId(mevcut.getId());
            derslikEslesmeRepository.flush();
            yerlesim = mevcut;
        }
        for (Long derslikId : derslikIdler) {
            derslikEslesmeRepository.save(new YerlesimDerslik(yerlesim.getId(), derslikId));
        }
        repository.flush();
        derslikEslesmeRepository.flush();
        return yanitlar(List.of(yerlesim), ihlaller).get(0);
    }

    @Transactional
    public void kaldir(Long blokId) {
        Yerlesim yerlesim = yetkiliYerlesim(blokId);
        if (yerlesim.isKilitli()) {
            throw new GecersizIslemException("Kilitli bloğu kaldırmak için önce kilidi kaldırın");
        }
        repository.delete(yerlesim);
        repository.flush();
    }

    @Transactional
    public YerlesimDto.Yanit kilitle(Long blokId, boolean kilitli) {
        Yerlesim yerlesim = yetkiliYerlesim(blokId);
        yerlesim.setKilitli(kilitli);
        repository.flush();
        return yanitlar(List.of(yerlesim), List.of()).get(0);
    }

    private Baglam baglam(Long blokId) {
        DersBlogu blok = blokBul(blokId);
        Sube sube = subeBul(blok.getSubeId());
        Ders ders = dersBul(sube.getDersId());
        yetki.bolumeErisim(ders.getBolumId());
        ProgramProjesi proje = projeBul(ders.getProjeId());
        return new Baglam(blok, sube, ders, proje);
    }

    private List<Long> girdiyiDogrula(Baglam b, YerlesimDto.Istek istek) {
        izgarayiDogrula(b.proje().getKurumId(), istek.gun(), istek.baslangicSlot(), b.blok().getSure());
        return dersliklerDogrula(b.proje().getKurumId(), b.sube(), istek.derslikIdler());
    }

    private void izgarayiDogrula(Long kurumId, int gun, int baslangicSlot, int sure) {
        ZamanIzgarasi izgara = izgaraRepository.findByKurumId(kurumId)
                .orElseThrow(() -> new KayitBulunamadiException("Kurumun zaman ızgarası tanımlı değil"));
        if (gun > izgara.getGunSayisi()) {
            throw new GecersizIslemException(
                    "Gün, zaman ızgarasının dışında (en fazla " + izgara.getGunSayisi() + " gün)");
        }
        int bitis = baslangicSlot + sure - 1;
        if (bitis > izgara.getGunlukSlotSayisi()) {
            throw new GecersizIslemException("Blok günün dışına taşıyor: " + sure + " saatlik blok "
                    + baslangicSlot + ". saatte başlarsa " + bitis + ". saate kadar sürer, günde en fazla "
                    + izgara.getGunlukSlotSayisi() + " ders saati var");
        }
    }

    private List<Long> dersliklerDogrula(Long kurumId, Sube sube, List<Long> idler) {
        Set<Long> tekil = new LinkedHashSet<>(idler);
        if (tekil.size() != idler.size()) {
            throw new GecersizIslemException("Aynı derslik birden fazla kez seçilemez");
        }
        if (tekil.size() > 1 && !sube.isCokluDerslikIzni()) {
            throw new GecersizIslemException("Bu şube için birden fazla derslik kullanma izni yok");
        }
        List<Derslik> bulunanlar = derslikRepository.findAllById(tekil);
        if (bulunanlar.size() != tekil.size()) {
            throw new KayitBulunamadiException("Seçilen derslik bulunamadı");
        }
        for (Derslik derslik : bulunanlar) {
            if (!derslik.getKurumId().equals(kurumId)) {
                throw new GecersizIslemException("Derslik (" + derslik.getAd() + ") bu kuruma ait değil");
            }
        }
        return new ArrayList<>(tekil);
    }

    private Yerlesim yetkiliYerlesim(Long blokId) {
        DersBlogu blok = blokBul(blokId);
        Ders ders = dersBul(subeBul(blok.getSubeId()).getDersId());
        yetki.bolumeErisim(ders.getBolumId());
        return repository.findByBlokId(blokId)
                .orElseThrow(() -> new KayitBulunamadiException(
                        "Blok henüz yerleştirilmemiş (blok id: " + blokId + ")"));
    }

    private List<YerlesimDto.Yanit> yanitlar(List<Yerlesim> yerlesimler, List<Ihlal> ihlaller) {
        if (yerlesimler.isEmpty()) {
            return List.of();
        }
        Map<Long, DersBlogu> bloklar = blokRepository.findAllById(
                yerlesimler.stream().map(Yerlesim::getBlokId).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(DersBlogu::getId, Function.identity()));
        Map<Long, Sube> subeler = subeRepository.findAllById(
                bloklar.values().stream().map(DersBlogu::getSubeId).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(Sube::getId, Function.identity()));
        Map<Long, List<Long>> derslikler = derslikEslesmeRepository.findByYerlesimIdIn(
                yerlesimler.stream().map(Yerlesim::getId).toList())
                .stream().collect(Collectors.groupingBy(YerlesimDerslik::getYerlesimId,
                        Collectors.mapping(YerlesimDerslik::getDerslikId, Collectors.toList())));

        return yerlesimler.stream().map(y -> {
            DersBlogu blok = bloklar.get(y.getBlokId());
            Sube sube = subeler.get(blok.getSubeId());
            List<Long> idler = derslikler.getOrDefault(y.getId(), List.of()).stream().sorted().toList();
            return new YerlesimDto.Yanit(y.getId(), blok.getId(), sube.getId(), sube.getDersId(),
                    y.getGun(), y.getBaslangicSlot(), y.getBaslangicSlot() + blok.getSure() - 1,
                    blok.getSure(), idler, y.isKilitli(), ihlaller);
        }).toList();
    }

    private DersBlogu blokBul(Long blokId) {
        return blokRepository.findById(blokId)
                .orElseThrow(() -> new KayitBulunamadiException("Blok bulunamadı (id: " + blokId + ")"));
    }

    private Sube subeBul(Long subeId) {
        return subeRepository.findById(subeId)
                .orElseThrow(() -> new KayitBulunamadiException("Şube bulunamadı (id: " + subeId + ")"));
    }

    private Ders dersBul(Long dersId) {
        return dersRepository.findById(dersId)
                .orElseThrow(() -> new KayitBulunamadiException("Ders bulunamadı (id: " + dersId + ")"));
    }

    private ProgramProjesi projeBul(Long projeId) {
        return projeRepository.findById(projeId)
                .orElseThrow(() -> new KayitBulunamadiException(
                        "Program projesi bulunamadı (id: " + projeId + ")"));
    }
}