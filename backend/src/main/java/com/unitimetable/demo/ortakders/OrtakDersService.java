package com.unitimetable.demo.ortakders;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unitimetable.demo.blok.DersBlogu;
import com.unitimetable.demo.blok.DersBloguRepository;
import com.unitimetable.demo.bolum.Bolum;
import com.unitimetable.demo.bolum.BolumRepository;
import com.unitimetable.demo.ders.Ders;
import com.unitimetable.demo.ders.DersRepository;
import com.unitimetable.demo.ders.DersTuru;
import com.unitimetable.demo.ogretimelemani.OgretimElemani;
import com.unitimetable.demo.ogretimelemani.OgretimElemaniRepository;
import com.unitimetable.demo.ortak.GecersizIslemException;
import com.unitimetable.demo.ortak.KayitBulunamadiException;
import com.unitimetable.demo.ortak.YasakException;
import com.unitimetable.demo.ortak.YetkiKontrol;
import com.unitimetable.demo.proje.ProgramProjesi;
import com.unitimetable.demo.proje.ProgramProjesiRepository;
import com.unitimetable.demo.sube.Sube;
import com.unitimetable.demo.sube.SubeRepository;
import com.unitimetable.demo.yerlesim.BolumlerArasiKurallar;

@Service
public class OrtakDersService {

    private static final int EN_FAZLA_UYE = 12;

    private record SubeBilgisi(Sube sube, Ders ders, ProgramProjesi proje, String hocaAdi, String hocaAnahtari,
            String bolumAdi, List<Integer> bloklar) {
    }

    private final OrtakGrupRepository grupRepository;
    private final OrtakGrupUyeRepository uyeRepository;
    private final ProgramProjesiRepository projeRepository;
    private final DersRepository dersRepository;
    private final SubeRepository subeRepository;
    private final DersBloguRepository blokRepository;
    private final OgretimElemaniRepository hocaRepository;
    private final BolumRepository bolumRepository;
    private final YetkiKontrol yetki;

    public OrtakDersService(OrtakGrupRepository grupRepository, OrtakGrupUyeRepository uyeRepository,
            ProgramProjesiRepository projeRepository, DersRepository dersRepository,
            SubeRepository subeRepository, DersBloguRepository blokRepository,
            OgretimElemaniRepository hocaRepository, BolumRepository bolumRepository, YetkiKontrol yetki) {
        this.grupRepository = grupRepository;
        this.uyeRepository = uyeRepository;
        this.projeRepository = projeRepository;
        this.dersRepository = dersRepository;
        this.subeRepository = subeRepository;
        this.blokRepository = blokRepository;
        this.hocaRepository = hocaRepository;
        this.bolumRepository = bolumRepository;
        this.yetki = yetki;
    }

