package com.unitimetable.demo.yerlesim;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unitimetable.demo.blok.DersBlogu;
import com.unitimetable.demo.blok.DersBloguRepository;
import com.unitimetable.demo.ders.Ders;
import com.unitimetable.demo.ders.DersRepository;
import com.unitimetable.demo.derslik.Derslik;
import com.unitimetable.demo.derslik.DerslikBolum;
import com.unitimetable.demo.derslik.DerslikBolumRepository;
import com.unitimetable.demo.derslik.DerslikRepository;
import com.unitimetable.demo.izgara.ZamanIzgarasi;
import com.unitimetable.demo.izgara.ZamanIzgarasiRepository;
import com.unitimetable.demo.ortak.GecersizIslemException;
import com.unitimetable.demo.ortak.KayitBulunamadiException;
import com.unitimetable.demo.ortak.YetkiKontrol;
import com.unitimetable.demo.proje.ProgramProjesi;
import com.unitimetable.demo.proje.ProgramProjesiRepository;
import com.unitimetable.demo.sube.Sube;
import com.unitimetable.demo.sube.SubeRepository;

@Service
public class UygunYerService {

    private final DersBloguRepository blokRepository;
    private final SubeRepository subeRepository;
    private final DersRepository dersRepository;
    private final ProgramProjesiRepository projeRepository;
    private final ZamanIzgarasiRepository izgaraRepository;
    private final DerslikRepository derslikRepository;
    private final DerslikBolumRepository derslikBolumRepository;
    private final CakismaDenetleyici denetleyici;
    private final YetkiKontrol yetki;

    public UygunYerService(DersBloguRepository blokRepository, SubeRepository subeRepository,
            DersRepository dersRepository, ProgramProjesiRepository projeRepository,
            ZamanIzgarasiRepository izgaraRepository, DerslikRepository derslikRepository,
            DerslikBolumRepository derslikBolumRepository, CakismaDenetleyici denetleyici,
            YetkiKontrol yetki) {
        this.blokRepository = blokRepository;
        this.subeRepository = subeRepository;
        this.dersRepository = dersRepository;
        this.projeRepository = projeRepository;
        this.izgaraRepository = izgaraRepository;
        this.derslikRepository = derslikRepository;
        this.derslikBolumRepository = derslikBolumRepository;
        this.denetleyici = denetleyici;
        this.yetki = yetki;
    }

    @Transactional(readOnly = true)
    public List<UygunYerDto.Yer> listele(Long blokId, List<Long> derslikIdler, UygunYerDto.Durum filtre) {
        DersBlogu blok = blokRepository.findById(blokId)
                .orElseThrow(() -> new KayitBulunamadiException("Blok bulunamadı (id: " + blokId + ")"));
        Sube sube = subeRepository.findById(blok.getSubeId())
                .orElseThrow(() -> new KayitBulunamadiException("Şube bulunamadı (id: " + blok.getSubeId() + ")"));
        Ders ders = dersRepository.findById(sube.getDersId())
                .orElseThrow(() -> new KayitBulunamadiException("Ders bulunamadı (id: " + sube.getDersId() + ")"));
        yetki.bolumeErisim(ders.getBolumId());
        ProgramProjesi proje = projeRepository.findById(ders.getProjeId())
                .orElseThrow(() -> new KayitBulunamadiException(
                        "Program projesi bulunamadı (id: " + ders.getProjeId() + ")"));
        ZamanIzgarasi izgara = izgaraRepository.findByKurumId(proje.getKurumId())
                .orElseThrow(() -> new KayitBulunamadiException("Kurumun zaman ızgarası tanımlı değil"));

        List<Long> adaylar = (derslikIdler == null || derslikIdler.isEmpty())
                ? varsayilanDerslikler(ders, proje.getKurumId())
                : secilenDerslikler(derslikIdler, proje.getKurumId());

        List<UygunYerDto.Yer> sonuc = new ArrayList<>();
        int enGecBaslangic = izgara.getGunlukSlotSayisi() - blok.getSure() + 1;
        for (int gun = 1; gun <= izgara.getGunSayisi(); gun++) {
            for (int baslangic = 1; baslangic <= enGecBaslangic; baslangic++) {
                for (Long derslikId : adaylar) {
                    List<Ihlal> ihlaller = denetleyici.kontrolEt(blok, sube, ders, gun, baslangic,
                            List.of(derslikId));
                    UygunYerDto.Durum durum = durum(ihlaller);
                    if (filtre == null || filtre == durum) {
                        sonuc.add(new UygunYerDto.Yer(gun, baslangic, baslangic + blok.getSure() - 1,
                                derslikId, durum, ihlaller));
                    }
                }
            }
        }
        return sonuc;
    }

    private static UygunYerDto.Durum durum(List<Ihlal> ihlaller) {
        if (ihlaller.stream().anyMatch(i -> i.tur() == IhlalTuru.ENGEL)) {
            return UygunYerDto.Durum.ENGELLI;
        }
        if (ihlaller.stream().anyMatch(i -> i.tur() == IhlalTuru.UYARI)) {
            return UygunYerDto.Durum.UYARILI;
        }
        return UygunYerDto.Durum.UYGUN;
    }

    private List<Long> varsayilanDerslikler(Ders ders, Long kurumId) {
        Set<Long> bolumunDersliklari = derslikBolumRepository.findByBolumId(ders.getBolumId()).stream()
                .map(DerslikBolum::getDerslikId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        List<Derslik> kurumDersliklari = derslikRepository.findByKurumIdOrderByAd(kurumId);
        List<Derslik> secilenler = bolumunDersliklari.isEmpty()
                ? kurumDersliklari
                : kurumDersliklari.stream().filter(d -> bolumunDersliklari.contains(d.getId())).toList();
        return secilenler.stream().map(Derslik::getId).toList();
    }

    private List<Long> secilenDerslikler(List<Long> idler, Long kurumId) {
        Set<Long> tekil = new LinkedHashSet<>(idler);
        List<Derslik> bulunanlar = derslikRepository.findAllById(tekil);
        if (bulunanlar.size() != tekil.size()) {
            throw new KayitBulunamadiException("Seçilen derslik bulunamadı");
        }
        for (Derslik derslik : bulunanlar) {
            if (!derslik.getKurumId().equals(kurumId)) {
                throw new GecersizIslemException("Derslik (" + derslik.getAd() + ") bu kuruma ait değil");
            }
        }
        return new ArrayList<>(tekil);
    }
}