package com.unitimetable.demo.derslik;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unitimetable.demo.ortak.KayitBulunamadiException;

@Service
public class DerslikBolumService {

    private final DerslikBolumRepository repository;

    public DerslikBolumService(DerslikBolumRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<DerslikBolumDto.Yanit> listele(Long derslikId, Long bolumId) {
        List<DerslikBolum> eslesmeler;
        if (derslikId != null) {
            eslesmeler = repository.findByDerslikId(derslikId);
        } else if (bolumId != null) {
            eslesmeler = repository.findByBolumId(bolumId);
        } else {
            eslesmeler = repository.findAll();
        }
        return eslesmeler.stream().map(DerslikBolumService::yanita).toList();
    }

    @Transactional
    public DerslikBolumDto.Yanit olustur(DerslikBolumDto.Istek istek) {
        DerslikBolum kaydedilen = repository.saveAndFlush(
                new DerslikBolum(istek.derslikId(), istek.bolumId(), istek.ortakMi()));
        return yanita(kaydedilen);
    }

    @Transactional
    public DerslikBolumDto.Yanit guncelle(Long id, DerslikBolumDto.Guncelleme istek) {
        DerslikBolum eslesme = bul(id);
        eslesme.setOrtakMi(istek.ortakMi());
        return yanita(eslesme);
    }

    @Transactional
    public void sil(Long id) {
        repository.delete(bul(id));
        repository.flush();
    }

    private DerslikBolum bul(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new KayitBulunamadiException("Eşleşme bulunamadı (id: " + id + ")"));
    }

    private static DerslikBolumDto.Yanit yanita(DerslikBolum e) {
        return new DerslikBolumDto.Yanit(e.getId(), e.getDerslikId(), e.getBolumId(), e.isOrtakMi());
    }
}