    /**
     * Projedeki şubeler için, aynı kurumda ve aynı dönemde başka bölümlerde aynı öğretim elemanı ve aynı
     * ders (kod ya da ad) ile yürütülen şubeleri bulur. Zaten bir ortak grupta olan şubeler listelenmez.
     */
    @Transactional(readOnly = true)
    public List<OrtakDersDto.Aday> adaylar(Long projeId) {
        ProgramProjesi proje = projeBul(projeId);
        yetki.bolumeErisim(proje.getBolumId());
        String donem = BolumlerArasiKurallar.donemAnahtari(proje.getDonem());

        List<Ders> benimDersler = dersRepository.findByProjeIdOrderByKod(projeId);
        List<Ders> digerDersler = new ArrayList<>();
        for (ProgramProjesi diger : projeRepository.findByKurumId(proje.getKurumId())) {
            if (diger.getBolumId().equals(proje.getBolumId())
                    || !BolumlerArasiKurallar.donemAnahtari(diger.getDonem()).equals(donem)) {
                continue;
            }
            digerDersler.addAll(dersRepository.findByProjeIdOrderByKod(diger.getId()));
        }
        List<Sube> benimSubeler = subelerOf(benimDersler);
        List<Sube> digerSubeler = subelerOf(digerDersler);
        if (benimSubeler.isEmpty() || digerSubeler.isEmpty()) {
            return List.of();
        }

        Set<Long> tumIdler = new LinkedHashSet<>();
        benimSubeler.forEach(s -> tumIdler.add(s.getId()));
        digerSubeler.forEach(s -> tumIdler.add(s.getId()));
        Map<Long, SubeBilgisi> bilgi = bilgiYukle(tumIdler);
        Set<Long> grupta = uyeRepository.findBySubeIdIn(tumIdler).stream()
                .map(OrtakGrupUye::getSubeId)
                .collect(Collectors.toSet());

        List<OrtakDersDto.Aday> sonuc = new ArrayList<>();
        for (Sube benim : benimSubeler) {
            SubeBilgisi b = bilgi.get(benim.getId());
            if (!adayOlabilir(b, grupta)) {
                continue;
            }
            List<OrtakDersDto.Eslesen> eslesenler = new ArrayList<>();
            for (Sube diger : digerSubeler) {
                SubeBilgisi d = bilgi.get(diger.getId());
                if (!adayOlabilir(d, grupta)
                        || !OrtakDersEsleme.ayniHoca(b.hocaAnahtari(), d.hocaAnahtari())
                        || !OrtakDersEsleme.ayniDers(b.ders().getKod(), b.ders().getAd(),
                                d.ders().getKod(), d.ders().getAd())) {
                    continue;
                }
                eslesenler.add(new OrtakDersDto.Eslesen(diger.getId(), d.ders().getBolumId(), d.bolumAdi(),
                        d.ders().getKod(), d.ders().getAd(), diger.getSubeNo(), diger.getTahminiOgrenci(),
                        OrtakDersEsleme.blokYapilariAyni(b.bloklar(), d.bloklar())));
            }
            if (!eslesenler.isEmpty()) {
                sonuc.add(new OrtakDersDto.Aday(benim.getId(), b.ders().getKod(), b.ders().getAd(),
                        benim.getSubeNo(), benim.getTahminiOgrenci(), b.hocaAdi(), eslesenler));
            }
        }
        return sonuc;
    }

