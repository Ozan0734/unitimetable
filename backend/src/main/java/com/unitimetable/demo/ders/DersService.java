package com.unitimetable.demo.ders;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unitimetable.demo.ortak.GecersizIslemException;
import com.unitimetable.demo.ortak.KayitBulunamadiException;
import com.unitimetable.demo.ortak.YetkiKontrol;
import com.unitimetable.demo.proje.ProgramProjesi;
import com.unitimetable.demo.proje.ProgramProjesiRepository;

@Service
public class DersService {

    private record SaatBilgisi(Integer haftalikSaat, String ifade) {
    }

    private final DersRepository repository;
    private final ProgramProjesiRepository projeRepository;
    private final YetkiKontrol yetki;

    public DersService(DersRepository repository, ProgramProjesiRepository projeRepository,
            YetkiKontrol yetki) {
        this.repository = repository;
        this.projeRepository = projeRepository;
        this.yetki = yetki;
    }

    @Transactional(readOnly = true)
    public List<DersDto.Yanit> listele(Long projeId) {
        Sort siralama = Sort.by("projeId", "kod");
        List<Ders> dersler;
        if (projeId != null) {
            ProgramProjesi proje = projeRepository.findById(projeId)
                    .orElseThrow(() -> new KayitBulunamadiException(
                            "Program projesi bulunamadı (id: " + projeId + ")"));
            yetki.bolumeErisim(proje.getBolumId());
            dersler = repository.findByProjeIdOrderByKod(projeId);
        } else {
            List<Long> izinli = yetki.erisilebilirBolumler();
            if (izinli == null) {
                dersler = repository.findAll(siralama);
            } else if (izinli.isEmpty()) {
                dersler = List.of();
            } else {
                dersler = repository.findByBolumIdIn(izinli, siralama);
            }
        }
        return dersler.stream().map(DersService::yanita).toList();
    }

    @Transactional(readOnly = true)
    public DersDto.Yanit getir(Long id) {
        Ders ders = bul(id);
        yetki.bolumeErisim(ders.getBolumId());
        return yanita(ders);
    }

    @Transactional
    public DersDto.Yanit olustur(DersDto.Istek istek) {
        yetki.bolumeErisim(istek.bolumId());
        SaatBilgisi saat = saatBilgisi(istek.tur(), istek.haftalikSaat(), istek.saatIfadesi());
        Ders kaydedilen = repository.saveAndFlush(new Ders(
                istek.projeId(), istek.bolumId(), istek.kod().trim(), istek.ad().trim(),
                istek.tur(), saat.haftalikSaat(), saat.ifade()));
        return yanita(kaydedilen);
    }

    @Transactional
    public DersDto.Yanit guncelle(Long id, DersDto.Guncelleme istek) {
        Ders ders = bul(id);
        yetki.bolumeErisim(ders.getBolumId());
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
        Ders ders = bul(id);
        yetki.bolumeErisim(ders.getBolumId());
        repository.delete(ders);
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