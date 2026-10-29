package com.unitimetable.demo.blok;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unitimetable.demo.ders.Ders;
import com.unitimetable.demo.ders.DersRepository;
import com.unitimetable.demo.ders.DersTuru;
import com.unitimetable.demo.ders.SaatIfadesi;
import com.unitimetable.demo.ortak.GecersizIslemException;
import com.unitimetable.demo.ortak.KayitBulunamadiException;
import com.unitimetable.demo.ortak.YetkiKontrol;
import com.unitimetable.demo.sube.Sube;
import com.unitimetable.demo.sube.SubeRepository;

@Service
public class BlokService {

    private final DersBloguRepository repository;
    private final SubeRepository subeRepository;
    private final DersRepository dersRepository;
    private final YetkiKontrol yetki;

    public BlokService(DersBloguRepository repository, SubeRepository subeRepository,
            DersRepository dersRepository, YetkiKontrol yetki) {
        this.repository = repository;
        this.subeRepository = subeRepository;
        this.dersRepository = dersRepository;
        this.yetki = yetki;
    }

    @Transactional(readOnly = true)
    public List<BlokDto.Yanit> listele(Long subeId, Long dersId) {
        if (subeId != null) {
            Sube sube = subeRepository.findById(subeId)
                    .orElseThrow(() -> new KayitBulunamadiException("Şube bulunamadı (id: " + subeId + ")"));
            yetki.bolumeErisim(dersBul(sube.getDersId()).getBolumId());
            return yanitlar(repository.findBySubeIdOrderBySiraNo(subeId));
        }
        if (dersId != null) {
            Ders ders = dersBul(dersId);
            yetki.bolumeErisim(ders.getBolumId());
            return yanitlar(repository.findByDersId(dersId));
        }
        throw new GecersizIslemException("subeId ya da dersId verilmelidir");
    }

    @Transactional
    public List<BlokDto.Yanit> yenile(Long dersId) {
        Ders ders = dersBul(dersId);
        yetki.bolumeErisim(ders.getBolumId());
        dersIcinYenile(ders);
        return yanitlar(repository.findByDersId(dersId));
    }

    /**
     * Dersin tüm şubelerinin bloklarını, dersin güncel saat ifadesine göre eşitler.
     */
    @Transactional
    public void dersIcinYenile(Ders ders) {
        for (Sube sube : subeRepository.findByDersIdOrderBySubeNo(ders.getId())) {
            subeIcinYenile(ders, sube);
        }
    }

    /**
     * Şubenin bloklarını dersin saat ifadesine göre eşitler. Blok yapısı zaten aynıysa dokunmaz.
     */
    @Transactional
    public void subeIcinYenile(Ders ders, Sube sube) {
                List<Integer> istenen = (ders.getTur() != DersTuru.YERLESIMSIZ && ders.getSaatIfadesi() != null)
                ? SaatIfadesi.bloklar(ders.getSaatIfadesi())
                : List.of();

        List<Integer> mevcut = new ArrayList<>();
        for (DersBlogu b : repository.findBySubeIdOrderBySiraNo(sube.getId())) {
            mevcut.add(b.getSure());
        }
        if (mevcut.equals(istenen)) {
            return;
        }

        repository.deleteBySubeId(sube.getId());
        repository.flush();
        List<DersBlogu> yeniler = new ArrayList<>();
        for (int i = 0; i < istenen.size(); i++) {
            yeniler.add(new DersBlogu(sube.getId(), i + 1, istenen.get(i)));
        }
        repository.saveAll(yeniler);
        repository.flush();
    }

    private Ders dersBul(Long dersId) {
        return dersRepository.findById(dersId)
                .orElseThrow(() -> new KayitBulunamadiException("Ders bulunamadı (id: " + dersId + ")"));
    }

    private static List<BlokDto.Yanit> yanitlar(List<DersBlogu> bloklar) {
        return bloklar.stream()
                .map(b -> new BlokDto.Yanit(b.getId(), b.getSubeId(), b.getSiraNo(), b.getSure()))
                .toList();
    }
}