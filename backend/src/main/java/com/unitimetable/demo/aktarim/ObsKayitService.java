package com.unitimetable.demo.aktarim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.unitimetable.demo.blok.BlokService;
import com.unitimetable.demo.ders.Ders;
import com.unitimetable.demo.ders.DersRepository;
import com.unitimetable.demo.ders.DersSinif;
import com.unitimetable.demo.ders.DersSinifRepository;
import com.unitimetable.demo.ders.DersTuru;
import com.unitimetable.demo.ders.SaatIfadesi;
import com.unitimetable.demo.ogretimelemani.OgretimElemani;
import com.unitimetable.demo.ogretimelemani.OgretimElemaniRepository;
import com.unitimetable.demo.ortak.GecersizIslemException;
import com.unitimetable.demo.ortak.KayitBulunamadiException;
import com.unitimetable.demo.ortak.YetkiKontrol;
import com.unitimetable.demo.program.AkademikProgram;
import com.unitimetable.demo.program.AkademikProgramRepository;
import com.unitimetable.demo.proje.ProgramProjesi;
import com.unitimetable.demo.proje.ProgramProjesiRepository;
import com.unitimetable.demo.sinif.Sinif;
import com.unitimetable.demo.sinif.SinifRepository;
import com.unitimetable.demo.sube.Sube;
import com.unitimetable.demo.sube.SubeRepository;

@Service
public class ObsKayitService {

    private static final Set<String> VARSAYILAN_SANAL_ONEKLERI = Set.of("ATA", "TDB", "ISG");
    private static final String ANA_GRUP = "[ANA GRUP]";

    private final ObsAktarimService aktarimService;
    private final ProgramProjesiRepository projeRepository;
    private final AkademikProgramRepository programRepository;
    private final SinifRepository sinifRepository;
    private final OgretimElemaniRepository hocaRepository;
    private final DersRepository dersRepository;
    private final DersSinifRepository dersSinifRepository;
    private final SubeRepository subeRepository;
    private final BlokService blokService;
    private final YetkiKontrol yetki;

    public ObsKayitService(ObsAktarimService aktarimService, ProgramProjesiRepository projeRepository,
            AkademikProgramRepository programRepository, SinifRepository sinifRepository,
            OgretimElemaniRepository hocaRepository, DersRepository dersRepository,
            DersSinifRepository dersSinifRepository, SubeRepository subeRepository,
            BlokService blokService, YetkiKontrol yetki) {
        this.aktarimService = aktarimService;
        this.projeRepository = projeRepository;
        this.programRepository = programRepository;
        this.sinifRepository = sinifRepository;
        this.hocaRepository = hocaRepository;
        this.dersRepository = dersRepository;
        this.dersSinifRepository = dersSinifRepository;
        this.subeRepository = subeRepository;
        this.blokService = blokService;
        this.yetki = yetki;
    }

    /** Aktarım sırasında değişen sınıf ve hoca bilgilerini tutar. */
    private final class Baglam {
        private final Long bolumId;
        private final Long programId;
        private final Map<Integer, Sinif> siniflar;
        private final List<String> olusturulanSiniflar = new ArrayList<>();
        private final Map<String, OgretimElemani> hocalar = new HashMap<>();
        private final Set<String> mevcutHocaAnahtarlari;
        private final Set<String> eslesenHocalar = new HashSet<>();
        private final List<String> olusturulanHocalar = new ArrayList<>();

        private Baglam(Long bolumId, Long programId) {
            this.bolumId = bolumId;
            this.programId = programId;
            this.siniflar = sinifRepository.findByProgramIdOrderByDuzey(programId).stream()
                    .collect(Collectors.toMap(Sinif::getDuzey, Function.identity(), (a, b) -> a,
                            HashMap::new));
            for (OgretimElemani h : hocaRepository.findByBolumIdOrderByAdSoyad(bolumId)) {
                hocalar.putIfAbsent(MetinTemizleyici.hocaAnahtari(h.getAdSoyad()), h);
            }
            this.mevcutHocaAnahtarlari = new HashSet<>(hocalar.keySet());
        }

        private Sinif sinif(int duzey) {
            Sinif var = siniflar.get(duzey);
            if (var != null) {
                return var;
            }
            Sinif yeni = sinifRepository.save(new Sinif(programId, duzey + ". Sınıf", duzey));
            siniflar.put(duzey, yeni);
            olusturulanSiniflar.add(yeni.getAd());
            return yeni;
        }

