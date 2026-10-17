package com.unitimetable.demo.proje;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unitimetable.demo.ortak.KayitBulunamadiException;

@Service
public class ProgramProjesiService {

    private final ProgramProjesiRepository repository;

    public ProgramProjesiService(ProgramProjesiRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ProgramProjesiDto.Yanit> listele(Long bolumId) {
        List<ProgramProjesi> projeler = (bolumId == null)
                ? repository.findAll(Sort.by(Sort.Direction.DESC, "olusturmaTarihi"))
                : repository.findByBolumIdOrderByOlusturmaTarihiDesc(bolumId);
        return projeler.stream().map(ProgramProjesiService::yanita).toList();
    }

    @Transactional(readOnly = true)
    public ProgramProjesiDto.Yanit getir(Long id) {
        return yanita(bul(id));
    }

    @Transactional
    public ProgramProjesiDto.Yanit olustur(ProgramProjesiDto.Istek istek) {
        ProgramProjesi kaydedilen = repository.saveAndFlush(new ProgramProjesi(
                istek.kurumId(), istek.bolumId(), istek.ad().trim(), istek.donem().trim()));
        return yanita(kaydedilen);
    }

    @Transactional
    public ProgramProjesiDto.Yanit guncelle(Long id, ProgramProjesiDto.Guncelleme istek) {
        ProgramProjesi proje = bul(id);
        proje.setAd(istek.ad().trim());
        proje.setDonem(istek.donem().trim());
        proje.setDurum(istek.durum());
        repository.flush();
        return yanita(proje);
    }

    @Transactional
    public void sil(Long id) {
        repository.delete(bul(id));
        repository.flush();
    }

    private ProgramProjesi bul(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new KayitBulunamadiException("Program projesi bulunamadı (id: " + id + ")"));
    }

    private static ProgramProjesiDto.Yanit yanita(ProgramProjesi p) {
        return new ProgramProjesiDto.Yanit(p.getId(), p.getKurumId(), p.getBolumId(),
                p.getAd(), p.getDonem(), p.getDurum(), p.getOlusturmaTarihi());
    }
}