package com.unitimetable.demo.ogretimelemani;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unitimetable.demo.ortak.GecersizIslemException;
import com.unitimetable.demo.ortak.KayitBulunamadiException;
import com.unitimetable.demo.ortak.YetkiKontrol;

@Service
public class HocaGunTercihiService {

    private final HocaGunTercihiRepository repository;
    private final OgretimElemaniRepository hocaRepository;
    private final YetkiKontrol yetki;

    public HocaGunTercihiService(HocaGunTercihiRepository repository,
            OgretimElemaniRepository hocaRepository, YetkiKontrol yetki) {
        this.repository = repository;
        this.hocaRepository = hocaRepository;
        this.yetki = yetki;
    }

    @Transactional(readOnly = true)
    public List<HocaGunTercihiDto.Yanit> listele(Long hocaId) {
        hocayiDogrula(hocaId);
        return yanitlar(hocaId);
    }

    @Transactional
    public List<HocaGunTercihiDto.Yanit> degistir(Long hocaId, HocaGunTercihiDto.Istek istek) {
        hocayiDogrula(hocaId);
        kurallariDogrula(istek.kayitlar());

        repository.deleteByOgretimElemaniId(hocaId);
        repository.flush();
        for (HocaGunTercihiDto.Kayit k : istek.kayitlar()) {
            repository.save(new HocaGunTercihi(
                    hocaId, k.tur(), k.deger(), k.gun(),
                    k.katilik() == null ? TercihKatiligi.TERCIH : k.katilik(),
                    k.asistanaBirakildi() != null && k.asistanaBirakildi()));
        }
        repository.flush();
        return yanitlar(hocaId);
    }

    private static void kurallariDogrula(List<HocaGunTercihiDto.Kayit> kayitlar) {
        boolean bosGunVar = false;
        boolean ardisikVar = false;
        Set<Integer> tercihEdilenler = new HashSet<>();
        Set<Integer> kacinilanlar = new HashSet<>();

        for (HocaGunTercihiDto.Kayit k : kayitlar) {
            boolean birakildi = k.asistanaBirakildi() != null && k.asistanaBirakildi();
            if (birakildi && k.tur() != GunTercihTuru.ARDISIK_GUN) {
                throw new GecersizIslemException(
                        "Günü asistana bırakma yalnızca ardışık gün tercihinde kullanılabilir");
            }
            switch (k.tur()) {
                case BOS_GUN_SAYISI -> {
                    if (k.deger() == null) {
                        throw new GecersizIslemException("Boş gün sayısı için değer zorunludur (1-6)");
                    }
                    if (k.gun() != null) {
                        throw new GecersizIslemException("Boş gün sayısı tercihinde gün belirtilemez");
                    }
                    if (bosGunVar) {
                        throw new GecersizIslemException("Boş gün sayısı tercihi yalnızca bir kez girilebilir");
                    }
                    bosGunVar = true;
                }
                case ARDISIK_GUN -> {
                    if (k.deger() != null || k.gun() != null) {
                        throw new GecersizIslemException(
                                "Ardışık gün tercihinde değer ya da gün belirtilemez");
                    }
                    if (ardisikVar) {
                        throw new GecersizIslemException("Ardışık gün tercihi yalnızca bir kez girilebilir");
                    }
                    ardisikVar = true;
                }
                case TERCIH_EDILEN_GUN, KACINILAN_GUN -> {
                    if (k.gun() == null) {
                        throw new GecersizIslemException("Tercih edilen ve kaçınılan günler için gün zorunludur");
                    }
                    if (k.deger() != null) {
                        throw new GecersizIslemException("Gün tercihinde değer belirtilemez");
                    }
                    Set<Integer> kendi = (k.tur() == GunTercihTuru.TERCIH_EDILEN_GUN)
                            ? tercihEdilenler : kacinilanlar;
                    Set<Integer> karsi = (k.tur() == GunTercihTuru.TERCIH_EDILEN_GUN)
                            ? kacinilanlar : tercihEdilenler;
                    if (karsi.contains(k.gun())) {
                        throw new GecersizIslemException("Aynı gün hem tercih edilen hem kaçınılan olamaz (gün: "
                                + k.gun() + ")");
                    }
                    if (!kendi.add(k.gun())) {
                        throw new GecersizIslemException("Aynı gün aynı tercih türünde birden fazla kez girilemez (gün: "
                                + k.gun() + ")");
                    }
                }
            }
        }
    }

    private void hocayiDogrula(Long hocaId) {
        OgretimElemani hoca = hocaRepository.findById(hocaId)
                .orElseThrow(() -> new KayitBulunamadiException(
                        "Öğretim elemanı bulunamadı (id: " + hocaId + ")"));
        yetki.bolumeErisim(hoca.getBolumId());
    }

    private List<HocaGunTercihiDto.Yanit> yanitlar(Long hocaId) {
        return repository.findByOgretimElemaniIdOrderByIdAsc(hocaId).stream()
                .map(t -> new HocaGunTercihiDto.Yanit(t.getId(), t.getTur(), t.getDeger(), t.getGun(),
                        t.getKatilik(), t.isAsistanaBirakildi()))
                .toList();
    }
}