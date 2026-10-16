package com.unitimetable.demo.fakulte;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unitimetable.demo.ortak.KayitBulunamadiException;

@Service
public class FakulteService {

    private final FakulteRepository repository;

    public FakulteService(FakulteRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<FakulteDto.Yanit> listele() {
        return repository.findAll().stream().map(FakulteService::yanita).toList();
    }

    @Transactional(readOnly = true)
    public FakulteDto.Yanit getir(Long id) {
        return yanita(bul(id));
    }

    @Transactional
    public FakulteDto.Yanit olustur(FakulteDto.Istek istek) {
        Fakulte kaydedilen = repository.save(new Fakulte(istek.kurumId(), istek.ad().trim()));
        return yanita(kaydedilen);
    }

    @Transactional
    public FakulteDto.Yanit guncelle(Long id, FakulteDto.Guncelleme istek) {
        Fakulte fakulte = bul(id);
        fakulte.setAd(istek.ad().trim());
        return yanita(fakulte);
    }

    @Transactional
    public void sil(Long id) {
        Fakulte fakulte = bul(id);
        repository.delete(fakulte);
        repository.flush();
    }

    private Fakulte bul(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new KayitBulunamadiException("Fakülte bulunamadı (id: " + id + ")"));
    }

    private static FakulteDto.Yanit yanita(Fakulte f) {
        return new FakulteDto.Yanit(f.getId(), f.getKurumId(), f.getAd());
    }
}