package com.unitimetable.demo.bolum;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unitimetable.demo.ortak.KayitBulunamadiException;

@Service
public class BolumService {

    private final BolumRepository repository;

    public BolumService(BolumRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<BolumDto.Yanit> listele(Long fakulteId) {
        List<Bolum> bolumler = (fakulteId == null)
                ? repository.findAll()
                : repository.findByFakulteId(fakulteId);
        return bolumler.stream().map(BolumService::yanita).toList();
    }

    @Transactional(readOnly = true)
    public BolumDto.Yanit getir(Long id) {
        return yanita(bul(id));
    }

    @Transactional
    public BolumDto.Yanit olustur(BolumDto.Istek istek) {
        Bolum kaydedilen = repository.save(new Bolum(istek.fakulteId(), istek.ad().trim()));
        return yanita(kaydedilen);
    }

    @Transactional
    public BolumDto.Yanit guncelle(Long id, BolumDto.Guncelleme istek) {
        Bolum bolum = bul(id);
        bolum.setAd(istek.ad().trim());
        return yanita(bolum);
    }

    @Transactional
    public void sil(Long id) {
        Bolum bolum = bul(id);
        repository.delete(bolum);
        repository.flush();
    }

    private Bolum bul(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new KayitBulunamadiException("Bölüm bulunamadı (id: " + id + ")"));
    }

    private static BolumDto.Yanit yanita(Bolum b) {
        return new BolumDto.Yanit(b.getId(), b.getFakulteId(), b.getAd());
    }
}