        private OgretimElemani hoca(String hocaMetni) {
            MetinTemizleyici.Hoca h = MetinTemizleyici.hocaAyir(hocaMetni);
            if (h == null) {
                return null;
            }
            String anahtar = MetinTemizleyici.hocaAnahtari(h.ad());
            OgretimElemani var = hocalar.get(anahtar);
            if (var != null) {
                if (mevcutHocaAnahtarlari.contains(anahtar)) {
                    eslesenHocalar.add(anahtar);
                }
                return var;
            }
            String unvan = h.unvan().isEmpty() ? null
                    : (h.unvan().length() > 50 ? h.unvan().substring(0, 50) : h.unvan());
            String ad = h.ad().length() > 200 ? h.ad().substring(0, 200) : h.ad();
            OgretimElemani yeni = hocaRepository.save(new OgretimElemani(bolumId, ad, unvan, null, 0));
            hocalar.put(anahtar, yeni);
            olusturulanHocalar.add(ad);
            return yeni;
        }
    }

    @Transactional
    public ObsKayitDto.Sonuc kaydet(MultipartFile dosya, String donem, Long projeId, Long programId,
            List<String> secmeliDersler, List<String> sanalOnekleri, List<String> seminerKodlari) {
        if (projeId == null) {
            throw new GecersizIslemException("Program projesi (projeId) zorunludur");
        }
        if (programId == null) {
            throw new GecersizIslemException("Program (programId) zorunludur");
        }
        ProgramProjesi proje = projeRepository.findById(projeId)
                .orElseThrow(() -> new KayitBulunamadiException("Program projesi bulunamadı (id: " + projeId + ")"));
        yetki.bolumeErisim(proje.getBolumId());
        AkademikProgram program = programRepository.findById(programId)
                .orElseThrow(() -> new KayitBulunamadiException("Program bulunamadı (id: " + programId + ")"));
        if (!program.getBolumId().equals(proje.getBolumId())) {
            throw new GecersizIslemException("Seçilen program, projenin bölümüne ait değil");
        }

        ObsAktarimService.Okunan okunan = aktarimService.oku(dosya, donem);
        List<ObsSatiri> satirlar = okunan.satirlar().stream()
                .filter(s -> okunan.donem().equals(s.donem()))
                .toList();

        Set<String> secilenler = kodlar(secmeliDersler);
        Set<String> sanalOnek = sanalOnekleri == null ? VARSAYILAN_SANAL_ONEKLERI : kodlar(sanalOnekleri);
        Set<String> seminerler = kodlar(seminerKodlari);
        secimiDogrula(satirlar, secilenler);

        Set<String> mevcutKodlar = dersRepository.findByProjeIdOrderByKod(projeId).stream()
                .map(d -> MetinTemizleyici.kod(d.getKod()))
                .collect(Collectors.toSet());
        Baglam baglam = new Baglam(proje.getBolumId(), programId);
        Set<String> islenenKodlar = new HashSet<>();
        List<ObsKayitDto.Atlanan> atlananlar = new ArrayList<>();

        int dersSayisi = 0;
        int blokSayisi = 0;
        int yerlesimsiz = 0;
        int sanal = 0;
        int seminer = 0;
        int hocasiz = 0;

        for (ObsSatiri s : satirlar) {
            if (anaGrupMu(s) || !(s.zorunlu() || secilenler.contains(s.kod()))) {
                continue;
            }
            if (!islenenKodlar.add(s.kod())) {
                atlananlar.add(new ObsKayitDto.Atlanan(s.satir(), s.kod(),
                        "Dosyada aynı ders kodu birden fazla kez geçiyor, ilki alındı"));
                continue;
            }
            if (mevcutKodlar.contains(s.kod())) {
                atlananlar.add(new ObsKayitDto.Atlanan(s.satir(), s.kod(), "Bu ders kodu projede zaten var"));
                continue;
            }

            DersSiniflandirici.Sonuc sinif = DersSiniflandirici.siniflandir(s, sanalOnek, seminerler);
            boolean yerlesimsizMi = sinif.tur() == DersTuru.YERLESIMSIZ;
            String ifade = yerlesimsizMi ? null : MetinTemizleyici.oneriIfade(s.teo(), s.uyg(), s.lab());
            String sebep = dogrula(s, sinif, ifade);
            if (sebep != null) {
                atlananlar.add(new ObsKayitDto.Atlanan(s.satir(), s.kod(), sebep));
                continue;
            }

            Ders ders = dersRepository.save(new Ders(projeId, proje.getBolumId(), s.kod(), s.ad(),
                    sinif.tur(), sinif.sanal(), yerlesimsizMi ? null : s.toplamSaat(), ifade));
            dersSinifRepository.save(new DersSinif(ders.getId(), baglam.sinif(s.sinif()).getId()));

            OgretimElemani hoca = baglam.hoca(s.hocaMetni());
            boolean seminerMi = sinif.tur() == DersTuru.SEMINER;
            Sube sube = subeRepository.save(new Sube(ders.getId(), hoca == null ? null : hoca.getId(), 1,
                    null, seminerMi, seminerMi, false));
            blokService.subeIcinYenile(ders, sube);

            dersSayisi++;
            if (yerlesimsizMi) {
                yerlesimsiz++;
            } else {
                blokSayisi += SaatIfadesi.bloklar(ifade).size();
            }
            if (sinif.sanal()) {
                sanal++;
            }
            if (seminerMi) {
                seminer++;
            }
            if (hoca == null) {
                hocasiz++;
            }
        }

        return new ObsKayitDto.Sonuc(dersSayisi, dersSayisi, blokSayisi, yerlesimsiz, sanal, seminer,
                hocasiz, baglam.eslesenHocalar.size(), baglam.olusturulanHocalar,
                baglam.olusturulanSiniflar, atlananlar);
    }

