package com.unitimetable.demo.ders;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unitimetable.demo.ortak.KayitBulunamadiException;

@Service
public class DersSinifService {

    private final DersSinifRepository repository;

    public DersSinifService(DersSinifRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<DersSinifDto.Yanit> listele(Long dersId, Long sinifId) {
        List<DersSinif> eslesmeler;
        if (dersId != null) {
            eslesmeler = repository.findByDersId(dersId);
        } else if (sinifId != null) {
            eslesmeler = repository.findBySinifId(sinifId);
        } else {
            eslesmeler = repository.findAll();
        }
        return eslesmeler.stream().map(DersSinifService::yanita).toList();
    }

    @Transactional
    public DersSinifDto.Yanit olustur(DersSinifDto.Istek istek) {
        DersSinif kaydedilen = repository.saveAndFlush(new DersSinif(istek.dersId(), istek.sinifId()));
        return yanita(kaydedilen);
    }

    @Transactional
    public void sil(Long id) {
        DersSinif eslesme = repository.findById(id)
                .orElseThrow(() -> new KayitBulunamadiException("Ders-sınıf eşleşmesi bulunamadı (id: " + id + ")"));
        repository.delete(eslesme);
        repository.flush();
    }

    private static DersSinifDto.Yanit yanita(DersSinif e) {
        return new DersSinifDto.Yanit(e.getId(), e.getDersId(), e.getSinifId());
    }
}