package com.unitimetable.demo.derslik;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unitimetable.demo.ortak.GecersizIslemException;
import com.unitimetable.demo.ortak.KayitBulunamadiException;

@Service
public class LabGunDagilimiService {

    private final LabGunDagilimiRepository repository;
    private final DerslikRepository derslikRepository;

    public LabGunDagilimiService(LabGunDagilimiRepository repository, DerslikRepository derslikRepository) {
        this.repository = repository;
        this.derslikRepository = derslikRepository;
    }

    @Transactional(readOnly = true)
    public List<LabGunDagilimiDto.Yanit> listele(Long derslikId, Long bolumId) {
        List<LabGunDagilimi> kayitlar;
        if (derslikId != null) {
            kayitlar = repository.findByDerslikIdOrderByGun(derslikId);
        } else if (bolumId != null) {
            kayitlar = repository.findByBolumId(bolumId);
        } else {
            kayitlar = repository.findAll();
        }
        return kayitlar.stream().map(LabGunDagilimiService::yanita).toList();
    }

    @Transactional
    public LabGunDagilimiDto.Yanit olustur(LabGunDagilimiDto.Istek istek) {
        Derslik derslik = derslikRepository.findById(istek.derslikId())
                .orElseThrow(() -> new KayitBulunamadiException(
                        "Derslik bulunamadı (id: " + istek.derslikId() + ")"));
        if (derslik.getTur() != DerslikTuru.LABORATUVAR) {
            throw new GecersizIslemException(
                    "Gün dağılımı yalnızca laboratuvarlar için tanımlanabilir (" + derslik.getAd() + " bir laboratuvar değil)");
        }
        LabGunDagilimi kaydedilen = repository.saveAndFlush(
                new LabGunDagilimi(istek.derslikId(), istek.bolumId(), istek.gun()));
        return yanita(kaydedilen);
    }

    @Transactional
    public LabGunDagilimiDto.Yanit guncelle(Long id, LabGunDagilimiDto.Guncelleme istek) {
        LabGunDagilimi kayit = bul(id);
        kayit.setBolumId(istek.bolumId());
        repository.flush();
        return yanita(kayit);
    }

    @Transactional
    public void sil(Long id) {
        repository.delete(bul(id));
        repository.flush();
    }

    private LabGunDagilimi bul(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new KayitBulunamadiException("Gün dağılımı kaydı bulunamadı (id: " + id + ")"));
    }

    private static LabGunDagilimiDto.Yanit yanita(LabGunDagilimi k) {
        return new LabGunDagilimiDto.Yanit(k.getId(), k.getDerslikId(), k.getBolumId(), k.getGun());
    }
}