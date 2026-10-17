package com.unitimetable.demo.sube;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unitimetable.demo.ortak.KayitBulunamadiException;

@Service
public class SubeService {

    private final SubeRepository repository;

    public SubeService(SubeRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<SubeDto.Yanit> listele(Long dersId, Long ogretimElemaniId) {
        List<Sube> subeler;
        if (dersId != null) {
            subeler = repository.findByDersIdOrderBySubeNo(dersId);
        } else if (ogretimElemaniId != null) {
            subeler = repository.findByOgretimElemaniIdOrderByDersIdAscSubeNoAsc(ogretimElemaniId);
        } else {
            subeler = repository.findAll(Sort.by("dersId", "subeNo"));
        }
        return subeler.stream().map(SubeService::yanita).toList();
    }

    @Transactional(readOnly = true)
    public SubeDto.Yanit getir(Long id) {
        return yanita(bul(id));
    }

    @Transactional
    public SubeDto.Yanit olustur(SubeDto.Istek istek) {
        int subeNo = (istek.subeNo() != null) ? istek.subeNo() : sonrakiNo(istek.dersId());
        Sube kaydedilen = repository.saveAndFlush(new Sube(
                istek.dersId(), istek.ogretimElemaniId(), subeNo, istek.tahminiOgrenci(),
                varsayilanFalse(istek.sinifCakismaIzni()),
                varsayilanFalse(istek.derslikCakismaIzni()),
                varsayilanFalse(istek.cokluDerslikIzni())));
        return yanita(kaydedilen);
    }

    @Transactional
    public SubeDto.Yanit guncelle(Long id, SubeDto.Guncelleme istek) {
        Sube sube = bul(id);
        sube.guncelle(istek.ogretimElemaniId(), istek.tahminiOgrenci(),
                varsayilanFalse(istek.sinifCakismaIzni()),
                varsayilanFalse(istek.derslikCakismaIzni()),
                varsayilanFalse(istek.cokluDerslikIzni()));
        repository.flush();
        return yanita(sube);
    }

    @Transactional
    public void sil(Long id) {
        repository.delete(bul(id));
        repository.flush();
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

    private static SubeDto.Yanit yanita(Sube s) {
        return new SubeDto.Yanit(s.getId(), s.getDersId(), s.getOgretimElemaniId(), s.getSubeNo(),
                s.getTahminiOgrenci(), s.isSinifCakismaIzni(), s.isDerslikCakismaIzni(),
                s.isCokluDerslikIzni());
    }
}