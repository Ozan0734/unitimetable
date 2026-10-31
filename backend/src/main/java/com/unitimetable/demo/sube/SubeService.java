package com.unitimetable.demo.sube;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unitimetable.demo.blok.BlokService;
import com.unitimetable.demo.ders.Ders;
import com.unitimetable.demo.ders.DersRepository;
import com.unitimetable.demo.ders.DersTuru;
import com.unitimetable.demo.ortak.KayitBulunamadiException;
import com.unitimetable.demo.ortak.YetkiKontrol;

@Service
public class SubeService {

    private final SubeRepository repository;
    private final DersRepository dersRepository;
    private final YetkiKontrol yetki;
    private final BlokService blokService;

    public SubeService(SubeRepository repository, DersRepository dersRepository, YetkiKontrol yetki,
            BlokService blokService) {
        this.repository = repository;
        this.dersRepository = dersRepository;
        this.yetki = yetki;
        this.blokService = blokService;
    }

    @Transactional(readOnly = true)
    public List<SubeDto.Yanit> listele(Long dersId, Long ogretimElemaniId) {
        List<Sube> subeler;
        if (dersId != null) {
            yetki.bolumeErisim(dersBolumu(dersId));
            subeler = repository.findByDersIdOrderBySubeNo(dersId);
        } else if (ogretimElemaniId != null) {
            subeler = erisilebilenler(
                    repository.findByOgretimElemaniIdOrderByDersIdAscSubeNoAsc(ogretimElemaniId));
        } else {
            List<Long> izinli = yetki.erisilebilirBolumler();
            if (izinli == null) {
                subeler = repository.findAll(Sort.by("dersId", "subeNo"));
            } else if (izinli.isEmpty()) {
                subeler = List.of();
            } else {
                subeler = repository.findByDersBolumIn(izinli);
            }
        }
        return subeler.stream().map(SubeService::yanita).toList();
    }

    @Transactional(readOnly = true)
    public SubeDto.Yanit getir(Long id) {
        Sube sube = bul(id);
        yetki.bolumeErisim(dersBolumu(sube.getDersId()));
        return yanita(sube);
    }

    @Transactional
    public SubeDto.Yanit olustur(SubeDto.Istek istek) {
        Ders ders = dersBul(istek.dersId());
        yetki.bolumeErisim(ders.getBolumId());
        boolean seminer = ders.getTur() == DersTuru.SEMINER;
        int subeNo = (istek.subeNo() != null) ? istek.subeNo() : sonrakiNo(istek.dersId());
        Sube kaydedilen = repository.saveAndFlush(new Sube(
                istek.dersId(), istek.ogretimElemaniId(), subeNo, istek.tahminiOgrenci(),
                varsayilan(istek.sinifCakismaIzni(), seminer),
                varsayilan(istek.derslikCakismaIzni(), seminer),
                varsayilanFalse(istek.cokluDerslikIzni())));
        blokService.subeIcinYenile(ders, kaydedilen);
        return yanita(kaydedilen);
    }

    @Transactional
    public SubeDto.Yanit guncelle(Long id, SubeDto.Guncelleme istek) {
        Sube sube = bul(id);
        yetki.bolumeErisim(dersBolumu(sube.getDersId()));
        sube.guncelle(istek.ogretimElemaniId(), istek.tahminiOgrenci(),
                varsayilanFalse(istek.sinifCakismaIzni()),
                varsayilanFalse(istek.derslikCakismaIzni()),
                varsayilanFalse(istek.cokluDerslikIzni()));
        repository.flush();
        return yanita(sube);
    }

    @Transactional
    public void sil(Long id) {
        Sube sube = bul(id);
        yetki.bolumeErisim(dersBolumu(sube.getDersId()));
        repository.delete(sube);
        repository.flush();
    }

    private Ders dersBul(Long dersId) {
        return dersRepository.findById(dersId)
                .orElseThrow(() -> new KayitBulunamadiException("Ders bulunamadı (id: " + dersId + ")"));
    }

    private Long dersBolumu(Long dersId) {
        return dersBul(dersId).getBolumId();
    }

    private List<Sube> erisilebilenler(List<Sube> subeler) {
        List<Long> izinli = yetki.erisilebilirBolumler();
        if (izinli == null) {
            return subeler;
        }
        Set<Long> dersIdler = subeler.stream().map(Sube::getDersId).collect(Collectors.toSet());
        Set<Long> izinliDersler = dersRepository.findAllById(dersIdler).stream()
                .filter(d -> izinli.contains(d.getBolumId()))
                .map(Ders::getId)
                .collect(Collectors.toSet());
        return subeler.stream().filter(s -> izinliDersler.contains(s.getDersId())).toList();
    }

    private int sonrakiNo(Long dersId) {
        List<Sube> mevcutlar = repository.findByDersIdOrderBySubeNo(dersId);
        if (mevcutlar.isEmpty()) {
            return 1;
        }
        return mevcutlar.get(mevcutlar.size() - 1).getSubeNo() + 1;
    }

    private Sube bul(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new KayitBulunamadiException("Şube bulunamadı (id: " + id + ")"));
    }

    private static boolean varsayilanFalse(Boolean deger) {
        return deger != null && deger;
    }

    private static boolean varsayilan(Boolean deger, boolean varsayilanDeger) {
        return deger == null ? varsayilanDeger : deger;
    }

    private static SubeDto.Yanit yanita(Sube s) {
        return new SubeDto.Yanit(s.getId(), s.getDersId(), s.getOgretimElemaniId(), s.getSubeNo(),
                s.getTahminiOgrenci(), s.isSinifCakismaIzni(), s.isDerslikCakismaIzni(),
                s.isCokluDerslikIzni());
    }
}