    /**
     * Ortak grup önerir. Önerenin bölümlerine ait şubeler onaylı, diğerleri onay bekler. Onaylanmadan
     * grup yerleşimleri etkilemez.
     */
    @Transactional
    public OrtakDersDto.Grup olustur(OrtakDersDto.Istek istek) {
        List<Long> idler = istek.subeIdler().stream().filter(Objects::nonNull).distinct().toList();
        if (idler.size() < 2 || idler.size() > EN_FAZLA_UYE) {
            throw new GecersizIslemException("Ortak grup en az 2, en fazla " + EN_FAZLA_UYE + " şubeden oluşur");
        }
        Map<Long, SubeBilgisi> bilgi = bilgiYukle(idler);
        for (Long id : idler) {
            if (!bilgi.containsKey(id)) {
                throw new KayitBulunamadiException("Şube bulunamadı (id: " + id + ")");
            }
        }

        SubeBilgisi ilk = bilgi.get(idler.get(0));
        Set<Long> bolumler = new HashSet<>();
        for (Long id : idler) {
            SubeBilgisi b = bilgi.get(id);
            String ad = b.bolumAdi() + " bölümü, " + b.ders().getKod();
            if (!bolumler.add(b.ders().getBolumId())) {
                throw new GecersizIslemException(
                        "Ortak gruptaki şubeler farklı bölümlere ait olmalı (" + ad + " aynı bölümden iki şube)");
            }
            if (!b.proje().getKurumId().equals(ilk.proje().getKurumId())
                    || !BolumlerArasiKurallar.donemAnahtari(b.proje().getDonem())
                            .equals(BolumlerArasiKurallar.donemAnahtari(ilk.proje().getDonem()))) {
                throw new GecersizIslemException("Ortak şubeler aynı kurumun aynı dönemine ait olmalı (" + ad + ")");
            }
            if (b.ders().getTur() == DersTuru.YERLESIMSIZ || b.bloklar().isEmpty()) {
                throw new GecersizIslemException(ad + " yerleşimsiz ya da blok içermiyor, ortak olamaz");
            }
            if (!OrtakDersEsleme.ayniHoca(ilk.hocaAnahtari(), b.hocaAnahtari())) {
                throw new GecersizIslemException(
                        "Ortak şubelerin öğretim elemanı aynı kişi olmalı (" + ad + ")");
            }
            if (!OrtakDersEsleme.ayniDers(ilk.ders().getKod(), ilk.ders().getAd(), b.ders().getKod(),
                    b.ders().getAd())) {
                throw new GecersizIslemException(
                        "Ortak şubeler aynı dersin şubeleri olmalı: kod ya da ad aynı değil (" + ad + ")");
            }
            if (!OrtakDersEsleme.blokYapilariAyni(ilk.bloklar(), b.bloklar())) {
                throw new GecersizIslemException("Blok yapıları aynı olmalı, " + ad + " için "
                        + b.bloklar() + " saatlik bloklar var, diğerinde " + ilk.bloklar()
                        + ". Önce ders saat ifadelerini eşitleyin");
            }
        }
        if (!uyeRepository.findBySubeIdIn(idler).isEmpty()) {
            throw new GecersizIslemException("Şubelerden biri zaten bir ortak grupta");
        }

        Long benimBolum = null;
        for (Long id : idler) {
            Long bolumId = bilgi.get(id).ders().getBolumId();
            if (erisebilir(bolumId)) {
                benimBolum = bolumId;
                break;
            }
        }
        if (benimBolum == null) {
            throw new YasakException("Ortak grup oluşturmak için en az bir şubenin kendi bölümünüze ait olması gerekir");
        }

        OrtakGrup grup = grupRepository.save(
                new OrtakGrup(ilk.proje().getKurumId(), ilk.proje().getDonem().trim(), benimBolum));
        List<OrtakGrupUye> uyeler = new ArrayList<>();
        for (Long id : idler) {
            boolean onayli = erisebilir(bilgi.get(id).ders().getBolumId());
            uyeler.add(uyeRepository.save(new OrtakGrupUye(grup.getId(), id, onayli)));
        }
        return grupYaniti(grup, uyeler);
    }