    private static String dogrula(ObsSatiri s, DersSiniflandirici.Sonuc sinif, String ifade) {
        if (s.kod().length() > 30) {
            return "Ders kodu 30 karakterden uzun";
        }
        if (s.ad().isBlank()) {
            return "Ders adı boş";
        }
        if (s.ad().length() > 200) {
            return "Ders adı 200 karakterden uzun";
        }
        if (s.sinif() < 1 || s.sinif() > 10) {
            return "Sınıf düzeyi 1 ile 10 arasında olmalı (bulunan: " + s.sinif() + ")";
        }
        if (sinif.tur() != DersTuru.YERLESIMSIZ) {
            if (s.toplamSaat() > 40) {
                return "Haftalık saat 40'ı aşıyor (" + s.toplamSaat() + ")";
            }
            try {
                SaatIfadesi.dogrula(ifade, s.toplamSaat());
            } catch (GecersizIslemException hata) {
                return "Saat ifadesi geçersiz: " + hata.getMessage();
            }
        }
        return null;
    }

    private static void secimiDogrula(List<ObsSatiri> satirlar, Set<String> secilenler) {
        if (secilenler.isEmpty()) {
            return;
        }
        Set<String> anaGruplar = new HashSet<>();
        Set<String> secmeliler = new HashSet<>();
        for (ObsSatiri s : satirlar) {
            if (anaGrupMu(s)) {
                anaGruplar.add(s.kod());
            } else if (!s.zorunlu()) {
                secmeliler.add(s.kod());
            }
        }
        List<String> basliklar = secilenler.stream().filter(anaGruplar::contains).toList();
        if (!basliklar.isEmpty()) {
            throw new GecersizIslemException("Şu kodlar seçmeli grup başlığı, ders olarak seçilemez; "
                    + "grubun altındaki dersleri seçin: " + String.join(", ", basliklar));
        }
        List<String> bulunmayanlar = secilenler.stream().filter(k -> !secmeliler.contains(k)).toList();
        if (!bulunmayanlar.isEmpty()) {
            throw new GecersizIslemException("Şu seçmeli dersler dosyada (seçilen dönemde) bulunamadı: "
                    + String.join(", ", bulunmayanlar));
        }
    }

    private static boolean anaGrupMu(ObsSatiri s) {
        return MetinTemizleyici.kod(s.grup()).equals(ANA_GRUP);
    }

    private static Set<String> kodlar(List<String> ham) {
        Set<String> sonuc = new LinkedHashSet<>();
        if (ham != null) {
            for (String k : ham) {
                String kod = MetinTemizleyici.kod(k);
                if (!kod.isEmpty()) {
                    sonuc.add(kod);
                }
            }
        }
        return sonuc;
    }
}