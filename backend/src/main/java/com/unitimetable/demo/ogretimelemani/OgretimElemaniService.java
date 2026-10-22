package com.unitimetable.demo.ogretimelemani;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unitimetable.demo.ortak.KayitBulunamadiException;
import com.unitimetable.demo.ortak.YetkiKontrol;

@Service
public class OgretimElemaniService {

    private final OgretimElemaniRepository repository;
    private final YetkiKontrol yetki;

    public OgretimElemaniService(OgretimElemaniRepository repository, YetkiKontrol yetki) {
        this.repository = repository;
        this.yetki = yetki;
    }

    @Transactional(readOnly = true)
    public List<OgretimElemaniDto.Yanit> listele(Long bolumId) {
        Sort siralama = Sort.by("bolumId", "adSoyad");
        List<OgretimElemani> kayitlar;
        if (bolumId != null) {
            yetki.bolumeErisim(bolumId);
            kayitlar = repository.findByBolumIdOrderByAdSoyad(bolumId);
        } else {
            List<Long> izinli = yetki.erisilebilirBolumler();
            if (izinli == null) {
                kayitlar = repository.findAll(siralama);
            } else if (izinli.isEmpty()) {
                kayitlar = List.of();
            } else {
                kayitlar = repository.findByBolumIdIn(izinli, siralama);
            }
        }
        return kayitlar.stream().map(OgretimElemaniService::yanita).toList();
    }

    @Transactional(readOnly = true)
    public OgretimElemaniDto.Yanit getir(Long id) {
        OgretimElemani hoca = bul(id);
        yetki.bolumeErisim(hoca.getBolumId());
        return yanita(hoca);
    }

    @Transactional
    public OgretimElemaniDto.Yanit olustur(OgretimElemaniDto.Istek istek) {
        yetki.bolumeErisim(istek.bolumId());
        OgretimElemani kaydedilen = repository.saveAndFlush(new OgretimElemani(
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
        yetki.bolumeErisim(hoca.getBolumId());
        hoca.setAdSoyad(istek.adSoyad().trim());
        hoca.setUnvan(bosuNullYap(istek.unvan()));
        hoca.setRenk(bosuNullYap(istek.renk()));
        hoca.setOncelik(istek.oncelik() == null ? 0 : istek.oncelik());
        return yanita(hoca);
    }

    @Transactional
    public void sil(Long id) {
        OgretimElemani hoca = bul(id);
        yetki.bolumeErisim(hoca.getBolumId());
        repository.delete(hoca);
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