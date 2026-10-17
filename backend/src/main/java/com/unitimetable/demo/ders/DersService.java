package com.unitimetable.demo.ders;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unitimetable.demo.ortak.GecersizIslemException;
import com.unitimetable.demo.ortak.KayitBulunamadiException;

@Service
public class DersService {

    private record SaatBilgisi(Integer haftalikSaat, String ifade) {
    }

    private final DersRepository repository;

    public DersService(DersRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<DersDto.Yanit> listele(Long projeId) {
        List<Ders> dersler = (projeId == null)
                ? repository.findAll(Sort.by("projeId", "kod"))
                : repository.findByProjeIdOrderByKod(projeId);
        return dersler.stream().map(DersService::yanita).toList();
    }

    @Transactional(readOnly = true)
    public DersDto.Yanit getir(Long id) {
        return yanita(bul(id));
    }

    @Transactional
    public DersDto.Yanit olustur(DersDto.Istek istek) {
        SaatBilgisi saat = saatBilgisi(istek.tur(), istek.haftalikSaat(), istek.saatIfadesi());
        Ders kaydedilen = repository.saveAndFlush(new Ders(
                istek.projeId(), istek.bolumId(), istek.kod().trim(), istek.ad().trim(),
                istek.tur(), saat.haftalikSaat(), saat.ifade()));
        return yanita(kaydedilen);
    }

    @Transactional
    public DersDto.Yanit guncelle(Long id, DersDto.Guncelleme istek) {
        Ders ders = bul(id);
        SaatBilgisi saat = saatBilgisi(istek.tur(), istek.haftalikSaat(), istek.saatIfadesi());
        ders.setKod(istek.kod().trim());
        ders.setAd(istek.ad().trim());
        ders.setTur(istek.tur());
        ders.setSaat(saat.haftalikSaat(), saat.ifade());
        repository.flush();
        return yanita(ders);
    }

    @Transactional
    public void sil(Long id) {
        repository.delete(bul(id));
        repository.flush();
    }

    private Ders bul(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new KayitBulunamadiException("Ders bulunamadı (id: " + id + ")"));
    }

    private static SaatBilgisi saatBilgisi(DersTuru tur, Integer haftalikSaat, String ifade) {
        if (tur == DersTuru.YERLESIMSIZ) {
            return new SaatBilgisi(null, null);
        }
        if (haftalikSaat == null) {
            throw new GecersizIslemException(
                    "Haftalık ders saati zorunludur (yerleşimsiz olmayan dersler için)");
        }
        String temiz = (ifade == null || ifade.isBlank()) ? String.valueOf(haftalikSaat) : ifade.trim();
        SaatIfadesi.dogrula(temiz, haftalikSaat);
        return new SaatBilgisi(haftalikSaat, temiz);
    }

    private static DersDto.Yanit yanita(Ders d) {
        List<Integer> bloklar = (d.getSaatIfadesi() == null)
                ? List.of()
                : SaatIfadesi.bloklar(d.getSaatIfadesi());
        return new DersDto.Yanit(d.getId(), d.getProjeId(), d.getBolumId(), d.getKod(), d.getAd(),
                d.getTur(), d.getHaftalikSaat(), d.getSaatIfadesi(), bloklar);
    }
}