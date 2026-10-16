package com.unitimetable.demo.ogretimelemani;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unitimetable.demo.ortak.KayitBulunamadiException;

@Service
public class OgretimElemaniService {

    private final OgretimElemaniRepository repository;

    public OgretimElemaniService(OgretimElemaniRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<OgretimElemaniDto.Yanit> listele(Long bolumId) {
        List<OgretimElemani> kayitlar = (bolumId == null)
                ? repository.findAll(Sort.by("bolumId", "adSoyad"))
                : repository.findByBolumIdOrderByAdSoyad(bolumId);
        return kayitlar.stream().map(OgretimElemaniService::yanita).toList();
    }

    @Transactional(readOnly = true)
    public OgretimElemaniDto.Yanit getir(Long id) {
        return yanita(bul(id));
    }

    @Transactional
    public OgretimElemaniDto.Yanit olustur(OgretimElemaniDto.Istek istek) {
        OgretimElemani kaydedilen = repository.save(new OgretimElemani(
                istek.bolumId(),
                istek.adSoyad().trim(),
                bosuNullYap(istek.unvan()),
                bosuNullYap(istek.renk()),
                istek.oncelik() == null ? 0 : istek.oncelik()));
        return yanita(kaydedilen);
    }

    @Transactional
    public OgretimElemaniDto.Yanit guncelle(Long id, OgretimElemaniDto.Guncelleme istek) {
        OgretimElemani hoca = bul(id);
        hoca.setAdSoyad(istek.adSoyad().trim());
        hoca.setUnvan(bosuNullYap(istek.unvan()));
        hoca.setRenk(bosuNullYap(istek.renk()));
        hoca.setOncelik(istek.oncelik() == null ? 0 : istek.oncelik());
        return yanita(hoca);
    }

    @Transactional
    public void sil(Long id) {
        repository.delete(bul(id));
        repository.flush();
    }

    private OgretimElemani bul(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new KayitBulunamadiException("Öğretim elemanı bulunamadı (id: " + id + ")"));
    }

    private static String bosuNullYap(String deger) {
        if (deger == null || deger.isBlank()) {
            return null;
        }
        return deger.trim();
    }

    private static OgretimElemaniDto.Yanit yanita(OgretimElemani h) {
        return new OgretimElemaniDto.Yanit(
                h.getId(), h.getBolumId(), h.getAdSoyad(), h.getUnvan(), h.getRenk(), h.getOncelik());
    }
}