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
public class HocaMusaitlikService {

    private final HocaMusaitlikRepository repository;
    private final OgretimElemaniRepository hocaRepository;
    private final YetkiKontrol yetki;

    public HocaMusaitlikService(HocaMusaitlikRepository repository,
            OgretimElemaniRepository hocaRepository, YetkiKontrol yetki) {
        this.repository = repository;
        this.hocaRepository = hocaRepository;
        this.yetki = yetki;
    }

    @Transactional(readOnly = true)
    public List<HocaMusaitlikDto.Yanit> listele(Long hocaId) {
        hocayiDogrula(hocaId);
        return yanitlar(hocaId);
    }

    @Transactional
    public List<HocaMusaitlikDto.Yanit> degistir(Long hocaId, HocaMusaitlikDto.Istek istek) {
        hocayiDogrula(hocaId);

        Set<String> gorulenler = new HashSet<>();
        for (HocaMusaitlikDto.Kayit kayit : istek.kayitlar()) {
            if (!gorulenler.add(kayit.gun() + "-" + kayit.slot())) {
                throw new GecersizIslemException("Aynı gün ve saat birden fazla kez gönderildi (gün: "
                        + kayit.gun() + ", saat: " + kayit.slot() + ")");
            }
        }

        repository.deleteByOgretimElemaniId(hocaId);
        repository.flush();
        List<HocaMusaitlik> yeniler = istek.kayitlar().stream()
                .map(k -> new HocaMusaitlik(hocaId, k.gun(), k.slot(), k.durum()))
                .toList();
        repository.saveAll(yeniler);
        repository.flush();
        return yanitlar(hocaId);
    }

    private void hocayiDogrula(Long hocaId) {
        OgretimElemani hoca = hocaRepository.findById(hocaId)
                .orElseThrow(() -> new KayitBulunamadiException(
                        "Öğretim elemanı bulunamadı (id: " + hocaId + ")"));
        yetki.bolumeErisim(hoca.getBolumId());
    }

    private List<HocaMusaitlikDto.Yanit> yanitlar(Long hocaId) {
        return repository.findByOgretimElemaniIdOrderByGunAscSlotAsc(hocaId).stream()
                .map(m -> new HocaMusaitlikDto.Yanit(m.getId(), m.getGun(), m.getSlot(), m.getDurum()))
                .toList();
    }
}