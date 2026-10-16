package com.unitimetable.demo.sinif;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unitimetable.demo.ortak.KayitBulunamadiException;

@Service
public class SinifService {

    private final SinifRepository repository;

    public SinifService(SinifRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<SinifDto.Yanit> listele(Long programId) {
        List<Sinif> siniflar = (programId == null)
                ? repository.findAll(Sort.by("programId", "duzey"))
                : repository.findByProgramIdOrderByDuzey(programId);
        return siniflar.stream().map(SinifService::yanita).toList();
    }

    @Transactional(readOnly = true)
    public SinifDto.Yanit getir(Long id) {
        return yanita(bul(id));
    }

    @Transactional
    public SinifDto.Yanit olustur(SinifDto.Istek istek) {
        Sinif kaydedilen = repository.save(new Sinif(istek.programId(), istek.ad().trim(), istek.duzey()));
        return yanita(kaydedilen);
    }

    @Transactional
    public SinifDto.Yanit guncelle(Long id, SinifDto.Guncelleme istek) {
        Sinif sinif = bul(id);
        sinif.setAd(istek.ad().trim());
        sinif.setDuzey(istek.duzey());
        return yanita(sinif);
    }

    @Transactional
    public void sil(Long id) {
        Sinif sinif = bul(id);
        repository.delete(sinif);
        repository.flush();
    }

    private Sinif bul(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new KayitBulunamadiException("Sınıf bulunamadı (id: " + id + ")"));
    }

    private static SinifDto.Yanit yanita(Sinif s) {
        return new SinifDto.Yanit(s.getId(), s.getProgramId(), s.getAd(), s.getDuzey());
    }
}