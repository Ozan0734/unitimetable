package com.unitimetable.demo.yerlesim;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unitimetable.demo.derslik.DerslikRepository;
import com.unitimetable.demo.ortak.GecersizIslemException;
import com.unitimetable.demo.ortak.KayitBulunamadiException;
import com.unitimetable.demo.ortak.YetkiKontrol;
import com.unitimetable.demo.proje.ProgramProjesi;
import com.unitimetable.demo.proje.ProgramProjesiRepository;

@Service
public class IslemGecmisiService {

    private static final int EN_FAZLA_KAYIT = 50;

    private final IslemGecmisiRepository repository;
    private final YerlesimRepository yerlesimRepository;
    private final YerlesimDerslikRepository yerlesimDerslikRepository;
    private final DerslikRepository derslikRepository;
    private final ProgramProjesiRepository projeRepository;
    private final YetkiKontrol yetki;

    public IslemGecmisiService(IslemGecmisiRepository repository, YerlesimRepository yerlesimRepository,
            YerlesimDerslikRepository yerlesimDerslikRepository, DerslikRepository derslikRepository,
            ProgramProjesiRepository projeRepository, YetkiKontrol yetki) {
        this.repository = repository;
        this.yerlesimRepository = yerlesimRepository;
        this.yerlesimDerslikRepository = yerlesimDerslikRepository;
        this.derslikRepository = derslikRepository;
        this.projeRepository = projeRepository;
        this.yetki = yetki;
    }

    /**
     * Bloğun şu anki yerleşim durumu. Yerleşik değilse null döner.
     */
    public BlokDurumu mevcutDurum(Long blokId) {
        Yerlesim y = yerlesimRepository.findByBlokId(blokId).orElse(null);
        if (y == null) {
            return null;
        }
        List<Long> derslikler = yerlesimDerslikRepository.findByYerlesimIdIn(List.of(y.getId())).stream()
                .map(YerlesimDerslik::getDerslikId)
                .toList();
        return new BlokDurumu(y.getGun(), y.getBaslangicSlot(), y.isKilitli(), derslikler);
    }

    @Transactional
    public void kaydet(Long projeId, Long blokId, IslemTuru tur, BlokDurumu onceki, BlokDurumu sonraki) {
        repository.deleteByProjeIdAndGeriAlindiTrue(projeId);
        repository.save(new IslemGecmisi(projeId, blokId, yetki.kullanici().id(), tur,
                onceki == null ? null : onceki.metin(),
                sonraki == null ? null : sonraki.metin()));
        repository.flush();
        List<IslemGecmisi> hepsi = repository.findByProjeIdOrderByIdDesc(projeId);
        if (hepsi.size() > EN_FAZLA_KAYIT) {
            repository.deleteAll(hepsi.subList(EN_FAZLA_KAYIT, hepsi.size()));
        }
    }

    @Transactional(readOnly = true)
    public List<IslemGecmisiDto.Yanit> listele(Long projeId) {
        projeyiDogrula(projeId);
        return repository.findByProjeIdOrderByIdDesc(projeId).stream()
                .map(IslemGecmisiService::yanita)
                .toList();
    }

    @Transactional
    public IslemGecmisiDto.Yanit geriAl(Long projeId) {
        projeyiDogrula(projeId);
        IslemGecmisi kayit = repository.findFirstByProjeIdAndGeriAlindiFalseOrderByIdDesc(projeId)
                .orElseThrow(() -> new GecersizIslemException("Geri alınacak işlem yok"));
        uygula(kayit.getBlokId(), BlokDurumu.coz(kayit.getSonraki()), BlokDurumu.coz(kayit.getOnceki()));
        kayit.setGeriAlindi(true);
        repository.flush();
        return yanita(kayit);
    }

    @Transactional
    public IslemGecmisiDto.Yanit yinele(Long projeId) {
        projeyiDogrula(projeId);
        IslemGecmisi kayit = repository.findFirstByProjeIdAndGeriAlindiTrueOrderByIdAsc(projeId)
                .orElseThrow(() -> new GecersizIslemException("Yinelenecek işlem yok"));
        uygula(kayit.getBlokId(), BlokDurumu.coz(kayit.getOnceki()), BlokDurumu.coz(kayit.getSonraki()));
        kayit.setGeriAlindi(false);
        repository.flush();
        return yanita(kayit);
    }

    private void uygula(Long blokId, BlokDurumu beklenen, BlokDurumu hedef) {
        if (!Objects.equals(mevcutDurum(blokId), beklenen)) {
            throw new GecersizIslemException(
                    "İşlem geri alınamadı: blok bu işlemden sonra başka bir yolla değişmiş");
        }
        if (hedef != null && derslikRepository.findAllById(hedef.derslikIdler()).size() != hedef.derslikIdler().size()) {
            throw new GecersizIslemException(
                    "İşlem geri alınamadı: yerleşimde kullanılan derslik artık yok");
        }

        Yerlesim yerlesim = yerlesimRepository.findByBlokId(blokId).orElse(null);
        if (hedef == null) {
            if (yerlesim != null) {
                yerlesimRepository.delete(yerlesim);
                yerlesimRepository.flush();
            }
            return;
        }

        if (yerlesim == null) {
            yerlesim = yerlesimRepository.saveAndFlush(
                    new Yerlesim(blokId, hedef.gun(), hedef.baslangicSlot()));
        } else {
            yerlesim.tasi(hedef.gun(), hedef.baslangicSlot());
            yerlesimDerslikRepository.deleteByYerlesimId(yerlesim.getId());
            yerlesimDerslikRepository.flush();
        }
        yerlesim.setKilitli(hedef.kilitli());
        for (Long derslikId : hedef.derslikIdler()) {
            yerlesimDerslikRepository.save(new YerlesimDerslik(yerlesim.getId(), derslikId));
        }
        yerlesimRepository.flush();
        yerlesimDerslikRepository.flush();
    }

    private void projeyiDogrula(Long projeId) {
        ProgramProjesi proje = projeRepository.findById(projeId)
                .orElseThrow(() -> new KayitBulunamadiException(
                        "Program projesi bulunamadı (id: " + projeId + ")"));
        yetki.bolumeErisim(proje.getBolumId());
    }

    private static IslemGecmisiDto.Yanit yanita(IslemGecmisi k) {
        return new IslemGecmisiDto.Yanit(k.getId(), k.getBlokId(), k.getTur(),
                BlokDurumu.coz(k.getOnceki()), BlokDurumu.coz(k.getSonraki()),
                k.isGeriAlindi(), k.getTarih());
    }
}