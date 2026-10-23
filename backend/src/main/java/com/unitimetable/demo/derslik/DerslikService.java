package com.unitimetable.demo.derslik;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unitimetable.demo.ortak.KayitBulunamadiException;
import com.unitimetable.demo.yerlesim.DerslikKullanimService;
import com.unitimetable.demo.yerlesim.SilinemezException;

@Service
public class DerslikService {

    private final DerslikRepository repository;
    private final DerslikKullanimService kullanimService;

    public DerslikService(DerslikRepository repository, DerslikKullanimService kullanimService) {
        this.repository = repository;
        this.kullanimService = kullanimService;
    }

    @Transactional(readOnly = true)
    public List<DerslikDto.Yanit> listele(Long kurumId) {
        List<Derslik> derslikler = (kurumId == null)
                ? repository.findAll(Sort.by("kurumId", "ad"))
                : repository.findByKurumIdOrderByAd(kurumId);
        return derslikler.stream().map(DerslikService::yanita).toList();
    }

    @Transactional(readOnly = true)
    public DerslikDto.Yanit getir(Long id) {
        return yanita(bul(id));
    }

    @Transactional
    public DerslikDto.Yanit olustur(DerslikDto.Istek istek) {
        Derslik kaydedilen = repository.saveAndFlush(new Derslik(
                istek.kurumId(), istek.ad().trim(), istek.kapasite(), istek.tur()));
        return yanita(kaydedilen);
    }

    @Transactional
    public DerslikDto.Yanit guncelle(Long id, DerslikDto.Guncelleme istek) {
        Derslik derslik = bul(id);
        derslik.setAd(istek.ad().trim());
        derslik.setKapasite(istek.kapasite());
        derslik.setTur(istek.tur());
        repository.flush();
        return yanita(derslik);
    }

    @Transactional
    public void sil(Long id) {
        Derslik derslik = bul(id);
        List<DerslikKullanimService.Kullanim> kullanimlar = kullanimService.kullanimlar(id);
        if (!kullanimlar.isEmpty()) {
            throw new SilinemezException("Derslik (" + derslik.getAd() + ") silinemez: içinde "
                    + kullanimlar.size() + " ders bloğu yerleşik. Önce bu blokları kaldırın "
                    + "ya da başka dersliklere taşıyın", kullanimlar);
        }
        repository.delete(derslik);
        repository.flush();
    }

    private Derslik bul(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new KayitBulunamadiException("Derslik bulunamadı (id: " + id + ")"));
    }

    private static DerslikDto.Yanit yanita(Derslik d) {
        return new DerslikDto.Yanit(d.getId(), d.getKurumId(), d.getAd(), d.getKapasite(), d.getTur());
    }
}