package com.unitimetable.demo.proje;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unitimetable.demo.ortak.KayitBulunamadiException;
import com.unitimetable.demo.ortak.YetkiKontrol;

@Service
public class ProgramProjesiService {

    private final ProgramProjesiRepository repository;
    private final YetkiKontrol yetki;

    public ProgramProjesiService(ProgramProjesiRepository repository, YetkiKontrol yetki) {
        this.repository = repository;
        this.yetki = yetki;
    }

    @Transactional(readOnly = true)
    public List<ProgramProjesiDto.Yanit> listele(Long bolumId) {
        Sort siralama = Sort.by(Sort.Direction.DESC, "olusturmaTarihi");
        List<ProgramProjesi> projeler;
        if (bolumId != null) {
            yetki.bolumeErisim(bolumId);
            projeler = repository.findByBolumIdOrderByOlusturmaTarihiDesc(bolumId);
        } else {
            List<Long> izinli = yetki.erisilebilirBolumler();
            if (izinli == null) {
                projeler = repository.findAll(siralama);
            } else if (izinli.isEmpty()) {
                projeler = List.of();
            } else {
                projeler = repository.findByBolumIdIn(izinli, siralama);
            }
        }
        return projeler.stream().map(ProgramProjesiService::yanita).toList();
    }

    @Transactional(readOnly = true)
    public ProgramProjesiDto.Yanit getir(Long id) {
        ProgramProjesi proje = bul(id);
        yetki.bolumeErisim(proje.getBolumId());
        return yanita(proje);
    }

    @Transactional
    public ProgramProjesiDto.Yanit olustur(ProgramProjesiDto.Istek istek) {
        yetki.bolumeErisim(istek.bolumId());
        ProgramProjesi kaydedilen = repository.saveAndFlush(new ProgramProjesi(
                istek.kurumId(), istek.bolumId(), istek.ad().trim(), istek.donem().trim()));
        return yanita(kaydedilen);
    }

    @Transactional
    public ProgramProjesiDto.Yanit guncelle(Long id, ProgramProjesiDto.Guncelleme istek) {
        ProgramProjesi proje = bul(id);
        yetki.bolumeErisim(proje.getBolumId());
        proje.setAd(istek.ad().trim());
        proje.setDonem(istek.donem().trim());
        proje.setDurum(istek.durum());
        repository.flush();
        return yanita(proje);
    }

    @Transactional
    public void sil(Long id) {
        ProgramProjesi proje = bul(id);
        yetki.bolumeErisim(proje.getBolumId());
        repository.delete(proje);
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