    /** Projenin şubelerinin üyesi olduğu ortak gruplar. */
    @Transactional(readOnly = true)
    public List<OrtakDersDto.Grup> listele(Long projeId) {
        ProgramProjesi proje = projeBul(projeId);
        yetki.bolumeErisim(proje.getBolumId());
        List<Sube> subeler = subelerOf(dersRepository.findByProjeIdOrderByKod(projeId));
        if (subeler.isEmpty()) {
            return List.of();
        }
        Set<Long> grupIdler = uyeRepository
                .findBySubeIdIn(subeler.stream().map(Sube::getId).collect(Collectors.toSet())).stream()
                .map(OrtakGrupUye::getGrupId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (grupIdler.isEmpty()) {
            return List.of();
        }
        Map<Long, OrtakGrup> gruplar = grupRepository.findAllById(grupIdler).stream()
                .collect(Collectors.toMap(OrtakGrup::getId, Function.identity()));
        Map<Long, List<OrtakGrupUye>> uyeler = uyeRepository.findByGrupIdIn(grupIdler).stream()
                .collect(Collectors.groupingBy(OrtakGrupUye::getGrupId));
        List<OrtakDersDto.Grup> sonuc = new ArrayList<>();
        for (Long grupId : grupIdler) {
            sonuc.add(grupYaniti(gruplar.get(grupId), uyeler.getOrDefault(grupId, List.of())));
        }
        return sonuc;
    }

    /** Çağıranın erişebildiği bölümlerin onay bekleyen şubelerini onaylar. */
    @Transactional
    public OrtakDersDto.Grup onayla(Long grupId) {
        OrtakGrup grup = grupBul(grupId);
        List<OrtakGrupUye> uyeler = uyeRepository.findByGrupId(grupId);
        Map<Long, SubeBilgisi> bilgi = bilgiYukle(uyeler.stream().map(OrtakGrupUye::getSubeId).toList());
        boolean ilgili = uyeler.stream().anyMatch(u -> bilgi.containsKey(u.getSubeId())
                && erisebilir(bilgi.get(u.getSubeId()).ders().getBolumId()));
        if (!ilgili) {
            throw new YasakException("Bu ortak grup bölümünüzle ilgili değil");
        }
        int onaylanan = 0;
        for (OrtakGrupUye uye : uyeler) {
            SubeBilgisi b = bilgi.get(uye.getSubeId());
            if (!uye.isOnayli() && b != null && erisebilir(b.ders().getBolumId())) {
                uye.setOnayli(true);
                onaylanan++;
            }
        }
        if (onaylanan == 0) {
            throw new GecersizIslemException("Bu grupta onayınızı bekleyen şube yok");
        }
        uyeRepository.flush();
        return grupYaniti(grup, uyeler);
    }

    /**
     * Şubeyi gruptan çıkarır (bekleyen öneriyi reddetmek de budur). Grupta 2'den az üye kalırsa grup silinir.
     * Daha önce yapılmış yerleşimler yerinde kalır, yalnızca ortak kural kalkar.
     */
    @Transactional
    public void ayril(Long grupId, Long subeId) {
        grupBul(grupId);
        OrtakGrupUye uye = uyeRepository.findBySubeId(subeId)
                .filter(u -> u.getGrupId().equals(grupId))
                .orElseThrow(() -> new KayitBulunamadiException("Şube bu ortak grupta değil (id: " + subeId + ")"));
        SubeBilgisi bilgi = bilgiYukle(List.of(subeId)).get(subeId);
        if (bilgi == null) {
            throw new KayitBulunamadiException("Şube bulunamadı (id: " + subeId + ")");
        }
        yetki.bolumeErisim(bilgi.ders().getBolumId());

        uyeRepository.delete(uye);
        uyeRepository.flush();
        List<OrtakGrupUye> kalanlar = uyeRepository.findByGrupId(grupId);
        if (kalanlar.size() < 2) {
            uyeRepository.deleteAll(kalanlar);
            grupRepository.deleteById(grupId);
        }
    }

    private boolean adayOlabilir(SubeBilgisi b, Set<Long> grupta) {
        return b != null
                && b.ders().getTur() != DersTuru.YERLESIMSIZ
                && b.hocaAnahtari() != null
                && !b.bloklar().isEmpty()
                && !grupta.contains(b.sube().getId());
    }

    private boolean erisebilir(Long bolumId) {
        List<Long> izinli = yetki.erisilebilirBolumler();
        return izinli == null || izinli.contains(bolumId);
    }

    private List<Sube> subelerOf(List<Ders> dersler) {
        if (dersler.isEmpty()) {
            return List.of();
        }
        Set<Long> dersIdleri = dersler.stream().map(Ders::getId).collect(Collectors.toSet());
        Set<Long> bolumler = dersler.stream().map(Ders::getBolumId).collect(Collectors.toSet());
        return subeRepository.findByDersBolumIn(bolumler).stream()
                .filter(s -> dersIdleri.contains(s.getDersId()))
                .toList();
    }

    private Map<Long, SubeBilgisi> bilgiYukle(Collection<Long> subeIdler) {
        Map<Long, Sube> subeler = subeRepository.findAllById(subeIdler).stream()
                .collect(Collectors.toMap(Sube::getId, Function.identity()));
        Map<Long, Ders> dersler = dersRepository
                .findAllById(subeler.values().stream().map(Sube::getDersId).collect(Collectors.toSet())).stream()
                .collect(Collectors.toMap(Ders::getId, Function.identity()));
        Map<Long, ProgramProjesi> projeler = projeRepository
                .findAllById(dersler.values().stream().map(Ders::getProjeId).collect(Collectors.toSet())).stream()
                .collect(Collectors.toMap(ProgramProjesi::getId, Function.identity()));
        Map<Long, OgretimElemani> hocalar = hocaRepository
                .findAllById(subeler.values().stream().map(Sube::getOgretimElemaniId)
                        .filter(Objects::nonNull).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(OgretimElemani::getId, Function.identity()));
        Map<Long, Bolum> bolumler = bolumRepository
                .findAllById(dersler.values().stream().map(Ders::getBolumId).collect(Collectors.toSet())).stream()
                .collect(Collectors.toMap(Bolum::getId, Function.identity()));

        Map<Long, SubeBilgisi> sonuc = new LinkedHashMap<>();
        for (Long id : subeIdler) {
            Sube sube = subeler.get(id);
            if (sube == null) {
                continue;
            }
            Ders ders = dersler.get(sube.getDersId());
            ProgramProjesi proje = projeler.get(ders.getProjeId());
            OgretimElemani hoca = sube.getOgretimElemaniId() == null ? null : hocalar.get(sube.getOgretimElemaniId());
            Bolum bolum = bolumler.get(ders.getBolumId());
            List<Integer> bloklar = blokRepository.findBySubeIdOrderBySiraNo(id).stream()
                    .map(DersBlogu::getSure)
                    .toList();
            sonuc.put(id, new SubeBilgisi(sube, ders, proje, hoca == null ? null : hoca.getAdSoyad(),
                    hoca == null ? null : BolumlerArasiKurallar.hocaAnahtari(hoca.getAdSoyad()),
                    bolum == null ? "" : bolum.getAd(), bloklar));
        }
        return sonuc;
    }

    private OrtakDersDto.Grup grupYaniti(OrtakGrup grup, List<OrtakGrupUye> uyeler) {
        Map<Long, SubeBilgisi> bilgi = bilgiYukle(uyeler.stream().map(OrtakGrupUye::getSubeId).toList());
        List<OrtakDersDto.Uye> liste = new ArrayList<>();
        for (OrtakGrupUye uye : uyeler) {
            SubeBilgisi b = bilgi.get(uye.getSubeId());
            if (b == null) {
                continue;
            }
            liste.add(new OrtakDersDto.Uye(uye.getSubeId(), b.ders().getBolumId(), b.bolumAdi(), b.ders().getKod(),
                    b.ders().getAd(), b.sube().getSubeNo(), b.hocaAdi(), b.sube().getTahminiOgrenci(),
                    uye.isOnayli(), erisebilir(b.ders().getBolumId())));
        }
        int toplam = liste.stream().mapToInt(u -> u.ogrenci() == null ? 0 : u.ogrenci()).sum();
        boolean bekliyor = liste.stream().anyMatch(u -> !u.onayli() && u.benim());
        boolean tam = !liste.isEmpty() && liste.stream().allMatch(OrtakDersDto.Uye::onayli);
        return new OrtakDersDto.Grup(grup.getId(), grup.getDonem(), liste, toplam, bekliyor, tam);
    }

    private OrtakGrup grupBul(Long grupId) {
        return grupRepository.findById(grupId)
                .orElseThrow(() -> new KayitBulunamadiException("Ortak grup bulunamadı (id: " + grupId + ")"));
    }

    private ProgramProjesi projeBul(Long projeId) {
        return projeRepository.findById(projeId)
                .orElseThrow(() -> new KayitBulunamadiException(
                        "Program projesi bulunamadı (id: " + projeId + ")"));
    }
}
